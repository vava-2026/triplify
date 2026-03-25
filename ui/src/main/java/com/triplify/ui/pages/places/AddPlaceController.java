package com.triplify.ui.pages.places;

import com.google.inject.Inject;
import com.triplify.application.error.ValidationMapper;
import com.triplify.application.error.ValidationResult;
import com.triplify.application.usecase.page.AddPageRequest;
import com.triplify.application.usecase.page.PagesService;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.toast.ToastService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
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

public class AddPlaceController extends SimpleLifecycleAwareController {

    private static final String MAP_URL =
            "https://staticmap.openstreetmap.de/staticmap.php?center=48.1485965,17.1077477&zoom=13&size=900x540&maptype=mapnik";
    private static final String ERROR_STYLE_CLASS = "input-error";

    private static final double DEFAULT_LATITUDE = 48.1485965;
    private static final double DEFAULT_LONGITUDE = 17.1077477;
    private static final double MAP_TOP_LATITUDE = 48.2085;
    private static final double MAP_BOTTOM_LATITUDE = 48.0865;
    private static final double MAP_LEFT_LONGITUDE = 16.9880;
    private static final double MAP_RIGHT_LONGITUDE = 17.2270;

    @FXML private VBox contentContainer;
    @FXML private FlowPane contentFlow;

    @FXML private TextField titleField;
    @FXML private TextField countryField;
    @FXML private TextArea descriptionArea;
    @FXML private Label titleError;
    @FXML private Label countryError;

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

    @Inject private PagesService pagesService;
    @Inject private ToastService toast;

    private Integer tripId;
    private String tripName;
    private Double selectedLatitude = DEFAULT_LATITUDE;
    private Double selectedLongitude = DEFAULT_LONGITUDE;
    private String coverImagePath;

    @FXML
    public void initialize() {
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

        configureButtonIcon(saveButton, "fth-check");
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
        updateFullScreenMode(true);
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

        AddPageRequest request = new AddPageRequest(
                tripId,
                normalize(titleField.getText()),
                normalize(countryField.getText()),
                normalizeNullable(descriptionArea.getText()),
                selectedLatitude,
                selectedLongitude,
                coverImagePath
        );

        ValidationResult<AddPageRequest> validation = ValidationMapper.validate(request);
        if (validation.isFailure()) {
            applyValidationErrors(validation);
            return;
        }

        pagesService.addPlace(request)
                .onSuccess(ignored -> {
                    String message = tripName == null || tripName.isBlank()
                            ? "Place saved successfully."
                            : "Place added to " + tripName + ".";
                    toast.success("Place saved", message);
                    getRouter().popBackStack();
                })
                .onFailureResponse(errors -> toast.error(I18n.t("error.validation.failed")));
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

    private void applyValidationErrors(ValidationResult<AddPageRequest> validation) {
        validation.getViolations().forEach(violation -> {
            switch (violation.getField()) {
                case "title" -> showFieldError(titleField, titleError, violation.getMessageKey());
                case "country" -> showFieldError(countryField, countryError, violation.getMessageKey());
                default -> toast.error(I18n.t("error.validation.failed"));
            }
        });
    }

    private void clearFieldErrors() {
        clearFieldError(titleField, titleError);
        clearFieldError(countryField, countryError);
    }

    private void clearFieldError(TextInputControl field, Label errorLabel) {
        field.getStyleClass().remove(ERROR_STYLE_CLASS);
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showFieldError(TextInputControl field, Label errorLabel, String messageKey) {
        if (!field.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            field.getStyleClass().add(ERROR_STYLE_CLASS);
        }

        errorLabel.setText(I18n.t(messageKey));
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
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
