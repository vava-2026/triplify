package com.triplify.ui.pages.places;

import com.google.inject.Inject;
import com.triplify.application.usecase.place.AddPlaceRequest;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

public class AddPlaceController extends SimpleLifecycleAwareController {

    private static final String MAP_URL =
            "https://staticmap.openstreetmap.de/staticmap.php?center=48.1485965,17.1077477&zoom=13&size=900x540&maptype=mapnik";

    private static final double DEFAULT_LATITUDE = 48.1485965;
    private static final double DEFAULT_LONGITUDE = 17.1077477;
    private static final double MAP_TOP_LATITUDE = 48.2085;
    private static final double MAP_BOTTOM_LATITUDE = 48.0865;
    private static final double MAP_LEFT_LONGITUDE = 16.9880;
    private static final double MAP_RIGHT_LONGITUDE = 17.2270;

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
    @FXML private ImageView mapImageView;
    @FXML private Label mapFallbackLabel;
    @FXML private Pane mapOverlay;
    @FXML private FontIcon mapPin;

    @FXML private Button saveButton;
    @FXML private Button discardButton;
    @FXML private Button recenterButton;

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

    @FXML
    public void initialize() {
        titleInput = createInput("input.placeholder.placeTitle");
        countryInput = createInput("input.placeholder.country");
        descriptionInput = createTextArea("input.placeholder.placeDescription");

        titleInputContainer.getChildren().add(titleInput);
        countryInputContainer.getChildren().add(countryInput);
        descriptionInputContainer.getChildren().add(descriptionInput);

        contentFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());

        mapImageView.fitWidthProperty().bind(mapShell.widthProperty());
        mapImageView.fitHeightProperty().bind(mapShell.heightProperty());
        mapOverlay.prefWidthProperty().bind(mapShell.widthProperty());
        mapOverlay.prefHeightProperty().bind(mapShell.heightProperty());
        mapOverlay.maxWidthProperty().bind(mapShell.widthProperty());
        mapOverlay.maxHeightProperty().bind(mapShell.heightProperty());
        coverPreview.fitWidthProperty().bind(uploadArea.widthProperty());
        coverPreview.fitHeightProperty().bind(uploadArea.heightProperty());

        mapOverlay.widthProperty().addListener((obs, oldVal, newVal) -> positionPinForCurrentSelection());
        mapOverlay.heightProperty().addListener((obs, oldVal, newVal) -> positionPinForCurrentSelection());

        configureButtonIcon(saveButton, "fth-save");
        configureButtonIcon(discardButton, "fth-trash-2");
        configureButtonIcon(recenterButton, "fth-crosshair");

        installRoundedClip(uploadArea, 16);
        installRoundedClip(mapShell, 18);

        loadMapPreview();
        Platform.runLater(this::positionPinForCurrentSelection);
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
                tripId,
                normalize(titleInput.getText()),
                normalize(countryInput.getText()),
                normalizeNullable(descriptionInput.getText()),
                selectedLatitude,
                selectedLongitude,
                coverImagePath
        );

        Map<String, Consumer<String>> fieldHandlers = Map.of(
                "title", message -> titleInput.showError(message),
                "country", message -> countryInput.showError(message)
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
    private void onMapClicked(MouseEvent event) {
        updateSelectionFromPoint(event.getX(), event.getY());
    }

    @FXML
    private void onRecenterMap() {
        selectedLatitude = DEFAULT_LATITUDE;
        selectedLongitude = DEFAULT_LONGITUDE;
        positionPinForCurrentSelection();
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
                coverPreview.setManaged(true);
                uploadPlaceholder.setVisible(false);
                uploadPlaceholder.setManaged(false);
            }
        });
    }

    private void loadMapPreview() {
        mapFallbackLabel.setText("Loading map preview...");
        mapFallbackLabel.setVisible(true);
        mapFallbackLabel.setManaged(true);

        Image image = new Image(MAP_URL, true);
        image.errorProperty().addListener((obs, oldVal, isError) -> {
            if (Boolean.TRUE.equals(isError)) {
                mapFallbackLabel.setText("Map preview unavailable.");
                mapFallbackLabel.setVisible(true);
                mapFallbackLabel.setManaged(true);
            }
        });
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0 && !image.isError()) {
                mapFallbackLabel.setVisible(false);
                mapFallbackLabel.setManaged(false);
            }
        });
        mapImageView.setImage(image);
    }

    private void updateSelectionFromPoint(double x, double y) {
        double width = mapOverlay.getWidth() > 0 ? mapOverlay.getWidth() : mapShell.getWidth();
        double height = mapOverlay.getHeight() > 0 ? mapOverlay.getHeight() : mapShell.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        double clampedX = clamp(x, 0, width);
        double clampedY = clamp(y, 0, height);

        selectedLongitude = MAP_LEFT_LONGITUDE + ((MAP_RIGHT_LONGITUDE - MAP_LEFT_LONGITUDE) * (clampedX / width));
        selectedLatitude = MAP_TOP_LATITUDE - ((MAP_TOP_LATITUDE - MAP_BOTTOM_LATITUDE) * (clampedY / height));

        placePin(clampedX, clampedY);
    }

    private void positionPinForCurrentSelection() {
        double width = mapOverlay.getWidth() > 0 ? mapOverlay.getWidth() : mapShell.getWidth();
        double height = mapOverlay.getHeight() > 0 ? mapOverlay.getHeight() : mapShell.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        double xRatio = (selectedLongitude - MAP_LEFT_LONGITUDE) / (MAP_RIGHT_LONGITUDE - MAP_LEFT_LONGITUDE);
        double yRatio = (MAP_TOP_LATITUDE - selectedLatitude) / (MAP_TOP_LATITUDE - MAP_BOTTOM_LATITUDE);
        placePin(clamp(xRatio * width, 0, width), clamp(yRatio * height, 0, height));
    }

    private void placePin(double x, double y) {
        mapPin.applyCss();
        double pinWidth = Math.max(24, mapPin.getLayoutBounds().getWidth());
        double pinHeight = Math.max(34, mapPin.getLayoutBounds().getHeight());
        double maxX = Math.max(0, mapOverlay.getWidth() - pinWidth);
        double maxY = Math.max(0, mapOverlay.getHeight() - pinHeight);

        mapPin.relocate(
                clamp(x - (pinWidth / 2), 0, maxX),
                clamp(y - pinHeight, 0, maxY)
        );
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
}
