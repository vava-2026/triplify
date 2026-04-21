package com.triplify.ui.pages.places;

import com.gluonhq.maps.MapPoint;
import com.google.inject.Inject;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.place.dto.AddPlaceRequest;
import com.triplify.application.usecase.place.dto.GetPlaceByIdRequest;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.UpdatePlaceRequest;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.map.InteractiveMap;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.countries.model.Countries;
import com.triplify.ui.shared.component.countries.view.CountriesView;
import com.triplify.ui.storage.EditorDraftStorage;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.upload_panel.view.ImageUploadPanelView;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.Localization;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
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
import java.util.UUID;
import java.util.function.Consumer;

public class AddPlaceController extends SimpleLifecycleAwareController {

    private static final double DEFAULT_LATITUDE = 48.1485965;
    private static final double DEFAULT_LONGITUDE = 17.1077477;

    @FXML private VBox contentContainer;
    @FXML private FlowPane contentFlow;

    @FXML private SectionHeaderView generalSectionHeader;
    @FXML private SectionHeaderView locationSectionHeader;
    @FXML private Label placeTitleLabel;
    @FXML private Label countryLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label mapHelperLabel;

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

    private String tripName;
    private String returnTarget;
    private String placeId;
    private boolean editMode;
    private boolean placeLoaded;
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
        titleInput = new InputItem("input.placeholder.placeTitle", FieldVariant.GHOST);

        countriesView = new CountriesView(
                Countries.builder(countryService)
                        .variant(FieldVariant.GHOST)
                        .searchOnTyping(true)
                        .onLoadFailed(errorHandler::handle)
                        .build()
        );
        countryInputContainer.getChildren().add(countriesView);

        descriptionInput = new TextAreaItem("input.placeholder.placeDescription", FieldVariant.GHOST);
        titleInputContainer.getChildren().add(titleInput);
        descriptionInputContainer.getChildren().add(descriptionInput);
        uploadArea = imageUploadPanel.getUploadArea();
        coverPreview = imageUploadPanel.getCoverPreview();
        uploadPlaceholder = imageUploadPanel.getUploadPlaceholder();
        selectedImageLabel = imageUploadPanel.getSelectedImageLabel();

        contentFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        EditorUtils.initializeCoverPreview(coverPreview, uploadArea);
        bindUploadPanelHandlers();

        EditorUtils.configureButtonIcon(saveButton, "fth-save", 15, "app-btn-icon");
        EditorUtils.configureButtonIcon(discardButton, "fth-trash-2", 15, "app-btn-icon");
        saveButton.setOnAction(event -> onSave());
        discardButton.setOnAction(event -> onDiscard());

        EditorUtils.installRoundedClip(uploadArea, 16);
        EditorUtils.installRoundedClip(interactiveMap, 18);

        bindLocalizedText();
        initializeMap();
        updateSelectedCoordinatesLabel();
        I18n.bundleProperty().addListener((obs, oldBundle, newBundle) -> updateSelectedCoordinatesLabel());
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        tripName = data == null ? null : data.getValue("tripName");
        returnTarget = data == null ? null : data.getValue("editorReturnTarget");
        placeId = data == null ? null : data.getValue("placeId");
        editMode = placeId != null && !placeId.isBlank();
    }

    @Override
    public void onLifecycleShow() {
        updateFullScreenMode(false);
        if (editMode && !placeLoaded) {
            loadPlaceForEdit();
        }
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

        String countryId = normalize(countriesView == null ? null : countriesView.getSelectedCountryId());
        java.nio.file.Path coverImage = coverImagePath == null ? null : java.nio.file.Path.of(coverImagePath);
        String title = normalize(titleInput.getText());
        String description = descriptionInput.getText();

        Map<String, Consumer<String>> fieldHandlers = countriesView == null
                ? Map.of("title", message -> titleInput.showError(message))
                : Map.of(
                        "title", message -> titleInput.showError(message),
                        "countryId", message -> countriesView.showError(message)
                );

        var result = editMode
                ? placeService.updatePlace(new UpdatePlaceRequest(
                        UUID.fromString(placeId),
                        UUID.fromString(countryId),
                        coverImage,
                        title,
                        description,
                        selectedLatitude,
                        selectedLongitude
                ))
                : placeService.addPlace(new AddPlaceRequest(
                        UUID.fromString(countryId),
                        coverImage,
                        title,
                        description,
                        selectedLatitude,
                        selectedLongitude
                ));
        result.onSuccess(place -> {
            if (returnTarget != null && !returnTarget.isBlank()) {
                EditorDraftStorage.savePendingPlace(returnTarget, place);
            }
            String message = editMode
                    ? I18n.t("place.edit.toast.saved.body")
                    : tripName == null || tripName.isBlank()
                    ? I18n.t("place.add.toast.saved.body")
                    : EditorUtils.formatMessage("place.add.toast.saved.body.trip", tripName);
            toast.success(I18n.t("place.add.toast.saved.title"), message);
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
        chooser.setTitle(I18n.t("place.add.dialog.cover.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.t("place.add.dialog.cover.filter"), "*.png", "*.jpg", "*.jpeg", "*.svg")
        );

        File file = chooser.showOpenDialog(uploadArea.getScene() == null ? null : uploadArea.getScene().getWindow());
        if (file != null) {
            handleCoverImage(file);
        }
    }

    @FXML
    private void onUploadDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles() && EditorUtils.isSupportedImageFile(event.getDragboard().getFiles().getFirst())) {
            event.acceptTransferModes(TransferMode.COPY);
            EditorUtils.toggleStyleClass(uploadArea, "editor-upload-area-active", true);
        }
        event.consume();
    }

    @FXML
    private void onUploadDragExited(DragEvent event) {
        EditorUtils.toggleStyleClass(uploadArea, "editor-upload-area-active", false);
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

        toggleStyleClass(uploadArea, "editor-upload-area-active", false);
        event.setDropCompleted(completed);
        event.consume();
    }

    private void clearFieldErrors() {
        titleInput.clearError();
        if (countriesView != null) {
            countriesView.clearError();
        }
    }

    private void handleCoverImage(File file) {
        if (!isSupportedImageFile(file)) {
            toast.warning(I18n.t("place.add.toast.image.unsupported"));
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
                toast.warning(I18n.t("place.add.toast.image.previewUnavailable"));
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


    private void setCoverPreviewImage(Image image) {
        EditorUtils.setCoverPreviewImage(coverPreview, uploadArea, image);
    }

    private void initializeMap() {
        interactiveMap.selectedPointProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedLatitude = newVal.getLatitude();
                selectedLongitude = newVal.getLongitude();
                updateSelectedCoordinatesLabel();
            }
        });
        interactiveMap.selectedCountryNameProperty().addListener((obs, oldVal, newVal) -> {
            if (countriesView != null && newVal != null && !newVal.isBlank()) {
                countriesView.selectCountryByName(newVal);
            }
        });
        
        interactiveMap.setMapCenter(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        interactiveMap.setPinPosition(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
    }

    private void loadPlaceForEdit() {
        var result = placeService.getPlaceById(new GetPlaceByIdRequest(UUID.fromString(placeId)));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            getRouter().popBackStack();
            return;
        }

        var place = result.getValue();
        titleInput.setText(place.title() == null ? "" : place.title());
        descriptionInput.setText(place.description() == null ? "" : place.description());
        if (countriesView != null && place.country() != null && place.country().name() != null) {
            countriesView.selectCountryByName(place.country().name());
        }

        selectedLatitude = place.latitude();
        selectedLongitude = place.longitude();
        interactiveMap.setMapCenter(selectedLatitude, selectedLongitude);
        interactiveMap.setPinPosition(selectedLatitude, selectedLongitude);
        updateSelectedCoordinatesLabel();

        if (place.coverImage() != null && place.coverImage().url() != null) {
            coverImagePath = place.coverImage().url().toString();
            selectedImageLabel.setText(new File(coverImagePath).getName());
            selectedImageLabel.setVisible(true);
            selectedImageLabel.setManaged(true);

            if (isVectorImage(new File(coverImagePath))) {
                coverPreview.setImage(null);
                coverPreview.setVisible(false);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(true);
                uploadPlaceholder.setManaged(true);
            } else {
                setCoverPreviewImage(new Image(new File(coverImagePath).toURI().toString(), true));
                coverPreview.setVisible(true);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(false);
                uploadPlaceholder.setManaged(false);
            }
        }

        placeLoaded = true;
    }

    private void updateSelectedCoordinatesLabel() {
        selectedCoordinatesLabel.setText(
                String.format(Locale.US, I18n.t("place.add.coordinates.format"), selectedLatitude, selectedLongitude)
        );
    }

    private void bindLocalizedText() {
        Localization.bindText(generalSectionHeader.titleProperty(), "place.add.section.general");
        Localization.bindText(placeTitleLabel.textProperty(), "place.add.field.title");
        Localization.bindText(countryLabel.textProperty(), "place.add.field.country");
        Localization.bindText(descriptionLabel.textProperty(), "place.add.field.description");
        Localization.bindText(locationSectionHeader.titleProperty(), "place.add.section.location");
        Localization.bindText(mapHelperLabel.textProperty(), "place.add.map.helper");
        Localization.bindText(imageUploadPanel.sectionTitleProperty(), "place.add.section.cover");
        Localization.bindText(imageUploadPanel.uploadTitleProperty(), "place.add.upload.title");
        Localization.bindText(imageUploadPanel.uploadSubtitleProperty(), "place.add.upload.subtitle");
        Localization.bindText(saveButton.textProperty(), "place.add.action.save");
        Localization.bindText(discardButton.textProperty(), "place.add.action.discard");
    }



    private void bindUploadPanelHandlers() {
        uploadArea.setOnMouseClicked(event -> onChooseCoverImage());
        uploadArea.setOnDragOver(this::onUploadDragOver);
        uploadArea.setOnDragExited(this::onUploadDragExited);
        uploadArea.setOnDragDropped(this::onUploadDragDropped);
    }



    private void updateFullScreenMode(boolean fullScreen) {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(fullScreen);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
