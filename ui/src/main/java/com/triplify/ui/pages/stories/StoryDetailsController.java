package com.triplify.ui.pages.stories;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.story.StoryService;
import com.triplify.application.usecase.story.dto.DeleteStoryRequest;
import com.triplify.application.usecase.story.dto.GetStoryByIdRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.domain.model.enums.ImageOwnerType;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.map.InteractiveMap;
import com.triplify.ui.pages.images.ImageFormModalView;
import com.triplify.ui.pages.images.ImageViewModalView;
import com.triplify.ui.pages.images.view.ImageCardView;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.add_card.view.AddCardView;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.DisplayUtils;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.EditorUtils.configureButtonIcon;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class StoryDetailsController extends SimpleLifecycleAwareController {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm").withZone(ZoneOffset.UTC);

    @FXML private VBox contentContainer;
    @FXML private Button backButton;
    @FXML private Label storyTitleLabel;
    @FXML private HBox metaRow;
    @FXML private Label storyTimeLabel;
    @FXML private HBox emotionRow;
    @FXML private ImageView emotionEmojiView;
    @FXML private Label emotionLabel;
    @FXML private FlowPane tagsFlow;
    @FXML private FlowPane topRowFlow;
    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionValueLabel;
    @FXML private SectionHeaderView contextHeader;
    @FXML private VBox contextContainer;
    @FXML private VBox mapCard;
    @FXML private Label mapTitleLabel;
    @FXML private InteractiveMap storyMap;
    @FXML private SectionHeaderView imagesHeader;
    @FXML private CardGridPane<ImageResponse> imagesGrid;
    @FXML private DetailActionButtonsView actionButtonsView;

    @Inject private StoryService storyService;
    @Inject private ImageService imageService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private ImageFormModalView imageFormModal;
    private ImageViewModalView imageViewModal;
    private String storyId;

    @FXML
    public void initialize() {
        configureButtonIcon(backButton, "fth-chevron-left", 16, "place-details-back-icon");
        Localization.bindText(backButton.textProperty(), "story.details.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "story.details.description");
        Localization.bindText(contextHeader.titleProperty(), "story.details.section.context");
        Localization.bindText(mapTitleLabel.textProperty(), "story.details.section.location");
        Localization.bindText(imagesHeader.titleProperty(), "story.details.section.images");

        topRowFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());

        EditorUtils.installRoundedClip(storyMap, 18);
        storyMap.setSelectionEnabled(false);
        storyMap.setControlsVisible(false);

        actionButtonsView.configurePrimary(fxmlLoader,
                Localization.textBinding("story.details.action.edit"), "fth-edit-3", this::onEditStory);
        actionButtonsView.configureDelete(fxmlLoader,
                Localization.textBinding("story.details.action.delete"), "fth-trash-2",
                Localization.textBinding("story.details.action.delete.confirm"), this::onDeleteStory);

        setupImagesGrid();
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

        DisplayUtils.bindEmoji(emotionRow, emotionLabel, emotionEmojiView, story.emotion(), story.emotion().emojiUnicode(), 18);
        descriptionValueLabel.setText(EditorUtils.safeText(story.description(), I18n.t("story.details.empty.description")));

        renderTags(story);
        renderContext(story);
        renderMap(story);
        setupImageLoader(UUID.fromString(storyId));
        imagesGrid.refresh();
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
            contextContainer.getChildren().add(buildContextLink(
                    I18n.t("story.details.context.trip"), () -> openTrip(story.tripId())));
        }
        if (story.tripRouteId() != null) {
            contextContainer.getChildren().add(buildContextLink(
                    I18n.t("story.details.context.route"), () -> openRoute(story.tripRouteId())));
        }
        if (story.tripPlaceId() != null) {
            contextContainer.getChildren().add(buildContextLink(
                    I18n.t("story.details.context.place"), () -> openPlace(story.tripPlaceId())));
        }
    }

    private void renderMap(StoryResponse story) {
        boolean hasLocation = story.latitude() != null && story.longitude() != null;
        mapCard.setVisible(hasLocation);
        mapCard.setManaged(hasLocation);
        if (hasLocation) {
            storyMap.setMapCenter(story.latitude(), story.longitude());
            storyMap.setPinPosition(story.latitude(), story.longitude());
        }
    }

    private void setupImagesGrid() {
        imagesGrid.setManualLoadMore(true);
        imagesGrid.setMinCardWidth(220);
        imagesGrid.setMaxColumns(4);
        imagesGrid.setPageSize(7);
        imagesGrid.setEmptyTextKey("story.details.empty.images");
        imagesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        imageFormModal = new ImageFormModalView(fxmlLoader, imageService, errorHandler);
        imageViewModal = new ImageViewModalView(imageService, errorHandler, fxmlLoader);

        imagesGrid.addPinnedNode(new AddCardView(
                "images.add.card.title", "images.add.card.subtitle", this::openAddImageModal));

        imagesGrid.setCardFactory(image ->
                ImageCardView.create(image, () -> openImageViewModal(image)).getRoot());
    }

    private void setupImageLoader(UUID forStoryId) {
        imagesGrid.setPageLoader((page, size) -> {
            var result = imageService.getImages(new GetImagesRequest(
                    new PageRequest(page - 1, size),
                    new GetImagesRequest.Filter(forStoryId.toString(), ImageOwnerType.STORY, null, null),
                    null));
            if (result.isFailure()) return new CardGridPane.PageResult<>(List.of(), null);
            var p = result.getValue();
            return new CardGridPane.PageResult<>(p.items(),
                    new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
    }

    private void openAddImageModal() {
        if (storyId == null || storyId.isBlank()) return;
        imageFormModal.show(
                contentContainer.getScene().getWindow(),
                UUID.fromString(storyId),
                ImageOwnerType.STORY,
                null,
                image -> imagesGrid.refresh());
    }

    private void openImageViewModal(ImageResponse image) {
        imageViewModal.show(
                contentContainer.getScene().getWindow(),
                image,
                deleted -> imagesGrid.refresh());
    }

    private Label buildContextLink(String text, Runnable action) {
        Label label = new Label(text);
        label.getStyleClass().add("story-details-context-link");
        label.setCursor(javafx.scene.Cursor.HAND);
        label.setOnMouseClicked(e -> action.run());
        return label;
    }

    private void openTrip(UUID tripId) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", tripId.toString());
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }

    private void openRoute(UUID tripRouteId) {
        // tripRouteId is not the same as routeId — no dedicated route details navigation
    }

    private void openPlace(UUID tripPlaceId) {
        // tripPlaceId is not the same as placeId — no dedicated navigation without a service lookup
    }
}
