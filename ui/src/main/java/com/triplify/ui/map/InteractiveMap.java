package com.triplify.ui.map;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class InteractiveMap extends StackPane {

    private static final double DEFAULT_ZOOM = 5.5;
    private static final double FOCUSED_ZOOM = 12.5;
    private static final double MIN_ZOOM = 2.0;
    private static final double MAX_ZOOM = 18.0;
    private static final double ZOOM_STEP = 1.0;
    private static final double MOUSE_WHEEL_DELTA_THRESHOLD = 12.0;
    private static final double TRACKPAD_PAN_FACTOR = 1.15;
    private static final double MAX_COUNTRY_HOVER_ZOOM = 7.0;

    private final MapView mapView;
    private final Label hoveredCountryLabel;
    private final Button recenterButton;
    private final Button zoomInButton;
    private final Button zoomOutButton;
    private final VBox controlsBox;

    private CountryHoverLayer countryHoverLayer;
    private PinLayer pinLayer;
    private List<CountryBoundary> countryBoundaries;

    private double mapPressSceneX;
    private double mapPressSceneY;
    private double mapDragSceneX;
    private double mapDragSceneY;
    private boolean rightMouseDragging;
    private boolean selectionEnabled = true;

    private final ObjectProperty<MapPoint> selectedPoint = new SimpleObjectProperty<>();
    private final ObjectProperty<String> selectedCountryName = new SimpleObjectProperty<>();

    public InteractiveMap() {
        this.getStyleClass().add("add-place-map-shell");

        StackPane mapContainer = new StackPane();
        mapContainer.getStyleClass().add("add-place-map-container");

        mapView = new MapView();
        mapView.getStyleClass().add("add-place-map-view");
        mapView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mapView.prefWidthProperty().bind(mapContainer.widthProperty());
        mapView.prefHeightProperty().bind(mapContainer.heightProperty());
        mapView.setZoom(DEFAULT_ZOOM);
        mapView.setCursor(Cursor.OPEN_HAND);

        mapContainer.getChildren().setAll(mapView);

        hoveredCountryLabel = new Label();
        hoveredCountryLabel.getStyleClass().add("add-place-country-label");
        hoveredCountryLabel.setManaged(false);
        hoveredCountryLabel.setVisible(false);

        recenterButton = new Button();
        recenterButton.getStyleClass().addAll("app-btn", "app-btn-secondary", "add-place-map-control");
        recenterButton.setFocusTraversable(false);
        FontIcon crosshairIcon = new FontIcon("fth-crosshair");
        crosshairIcon.setIconSize(15);
        crosshairIcon.getStyleClass().add("app-btn-icon");
        recenterButton.setGraphic(crosshairIcon);
        recenterButton.setOnAction(ignore -> focusMapOnSelection());

        zoomInButton = new Button("+");
        zoomInButton.getStyleClass().addAll("app-btn", "app-btn-secondary", "add-place-map-control");
        zoomInButton.setFocusTraversable(false);
        zoomInButton.setOnAction(ignore -> handleZoom(ZOOM_STEP));

        zoomOutButton = new Button("-");
        zoomOutButton.getStyleClass().addAll("app-btn", "app-btn-secondary", "add-place-map-control");
        zoomOutButton.setFocusTraversable(false);
        zoomOutButton.setOnAction(ignore -> handleZoom(-ZOOM_STEP));

        controlsBox = new VBox();
        controlsBox.getStyleClass().add("add-place-map-controls");
        controlsBox.setSpacing(10);
        controlsBox.setPadding(new Insets(10));
        controlsBox.getChildren().addAll(recenterButton, zoomInButton, zoomOutButton);

        getChildren().addAll(mapContainer, hoveredCountryLabel, controlsBox);

        StackPane.setAlignment(hoveredCountryLabel, Pos.TOP_LEFT);
        StackPane.setMargin(hoveredCountryLabel, new Insets(14, 0, 0, 14));

        StackPane.setAlignment(controlsBox, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(controlsBox, new Insets(0, 10, 10, 0));

        initMapLayers();
        initEventHandlers();
    }

    private void initMapLayers() {
        countryBoundaries = CountryBoundaryLoader.load();
        countryHoverLayer = new CountryHoverLayer();
        mapView.addLayer(countryHoverLayer);

        pinLayer = new PinLayer();
        mapView.addLayer(pinLayer);
    }

    private void initEventHandlers() {
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (isMapControlTarget(event.getTarget()) || !isScenePointInsideMap(event.getSceneX(), event.getSceneY())) {
                return;
            }
            if (event.getButton() == MouseButton.SECONDARY) {
                rightMouseDragging = true;
                mapDragSceneX = event.getSceneX();
                mapDragSceneY = event.getSceneY();
                mapView.setCursor(Cursor.CLOSED_HAND);
                event.consume();
                return;
            }
            if (event.getButton() == MouseButton.PRIMARY) {
                mapPressSceneX = event.getSceneX();
                mapPressSceneY = event.getSceneY();
                event.consume();
            }
        });

        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!rightMouseDragging || !event.isSecondaryButtonDown()) {
                if (event.isPrimaryButtonDown()) {
                    event.consume();
                }
                return;
            }
            panMapBy(event.getSceneX() - mapDragSceneX, event.getSceneY() - mapDragSceneY);
            mapDragSceneX = event.getSceneX();
            mapDragSceneY = event.getSceneY();
            clearHoveredCountry();
            event.consume();
        });

        this.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (isMapControlTarget(event.getTarget())) {
                return;
            }
            if (event.getButton() == MouseButton.SECONDARY) {
                rightMouseDragging = false;
                mapView.setCursor(Cursor.OPEN_HAND);
                event.consume();
                return;
            }
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            mapView.setCursor(Cursor.OPEN_HAND);
            double dx = event.getSceneX() - mapPressSceneX;
            double dy = event.getSceneY() - mapPressSceneY;
            double dragDistance = Math.hypot(dx, dy);
            if (dragDistance <= 5) {
                updateSelectionFromScenePoint(event.getSceneX(), event.getSceneY());
            }
            event.consume();
        });

        this.addEventFilter(MouseEvent.MOUSE_MOVED, event -> 
            updateHoveredCountryFromScenePoint(event.getSceneX(), event.getSceneY())
        );

        this.addEventFilter(MouseEvent.MOUSE_EXITED, ignore -> {
            if (!rightMouseDragging) {
                mapView.setCursor(Cursor.OPEN_HAND);
            }
            clearHoveredCountry();
        });

        this.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            if (rightMouseDragging || isScenePointInsideMap(event.getSceneX(), event.getSceneY())) {
                event.consume();
            }
        });

        this.addEventFilter(ScrollEvent.SCROLL, this::handleMapScroll);
        mapView.addEventHandler(ScrollEvent.SCROLL, ignore -> refreshInteractiveLayersLater());
        mapView.addEventHandler(ZoomEvent.ZOOM, ignore -> refreshInteractiveLayersLater());
    }

    public ObjectProperty<MapPoint> selectedPointProperty() {
        return selectedPoint;
    }

    public ObjectProperty<String> selectedCountryNameProperty() {
        return selectedCountryName;
    }

    public void setMapCenter(double latitude, double longitude) {
        mapView.setCenter(latitude, longitude);
    }

    public void setPinPosition(double latitude, double longitude) {
        pinLayer.setPoint(latitude, longitude);
        selectedPoint.set(new MapPoint(latitude, longitude));
        CountryBoundary countryBoundary = findCountry(latitude, longitude);
        selectedCountryName.set(countryBoundary == null ? null : countryBoundary.name());
    }

    public void setSelectionEnabled(boolean selectionEnabled) {
        this.selectionEnabled = selectionEnabled;
    }

    public void setControlsVisible(boolean visible) {
        controlsBox.setVisible(visible);
        controlsBox.setManaged(visible);
    }

    private void handleZoom(double zoomDelta) {
        mapView.setZoom(clamp(mapView.getZoom() + zoomDelta, MIN_ZOOM, MAX_ZOOM));
        if (mapView.getZoom() > MAX_COUNTRY_HOVER_ZOOM) {
            clearHoveredCountry();
        }
        refreshInteractiveLayersLater();
    }

    private void handleMapScroll(ScrollEvent event) {
        if (isMapControlTarget(event.getTarget())) {
            return;
        }

        Point2D localPoint = mapView.sceneToLocal(event.getSceneX(), event.getSceneY());
        if (!isPointInsideMap(localPoint.getX(), localPoint.getY())) {
            return;
        }

        if (isTrackpadScroll(event)) {
            panMapBy(event.getDeltaX(), event.getDeltaY());
            updateHoveredCountryFromScenePoint(event.getSceneX(), event.getSceneY());
        } else {
            double delta = event.getDeltaY();
            if (delta != 0) {
                double zoomDelta = delta > 0 ? ZOOM_STEP : -ZOOM_STEP;
                double oldZoom = mapView.getZoom();
                double newZoom = clamp(oldZoom + zoomDelta, MIN_ZOOM, MAX_ZOOM);

                if (newZoom != oldZoom) {
                    MapPoint mapPoint = mapView.getMapPosition(localPoint.getX(), localPoint.getY());
                    mapView.setZoom(newZoom);
                    
                    if (newZoom > MAX_COUNTRY_HOVER_ZOOM) {
                        clearHoveredCountry();
                    }

                    MapPoint center = mapView.getMapPosition(mapView.getWidth() / 2, mapView.getHeight() / 2);
                    MapPoint zoomedPoint = mapView.getMapPosition(localPoint.getX(), localPoint.getY());
                    
                    double latDiff = mapPoint.getLatitude() - zoomedPoint.getLatitude();
                    double lonDiff = mapPoint.getLongitude() - zoomedPoint.getLongitude();
                    
                    mapView.setCenter(center.getLatitude() + latDiff, center.getLongitude() + lonDiff);
                }
            }
        }
        event.consume();
    }

    private boolean isTrackpadScroll(ScrollEvent event) {
        if (event.isControlDown() || event.isShortcutDown()) return false;
        double absDeltaX = Math.abs(event.getDeltaX());
        double absDeltaY = Math.abs(event.getDeltaY());
        if (absDeltaX > 0.01) return true;
        return absDeltaY > 0.01 && absDeltaY < MOUSE_WHEEL_DELTA_THRESHOLD;
    }

    private void panMapBy(double deltaX, double deltaY) {
        double targetX = clamp((mapView.getWidth() / 2.0) - (deltaX * TRACKPAD_PAN_FACTOR), 0, mapView.getWidth());
        double targetY = clamp((mapView.getHeight() / 2.0) - (deltaY * TRACKPAD_PAN_FACTOR), 0, mapView.getHeight());
        MapPoint targetCenter = mapView.getMapPosition(targetX, targetY);
        mapView.setCenter(targetCenter.getLatitude(), targetCenter.getLongitude());
    }

    private void updateSelectionFromScenePoint(double sceneX, double sceneY) {
        if (!selectionEnabled) {
            return;
        }
        Point2D localPoint = mapView.sceneToLocal(sceneX, sceneY);
        if (!isPointInsideMap(localPoint.getX(), localPoint.getY())) return;
        MapPoint point = mapView.getMapPosition(localPoint.getX(), localPoint.getY());
        pinLayer.setPoint(point.getLatitude(), point.getLongitude());
        selectedPoint.set(point);
        CountryBoundary countryBoundary = findCountry(point.getLatitude(), point.getLongitude());
        selectedCountryName.set(countryBoundary == null ? null : countryBoundary.name());
    }

    private void updateHoveredCountryFromScenePoint(double sceneX, double sceneY) {
        if (mapView.getZoom() > MAX_COUNTRY_HOVER_ZOOM) {
            clearHoveredCountry();
            return;
        }

        Point2D localPoint = mapView.sceneToLocal(sceneX, sceneY);
        if (!isPointInsideMap(localPoint.getX(), localPoint.getY())) {
            clearHoveredCountry();
            return;
        }

        MapPoint point = mapView.getMapPosition(localPoint.getX(), localPoint.getY());
        CountryBoundary hoveredCountry = findCountry(point.getLatitude(), point.getLongitude());
        countryHoverLayer.setHoveredCountry(hoveredCountry);
        updateHoveredCountryLabel(hoveredCountry);
    }

    private CountryBoundary findCountry(double latitude, double longitude) {
        for (CountryBoundary countryBoundary : countryBoundaries) {
            if (countryBoundary.contains(latitude, longitude)) return countryBoundary;
        }
        return null;
    }

    private void clearHoveredCountry() {
        countryHoverLayer.setHoveredCountry(null);
        updateHoveredCountryLabel(null);
    }

    private void updateHoveredCountryLabel(CountryBoundary countryBoundary) {
        if (countryBoundary == null) {
            hoveredCountryLabel.setVisible(false);
            hoveredCountryLabel.setManaged(false);
            hoveredCountryLabel.setText("");
        } else {
            hoveredCountryLabel.setText(countryBoundary.name());
            hoveredCountryLabel.setVisible(true);
            hoveredCountryLabel.setManaged(true);
        }
    }

    private void focusMapOnSelection() {
        MapPoint point = selectedPoint.get();
        if (point == null) return;
        mapView.setCenter(point.getLatitude(), point.getLongitude());
        if (mapView.getZoom() < FOCUSED_ZOOM) {
            mapView.setZoom(FOCUSED_ZOOM);
        }
        refreshInteractiveLayersLater();
    }

    private void refreshInteractiveLayersLater() {
        Platform.runLater(() -> {
            pinLayer.refresh();
            countryHoverLayer.refresh();
        });
    }

    private boolean isPointInsideMap(double x, double y) {
        return x >= 0 && y >= 0 && x <= mapView.getWidth() && y <= mapView.getHeight();
    }

    private boolean isMapControlTarget(Object target) {
        if (!(target instanceof Node node)) return false;
        return isDescendantOf(node, recenterButton) || isDescendantOf(node, zoomInButton) || isDescendantOf(node, zoomOutButton);
    }

    private boolean isDescendantOf(Node node, Node ancestor) {
        Node current = node;
        while (current != null) {
            if (current == ancestor) return true;
            current = current.getParent();
        }
        return false;
    }

    private boolean isScenePointInsideMap(double sceneX, double sceneY) {
        Point2D localPoint = mapView.sceneToLocal(sceneX, sceneY);
        return isPointInsideMap(localPoint.getX(), localPoint.getY());
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class PinLayer extends MapLayer {
        private final FontIcon pin = new FontIcon("fth-map-pin");
        private final MapPoint point = new MapPoint(0, 0);

        public PinLayer() {
        }

        @Override
        protected void initialize() {
            pin.setIconSize(34);
            pin.getStyleClass().add("add-place-map-pin");
            pin.setManaged(false);
            pin.setMouseTransparent(true);
            getChildren().setAll(pin);
            markDirty();
        }

        @Override
        protected void layoutLayer() {
            Point2D pixelPoint = getMapPoint(point.getLatitude(), point.getLongitude());
            pin.applyCss();
            pin.autosize();
            double pinWidth = Math.max(24, pin.getLayoutBounds().getWidth());
            double pinHeight = Math.max(34, pin.getLayoutBounds().getHeight());
            pin.relocate(pixelPoint.getX() - (pinWidth / 2), pixelPoint.getY() - pinHeight);
        }

        private void setPoint(double latitude, double longitude) {
            point.update(latitude, longitude);
            markDirty();
        }

        private void refresh() { markDirty(); }
    }

    private static final class CountryHoverLayer extends MapLayer {
        private CountryBoundary hoveredCountry;

        public CountryHoverLayer() {
        }

        private void setHoveredCountry(CountryBoundary hoveredCountry) {
            if (this.hoveredCountry == hoveredCountry) return;
            this.hoveredCountry = hoveredCountry;
            markDirty();
        }

        private void refresh() { markDirty(); }

        @Override
        protected void layoutLayer() {
            getChildren().clear();
            if (hoveredCountry == null) return;

            for (List<CountryBoundary.LonLat> ring : hoveredCountry.rings()) {
                Polygon polygon = new Polygon();
                polygon.getStyleClass().add("add-place-country-hover");
                polygon.setMouseTransparent(true);
                for (CountryBoundary.LonLat pt : ring) {
                    Point2D projectedPoint = getMapPoint(pt.latitude(), pt.longitude());
                    if (projectedPoint != null) {
                        polygon.getPoints().addAll(projectedPoint.getX(), projectedPoint.getY());
                    }
                }
                if (polygon.getPoints().size() >= 6) {
                    getChildren().add(polygon);
                }
            }
        }
    }
}
