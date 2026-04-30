package com.triplify.ui.pages.trips;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
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
import com.triplify.application.usecase.tripplace.dto.GetTripPlacesRequest;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.application.usecase.triproute.TripRouteService;
import com.triplify.application.usecase.triproute.dto.GetTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.application.usecase.triproute.dto.UpdateTripRouteStatusRequest;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.model.enums.TripPlaceSourceType;
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
    @FXML private CardGridPane<TripRouteResponse> routesGrid;
    @FXML private CardGridPane<TripPlaceResponse> placesGrid;
    @FXML private CardGridPane<StoryResponse> storiesGrid;
    @FXML private Button addStoryButton;
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

        heroImageView.fitWidthProperty().bind(heroContainer.widthProperty());
        heroImageView.fitHeightProperty().bind(heroContainer.heightProperty());
        installRoundedClip(heroContainer, 28);

        actionButtonsView.configurePrimary(fxmlLoader, Localization.textBinding("trip.details.action.edit"), "fth-edit-3", this::onEditTrip);
        actionButtonsView.configureDelete(fxmlLoader, Localization.textBinding("trip.details.action.delete"), "fth-trash-2", Localization.textBinding("trip.details.action.delete.confirm"), this::onDeleteTrip);

        Localization.bindText(imagesHeader.titleProperty(), "trip.details.section.images");
        Localization.bindText(addStoryButton.textProperty(), "trip.details.action.addStory");

        setupRoutesGrid();
        setupPlacesGrid();
        setupStoriesGrid();
        setupImagesGrid();
    }

    private void setupRoutesGrid() {
        routesGrid.setManualLoadMore(true);
        routesGrid.setPageSize(8);
        routesGrid.setMinCardWidth(220);
        routesGrid.setMaxColumns(3);
        routesGrid.setLoadMoreKey("trip.details.show.more.routes");
        routesGrid.setEmptyTextKey("trip.details.empty.routes");
    }

    private void setupPlacesGrid() {
        placesGrid.setManualLoadMore(true);
        placesGrid.setPageSize(8);
        placesGrid.setMinCardWidth(180);
        placesGrid.setMaxColumns(4);
        placesGrid.setLoadMoreKey("trip.details.show.more.places");
        placesGrid.setEmptyTextKey("trip.details.empty.places");
    }

    private void setupStoriesGrid() {
        storiesGrid.setManualLoadMore(true);
        storiesGrid.setPageSize(8);
        storiesGrid.setMinCardWidth(220);
        storiesGrid.setMaxColumns(3);
        storiesGrid.setLoadMoreKey("trip.details.show.more.stories");
        storiesGrid.setEmptyTextKey("trip.details.empty.stories");
    }

    private void setupImagesGrid() {
        imagesGrid.setManualLoadMore(true);
        imagesGrid.setMinCardWidth(220);
        imagesGrid.setMaxColumns(4);
        imagesGrid.setPageSize(8);
        imagesGrid.setEmptyTextKey("trip.details.empty.images");

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

        bind(tripResult.getValue());
    }

    private void bind(TripResponse trip) {
        heroImageView.setImage(loadImage(trip));
        tripTitleLabel.setText(safeText(trip.title(), I18n.t("trip.add.fallback.trip")));
        tripStatusLabel.setText(trip.status() == null ? "" : trip.status().getLabel());
        tripDatesLabel.setText(DisplayUtils.formatDateRange(toLocalDate(trip.startedAt()), toLocalDate(trip.endedAt())));
        tripCountriesLabel.setText(DisplayUtils.deriveCountryLabel(trip.countries()));
        tripCategoryLabel.setText(trip.category() == null ? "" : safeText(Localization.localize(trip.category()), ""));
        descriptionValueLabel.setText(safeText(trip.description(), I18n.t("trip.details.empty.description")));

        UUID tripUuid = trip.id();

        routesGrid.setCardFactory(tr -> buildTripRouteCard(tr, tripUuid));
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
                    new GetTripPlacesRequest.Filter(tripUuid, TripPlaceSourceType.MANUAL, null, null, null, null),
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

        storiesGrid.setCardFactory(story -> buildStoryCard(story, tripUuid));
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

        addStoryButton.setOnAction(e -> navigateToAddStory(tripUuid));

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
