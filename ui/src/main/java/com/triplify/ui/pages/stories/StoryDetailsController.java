package com.triplify.ui.pages.stories;

import com.google.inject.Inject;
import com.triplify.application.usecase.story.StoryService;
import com.triplify.application.usecase.story.dto.DeleteStoryRequest;
import com.triplify.application.usecase.story.dto.GetStoryByIdRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.EditorUtils.configureButtonIcon;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class StoryDetailsController extends SimpleLifecycleAwareController {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneOffset.UTC);

    @FXML private Button backButton;
    @FXML private Label storyTitleLabel;
    @FXML private Label storyTimeLabel;
    @FXML private Label storyEmotionLabel;
    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionValueLabel;
    @FXML private FlowPane tagsFlow;
    @FXML private SectionHeaderView contextHeader;
    @FXML private VBox contextContainer;
    @FXML private DetailActionButtonsView actionButtonsView;

    @Inject private StoryService storyService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;

    private String storyId;

    @FXML
    public void initialize() {
        configureButtonIcon(backButton, "fth-chevron-left", "place-details-back-icon");
        Localization.bindText(backButton.textProperty(), "story.details.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "story.details.description");
        Localization.bindText(contextHeader.titleProperty(), "story.details.section.context");

        Localization.bindText(actionButtonsView.getPrimaryButton().textProperty(), "story.details.action.edit");
        Localization.bindText(actionButtonsView.getSecondaryButton().textProperty(), "story.details.action.delete");
        actionButtonsView.getPrimaryButton().setOnAction(e -> onEditStory());
        actionButtonsView.getSecondaryButton().setOnAction(e -> onDeleteStory());
        configureButtonIcon(actionButtonsView.getPrimaryButton(), "fth-edit-3", "app-btn-icon");
        configureButtonIcon(actionButtonsView.getSecondaryButton(), "fth-trash-2", "app-btn-icon");
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        storyId = data == null ? null : data.getValue("storyId");
    }

    @Override
    public void onLifecycleShow() {
        loadStoryDetails();
    }

    @FXML
    private void onBack() {
        getRouter().popBackStack();
    }

    private void onEditStory() {
        if (storyId == null || storyId.isBlank()) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("storyId", storyId);
        getRouter().moveto(RouteIds.ADD_STORY, args);
    }

    private void onDeleteStory() {
        if (storyId == null || storyId.isBlank()) return;
        var result = storyService.deleteStory(new DeleteStoryRequest(UUID.fromString(storyId)));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            return;
        }
        toast.success(I18n.t("story.details.toast.deleted.title"), I18n.t("story.details.toast.deleted.body"));
        getRouter().popBackStack();
    }

    private void loadStoryDetails() {
        if (storyId == null || storyId.isBlank()) {
            toast.warning(I18n.t("story.details.toast.notFound"));
            getRouter().popBackStack();
            return;
        }

        var result = storyService.getStoryById(new GetStoryByIdRequest(UUID.fromString(storyId)));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            getRouter().popBackStack();
            return;
        }

        bind(result.getValue());
    }

    private void bind(StoryResponse story) {
        storyTitleLabel.setText(story.title() == null || story.title().isBlank()
                ? I18n.t("story.details.fallback.title") : story.title());

        storyTimeLabel.setText(story.storyTime() == null ? "" : TIME_FORMAT.format(story.storyTime()));

        if (story.emotion() != null) {
            String emotionText = (story.emotion().emojiUnicode() != null ? story.emotion().emojiUnicode() + " " : "")
                    + Localization.localize(story.emotion());
            storyEmotionLabel.setText(emotionText);
            storyEmotionLabel.setVisible(true);
            storyEmotionLabel.setManaged(true);
        } else {
            storyEmotionLabel.setVisible(false);
            storyEmotionLabel.setManaged(false);
        }

        descriptionValueLabel.setText(story.description() == null || story.description().isBlank()
                ? I18n.t("story.details.empty.description") : story.description());

        renderTags(story);
        renderContext(story);
    }

    private void renderTags(StoryResponse story) {
        tagsFlow.getChildren().clear();
        if (story.tags() == null || story.tags().isEmpty()) return;
        for (TagResponse tag : story.tags()) {
            if (tag == null || tag.name() == null) continue;
            Label chip = new Label(tag.name());
            chip.getStyleClass().addAll("trip-editor-chip", "trip-editor-chip-soft");
            tagsFlow.getChildren().add(chip);
        }
    }

    private void renderContext(StoryResponse story) {
        contextContainer.getChildren().clear();

        if (story.tripId() != null) {
            Label link = buildContextLink(I18n.t("story.details.context.trip"));
            link.setOnMouseClicked(e -> openTrip(story.tripId()));
            contextContainer.getChildren().add(link);
        }

        if (story.tripRouteId() != null) {
            Label link = buildContextLink(I18n.t("story.details.context.route"));
            link.setOnMouseClicked(e -> openRoute(story.tripRouteId()));
            contextContainer.getChildren().add(link);
        }

        if (story.tripPlaceId() != null) {
            Label link = buildContextLink(I18n.t("story.details.context.place"));
            link.setOnMouseClicked(e -> openPlace(story.tripPlaceId()));
            contextContainer.getChildren().add(link);
        }
    }

    private Label buildContextLink(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("story-details-context-link");
        label.setCursor(javafx.scene.Cursor.HAND);
        return label;
    }

    private void openTrip(UUID tripId) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", tripId.toString());
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }

    private void openRoute(UUID tripRouteId) {
        // tripRouteId is not the same as routeId — we navigate to trip details for now
        // A dedicated TripRoute details page is not in scope
    }

    private void openPlace(UUID tripPlaceId) {
        // tripPlaceId is not the same as placeId — without a service lookup we cannot navigate directly
    }

    private void configureButtonIcon(Button button, String iconLiteral, String styleClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
    }
}
