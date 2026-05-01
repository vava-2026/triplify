package com.triplify.ui.pages.map;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.google.inject.Inject;
import com.triplify.application.usecase.map.MapService;
import com.triplify.application.usecase.map.dto.GetMapObjectsRequest;
import com.triplify.application.usecase.map.dto.MapObjectResponse;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.GetRouteByIdRequest;
import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.map.MapObjectType;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.map.MapMarkersLayer;
import com.triplify.ui.map.MapRouteHighlightLayer;
import com.triplify.ui.routing.GuardedNavigator;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.Localization;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class MapController extends SimpleLifecycleAwareController {

    private static final double DEFAULT_ZOOM = 3.0;
    private static final double PLACE_FOCUS_ZOOM = 12.0;
    private static final double DEFAULT_LAT = 20.0;
    private static final double DEFAULT_LON = 10.0;
    private static final double MIN_ZOOM = 2.0;
    private static final double MAX_ZOOM = 18.0;
    private static final double ZOOM_STEP = 1.0;
    private static final double TRACKPAD_THRESHOLD = 12.0;
    private static final double PAN_FACTOR = 1.15;
    private static final Duration ROUTE_HOVER_DEBOUNCE = Duration.millis(120);

    @Inject private MapService mapService;
    @Inject private PlaceService placeService;
    @Inject private RouteService routeService;
    @Inject private GuardedNavigator guardedNavigator;
    @Inject private ErrorHandler errorHandler;
    @Inject private ClusterPopupController clusterPopup;

    @FXML private StackPane mapContainer;
    @FXML private HBox searchContainer;
    @FXML private HBox filterToolbar;
    @FXML private StackPane clusterPopupContainer;

    private MapView mapView;
    private MapMarkersLayer markersLayer;
    private MapRouteHighlightLayer routeLayer;

    private final PauseTransition viewportDebounce = new PauseTransition(Duration.millis(350));
    private final PauseTransition routeHoverDebounce = new PauseTransition(ROUTE_HOVER_DEBOUNCE);
    private final Set<MapObjectType> activeFilter = new HashSet<>();

    private double dragStartX;
    private double dragStartY;
    private boolean dragging = false;
    private boolean dragPanEnabled = false;
    private boolean firstShow = true;
    private long routeHoverVersion = 0L;
    private String pendingRouteHoverId;
    private String highlightedRouteId;

    @FXML
    private void initialize() {
        buildMap();
        buildFilterToolbar();
        viewportDebounce.setOnFinished(e -> loadMarkers());
        routeHoverDebounce.setOnFinished(e -> fetchHoveredRoute());
    }

    @Override
    public void onLifecycleShow() {
        if (firstShow) {
            firstShow = false;
            mapView.setZoom(DEFAULT_ZOOM);
            mapView.setCenter(DEFAULT_LAT, DEFAULT_LON);
        }
        clusterPopup.hide();
        loadMarkers();
    }

    private void buildMap() {
        mapView = new MapView();
        mapView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mapView.prefWidthProperty().bind(mapContainer.widthProperty());
        mapView.prefHeightProperty().bind(mapContainer.heightProperty());
        mapView.setCursor(Cursor.OPEN_HAND);

        markersLayer = new MapMarkersLayer();
        markersLayer.setOnMarkerClick(this::onMarkerClick);
        markersLayer.setOnClusterClick(this::onClusterClick);
        markersLayer.setOnRouteHover(this::onRouteHover);
        mapView.addLayer(markersLayer);

        routeLayer = new MapRouteHighlightLayer();
        mapView.addLayer(routeLayer);

        mapContainer.getChildren().add(mapView);

        clusterPopupContainer.getChildren().add(clusterPopup);

        buildSearch();
        attachMapEvents();
    }

    private void buildSearch() {
        Search<PlaceResponse> search = Search.<PlaceResponse>builder(this::searchPlaces)
                .placeholderKey("map.search.placeholder")
                .maxVisibleResults(6)
                .variant(FieldVariant.OUTLINED)
                .onResultSelected(entry -> navigateToPlace(entry.getValue()))
                .build();
        searchContainer.getChildren().add(new SearchView<>(search));
    }

    private List<Entry<PlaceResponse>> searchPlaces(String query) {
        if (query == null || query.isBlank()) return List.of();
        var result = placeService.getPlaces(
                new GetPlacesRequest(new PageRequest(0, 8), new PlaceFilter(query, null)));
        return result.isSuccess()
                ? result.getValue().items().stream()
                        .map(p -> Entry.<PlaceResponse>builder(p, p.title()).icon("fth-map-pin").build())
                        .toList()
                : List.of();
    }

    private void navigateToPlace(PlaceResponse place) {
        mapView.setCenter(place.latitude(), place.longitude());
        mapView.setZoom(PLACE_FOCUS_ZOOM);
        viewportDebounce.playFromStart();
    }

    private void attachMapEvents() {
        mapView.addEventHandler(ScrollEvent.SCROLL, this::handleScroll);

        mapContainer.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && !isMarkerTarget(event.getTarget())) {
                dragPanEnabled = true;
                dragStartX = event.getSceneX();
                dragStartY = event.getSceneY();
                dragging = false;
                mapView.setCursor(Cursor.CLOSED_HAND);
                event.consume();
            } else {
                dragPanEnabled = false;
            }
        });

        mapContainer.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (dragPanEnabled && event.isPrimaryButtonDown()) {
                double dx = event.getSceneX() - dragStartX;
                double dy = event.getSceneY() - dragStartY;
                if (Math.abs(dx) + Math.abs(dy) > 2) {
                    dragging = true;
                    panBy(dx, dy);
                    dragStartX = event.getSceneX();
                    dragStartY = event.getSceneY();
                }
                event.consume();
            }
        });

        mapContainer.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (dragPanEnabled) {
                mapView.setCursor(Cursor.OPEN_HAND);
                if (dragging) {
                    viewportDebounce.playFromStart();
                }
                event.consume();
            }
            dragging = false;
            dragPanEnabled = false;
        });
    }

    private boolean isMarkerTarget(Object target) {
        if (!(target instanceof Node node)) {
            return false;
        }

        Node current = node;
        while (current != null) {
            if (current.getStyleClass().contains("map-marker")
                    || current.getStyleClass().contains("map-cluster")) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private void handleScroll(ScrollEvent event) {
        if (isTrackpad(event)) {
            panBy(event.getDeltaX(), event.getDeltaY());
            viewportDebounce.playFromStart();
        } else {
            double delta = event.getDeltaY() > 0 ? ZOOM_STEP : -ZOOM_STEP;
            double newZoom = clamp(mapView.getZoom() + delta, MIN_ZOOM, MAX_ZOOM);
            if (newZoom != mapView.getZoom()) {
                mapView.setZoom(newZoom);
                viewportDebounce.playFromStart();
            }
        }
        event.consume();
    }

    private void panBy(double dx, double dy) {
        double cx = (mapView.getWidth() / 2.0) - (dx * PAN_FACTOR);
        double cy = (mapView.getHeight() / 2.0) - (dy * PAN_FACTOR);
        MapPoint target = mapView.getMapPosition(
                clamp(cx, 0, mapView.getWidth()),
                clamp(cy, 0, mapView.getHeight()));
        if (target != null) mapView.setCenter(target.getLatitude(), target.getLongitude());
    }

    private void buildFilterToolbar() {
        ToggleGroup group = new ToggleGroup();

        ToggleButton all     = filterChip("map.filter.all",     group, null);
        ToggleButton places  = filterChip("map.filter.places",  group, MapObjectType.PLACE);
        ToggleButton routes  = filterChip("map.filter.routes",  group, MapObjectType.ROUTE);
        ToggleButton stories = filterChip("map.filter.stories", group, MapObjectType.STORY);

        all.setSelected(true);

        filterToolbar.getChildren().addAll(all, places, routes, stories);
        filterToolbar.setSpacing(6);
        filterToolbar.setPadding(new Insets(10));
        filterToolbar.setAlignment(Pos.CENTER);
    }

    private ToggleButton filterChip(String i18nKey, ToggleGroup group, MapObjectType type) {
        ToggleButton btn = new ToggleButton();
        Localization.bindText(btn.textProperty(), i18nKey);
        btn.getStyleClass().addAll("app-btn", "app-btn-ghost", "map-filter-chip");
        btn.setToggleGroup(group);

        btn.selectedProperty().addListener((obs, oldVal, selected) -> {
            if (!selected) return;
            activeFilter.clear();
            if (type != null) activeFilter.add(type);
            viewportDebounce.playFromStart();
        });

        return btn;
    }

    private void loadMarkers() {
        if (mapView.getWidth() == 0 || mapView.getHeight() == 0) return;

        MapPoint topLeft     = mapView.getMapPosition(0, 0);
        MapPoint bottomRight = mapView.getMapPosition(mapView.getWidth(), mapView.getHeight());
        if (topLeft == null || bottomRight == null) return;

        double minLat = Math.min(topLeft.getLatitude(),  bottomRight.getLatitude());
        double maxLat = Math.max(topLeft.getLatitude(),  bottomRight.getLatitude());
        double minLon = Math.min(topLeft.getLongitude(), bottomRight.getLongitude());
        double maxLon = Math.max(topLeft.getLongitude(), bottomRight.getLongitude());

        double latRange = maxLat - minLat;
        double lonRange = maxLon - minLon;
        minLat -= latRange * 0.5;
        maxLat += latRange * 0.5;
        minLon -= lonRange * 0.5;
        maxLon += lonRange * 0.5;

        GetMapObjectsRequest request = new GetMapObjectsRequest(
                clamp(minLat, -90, 90), clamp(minLon, -180, 180),
                clamp(maxLat, -90, 90), clamp(maxLon, -180, 180),
                clamp(mapView.getZoom(), 2, 18),
                Set.copyOf(activeFilter)
        );

        CompletableFuture.supplyAsync(() -> mapService.getMapObjects(request))
                .thenAccept(result -> Platform.runLater(() -> {
                    result.onSuccess(markers -> markersLayer.setMarkers(markers));
                    result.onFailure(e -> errorHandler.handle(e, Map.of()));
                }));
    }

    private void onMarkerClick(MapObjectResponse item) {
        clusterPopup.hide();
        navigate(item);
    }

    private void navigate(MapObjectResponse item) {
        if (item.objectType() == null) return;
        switch (item.objectType()) {
            case PLACE -> {
                RouterArgument args = new RouterArgument();
                args.addArgument("placeId", item.id());
                guardedNavigator.goTo(getRouter(), RouteIds.PLACE_DETAILS, args);
            }
            case ROUTE -> guardedNavigator.goTo(getRouter(), RouteIds.MY_ROUTES);
            case STORY -> guardedNavigator.goTo(getRouter(), RouteIds.MY_TRIPS);
            default    -> {}
        }
    }

    private void onClusterClick(MapObjectResponse cluster) {
        clusterPopup.show(cluster, mapView.getZoom(), Set.copyOf(activeFilter), this::navigate);
    }

    private void onRouteHover(String routeId) {
        routeHoverVersion++;

        if (routeId == null) {
            routeHoverDebounce.stop();
            pendingRouteHoverId = null;
            highlightedRouteId = null;
            routeLayer.clear();
            return;
        }

        if (routeId.equals(highlightedRouteId) || routeId.equals(pendingRouteHoverId)) {
            return;
        }

        pendingRouteHoverId = routeId;
        routeHoverDebounce.playFromStart();
    }

    private void fetchHoveredRoute() {
        String routeId = pendingRouteHoverId;
        if (routeId == null) {
            return;
        }

        UUID parsedRouteId;
        try {
            parsedRouteId = UUID.fromString(routeId);
        } catch (IllegalArgumentException ex) {
            highlightedRouteId = null;
            routeLayer.clear();
            return;
        }

        long requestVersion = routeHoverVersion;

        CompletableFuture.supplyAsync(() ->
                routeService.getRouteById(new GetRouteByIdRequest(parsedRouteId)))
                .thenAccept(result -> Platform.runLater(() -> {
                    if (requestVersion != routeHoverVersion) {
                        return;
                    }

                    result.onSuccess(route -> {
                        List<MapPoint> waypoints = route.places().stream()
                                .map(p -> new MapPoint(p.latitude(), p.longitude()))
                                .toList();
                        highlightedRouteId = routeId;
                        routeLayer.setWaypoints(waypoints);
                    });

                    result.onFailure(error -> {
                        highlightedRouteId = null;
                        routeLayer.clear();
                    });
                }));
    }

    private boolean isTrackpad(ScrollEvent e) {
        if (e.isControlDown() || e.isShortcutDown()) return false;
        double absX = Math.abs(e.getDeltaX());
        double absY = Math.abs(e.getDeltaY());
        if (absX > 0.01) return true;
        return absY > 0.01 && absY < TRACKPAD_THRESHOLD;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
