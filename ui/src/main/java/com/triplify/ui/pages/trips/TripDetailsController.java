package com.triplify.ui.pages.trips;

import com.google.inject.Inject;
import com.triplify.application.shared.ColorTheme;
import com.triplify.application.shared.Pagination;
import com.triplify.application.shared.localization.LocalizedName;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.domain.model.enums.ImageOwnerType;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.story.StoryService;
import com.triplify.application.usecase.story.dto.GetStoriesRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.DeleteTripRequest;
import com.triplify.application.usecase.trip.dto.GetTripByIdRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.tripplace.TripPlaceService;
import com.triplify.application.usecase.tripplace.dto.GetTripPlacesRequest;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.application.usecase.triproute.TripRouteService;
import com.triplify.application.usecase.triproute.dto.GetTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.pages.images.ImageFormModalView;
import com.triplify.ui.pages.images.ImageViewModalView;
import com.triplify.ui.pages.images.view.ImageCardView;
import com.triplify.ui.pages.places.view.TripPlaceCardView;
import com.triplify.ui.pages.routes.RouteDetailsController;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.add_card.view.AddCardView;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.pages.routes.view.TripRouteCardView;
import com.triplify.ui.pages.stories.view.StoryCardView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.DisplayUtils;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.DisplayUtils.toLocalDate;
import static com.triplify.ui.shared.util.EditorUtils.configureButtonIcon;
import static com.triplify.ui.shared.util.EditorUtils.installRoundedClip;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;

public class TripDetailsController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(TripDetailsController.class);
    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneOffset.UTC);
    private static final int TAG_COLOR_VARIANTS = 20;

    @FXML private VBox contentContainer;
    @FXML private StackPane heroContainer;
    @FXML private Button backButton;
    @FXML private ImageView heroImageView;
    @FXML private Label tripTitleLabel;
    @FXML private Label heroStatusLabel;
    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionValueLabel;
    @FXML private Label categoryLabel;
    @FXML private Label changeStatusLabel;
    @FXML private HBox tripCountriesContainer;
    @FXML private HBox categoryContainer;
    @FXML private FlowPane tagContainer;
    @FXML private SectionHeaderView tripDateView;
    @FXML private VBox changeTripStatusContainer;
    @FXML private SelectView<StatusEnum> tripStatusSelect;
    @FXML private VBox editTripButtonContainer;
    @FXML private VBox deleteTripButtonContainer;
    @FXML private SectionHeaderView routesHeader;
    @FXML private SectionHeaderView placesHeader;
    @FXML private SectionHeaderView storiesHeader;
    @FXML private SectionHeaderView imagesHeader;
    @FXML private CardGridPane<TripRouteResponse> routesGrid;
    @FXML private CardGridPane<TripPlaceResponse> placesGrid;
    @FXML private CardGridPane<StoryResponse> storiesGrid;
    @FXML private CardGridPane<ImageResponse> imagesGrid;

    @Inject private TripService tripService;
    @Inject private TripRouteService tripRouteService;
    @Inject private TripPlaceService tripPlaceService;
    @Inject private StoryService storyService;
    @Inject private ImageService imageService;
    @Inject private UserSessionContext userSessionContext;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private String tripId;
    private ImageFormModalView imageFormModal;
    private ImageViewModalView imageViewModal;

    private Select<StatusEnum> statusSelectModel;
    private StatusEnum currentTripStatus = StatusEnum.PLANNED;
    private List<Entry<StatusEnum>> cachedStatusEntries;

    private final ChangeListener<ResourceBundle> i18nBundleListener = (obs, oldBundle, newBundle) -> {
        if (heroStatusLabel != null) {
            DisplayUtils.applyStatus(heroStatusLabel, currentTripStatus);
        }
    };
    private final WeakChangeListener<ResourceBundle> weakI18nBundleListener = new WeakChangeListener<>(i18nBundleListener);

    @FXML
    public void initialize() {
        configureButtonIcon(backButton, "fth-chevron-left", "place-details-back-icon");
        Localization.bindText(backButton.textProperty(), "trip.details.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "trip.details.description");
        Localization.bindText(routesHeader.titleProperty(), "trip.details.section.routes");
        Localization.bindText(placesHeader.titleProperty(), "trip.details.section.places");
        Localization.bindText(storiesHeader.titleProperty(), "trip.details.section.stories");
        Localization.bindText(storiesHeader.tagTextProperty(), "common.pro");
        storiesHeader.setTagVisible(true);

        topRowFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        Localization.bindText(imagesHeader.titleProperty(), "trip.details.section.images");
        Localization.bindText(categoryLabel.textProperty(), "trip.details.category");
        Localization.bindText(changeStatusLabel.textProperty(), "trip.details.changeStatus");

        heroImageView.fitWidthProperty().bind(heroContainer.widthProperty());
        heroImageView.fitHeightProperty().bind(heroContainer.heightProperty());
        installRoundedClip(heroContainer, 28);

        I18n.bundleProperty().addListener(weakI18nBundleListener);

        setupInputs();
        setupStatusSelect();
        setupRoutesGrid();
        setupPlacesGrid();
        setupStoriesGrid();
        setupImagesGrid();
    }


private void setupInputs()
{
    var editButton = AppButtonView.builder(fxmlLoader)
        .variant(ButtonVariant.PRIMARY)
        .labelBinding(Localization.textBinding("trip.details.action.edit"))
        .icon("fth-edit-3")
        .onAction(this::onEditTrip)
        .build();
    editButton.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(editButton, Priority.ALWAYS);
    editTripButtonContainer.getChildren().setAll(editButton);

    var deleteButton = AppButtonView.builder(fxmlLoader)
            .variant(ButtonVariant.DANGER_OUTLINE)
            .labelBinding(Localization.textBinding("trip.details.action.delete"))
            .icon("fth-trash-2")
            .onAction(this::onDeleteTrip)
            .build();
    deleteButton.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(deleteButton, Priority.ALWAYS);
    deleteTripButtonContainer.getChildren().setAll(deleteButton);
}

    private void setupStatusSelect() {
        cachedStatusEntries = buildStatusEntries();
        statusSelectModel = Select.<StatusEnum>builder()
                .placeholder(I18n.t("tripplace.context.status.placeholder"))
                .items(FXCollections.observableArrayList(cachedStatusEntries))
                .variant(FieldVariant.GHOST)
                .size(AppComponentSize.MIDDLE)
                .onSelect(e -> onTripStatusSelected(e.getValue()))
                .build();
        if (tripStatusSelect != null) {
            tripStatusSelect.update(statusSelectModel);
        }
    }

    private void setupRoutesGrid() {
        routesGrid.setManualLoadMore(true);
        routesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        routesGrid.setPageSize(8);
        routesGrid.setMinCardWidth(220);
        routesGrid.setMaxColumns(4);
        routesGrid.setLoadMoreKey("trip.details.show.more.routes");
        routesGrid.setEmptyTextKey("trip.details.empty.routes");
    }

    private void setupPlacesGrid() {
        placesGrid.setManualLoadMore(true);
        placesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        placesGrid.setPageSize(8);
        placesGrid.setMinCardWidth(220);
        placesGrid.setMaxColumns(4);
        placesGrid.setLoadMoreKey("trip.details.show.more.places");
        placesGrid.setEmptyTextKey("trip.details.empty.places");
    }

    private void setupStoriesGrid() {
        storiesGrid.setManualLoadMore(true);
        storiesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        storiesGrid.setPageSize(8);
        storiesGrid.setMinCardWidth(220);
        storiesGrid.setMaxColumns(4);
        storiesGrid.setLoadMoreKey("trip.details.show.more.stories");
        storiesGrid.setEmptyTextKey("trip.details.empty.stories");

        AddCardView addCard = new AddCardView(
                "stories.add.card.title",
                "stories.add.card.subtitle",
                () -> navigateToAddStory(UUID.fromString(tripId)),
                isProUser(),
                this::showProRequiredToast
        );
        storiesGrid.addPinnedNode(addCard);
    }

    private void setupImagesGrid() {
        imagesGrid.setManualLoadMore(true);
        imagesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        imagesGrid.setMinCardWidth(220);
        imagesGrid.setMaxColumns(4);
        imagesGrid.setPageSize(8);
        imagesGrid.setEmptyTextKey("trip.details.empty.images");

        imageFormModal = new ImageFormModalView(fxmlLoader, imageService, errorHandler);
        imageViewModal = new ImageViewModalView(imageService, errorHandler, fxmlLoader);

        AddCardView addCard = new AddCardView(
                "images.add.card.title",
                "images.add.card.subtitle",
                this::openAddImageModal
        );
        imagesGrid.addPinnedNode(addCard);

        imagesGrid.setCardFactory(image -> {
            ImageCardView card = ImageCardView.create(image, () -> openImageViewModal(image));
            return card.getRoot();
        });
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        tripId = data == null ? null : data.getValue("tripId");
    }

    @Override
    public void onLifecycleShow() {
        loadTripDetails();
    }

    @FXML
    private void onBack() {
        getRouter().popBackStack();
    }

    private void onEditTrip() {
        if (tripId == null || tripId.isBlank()) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", tripId);
        getRouter().moveto(RouteIds.ADD_TRIP, args);
    }

    private void onDeleteTrip() {
        if (tripId == null || tripId.isBlank()) return;
        var result = tripService.deleteTrip(new DeleteTripRequest(UUID.fromString(tripId)));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            return;
        }
        toast.success(I18n.t("trip.details.toast.deleted.title"), I18n.t("trip.details.toast.deleted.body"));
        getRouter().popBackStack();
    }

    private void loadTripDetails() {
        if (tripId == null || tripId.isBlank()) {
            toast.warning(I18n.t("trip.details.toast.notFound"));
            getRouter().popBackStack();
            return;
        }

        UUID uuid = UUID.fromString(tripId);

        var tripResult = tripService.getTripById(new GetTripByIdRequest(uuid));
        if (tripResult.isFailure()) {
            errorHandler.handle(tripResult.getError());
            getRouter().popBackStack();
            return;
        }

        bind(tripResult.getValue());
    }

    private void bind(TripResponse trip) {
        currentTripStatus = trip.status() == null ? StatusEnum.PLANNED : trip.status();
        if (heroStatusLabel != null) {
            DisplayUtils.applyStatus(heroStatusLabel, currentTripStatus);
        }

        if (trip.coverImage() != null) {
            String coverUrl = DisplayUtils.deriveCoverUrl(trip.coverImage());
            Image image = EditorUtils.loadImage(coverUrl, DEFAULT_IMAGE, RouteDetailsController.class);
            EditorUtils.setCoverPreviewImage(heroImageView, heroContainer, image);
        }
        tripTitleLabel.setText(trip.title());
        descriptionValueLabel.setText(EditorUtils.safeText(trip.description(), I18n.t("trip.details.empty.description")));

        tripCountriesContainer.getChildren().clear();
        for (CountryResponse country : trip.countries()) {
            HBox pill = new HBox(6);
            pill.getStyleClass().addAll("trip-editor-chip", "trip-editor-chip-accent");
            pill.setAlignment(Pos.CENTER_LEFT);

            ImageView emojiView = new ImageView();
            Label countryLabel = new Label();
            DisplayUtils.bindCountry(pill, countryLabel, emojiView, country, 16);

            pill.getChildren().addAll(emojiView, countryLabel);
            tripCountriesContainer.getChildren().add(pill);
        }

        var categoryChildren = categoryContainer.getChildren();
        categoryChildren.remove(1, categoryChildren.size());
        if (trip.category() != null) {
            HBox pill = new HBox(6);
            pill.getStyleClass().addAll("trip-editor-chip", "trip-editor-chip-accent");
            pill.setAlignment(Pos.CENTER_LEFT);

            ImageView emojiView = new ImageView();
            Label categoryLabel = new Label();
            DisplayUtils.bindEmoji(pill, categoryLabel, emojiView, trip.category(), trip.category().emojiUnicode(), 16);

            pill.getChildren().addAll(emojiView, categoryLabel);
            categoryContainer.getChildren().add(pill);
        }

        tagContainer.getChildren().clear();
        if (trip.tags() != null && !trip.tags().isEmpty()) {
            for (TagResponse tag : trip.tags()) {
                String name = EditorUtils.safeText(tag.name(), "");
                Button tagChip = new Button(name);
                tagChip.setFocusTraversable(false);
                tagChip.getStyleClass().addAll("trip-editor-chip", tagColorClass(name));
                tagContainer.getChildren().add(tagChip);
            }
        }

        LocalDate startDate = toLocalDate(trip.startedAt());
        LocalDate endDate = toLocalDate(trip.endedAt());
        tripDateView.titleProperty().unbind();
        tripDateView.titleProperty().bind(Bindings.createStringBinding(
                () -> DisplayUtils.formatDateRangeLocalized(startDate, endDate),
                I18n.languageProperty()
        ));

        Entry<StatusEnum> selectedEntry = cachedStatusEntries.stream()
                .filter(e -> e.getValue() == currentTripStatus)
                .findFirst()
                .orElse(null);
        if (statusSelectModel != null) {
            statusSelectModel.setSelectedItem(selectedEntry);
            if (tripStatusSelect != null) {
                tripStatusSelect.update(statusSelectModel);
            }
        }

        UUID tripUuid = trip.id();

        routesGrid.setCardFactory(this::buildTripRouteCard);
        routesGrid.setPageLoader((page, size) -> {
            var r = tripRouteService.getTripRoutes(new GetTripRoutesRequest(
                    new PageRequest(page - 1, size),
                    new GetTripRoutesRequest.Filter(tripUuid, null)));
            if (r.isFailure()) {
                log.warn("Failed to load trip routes: {}", r.getError().message());
                return new CardGridPane.PageResult<>(List.of(), null);
            }
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(),
                    new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        routesGrid.refresh();

        placesGrid.setCardFactory(this::buildPlaceCard);
        placesGrid.setPageLoader((page, size) -> {
            var r = tripPlaceService.getTripPlaces(new GetTripPlacesRequest(
                    new PageRequest(page - 1, size),
                    new GetTripPlacesRequest.Filter(tripUuid, null, null, null, null, null),
                    new GetTripPlacesRequest.OrderBy(false)));
            if (r.isFailure()) {
                log.warn("Failed to load trip places: {}", r.getError().message());
                return new CardGridPane.PageResult<>(List.of(), null);
            }
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(),
                    new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        placesGrid.refresh();

        storiesGrid.setCardFactory(this::buildStoryCard);
        storiesGrid.setPageLoader((page, size) -> {
            var r = storyService.getStories(new GetStoriesRequest(
                    new PageRequest(page - 1, size),
                    new GetStoriesRequest.Filter(tripUuid, null, null, null, null, null),
                    new GetStoriesRequest.OrderBy(false)));
            if (r.isFailure()) {
                log.warn("Failed to load trip stories: {}", r.getError().message());
                return new CardGridPane.PageResult<>(List.of(), null);
            }
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(),
                    new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        storiesGrid.refresh();

        setupImageLoader(tripUuid);
    }

    private void setupImageLoader(UUID forTripId) {
        imagesGrid.setPageLoader((page, size) -> {
            var result = imageService.getImages(new GetImagesRequest(
                    new PageRequest(page - 1, size),
                    new GetImagesRequest.Filter(forTripId.toString(), ImageOwnerType.TRIP, null, null),
                    null
            ));
            if (result.isFailure()) {
                log.warn("Failed to load trip images: {}", result.getError().message());
                return new CardGridPane.PageResult<>(List.of(), null);
            }
            var domainPage = result.getValue();
            int totalPages = domainPage.hasNext() ? page + 1 : page;
            return new CardGridPane.PageResult<>(domainPage.items(),
                    new Pagination(page, size, null, totalPages));
        });
        imagesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        imagesGrid.refresh();
    }

    private void openAddImageModal() {
        if (tripId == null || tripId.isBlank()) return;
        UUID uuid = UUID.fromString(tripId);
        imageFormModal.show(
                contentContainer.getScene().getWindow(),
                uuid,
                ImageOwnerType.TRIP,
                null,
                image -> imagesGrid.refresh()
        );
    }

    private void openImageViewModal(ImageResponse image) {
        imageViewModal.show(
                contentContainer.getScene().getWindow(),
                image,
                deleted -> imagesGrid.refresh()
        );
    }

    private void onTripStatusSelected(StatusEnum status) {
        if (tripId == null || tripId.isBlank()) return;
        if (status == null) return;
        if (currentTripStatus == status) return;

        var result = tripService.updateStatus(new com.triplify.application.usecase.trip.dto.UpdateTripStatusRequest(
                UUID.fromString(tripId), status, null, null
        ));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            return;
        }
        var updated = result.getValue();
        bind(updated);
        toast.success(I18n.t("trip.details.status.updated"));
    }

    private List<Entry<StatusEnum>> buildStatusEntries() {
        return List.of(
                Entry.builder(StatusEnum.PLANNED, Localization.textBinding("status.planned")).colorTheme(ColorTheme.STEEL_BLUE).build(),
                Entry.builder(StatusEnum.ONGOING, Localization.textBinding("status.ongoing")).colorTheme(ColorTheme.PURPLE).build(),
                Entry.builder(StatusEnum.VISITED, Localization.textBinding("status.visited")).colorTheme(ColorTheme.GREEN).build(),
                Entry.builder(StatusEnum.CANCELED, Localization.textBinding("status.canceled")).colorTheme(ColorTheme.RED).build()
        );
    }

    private Node buildTripRouteCard(TripRouteResponse tripRoute) {
        return Objects.requireNonNull(TripRouteCardView.create(tripRoute, () -> openTripRoute(tripRoute))).getRoot();
    }

    private Node buildPlaceCard(TripPlaceResponse tripPlace) {
        return TripPlaceCardView.create(tripPlace, () -> openTripPlace(tripPlace.id(), tripPlace.place().id())).getRoot();
    }

    private Node buildStoryCard(StoryResponse story) {
        return StoryCardView.create(story, () -> openStory(story)).getRoot();
    }

    private void openTripRoute(TripRouteResponse route) {
        if (route == null || route.id() == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("routeId", route.route().id().toString());
        args.addArgument("tripRouteId", route.id().toString());
        getRouter().moveto(RouteIds.ROUTE_DETAILS, args);
    }

    private void openTripPlace(UUID tripPlaceId, UUID placeId) {
        if (placeId == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("placeId", placeId.toString());
        args.addArgument("tripPlaceId", tripPlaceId.toString());
        getRouter().moveto(RouteIds.PLACE_DETAILS, args);
    }

    private void openStory(StoryResponse story) {
        if (story == null || story.id() == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("storyId", story.id().toString());
        getRouter().moveto(RouteIds.STORY_DETAILS, args);
    }

    private void navigateToAddStory(UUID forTripId) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", forTripId.toString());
        getRouter().moveto(RouteIds.ADD_STORY, args);
    }

    private boolean isProUser() {
        return userSessionContext.getCurrent()
                .map(user -> user.role() == RoleEnum.PRO_USER)
                .orElse(false);
    }

    private void showProRequiredToast() {
        toast.error(I18n.t("error.story.premium.required"));
    }

    private Image loadImage(TripResponse trip) {
        String url = trip.coverImage() != null && trip.coverImage().url() != null
                ? trip.coverImage().url().toString() : DEFAULT_IMAGE;
        return EditorUtils.loadImage(url, DEFAULT_IMAGE, getClass());
    private String tagColorClass(String tag) {
        int index = Math.floorMod(tag == null ? 0 : tag.hashCode(), TAG_COLOR_VARIANTS);
        return "app-tag-picker-chip-color-" + index;
    }

    private void configureButtonIcon(Button button, String iconLiteral, String styleClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
    }
}
