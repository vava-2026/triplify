package com.triplify.ui.pages.places;

import com.gluonhq.maps.MapPoint;
import com.google.inject.Inject;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.place.dto.AddPlaceRequest;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.ui.map.InteractiveMap;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.countries.model.Countries;
import com.triplify.ui.shared.component.countries.view.CountriesView;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.upload_panel.view.ImageUploadPanelView;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.input.ZoomEvent;
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

    @FXML private VBox contentContainer;
    @FXML private FlowPane contentFlow;

    @FXML private VBox titleInputContainer;
    @FXML private VBox countryInputContainer;
    @FXML private VBox descriptionInputContainer;

    @FXML private ImageUploadPanelView imageUploadPanel;

    @FXML private InteractiveMap interactiveMap;
    @FXML private Label selectedCoordinatesLabel;

    @FXML private Button saveButton;
    @FXML private Button discardButton;

    @Inject private PlaceService placeService;
    @Inject private CountryService countryService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;

    private Integer tripId;
    private String tripName;
    private Double selectedLatitude = DEFAULT_LATITUDE;
    private Double selectedLongitude = DEFAULT_LONGITUDE;
    private String coverImagePath;
    private InputItem titleInput;
    private CountriesView countriesView;
    private TextAreaItem descriptionInput;
    private StackPane uploadArea;
    private ImageView coverPreview;
    private VBox uploadPlaceholder;
    private Label selectedImageLabel;

    @FXML
    public void initialize() {
        titleInput = createInput("input.placeholder.placeTitle");
        countriesView = new CountriesView(
                Countries.builder(countryService)
                        .variant(FieldVariant.FILLED)
                        .searchOnTyping(true)
                        .onLoadFailed(errorHandler::handle)
                        .build()
        );
        countriesView.getStyleClass().add("add-place-country-field");
        descriptionInput = createTextArea("input.placeholder.placeDescription");

        titleInputContainer.getChildren().add(titleInput);
        countryInputContainer.getChildren().add(countriesView);
        descriptionInputContainer.getChildren().add(descriptionInput);
        uploadArea = imageUploadPanel.getUploadArea();
        coverPreview = imageUploadPanel.getCoverPreview();
        uploadPlaceholder = imageUploadPanel.getUploadPlaceholder();
        selectedImageLabel = imageUploadPanel.getSelectedImageLabel();

        contentFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        initializeCoverPreview();
        bindUploadPanelHandlers();

        configureButtonIcon(saveButton, "fth-save");
        configureButtonIcon(discardButton, "fth-trash-2");

        installRoundedClip(uploadArea, 16);
        installRoundedClip(interactiveMap, 18);

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
                normalize(countriesView.getSelectedCountryId()),
                coverImagePath == null ? null : java.nio.file.Path.of(coverImagePath),
                normalize(titleInput.getText()),
                normalizeNullable(descriptionInput.getText()),
                selectedLatitude,
                selectedLongitude
        );

        Map<String, Consumer<String>> fieldHandlers = Map.of(
                "title", message -> titleInput.showError(message),
                "countryId", message -> countriesView.showError(message)
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

    private void clearFieldErrors() {
        titleInput.clearError();
        countriesView.clearError();
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
            coverPreview.setViewport(null);
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
                coverPreview.setViewport(null);
                coverPreview.setVisible(false);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(true);
                uploadPlaceholder.setManaged(true);
                toast.warning("Image preview is unavailable, but the file is attached.");
            }
        });
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0 && !image.isError()) {
                setCoverPreviewImage(image);
                coverPreview.setVisible(true);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(false);
                uploadPlaceholder.setManaged(false);
            }
        });
    }

    private void initializeCoverPreview() {
        coverPreview.setPreserveRatio(false);
        coverPreview.fitWidthProperty().bind(uploadArea.widthProperty());
        coverPreview.fitHeightProperty().bind(uploadArea.heightProperty());
        uploadArea.widthProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
        uploadArea.heightProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
        coverPreview.imageProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
    }

    private void setCoverPreviewImage(Image image) {
        coverPreview.setImage(image);
        updateCoverPreviewViewport();
        if (image == null) {
            coverPreview.setViewport(null);
            return;
        }

        image.widthProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
        image.heightProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
        image.progressProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
    }

    private void updateCoverPreviewViewport() {
        Image image = coverPreview.getImage();
        if (image == null) {
            coverPreview.setViewport(null);
            return;
        }

        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double viewportWidth = uploadArea.getWidth();
        double viewportHeight = uploadArea.getHeight();

        if (imageWidth <= 0 || imageHeight <= 0 || viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }

        double imageRatio = imageWidth / imageHeight;
        double viewportRatio = viewportWidth / viewportHeight;

        if (imageRatio > viewportRatio) {
            double cropWidth = imageHeight * viewportRatio;
            double x = (imageWidth - cropWidth) / 2.0;
            coverPreview.setViewport(new Rectangle2D(x, 0, cropWidth, imageHeight));
            return;
        }

        double cropHeight = imageWidth / viewportRatio;
        double y = (imageHeight - cropHeight) / 2.0;
        coverPreview.setViewport(new Rectangle2D(0, y, imageWidth, cropHeight));
    }

    private void initializeMap() {
        interactiveMap.selectedPointProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedLatitude = newVal.getLatitude();
                selectedLongitude = newVal.getLongitude();
                updateSelectedCoordinatesLabel();
            }
        });
        
        interactiveMap.setMapCenter(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        interactiveMap.setPinPosition(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
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

    private void bindUploadPanelHandlers() {
        uploadArea.setOnMouseClicked(event -> onChooseCoverImage());
        uploadArea.setOnDragOver(this::onUploadDragOver);
        uploadArea.setOnDragExited(this::onUploadDragExited);
        uploadArea.setOnDragDropped(this::onUploadDragDropped);
    }

    private void addUploadActiveState(boolean active) {
        if (active) {
            if (!uploadArea.getStyleClass().contains("editor-upload-area-active")) {
                uploadArea.getStyleClass().add("editor-upload-area-active");
            }
            return;
        }

        uploadArea.getStyleClass().remove("editor-upload-area-active");
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
