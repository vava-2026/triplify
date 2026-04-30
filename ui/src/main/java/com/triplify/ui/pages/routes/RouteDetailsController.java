package com.triplify.ui.pages.routes;

import com.google.inject.Inject;
import com.gluonhq.maps.MapPoint;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.DeleteRouteRequest;
import com.triplify.application.usecase.route.dto.GetRouteByIdRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.map.InteractiveMap;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.EditorUtils.installRoundedClip;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class RouteDetailsController extends SimpleLifecycleAwareController {

    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final int ROUTE_MAP_ZOOM = 8;

    @FXML private VBox contentContainer;
    @FXML private StackPane heroContainer;
    @FXML private FlowPane topRowFlow;
    @FXML private Button backButton;
    @FXML private ImageView heroImageView;
    @FXML private Label routeTitleLabel;
    @FXML private Label routeLengthLabel;
    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionValueLabel;
    @FXML private Label mapTitleLabel;
    @FXML private InteractiveMap routeMap;
    @FXML private DetailActionButtonsView actionButtonsView;
    @FXML private SectionHeaderView placesHeader;
    @FXML private CardGridPane<PlaceResponse> placesGrid;

    @Inject private RouteService routeService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private String routeId;
    private List<PlaceResponse> currentPlaces = List.of();
    private final AtomicInteger placeIndexCounter = new AtomicInteger(0);

    @FXML
    public void initialize() {
        configureButtonIcon(backButton, "fth-chevron-left", "place-details-back-icon");
        Localization.bindText(backButton.textProperty(), "route.details.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "route.details.description");
        Localization.bindText(mapTitleLabel.textProperty(), "route.details.map");
        Localization.bindText(placesHeader.titleProperty(), "route.details.section.places");

        topRowFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());

        heroImageView.fitWidthProperty().bind(heroContainer.widthProperty());
        heroImageView.fitHeightProperty().bind(heroContainer.heightProperty());
        installRoundedClip(heroContainer, 28);
        installRoundedClip(routeMap, 20);
        routeMap.setSelectionEnabled(false);
        routeMap.setControlsVisible(false);

        actionButtonsView.configurePrimary(fxmlLoader, Localization.textBinding("route.details.action.edit"), "fth-edit-3", this::onEditRoute);
        actionButtonsView.configureDelete(fxmlLoader, Localization.textBinding("route.details.action.delete"), "fth-trash-2", Localization.textBinding("route.details.action.delete.confirm"), this::onDeleteRoute);

        placesGrid.setManualLoadMore(true);
        placesGrid.setPageSize(50);
        placesGrid.setMinCardWidth(560);
        placesGrid.setMaxColumns(1);
        placesGrid.setLoadMoreKey("route.details.show.more.places");
        placesGrid.setEmptyText(I18n.t("route.details.empty.places"));
        placesGrid.setCardFactory(place -> {
            int idx = placeIndexCounter.incrementAndGet();
            return buildPlaceRow(place, idx);
        });
        placesGrid.setPageLoader((page, size) -> {
            if (page > 1) return new CardGridPane.PageResult<>(List.of(), null);
            List<PlaceResponse> snapshot = new ArrayList<>(currentPlaces);
            return new CardGridPane.PageResult<>(snapshot,
                    new Pagination(1, snapshot.size(), snapshot.size(), 1));
        });
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        routeId = data == null ? null : data.getValue("routeId");
    }

    @Override
    public void onLifecycleShow() {
        loadRouteDetails();
    }

    @FXML
    private void onBack() {
        getRouter().popBackStack();
    }

    private void onEditRoute() {
        if (routeId == null || routeId.isBlank()) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", UUID.randomUUID().toString());
        args.addArgument("tripName", "");
        args.addArgument("routeId", routeId);
        getRouter().moveto(RouteIds.ADD_ROUTE, args);
    }

    private void onDeleteRoute() {
        if (routeId == null || routeId.isBlank()) return;
        var result = routeService.deleteRoute(new DeleteRouteRequest(UUID.fromString(routeId)));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            return;
        }
        toast.success(I18n.t("route.details.toast.deleted.title"), I18n.t("route.details.toast.deleted.body"));
        getRouter().popBackStack();
    }

    private void loadRouteDetails() {
        if (routeId == null || routeId.isBlank()) {
            toast.warning(I18n.t("route.details.toast.notFound"));
            getRouter().popBackStack();
            return;
        }

        var result = routeService.getRouteById(new GetRouteByIdRequest(UUID.fromString(routeId)));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            getRouter().popBackStack();
            return;
        }

        bind(result.getValue());
    }

    private void bind(RouteResponse route) {
        heroImageView.setImage(loadImage(route));
        routeTitleLabel.setText(route.title() == null || route.title().isBlank() ? I18n.t("trip.add.fallback.route") : route.title());
        routeLengthLabel.setText(String.format(java.util.Locale.US, I18n.t("route.add.length"), route.length()));
        descriptionValueLabel.setText(route.description() == null || route.description().isBlank()
                ? I18n.t("route.details.empty.description") : route.description());

        List<PlaceResponse> places = route.places() == null ? List.of() : List.copyOf(route.places());
        renderMap(places);

        currentPlaces = places;
        placeIndexCounter.set(0);
        placesGrid.refresh();
    }

    private void renderMap(List<PlaceResponse> places) {
        routeMap.clearMarkers();
        routeMap.clearPolyline();

        if (places.isEmpty()) return;

        List<MapPoint> waypoints = places.stream()
                .map(p -> new MapPoint(p.latitude(), p.longitude()))
                .toList();

        PlaceResponse first = places.get(0);
        routeMap.setMapCenter(first.latitude(), first.longitude());

        for (int i = 0; i < places.size(); i++) {
            routeMap.addMarker(new MapPoint(places.get(i).latitude(), places.get(i).longitude()), i + 1);
        }

        if (waypoints.size() >= 2) {
            routeMap.setPolyline(waypoints);
        }
    }

    private HBox buildPlaceRow(PlaceResponse place, int index) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("add-route-place-row");
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setOnMouseClicked(e -> openPlace(place));

        Label indexLabel = new Label(String.valueOf(index));
        indexLabel.getStyleClass().add("add-route-place-index");
        indexLabel.setMinWidth(24);

        String imgUrl = place.coverImage() != null && place.coverImage().url() != null
                ? place.coverImage().url().toString() : DEFAULT_IMAGE;
        ImageView thumb = new ImageView(EditorUtils.loadImage(imgUrl, DEFAULT_IMAGE, getClass()));
        thumb.setFitWidth(112);
        thumb.setFitHeight(72);
        thumb.setPreserveRatio(false);
        thumb.getStyleClass().add("add-route-thumb");
        Rectangle clip = new Rectangle(112, 72);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        thumb.setClip(clip);

        Label title = new Label(place.title() == null || place.title().isBlank()
                ? I18n.t("trip.add.fallback.place") : place.title());
        title.getStyleClass().add("add-route-place-title");
        title.setWrapText(true);

        String countryName = place.country() == null ? "" : place.country().name();
        Label country = new Label(countryName == null ? "" : countryName);
        country.getStyleClass().add("add-route-place-subtitle");

        VBox textBox = new VBox(4, title, country);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        row.getChildren().addAll(indexLabel, thumb, textBox);
        return row;
    }

    private void openPlace(PlaceResponse place) {
        if (place == null || place.id() == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("placeId", place.id().toString());
        getRouter().moveto(RouteIds.PLACE_DETAILS, args);
    }

    private Image loadImage(RouteResponse route) {
        String url = route.coverImage() != null && route.coverImage().url() != null
                ? route.coverImage().url().toString() : DEFAULT_IMAGE;
        return EditorUtils.loadImage(url, DEFAULT_IMAGE, getClass());
    }

    private void configureButtonIcon(Button button, String iconLiteral, String styleClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
    }
}
