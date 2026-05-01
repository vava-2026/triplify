package com.triplify.ui.pages.stories;

import com.google.inject.Inject;
import com.triplify.application.usecase.emotion.EmotionService;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.image.dto.LinkImageRequest;
import com.triplify.application.usecase.story.StoryService;
import com.triplify.application.usecase.story.dto.AddStoryRequest;
import com.triplify.application.usecase.story.dto.GetStoryByIdRequest;
import com.triplify.application.usecase.story.dto.UpdateStoryRequest;
import com.triplify.application.usecase.tag.TagService;
import com.triplify.domain.model.enums.ImageOwnerType;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.map.InteractiveMap;
import com.triplify.ui.pages.WindowedPageController;
import com.triplify.ui.pages.emotions.model.Emotions;
import com.triplify.ui.pages.emotions.view.EmotionsView;
import com.triplify.ui.shared.component.add_card.view.AddCardView;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.date_picker.DatePickerItem;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.NumericInputItem;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.tag_picker.TagPickerItem;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.EditorUtils.parseUUID;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class AddStoryController extends WindowedPageController {

    private static final Logger log = LoggerFactory.getLogger(AddStoryController.class);

    private static final double DEFAULT_LATITUDE = 48.1485965;
    private static final double DEFAULT_LONGITUDE = 17.1077477;

    @FXML private VBox contentContainer;
    @FXML private SectionHeaderView contextSectionHeader;
    @FXML private Label contextLabel;
    @FXML private SectionHeaderView generalSectionHeader;
    @FXML private Label titleLabel;
    @FXML private VBox titleInputContainer;
    @FXML private Label descriptionLabel;
    @FXML private VBox descriptionInputContainer;
    @FXML private Label storyTimeLabel;
    @FXML private VBox dateContainer;
    @FXML private HBox timeRow;
    @FXML private SectionHeaderView imagesSectionHeader;
    @FXML private VBox imageCardsContainer;
    @FXML private VBox addImageContainer;
    @FXML private SectionHeaderView locationSectionHeader;
    @FXML private InteractiveMap interactiveMap;
    @FXML private Label mapHelperLabel;
    @FXML private Label selectedCoordinatesLabel;
    @FXML private SectionHeaderView emotionSectionHeader;
    @FXML private Label emotionLabel;
    @FXML private VBox emotionSelectContainer;
    @FXML private SectionHeaderView tagsSectionHeader;
    @FXML private Label tagsLabel;
    @FXML private VBox tagPickerContainer;
    @FXML private VBox actionButtonsContainer;

    @Inject private StoryService storyService;
    @Inject private EmotionService emotionService;
    @Inject private ImageService imageService;
    @Inject private TagService tagService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private InputItem titleInput;
    private TextAreaItem descriptionInput;
    private DatePickerItem datePicker;
    private NumericInputItem hourInput;
    private NumericInputItem minuteInput;
    private EmotionsView emotionsView;
    private TagPickerItem tagPicker;

    private final List<StoryImageCard> imageCards = new ArrayList<>();

    private String storyId;
    private String tripId;
    private String tripRouteId;
    private String tripPlaceId;
    private boolean createMode;
    private double selectedLatitude = DEFAULT_LATITUDE;
    private double selectedLongitude = DEFAULT_LONGITUDE;

    @FXML
    public void initialize() {
        titleInput = new InputItem("input.placeholder.storyTitle", FieldVariant.GHOST);
        descriptionInput = new TextAreaItem("input.placeholder.storyDescription", FieldVariant.GHOST);
        datePicker = new DatePickerItem("dd/MM/yyyy", FieldVariant.GHOST);
        hourInput = new NumericInputItem(0, 23, 0, FieldVariant.GHOST);
        minuteInput = new NumericInputItem(0, 59, 0, FieldVariant.GHOST);

        tagPicker = new TagPickerItem();

        titleInputContainer.getChildren().add(titleInput);
        descriptionInputContainer.getChildren().add(descriptionInput);
        dateContainer.getChildren().add(datePicker);

        Label separator = new Label(":");
        separator.getStyleClass().add("story-time-separator");
        timeRow.getChildren().addAll(hourInput, separator, minuteInput);

        tagPickerContainer.getChildren().add(tagPicker);

        addImageContainer.getChildren().add(
                new AddCardView("story.add.image.add.title", "story.add.image.add.subtitle", this::onAddImage));

        EditorUtils.installRoundedClip(interactiveMap, 18);
        initializeMap();
        updateSelectedCoordinatesLabel();
        I18n.bundleProperty().addListener((obs, old, newBundle) -> updateSelectedCoordinatesLabel());

        Localization.bindText(contextSectionHeader.titleProperty(), "story.add.section.context");
        Localization.bindText(generalSectionHeader.titleProperty(), "story.add.section.general");
        Localization.bindText(imagesSectionHeader.titleProperty(), "story.add.section.images");
        Localization.bindText(locationSectionHeader.titleProperty(), "story.add.section.location");
        Localization.bindText(emotionSectionHeader.titleProperty(), "story.add.section.emotion");
        Localization.bindText(tagsSectionHeader.titleProperty(), "story.add.section.tags");
        Localization.bindText(titleLabel.textProperty(), "story.add.field.title");
        Localization.bindText(descriptionLabel.textProperty(), "story.add.field.description");
        Localization.bindText(storyTimeLabel.textProperty(), "story.add.field.storyTime");
        Localization.bindText(emotionLabel.textProperty(), "story.add.field.emotion");
        Localization.bindText(tagsLabel.textProperty(), "story.add.field.tags");
        Localization.bindText(mapHelperLabel.textProperty(), "place.add.map.helper");

        buildEmotionsView();
        createActionButtons();

        tagPicker.configureTagService(tagService,
                err -> toast.error(I18n.t("story.add.toast.tags.loadFailed")));
        tagPicker.setPlaceholderText(I18n.t("story.add.select.tag"));
        tagPicker.setPopupTitle(I18n.t("story.add.tag.popupTitle"));
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        storyId = data == null ? null : data.getValue("storyId");
        tripId = data == null ? null : data.getValue("tripId");
        tripRouteId = data == null ? null : data.getValue("tripRouteId");
        tripPlaceId = data == null ? null : data.getValue("tripPlaceId");
        createMode = storyId == null || storyId.isBlank();
    }

    @Override
    public void onWindowedShow() {
        imageCards.clear();
        imageCardsContainer.getChildren().clear();

        if (createMode) {
            selectedLatitude = DEFAULT_LATITUDE;
            selectedLongitude = DEFAULT_LONGITUDE;
            interactiveMap.setMapCenter(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
            interactiveMap.setPinPosition(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
            updateSelectedCoordinatesLabel();
        } else {
            loadExistingStory();
        }
        updateContextLabel();
    }

    @FXML
    private void onDiscard() {
        getRouter().popBackStack();
    }

    private void onSave() {
        Map<String, Consumer<String>> fieldHandlers = Map.of(
                "title", msg -> titleInput.showError(msg)
        );

        String title = titleInput.getText().trim();
        String description = descriptionInput.getText();
        Instant storyTime = buildStoryTime();
        UUID emotionId = parseUUID(emotionsView.getSelectedEmotionId());

        var result = createMode
                ? storyService.addStory(new AddStoryRequest(
                        title, description, storyTime,
                        parseUUID(tripId), parseUUID(tripRouteId), parseUUID(tripPlaceId),
                        emotionId, tagPicker.getSelectedTagIds(),
                        selectedLatitude, selectedLongitude))
                : storyService.updateStory(new UpdateStoryRequest(
                        UUID.fromString(storyId),
                        title, description, storyTime,
                        parseUUID(tripId), parseUUID(tripRouteId), parseUUID(tripPlaceId),
                        emotionId, tagPicker.getSelectedTagIds(),
                        selectedLatitude, selectedLongitude));

        result.onSuccess(saved -> {
            syncImages(saved.id());
            toast.success(I18n.t("story.add.toast.saved.title"), I18n.t("story.add.toast.saved.body"));
            getRouter().popBackStack();
        });
        result.onFailure(error -> errorHandler.handle(error, fieldHandlers));
    }

    private void loadExistingStory() {
        var result = storyService.getStoryById(new GetStoryByIdRequest(UUID.fromString(storyId)));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            return;
        }
        var story = result.getValue();
        titleInput.setText(story.title() == null ? "" : story.title());
        descriptionInput.setText(story.description() == null ? "" : story.description());

        if (story.storyTime() != null) {
            var zoned = story.storyTime().atZone(ZoneOffset.UTC);
            datePicker.setValue(zoned.toLocalDate());
            hourInput.setValue(zoned.getHour());
            minuteInput.setValue(zoned.getMinute());
        }

        if (story.emotion() != null) {
            emotionsView.selectEmotionById(story.emotion().id().toString());
        }

        if (story.tags() != null) {
            tagPicker.setSelectedTagIds(story.tags().stream().map(t -> t.id()).toList());
        }

        if (story.latitude() != null && story.longitude() != null) {
            selectedLatitude = story.latitude();
            selectedLongitude = story.longitude();
        } else {
            selectedLatitude = DEFAULT_LATITUDE;
            selectedLongitude = DEFAULT_LONGITUDE;
        }
        interactiveMap.setMapCenter(selectedLatitude, selectedLongitude);
        interactiveMap.setPinPosition(selectedLatitude, selectedLongitude);
        updateSelectedCoordinatesLabel();

        if (story.images() != null) {
            story.images().forEach(this::addExistingImageCard);
        }
    }

    private void addExistingImageCard(ImageResponse image) {
        StoryImageCard[] ref = {null};
        StoryImageCard card = new StoryImageCard(image, () -> removeImageCard(ref[0]));
        ref[0] = card;
        imageCards.add(card);
        imageCardsContainer.getChildren().add(card);
    }

    private void onAddImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.t("story.add.dialog.image.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.t("place.add.dialog.cover.filter"), "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(
                addImageContainer.getScene() == null ? null : addImageContainer.getScene().getWindow());
        if (file == null) return;

        if (!EditorUtils.isSupportedImageFile(file)) {
            toast.warning(I18n.t("place.add.toast.image.unsupported"));
            return;
        }

        StoryImageCard[] ref = {null};
        StoryImageCard card = new StoryImageCard(file, () -> removeImageCard(ref[0]));
        ref[0] = card;
        imageCards.add(card);
        imageCardsContainer.getChildren().add(card);
    }

    private void removeImageCard(StoryImageCard card) {
        if (card == null) return;
        if (!card.isDraft() && card.getImageId() != null) {
            imageService.deleteImage(new DeleteImageRequest(card.getImageId()))
                    .onFailure(err -> log.warn("Failed to delete image id='{}'", card.getImageId()));
        }
        imageCards.remove(card);
        imageCardsContainer.getChildren().remove(card);
    }

    private void syncImages(UUID savedStoryId) {
        UUID ownerTripId = parseUUID(tripId);
        for (StoryImageCard card : imageCards) {
            if (!card.isDraft()) continue;
            var addResult = imageService.addImage(
                    new AddImageRequest(Path.of(card.getFilePath()), card.getDescription()));
            addResult.onSuccess(image ->
                    imageService.linkImage(new LinkImageRequest(
                            image.id(), savedStoryId, ImageOwnerType.STORY, ownerTripId))
                            .onFailure(err -> log.warn("Failed to link image id='{}' to story id='{}'",
                                    image.id(), savedStoryId)));
            addResult.onFailure(err ->
                    log.warn("Failed to upload image file='{}': {}", card.getFilePath(), err));
        }
    }

    private void updateSelectedCoordinatesLabel() {
        selectedCoordinatesLabel.setText(
                String.format(I18n.t("place.add.coordinates.format"), selectedLatitude, selectedLongitude));
    }

    private void updateContextLabel() {
        String text = "";
        if (tripId != null && !tripId.isBlank()) {
            text = I18n.t("story.add.context.trip");
        } else if (tripRouteId != null && !tripRouteId.isBlank()) {
            text = I18n.t("story.add.context.route");
        } else if (tripPlaceId != null && !tripPlaceId.isBlank()) {
            text = I18n.t("story.add.context.place");
        }
        contextLabel.setText(text);
    }

    private void buildEmotionsView() {
        Emotions emotionsModel = Emotions.builder(emotionService)
                .variant(FieldVariant.GHOST)
                .build();
        emotionsView = new EmotionsView(emotionsModel);
        emotionsView.setMaxWidth(Double.MAX_VALUE);
        emotionSelectContainer.getChildren().setAll(emotionsView);
    }

    private Instant buildStoryTime() {
        LocalDate date = datePicker.getValue();
        if (date == null) return Instant.now();
        return date.atTime(hourInput.getValue(), minuteInput.getValue()).toInstant(ZoneOffset.UTC);
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

    private void createActionButtons() {
        javafx.scene.control.Button saveButton = AppButtonView.builder(fxmlLoader)
                .labelBinding(Localization.textBinding("story.add.action.save"))
                .variant(ButtonVariant.PRIMARY)
                .icon("fth-save")
                .onAction(this::onSave)
                .build();
        saveButton.getStyleClass().add("editor-action-button");
        saveButton.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.Button discardButton = AppButtonView.builder(fxmlLoader)
                .labelBinding(Localization.textBinding("story.add.action.discard"))
                .variant(ButtonVariant.DANGER_OUTLINE)
                .icon("fth-trash-2")
                .onAction(this::onDiscard)
                .build();
        discardButton.getStyleClass().add("editor-action-button");
        discardButton.setMaxWidth(Double.MAX_VALUE);

        actionButtonsContainer.getChildren().setAll(saveButton, discardButton);
    }
}
