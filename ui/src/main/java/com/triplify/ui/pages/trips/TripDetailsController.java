package com.triplify.ui.pages.trips;

import com.google.inject.Inject;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.application.usecase.image.dto.ImageOwnerType;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.story.StoryService;
import com.triplify.application.usecase.story.dto.GetStoriesRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.DeleteTripRequest;
import com.triplify.application.usecase.trip.dto.GetTripByIdRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.tripplace.TripPlaceService;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.application.usecase.triproute.TripRouteService;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.application.usecase.triproute.dto.UpdateTripRouteStatusRequest;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.pages.images.ImageFormModalView;
import com.triplify.ui.pages.images.ImageViewModalView;
import com.triplify.ui.pages.images.view.ImageCardView;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.add_card.view.AddCardView;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.shared.component.empty_state.view.EmptyStateCardView;
import com.triplify.ui.pages.routes.view.RouteCardView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.DisplayUtils;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.DisplayUtils.toLocalDate;
import static com.triplify.ui.shared.util.EditorUtils.configureButtonIcon;
import static com.triplify.ui.shared.util.EditorUtils.installRoundedClip;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class TripDetailsController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(TripDetailsController.class);
    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final int STORIES_PAGE_SIZE = 8;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneOffset.UTC);

    @FXML private VBox contentContainer;
    @FXML private StackPane heroContainer;
    @FXML private FlowPane topRowFlow;
    @FXML private Button backButton;
    @FXML private ImageView heroImageView;
    @FXML private Label tripTitleLabel;
    @FXML private Label tripStatusLabel;
    @FXML private Label tripDatesLabel;
    @FXML private Label tripCountriesLabel;
    @FXML private Label tripCategoryLabel;
    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionValueLabel;
    @FXML private DetailActionButtonsView actionButtonsView;
    @FXML private SectionHeaderView routesHeader;
    @FXML private SectionHeaderView placesHeader;
    @FXML private SectionHeaderView storiesHeader;
    @FXML private SectionHeaderView imagesHeader;
    @FXML private FlowPane routesFlow;
    @FXML private FlowPane placesFlow;
    @FXML private FlowPane storiesFlow;
    @FXML private CardGridPane<ImageResponse> imagesGrid;

    @Inject private TripService tripService;
    @Inject private TripRouteService tripRouteService;
    @Inject private TripPlaceService tripPlaceService;
    @Inject private StoryService storyService;
    @Inject private ImageService imageService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private String tripId;
    private ImageFormModalView imageFormModal;
    private ImageViewModalView imageViewModal;

    @FXML
    public void initialize() {
        configureButtonIcon(backButton, "fth-chevron-left", "place-details-back-icon");
        Localization.bindText(backButton.textProperty(), "trip.details.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "trip.details.description");
        Localization.bindText(routesHeader.titleProperty(), "trip.details.section.routes");
        Localization.bindText(placesHeader.titleProperty(), "trip.details.section.places");
        Localization.bindText(storiesHeader.titleProperty(), "trip.details.section.stories");

        topRowFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        routesFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        placesFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        storiesFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());

        heroImageView.fitWidthProperty().bind(heroContainer.widthProperty());
        heroImageView.fitHeightProperty().bind(heroContainer.heightProperty());
        installRoundedClip(heroContainer, 28);

        actionButtonsView.configurePrimary(fxmlLoader, Localization.textBinding("trip.details.action.edit"), "fth-edit-3", this::onEditTrip);
        actionButtonsView.configureDelete(fxmlLoader, Localization.textBinding("trip.details.action.delete"), "fth-trash-2", Localization.textBinding("trip.details.action.delete.confirm"), this::onDeleteTrip);

        Localization.bindText(imagesHeader.titleProperty(), "trip.details.section.images");
        setupImagesGrid();
    }

    private void setupImagesGrid() {
        imagesGrid.setManualLoadMore(true);
        imagesGrid.setMinCardWidth(220);
        imagesGrid.setMaxColumns(4);
        imagesGrid.setPageSize(8);
        imagesGrid.setEmptyText(I18n.t("trip.details.empty.images"));

        imageFormModal = new ImageFormModalView(fxmlLoader, imageService, errorHandler);
        imageViewModal = new ImageViewModalView(imageService, errorHandler);

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

        var routesResult = tripRouteService.getAllTripRoutes(uuid);
        if (routesResult.isFailure()) {
            log.warn("Failed to load trip routes: {}", routesResult.getError().message());
        }

        var placesResult = tripPlaceService.getAllManualTripPlaces(uuid);
        if (placesResult.isFailure()) {
            log.warn("Failed to load trip places: {}", placesResult.getError().message());
        }

        var storiesResult = storyService.getStories(new GetStoriesRequest(
                new PageRequest(0, STORIES_PAGE_SIZE),
                new GetStoriesRequest.Filter(uuid, null, null, null, null, null),
                new GetStoriesRequest.OrderBy(false)
        ));
        if (storiesResult.isFailure()) {
            log.warn("Failed to load trip stories: {}", storiesResult.getError().message());
        }

        bind(
                tripResult.getValue(),
                routesResult.isSuccess() ? routesResult.getValue() : List.of(),
                placesResult.isSuccess() ? placesResult.getValue() : List.of(),
                storiesResult.isSuccess() ? storiesResult.getValue().items() : List.of()
        );
    }

    private void bind(TripResponse trip, List<TripRouteResponse> routes,
                      List<TripPlaceResponse> places, List<StoryResponse> stories) {
        heroImageView.setImage(loadImage(trip));
        tripTitleLabel.setText(safeText(trip.title(), I18n.t("trip.add.fallback.trip")));
        tripStatusLabel.setText(trip.status() == null ? "" : trip.status().getLabel());
        tripDatesLabel.setText(DisplayUtils.formatDateRange(toLocalDate(trip.startedAt()), toLocalDate(trip.endedAt())));
        tripCountriesLabel.setText(DisplayUtils.deriveCountryLabel(trip.countries()));
        tripCategoryLabel.setText(trip.category() == null ? "" : safeText(Localization.localize(trip.category()), ""));
        descriptionValueLabel.setText(safeText(trip.description(), I18n.t("trip.details.empty.description")));

        renderRoutes(routes, trip.id());
        renderPlaces(places);
        renderStories(stories, trip.id());
        setupImageLoader(trip.id());
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
            com.triplify.application.shared.Pagination pagination =
                    new com.triplify.application.shared.Pagination(page, size, null, totalPages);
            return new CardGridPane.PageResult<>(domainPage.items(), pagination);
        });
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

    private void renderRoutes(List<TripRouteResponse> routes, UUID forTripId) {
        routesFlow.getChildren().clear();
        if (routes.isEmpty()) {
            routesFlow.getChildren().add(createEmptyState(I18n.t("trip.details.empty.routes"), routesFlow));
            return;
        }

        for (TripRouteResponse tripRoute : routes) {
            if (tripRoute.route() == null) continue;
            VBox card = buildTripRouteCard(tripRoute, forTripId);
            routesFlow.getChildren().add(card);
        }
    }

    private VBox buildTripRouteCard(TripRouteResponse tripRoute, UUID forTripId) {
        RouteCardView routeCard = RouteCardView.create(
                tripRoute.route(), () -> openRoute(tripRoute.route()));

        ComboBox<StatusEnum> statusCombo = new ComboBox<>(
                FXCollections.observableArrayList(StatusEnum.values()));
        statusCombo.setValue(tripRoute.status());
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.getStyleClass().add("trip-details-status-combo");
        statusCombo.setOnAction(e -> updateTripRouteStatus(tripRoute.id(), statusCombo.getValue(), forTripId));

        VBox wrapper = new VBox(8);
        wrapper.getChildren().addAll(routeCard.getRoot(), statusCombo);
        return wrapper;
    }

    private void updateTripRouteStatus(UUID tripRouteId, StatusEnum newStatus, UUID forTripId) {
        if (newStatus == null) return;
        var result = tripRouteService.updateStatus(new UpdateTripRouteStatusRequest(tripRouteId, newStatus, null, null));
        if (result.isFailure()) {
            toast.error(I18n.t("trip.details.route.status.failed"));
            errorHandler.handle(result.getError());
        } else {
            toast.success(I18n.t("trip.details.route.status.updated"));
        }
    }

    private void renderPlaces(List<TripPlaceResponse> places) {
        placesFlow.getChildren().clear();
        if (places.isEmpty()) {
            placesFlow.getChildren().add(createEmptyState(I18n.t("trip.details.empty.places"), placesFlow));
            return;
        }

        for (TripPlaceResponse tripPlace : places) {
            if (tripPlace.place() == null) continue;
            VBox card = buildPlaceCard(tripPlace);
            placesFlow.getChildren().add(card);
        }
    }

    private VBox buildPlaceCard(TripPlaceResponse tripPlace) {
        VBox card = new VBox(4);
        card.getStyleClass().add("trip-details-place-card");
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> openPlace(tripPlace.place().id()));

        Label title = new Label(safeText(tripPlace.place().title(), I18n.t("trip.add.fallback.place")));
        title.getStyleClass().add("trip-details-place-title");
        title.setWrapText(true);

        String country = tripPlace.place().country() == null ? "" : safeText(tripPlace.place().country().name(), "");
        Label countryLabel = new Label(country);
        countryLabel.getStyleClass().add("trip-details-place-country");

        card.getChildren().addAll(title, countryLabel);
        return card;
    }

    private void renderStories(List<StoryResponse> stories, UUID forTripId) {
        storiesFlow.getChildren().clear();

        javafx.scene.control.Button addStoryBtn = new javafx.scene.control.Button(I18n.t("trip.details.action.addStory"));
        addStoryBtn.getStyleClass().addAll("app-btn", "app-btn-tint");
        addStoryBtn.setOnAction(e -> navigateToAddStory(forTripId));
        storiesFlow.getChildren().add(addStoryBtn);

        if (stories.isEmpty()) {
            storiesFlow.getChildren().add(createEmptyState(I18n.t("trip.details.empty.stories"), storiesFlow));
            return;
        }

        for (StoryResponse story : stories) {
            VBox card = buildStoryCard(story, forTripId);
            storiesFlow.getChildren().add(card);
        }
    }

    private VBox buildStoryCard(StoryResponse story, UUID forTripId) {
        VBox card = new VBox(6);
        card.getStyleClass().add("trip-details-story-card");
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> openStory(story));

        Label title = new Label(safeText(story.title(), I18n.t("trip.details.story.fallback")));
        title.getStyleClass().add("trip-details-story-title");
        title.setWrapText(true);

        String timeText = story.storyTime() == null ? "" : TIME_FORMAT.format(story.storyTime());
        HBox meta = new HBox(10);
        meta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label timeLabel = new Label(timeText);
        timeLabel.getStyleClass().add("trip-details-story-time");

        if (story.emotion() != null) {
            String emotionText = story.emotion().emojiUnicode() != null
                    ? story.emotion().emojiUnicode() + " " + Localization.localize(story.emotion())
                    : Localization.localize(story.emotion());
            Label emotionLabel = new Label(emotionText);
            emotionLabel.getStyleClass().add("trip-details-story-emotion");
            meta.getChildren().addAll(timeLabel, emotionLabel);
        } else {
            meta.getChildren().add(timeLabel);
        }

        card.getChildren().addAll(title, meta);
        return card;
    }

    private void openRoute(com.triplify.application.usecase.route.dto.RouteResponse route) {
        if (route == null || route.id() == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("routeId", route.id().toString());
        getRouter().moveto(RouteIds.ROUTE_DETAILS, args);
    }

    private void openPlace(UUID placeId) {
        if (placeId == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("placeId", placeId.toString());
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

    private EmptyStateCardView createEmptyState(String text, FlowPane parent) {
        EmptyStateCardView card = new EmptyStateCardView();
        card.setText(text);
        card.prefWidthProperty().bind(parent.widthProperty());
        card.maxWidthProperty().bind(parent.widthProperty());
        return card;
    }

    private Image loadImage(TripResponse trip) {
        String url = trip.coverImage() != null && trip.coverImage().url() != null
                ? trip.coverImage().url().toString() : DEFAULT_IMAGE;
        return EditorUtils.loadImage(url, DEFAULT_IMAGE, getClass());
    }

    private static String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void configureButtonIcon(Button button, String iconLiteral, String styleClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
    }
}
