package com.triplify.ui.pages.stories;

import com.google.inject.Inject;
import com.triplify.application.usecase.emotion.EmotionService;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.story.StoryService;
import com.triplify.application.usecase.story.dto.AddStoryRequest;
import com.triplify.application.usecase.story.dto.GetStoryByIdRequest;
import com.triplify.application.usecase.story.dto.UpdateStoryRequest;
import com.triplify.application.usecase.tag.TagService;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.pages.WindowedPageController;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.date_picker.DatePickerItem;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.component.tag_picker.TagPickerItem;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.EditorUtils.parseUUID;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;

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
    @FXML private SectionHeaderView emotionSectionHeader;
    @FXML private Label emotionLabel;
    @FXML private VBox emotionSelectContainer;
    @FXML private SectionHeaderView tagsSectionHeader;
    @FXML private Label tagsLabel;
    @FXML private VBox tagPickerContainer;
    @FXML private VBox actionButtonsContainer;

    @Inject private StoryService storyService;
    @Inject private EmotionService emotionService;
    @Inject private TagService tagService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private InputItem titleInput;
    private TextAreaItem descriptionInput;
    private DatePickerItem datePicker;
    private Spinner<Integer> hourSpinner;
    private Spinner<Integer> minuteSpinner;
    private Select<String> emotionSelectModel;
    private SelectView<String> emotionSelectView;
    private TagPickerItem tagPicker;
    private List<EmotionResponse> availableEmotions = List.of();

    private String storyId;
    private String tripId;
    private String tripRouteId;
    private String tripPlaceId;
    private boolean createMode;

    @FXML
    public void initialize() {
        titleInput = new InputItem("input.placeholder.storyTitle", FieldVariant.GHOST);
        descriptionInput = new TextAreaItem("input.placeholder.storyDescription", FieldVariant.GHOST);
        datePicker = new DatePickerItem("dd/MM/yyyy", FieldVariant.GHOST);
        hourSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        minuteSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        hourSpinner.setEditable(true);
        minuteSpinner.setEditable(true);
        hourSpinner.getStyleClass().add("story-time-spinner");
        minuteSpinner.getStyleClass().add("story-time-spinner");

        tagPicker = new TagPickerItem();

        titleInputContainer.getChildren().add(titleInput);
        descriptionInputContainer.getChildren().add(descriptionInput);
        dateContainer.getChildren().add(datePicker);

        Label separator = new Label(":");
        separator.getStyleClass().add("story-time-separator");
        timeRow.getChildren().addAll(hourSpinner, separator, minuteSpinner);

        tagPickerContainer.getChildren().add(tagPicker);

        Localization.bindText(contextSectionHeader.titleProperty(), "story.add.section.context");
        Localization.bindText(generalSectionHeader.titleProperty(), "story.add.section.general");
        Localization.bindText(emotionSectionHeader.titleProperty(), "story.add.section.emotion");
        Localization.bindText(tagsSectionHeader.titleProperty(), "story.add.section.tags");
        Localization.bindText(titleLabel.textProperty(), "story.add.field.title");
        Localization.bindText(descriptionLabel.textProperty(), "story.add.field.description");
        Localization.bindText(storyTimeLabel.textProperty(), "story.add.field.storyTime");
        Localization.bindText(emotionLabel.textProperty(), "story.add.field.emotion");
        Localization.bindText(tagsLabel.textProperty(), "story.add.field.tags");

        loadAvailableEmotions();
        buildEmotionSelect(null);
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
        if (!createMode) {
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
        UUID emotionId = parseUUID(resolveSelectedEmotionId());

        var result = createMode
                ? storyService.addStory(new AddStoryRequest(
                        title, description, storyTime,
                        parseUUID(tripId), parseUUID(tripRouteId), parseUUID(tripPlaceId),
                        emotionId, tagPicker.getSelectedTagIds()))
                : storyService.updateStory(new UpdateStoryRequest(
                        UUID.fromString(storyId),
                        title, description, storyTime,
                        parseUUID(tripId), parseUUID(tripRouteId), parseUUID(tripPlaceId),
                        emotionId, tagPicker.getSelectedTagIds()));

        result.onSuccess(saved -> {
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
            hourSpinner.getValueFactory().setValue(zoned.getHour());
            minuteSpinner.getValueFactory().setValue(zoned.getMinute());
        }

        if (story.emotion() != null) {
            buildEmotionSelect(story.emotion().id().toString());
        }

        if (story.tags() != null) {
            tagPicker.setSelectedTagIds(story.tags().stream().map(t -> t.id()).toList());
        }
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

    private void loadAvailableEmotions() {
        var result = emotionService.getAllEmotions();
        if (result.isFailure()) {
            log.warn("Failed to load emotions: {}", result.getError().message());
            availableEmotions = List.of();
            return;
        }
        availableEmotions = result.getValue();
    }

    private void buildEmotionSelect(String selectedEmotionId) {
        List<Entry<String>> entries = new ArrayList<>();
        entries.add(Entry.<String>builder("", I18n.t("story.add.emotion.none")).build());
        for (EmotionResponse emotion : availableEmotions) {
            if (emotion == null || emotion.id() == null) continue;
            String label = (emotion.emojiUnicode() != null ? emotion.emojiUnicode() + " " : "")
                    + Localization.localize(emotion);
            entries.add(Entry.<String>builder(emotion.id().toString(), label).build());
        }

        emotionSelectModel = Select.<String>builder()
                .placeholder(I18n.t("story.add.select.emotion"))
                .variant(FieldVariant.GHOST)
                .items(entries)
                .build();

        if (selectedEmotionId != null && !selectedEmotionId.isBlank()) {
            entries.stream()
                    .filter(e -> selectedEmotionId.equals(e.getValue()))
                    .findFirst()
                    .ifPresent(emotionSelectModel::setSelectedItem);
        }

        emotionSelectView = new SelectView<>();
        emotionSelectView.update(emotionSelectModel);
        emotionSelectView.setMaxWidth(Double.MAX_VALUE);
        emotionSelectContainer.getChildren().setAll(emotionSelectView);
    }

    private String resolveSelectedEmotionId() {
        if (emotionSelectModel == null || emotionSelectModel.getSelectedItem() == null) return null;
        String val = emotionSelectModel.getSelectedItem().getValue();
        return val == null || val.isBlank() ? null : val;
    }

    private Instant buildStoryTime() {
        LocalDate date = datePicker.getValue();
        if (date == null) return Instant.now();
        int hour = hourSpinner.getValue() == null ? 0 : hourSpinner.getValue();
        int minute = minuteSpinner.getValue() == null ? 0 : minuteSpinner.getValue();
        return date.atTime(hour, minute).toInstant(ZoneOffset.UTC);
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
