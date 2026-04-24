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
import com.triplify.ui.pages.WindowedPageController;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
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
import com.triplify.ui.shared.util.FxmlLoaderHelper;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class AddPlaceController extends WindowedPageController {

    private static final double DEFAULT_LATITUDE = 48.1485965;
    private static final double DEFAULT_LONGITUDE = 17.1077477;

    private static final Logger log = LoggerFactory.getLogger(AddPlaceController.class);

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

    @FXML private VBox actionButtonsContainer;

    @Inject private PlaceService placeService;
    @Inject private CountryService countryService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

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
        log.info("Initializing AddPlaceController");
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


        Button saveButton = AppButtonView.builder(fxmlLoader)
                .labelBinding(Localization.textBinding("place.add.action.save"))
                .variant(ButtonVariant.PRIMARY)
                .icon("fth-save")
                .onAction(this::onSave)
                .build();
        saveButton.setMaxWidth(Double.MAX_VALUE);

        Button discardButton = AppButtonView.builder(fxmlLoader)
                .labelBinding(Localization.textBinding("place.add.action.discard"))
                .variant(ButtonVariant.DANGER_OUTLINE)
                .icon("fth-trash-2")
                .onAction(this::onDiscard)
                .build();
        discardButton.setMaxWidth(Double.MAX_VALUE);

        actionButtonsContainer.getChildren().setAll(saveButton, discardButton);


        EditorUtils.installRoundedClip(uploadArea, 16);
        EditorUtils.installRoundedClip(interactiveMap, 18);

        bindLocalizedText();
        initializeMap();
        updateSelectedCoordinatesLabel();
        I18n.bundleProperty().addListener((obs, oldBundle, newBundle) -> updateSelectedCoordinatesLabel());
        log.info("Initialized AddPlaceController");
    }

    @Override
    public void onLifecycleInitialize() {
        log.info("Getting AddPlaceController attributes");
        resetFormState();
        RouterArgument data = getRouter().getCurrentData();
        if (data == null) {
            log.warn("No router data found for AddPlaceController – this may cause issues with returning to the correct place after saving an edit");
            return;
        }
        tripName = data.getValue("tripName");
        returnTarget = data.getValue("editorReturnTarget");
        placeId = data.getValue("placeId");
        editMode = placeId != null && !placeId.isBlank();
        log.info("AddPlaceController attributes retrieved");
    }

    @Override
    protected void onWindowedShow() {
        if (editMode && !placeLoaded) {
            loadPlaceForEdit();
        }
    }

    @FXML
    private void onSave() {
        clearFieldErrors();

        String countryId = countriesView.getSelectedCountryId();
        Path coverImage = coverImagePath == null ? null : Path.of(coverImagePath);
        String title = titleInput.getText().trim();
        String description = descriptionInput.getText();

        Map<String, Consumer<String>> fieldHandlers = Map.of(
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
            String message = I18n.t("place.edit.toast.saved.body");
            if (editMode && (tripName != null && !tripName.isBlank())) {
                message = EditorUtils.formatMessage("place.edit.toast.saved.body.trip", tripName);
            }
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
                new FileChooser.ExtensionFilter(I18n.t("place.add.dialog.cover.filter"), "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(uploadArea.getScene() == null ? null : uploadArea.getScene().getWindow());
        if (file != null) {
            handleCoverImage(file);
        }
    }

    private void clearFieldErrors() {
        titleInput.clearError();
        countriesView.clearError();
    }

    private void handleCoverImage(File file) {
        if (!EditorUtils.isSupportedImageFile(file)) {
            toast.warning(I18n.t("place.add.toast.image.unsupported"));
            return;
        }

        coverImagePath = file.getAbsolutePath();
        selectedImageLabel.setText(file.getName());
        selectedImageLabel.setVisible(true);
        selectedImageLabel.setManaged(true);

        Image image = new Image(file.toURI().toString(), true);
        image.errorProperty().addListener((obs, oldVal, isError) -> {
            if (Boolean.TRUE.equals(isError)) {
                coverPreview.setImage(null);
                coverPreview.setViewport(null);
                coverPreview.setVisible(false);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(true);
                uploadPlaceholder.setManaged(true);
                log.warn("Failed to load cover image preview", image.getException());
                toast.warning(I18n.t("place.add.toast.image.previewUnavailable"));
            }
        });
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0 && !image.isError()) {
                EditorUtils.setCoverPreviewImage(coverPreview, uploadArea, image);
                coverPreview.setVisible(true);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(false);
                uploadPlaceholder.setManaged(false);
            }
        });
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
            if (newVal != null && !newVal.isBlank()) {
                countriesView.selectCountryByName(newVal);
            }
        });
        
        interactiveMap.setMapCenter(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        interactiveMap.setPinPosition(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
    }

    private void loadPlaceForEdit() {
        log.debug("Loading place for edit with ID: {}", placeId);
        var result = placeService.getPlaceById(new GetPlaceByIdRequest(UUID.fromString(placeId)));
        if (result.isFailure()) {
            log.error("Failed to load place for edit: {}", result.getError());
            errorHandler.handle(result.getError());
            getRouter().popBackStack();
            return;
        }
        log.debug("Place data loaded successfully for place ID: {}", placeId);

        var place = result.getValue();
        titleInput.setText(place.title());
        descriptionInput.setText(place.description() == null ? "" : place.description());
        if (place.country() != null && place.country().name() != null) {
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

            EditorUtils.setCoverPreviewImage(coverPreview, uploadArea, new Image(new File(coverImagePath).toURI().toString(), true));
            coverPreview.setVisible(true);
            coverPreview.setManaged(false);
            uploadPlaceholder.setVisible(false);
            uploadPlaceholder.setManaged(false);
        }
        placeLoaded = true;
        log.debug("Place loaded successfully for place ID: {}", placeId);
    }

    private void updateSelectedCoordinatesLabel() {
        selectedCoordinatesLabel.setText(
                String.format(I18n.t("place.add.coordinates.format"), selectedLatitude, selectedLongitude)
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
    }

    private void bindUploadPanelHandlers() {
        imageUploadPanel.setOnUploadClicked(event -> onChooseCoverImage());
        imageUploadPanel.setOnImageFileSelected(this::handleCoverImage);
        imageUploadPanel.setOnUnsupportedImageFile(file -> toast.warning(I18n.t("place.add.toast.image.unsupported")));
        imageUploadPanel.installDefaultImageDragAndDrop();
    }

    private void resetFormState() {
        tripName = null;
        returnTarget = null;
        placeId = null;
        editMode = false;
        placeLoaded = false;
        coverImagePath = null;
        selectedLatitude = DEFAULT_LATITUDE;
        selectedLongitude = DEFAULT_LONGITUDE;

        if (titleInput != null) {
            titleInput.setText("");
            titleInput.clearError();
        }
        if (descriptionInput != null) {
            descriptionInput.setText("");
            descriptionInput.clearError();
        }
        if (countriesView != null) {
            countriesView.clearSearch();
            countriesView.clearError();
        }

        if (coverPreview != null) {
            coverPreview.setImage(null);
            coverPreview.setViewport(null);
            coverPreview.setVisible(false);
            coverPreview.setManaged(false);
        }
        if (uploadPlaceholder != null) {
            uploadPlaceholder.setVisible(true);
            uploadPlaceholder.setManaged(true);
        }
        if (selectedImageLabel != null) {
            selectedImageLabel.setText("");
            selectedImageLabel.setVisible(false);
            selectedImageLabel.setManaged(false);
        }

        if (interactiveMap != null) {
            interactiveMap.setMapCenter(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
            interactiveMap.setPinPosition(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        }
        updateSelectedCoordinatesLabel();
    }
}
