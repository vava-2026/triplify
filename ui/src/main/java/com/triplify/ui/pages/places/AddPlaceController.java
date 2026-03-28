package com.triplify.ui.pages.places;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.google.inject.Inject;
import com.triplify.application.usecase.place.dto.AddPlaceRequest;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.ui.map.CountryBoundary;
import com.triplify.ui.map.CountryBoundaryLoader;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class AddPlaceController extends SimpleLifecycleAwareController {

    private static final double DEFAULT_LATITUDE = 48.1485965;
    private static final double DEFAULT_LONGITUDE = 17.1077477;
    private static final double DEFAULT_ZOOM = 5.5;
    private static final double FOCUSED_ZOOM = 12.5;
    private static final double MIN_ZOOM = 2.0;
    private static final double MAX_ZOOM = 18.0;
    private static final double ZOOM_STEP = 1.0;
    private static final double TRACKPAD_ZOOM_STEP = 0.2;

    @FXML private VBox contentContainer;
    @FXML private FlowPane contentFlow;

    @FXML private VBox titleInputContainer;
    @FXML private VBox countryInputContainer;
    @FXML private VBox descriptionInputContainer;

    @FXML private StackPane uploadArea;
    @FXML private ImageView coverPreview;
    @FXML private VBox uploadPlaceholder;
    @FXML private Label selectedImageLabel;

    @FXML private StackPane mapShell;
    @FXML private StackPane mapContainer;
    @FXML private Label hoveredCountryLabel;
    @FXML private Label selectedCoordinatesLabel;

    @FXML private Button saveButton;
    @FXML private Button discardButton;
    @FXML private Button recenterButton;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;

    @Inject private PlaceService placeService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;

    private Integer tripId;
    private String tripName;
    private Double selectedLatitude = DEFAULT_LATITUDE;
    private Double selectedLongitude = DEFAULT_LONGITUDE;
    private String coverImagePath;
    private InputItem titleInput;
    private InputItem countryInput;
    private TextAreaItem descriptionInput;
    private MapView mapView;
    private CountryHoverLayer countryHoverLayer;
    private PinLayer pinLayer;
    private List<CountryBoundary> countryBoundaries = List.of();
    private double mapPressSceneX;
    private double mapPressSceneY;

    @FXML
    public void initialize() {
        titleInput = createInput("input.placeholder.placeTitle");
        countryInput = createInput("input.placeholder.country");
        descriptionInput = createTextArea("input.placeholder.placeDescription");

        titleInputContainer.getChildren().add(titleInput);
        countryInputContainer.getChildren().add(countryInput);
        descriptionInputContainer.getChildren().add(descriptionInput);

        contentFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        coverPreview.fitWidthProperty().bind(uploadArea.widthProperty());
        coverPreview.fitHeightProperty().bind(uploadArea.heightProperty());

        configureButtonIcon(saveButton, "fth-save");
        configureButtonIcon(discardButton, "fth-trash-2");
        configureButtonIcon(recenterButton, "fth-crosshair");

        installRoundedClip(uploadArea, 16);
        installRoundedClip(mapShell, 18);

        initializeMap();
        updateSelectedCoordinatesLabel();
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        tripId = data == null ? null : data.getValue("tripId");
        tripName = data == null ? null : data.getValue("tripName");
    }

    @Override
    public void onLifecycleShow() {
        updateFullScreenMode(false);
    }

    @Override
    public void onLifecycleHide() {
        updateFullScreenMode(false);
    }

    @Override
    public void onLifecycleDestroy() {
        updateFullScreenMode(false);
    }

    @FXML
    private void onSave() {
        clearFieldErrors();

        AddPlaceRequest request = new AddPlaceRequest(
                normalize(countryInput.getText()),
                coverImagePath == null ? null : java.nio.file.Path.of(coverImagePath),
                normalize(titleInput.getText()),
                normalizeNullable(descriptionInput.getText()),
                selectedLatitude,
                selectedLongitude
        );

        Map<String, Consumer<String>> fieldHandlers = Map.of(
                "title", message -> titleInput.showError(message),
                "countryId", message -> countryInput.showError(message)
        );

        var result = placeService.addPlace(request);
        result.onSuccess(ignored -> {
            String message = tripName == null || tripName.isBlank()
                    ? "Place saved successfully."
                    : "Place added to " + tripName + ".";
            toast.success("Place saved", message);
            getRouter().popBackStack();
        });
        result.onFailure(error -> errorHandler.handle(error, fieldHandlers));
    }

    @FXML
    private void onDiscard() {
        getRouter().popBackStack();
    }

    @FXML
    private void onChooseCoverImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose cover image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.svg")
        );

        File file = chooser.showOpenDialog(uploadArea.getScene() == null ? null : uploadArea.getScene().getWindow());
        if (file != null) {
            handleCoverImage(file);
        }
    }

    @FXML
    private void onUploadDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles() && isSupportedImageFile(event.getDragboard().getFiles().getFirst())) {
            event.acceptTransferModes(TransferMode.COPY);
            addUploadActiveState(true);
        }
        event.consume();
    }

    @FXML
    private void onUploadDragExited(DragEvent event) {
        addUploadActiveState(false);
        event.consume();
    }

    @FXML
    private void onUploadDragDropped(DragEvent event) {
        boolean completed = false;
        if (event.getDragboard().hasFiles()) {
            File file = event.getDragboard().getFiles().getFirst();
            if (isSupportedImageFile(file)) {
                handleCoverImage(file);
                completed = true;
            }
        }

        addUploadActiveState(false);
        event.setDropCompleted(completed);
        event.consume();
    }

    @FXML
    private void onRecenterMap() {
        focusMapOnSelection(true);
    }

    @FXML
    private void onZoomIn() {
        if (mapView == null) {
            return;
        }
        mapView.setZoom(clamp(mapView.getZoom() + ZOOM_STEP, MIN_ZOOM, MAX_ZOOM));
    }

    @FXML
    private void onZoomOut() {
        if (mapView == null) {
            return;
        }
        mapView.setZoom(clamp(mapView.getZoom() - ZOOM_STEP, MIN_ZOOM, MAX_ZOOM));
    }

    private void clearFieldErrors() {
        titleInput.clearError();
        countryInput.clearError();
    }

    private void handleCoverImage(File file) {
        if (!isSupportedImageFile(file)) {
            toast.warning("Unsupported image format.");
            return;
        }

        coverImagePath = file.getAbsolutePath();
        selectedImageLabel.setText(file.getName());
        selectedImageLabel.setVisible(true);
        selectedImageLabel.setManaged(true);

        if (isVectorImage(file)) {
            coverPreview.setImage(null);
            coverPreview.setVisible(false);
            coverPreview.setManaged(false);
            uploadPlaceholder.setVisible(true);
            uploadPlaceholder.setManaged(true);
            return;
        }

        Image image = new Image(file.toURI().toString(), true);
        image.errorProperty().addListener((obs, oldVal, isError) -> {
            if (Boolean.TRUE.equals(isError)) {
                coverPreview.setImage(null);
                coverPreview.setVisible(false);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(true);
                uploadPlaceholder.setManaged(true);
                toast.warning("Image preview is unavailable, but the file is attached.");
            }
        });
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0 && !image.isError()) {
                coverPreview.setImage(image);
                coverPreview.setVisible(true);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(false);
                uploadPlaceholder.setManaged(false);
            }
        });
    }

    private void initializeMap() {
        mapView = new MapView();
        mapView.getStyleClass().add("add-place-map-view");
        mapView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mapView.prefWidthProperty().bind(mapContainer.widthProperty());
        mapView.prefHeightProperty().bind(mapContainer.heightProperty());
        mapView.setZoom(DEFAULT_ZOOM);
        mapView.setCenter(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);

        countryBoundaries = CountryBoundaryLoader.load();
        countryHoverLayer = new CountryHoverLayer();
        mapView.addLayer(countryHoverLayer);

        pinLayer = new PinLayer();
        mapView.addLayer(pinLayer);
        pinLayer.setPoint(selectedLatitude, selectedLongitude);

        mapShell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (isMapControlTarget(event.getTarget())) {
                return;
            }
            mapPressSceneX = event.getSceneX();
            mapPressSceneY = event.getSceneY();
        });
        mapShell.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (isMapControlTarget(event.getTarget())) {
                return;
            }
            double dx = event.getSceneX() - mapPressSceneX;
            double dy = event.getSceneY() - mapPressSceneY;
            double dragDistance = Math.hypot(dx, dy);
            if (dragDistance <= 5) {
                updateSelectionFromScenePoint(event.getSceneX(), event.getSceneY());
            }
        });
        mapShell.addEventFilter(MouseEvent.MOUSE_MOVED, event ->
                updateHoveredCountryFromScenePoint(event.getSceneX(), event.getSceneY()));
        mapShell.addEventFilter(MouseEvent.MOUSE_EXITED, event -> clearHoveredCountry());
        mapShell.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (isMapControlTarget(event.getTarget()) || mapView == null) {
                return;
            }
            if (Math.abs(event.getDeltaY()) < 0.01) {
                return;
            }

            Point2D localPoint = mapView.sceneToLocal(event.getSceneX(), event.getSceneY());
            if (localPoint.getX() < 0 || localPoint.getY() < 0
                    || localPoint.getX() > mapView.getWidth()
                    || localPoint.getY() > mapView.getHeight()) {
                return;
            }

            double direction = Math.signum(event.getDeltaY());
            double zoomDelta = direction * TRACKPAD_ZOOM_STEP;
            mapView.setZoom(clamp(mapView.getZoom() + zoomDelta, MIN_ZOOM, MAX_ZOOM));
            event.consume();
        });

        mapContainer.getChildren().setAll(mapView);
    }

    private void updateSelectionFromScenePoint(double sceneX, double sceneY) {
        Point2D localPoint = mapView.sceneToLocal(sceneX, sceneY);
        updateSelectionFromPoint(localPoint.getX(), localPoint.getY());
    }

    private void updateHoveredCountryFromScenePoint(double sceneX, double sceneY) {
        Point2D localPoint = mapView.sceneToLocal(sceneX, sceneY);
        updateHoveredCountryFromPoint(localPoint.getX(), localPoint.getY());
    }

    private boolean isMapControlTarget(Object target) {
        if (!(target instanceof Node node)) {
            return false;
        }
        return isDescendantOf(node, recenterButton)
                || isDescendantOf(node, zoomInButton)
                || isDescendantOf(node, zoomOutButton);
    }

    private boolean isDescendantOf(Node node, Node ancestor) {
        Node current = node;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private void updateSelectionFromPoint(double x, double y) {
        if (mapView == null) {
            return;
        }

        if (x < 0 || y < 0 || x > mapView.getWidth() || y > mapView.getHeight()) {
            return;
        }

        MapPoint point = mapView.getMapPosition(x, y);
        selectedLatitude = point.getLatitude();
        selectedLongitude = point.getLongitude();
        pinLayer.setPoint(selectedLatitude, selectedLongitude);
        updateSelectedCoordinatesLabel();
    }

    private void updateHoveredCountryFromPoint(double x, double y) {
        if (mapView == null || countryHoverLayer == null) {
            return;
        }

        if (x < 0 || y < 0 || x > mapView.getWidth() || y > mapView.getHeight()) {
            clearHoveredCountry();
            return;
        }

        MapPoint point = mapView.getMapPosition(x, y);
        CountryBoundary hoveredCountry = findCountry(point.getLatitude(), point.getLongitude());
        countryHoverLayer.setHoveredCountry(hoveredCountry);
        updateHoveredCountryLabel(hoveredCountry);
    }

    private CountryBoundary findCountry(double latitude, double longitude) {
        for (CountryBoundary countryBoundary : countryBoundaries) {
            if (countryBoundary.contains(latitude, longitude)) {
                return countryBoundary;
            }
        }
        return null;
    }

    private void clearHoveredCountry() {
        if (countryHoverLayer != null) {
            countryHoverLayer.setHoveredCountry(null);
        }
        updateHoveredCountryLabel(null);
    }

    private void updateHoveredCountryLabel(CountryBoundary countryBoundary) {
        if (countryBoundary == null) {
            hoveredCountryLabel.setVisible(false);
            hoveredCountryLabel.setManaged(false);
            hoveredCountryLabel.setText("");
            return;
        }

        hoveredCountryLabel.setText(countryBoundary.name());
        hoveredCountryLabel.setVisible(true);
        hoveredCountryLabel.setManaged(true);
    }

    private void focusMapOnSelection(boolean useFocusedZoom) {
        if (mapView == null) {
            return;
        }

        mapView.setCenter(selectedLatitude, selectedLongitude);
        if (useFocusedZoom && mapView.getZoom() < FOCUSED_ZOOM) {
            mapView.setZoom(FOCUSED_ZOOM);
        }
    }

    private void updateSelectedCoordinatesLabel() {
        selectedCoordinatesLabel.setText(String.format(
                Locale.US,
                "Lat: %.6f   Lon: %.6f",
                selectedLatitude,
                selectedLongitude
        ));
    }

    private void configureButtonIcon(Button button, String iconLiteral) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        icon.getStyleClass().add("app-btn-icon");
        button.setGraphic(icon);
    }

    private void addUploadActiveState(boolean active) {
        if (active) {
            if (!uploadArea.getStyleClass().contains("add-place-upload-area-active")) {
                uploadArea.getStyleClass().add("add-place-upload-area-active");
            }
            return;
        }

        uploadArea.getStyleClass().remove("add-place-upload-area-active");
    }

    private void installRoundedClip(StackPane target, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(target.widthProperty());
        clip.heightProperty().bind(target.heightProperty());
        target.setClip(clip);
    }

    private void updateFullScreenMode(boolean fullScreen) {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(fullScreen);
    }

    private boolean isSupportedImageFile(File file) {
        String lowerName = file.getName().toLowerCase();
        return lowerName.endsWith(".png")
                || lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".svg");
    }

    private boolean isVectorImage(File file) {
        return file.getName().toLowerCase().endsWith(".svg");
    }

    private InputItem createInput(String placeholderKey) {
        InputItem input = new InputItem(placeholderKey, FieldVariant.FILLED);
        input.getStyleClass().add("add-place-field");
        return input;
    }

    private TextAreaItem createTextArea(String placeholderKey) {
        TextAreaItem input = new TextAreaItem(placeholderKey, FieldVariant.FILLED);
        input.getStyleClass().addAll("add-place-field", "add-place-textarea-field");
        input.setRows(6);
        return input;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class PinLayer extends MapLayer {

        private final FontIcon pin = new FontIcon("fth-map-pin");
        private final MapPoint point = new MapPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);

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

            pin.relocate(
                    pixelPoint.getX() - (pinWidth / 2),
                    pixelPoint.getY() - pinHeight
            );
        }

        private void setPoint(double latitude, double longitude) {
            point.update(latitude, longitude);
            markDirty();
        }
    }

    private static final class CountryHoverLayer extends MapLayer {

        private CountryBoundary hoveredCountry;

        private void setHoveredCountry(CountryBoundary hoveredCountry) {
            if (this.hoveredCountry == hoveredCountry) {
                return;
            }
            this.hoveredCountry = hoveredCountry;
            markDirty();
        }

        @Override
        protected void layoutLayer() {
            getChildren().clear();

            if (hoveredCountry == null) {
                return;
            }

            for (List<CountryBoundary.LonLat> ring : hoveredCountry.rings()) {
                Polygon polygon = new Polygon();
                polygon.getStyleClass().add("add-place-country-hover");
                polygon.setMouseTransparent(true);

                for (CountryBoundary.LonLat point : ring) {
                    Point2D projectedPoint = getMapPoint(point.latitude(), point.longitude());
                    if (projectedPoint == null) {
                        continue;
                    }
                    polygon.getPoints().addAll(projectedPoint.getX(), projectedPoint.getY());
                }

                if (polygon.getPoints().size() >= 6) {
                    getChildren().add(polygon);
                }
            }
        }
    }
}
