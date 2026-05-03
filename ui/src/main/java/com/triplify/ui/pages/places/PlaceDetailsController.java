package com.triplify.ui.pages.places;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.DeletePlaceRequest;
import com.triplify.application.usecase.place.dto.GetPlaceByIdRequest;
import com.triplify.application.usecase.place.dto.GetPlaceRoutesRequest;
import com.triplify.application.usecase.place.dto.GetPlaceTripsRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.story.StoryService;
import com.triplify.application.usecase.story.dto.GetStoriesRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.GetTripByIdRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.tripplace.TripPlaceService;
import com.triplify.application.usecase.tripplace.dto.GetTripPlaceByIdRequest;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.application.usecase.tripplace.dto.UpdateTripPlaceStatusRequest;
import com.triplify.domain.model.enums.ImageOwnerType;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.result.Result;
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
import com.triplify.ui.pages.routes.view.RouteCardView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.pages.stories.view.StoryCardView;
import com.triplify.ui.pages.trips.view.TripCardView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.DisplayUtils;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.DisplayUtils.*;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.List;
import java.util.UUID;

public class PlaceDetailsController extends SimpleLifecycleAwareController {

    private static final int COUNTRY_EMOJI_SIZE = 18;
    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";

    @FXML private VBox contentContainer;
    @FXML private StackPane heroContainer;
    @FXML private ImageView heroImageView;
    @FXML private FlowPane topRowFlow;
    @FXML private Button backButton;
    @FXML private Label placeTitleLabel;

    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionValueLabel;
    @FXML private Label mapTitleLabel;
    @FXML private InteractiveMap placeMap;
    @FXML private DetailActionButtonsView actionButtonsView;
    @FXML private SectionHeaderView associatedTripsHeader;
    @FXML private SectionHeaderView associatedStoriesHeader;
    @FXML private SectionHeaderView associatedRoutesHeader;
    @FXML private CardGridPane<TripResponse> tripsGrid;
    @FXML private CardGridPane<RouteResponse> routesGrid;
    @FXML private CardGridPane<StoryResponse> storiesGrid;

    @FXML private HBox countryRow;
    @FXML private ImageView countryEmojiView;
    @FXML private Label countryLabel;

    @FXML private SelectView<StatusEnum> tripPlaceStatusSelect;
    @FXML private SectionHeaderView tripImagesHeader;
    @FXML private CardGridPane<ImageResponse> tripImagesGrid;
    @FXML private SectionHeaderView tripStoriesHeader;
    @FXML private CardGridPane<StoryResponse> tripStoriesGrid;
    @FXML private VBox tripPlaceAssociatedItemsContainer;
    @FXML private VBox placeAssociatedItemsContainer;


    @FXML private VBox contextContainerCont;
    @FXML private SectionHeaderView contextHeader;
    @FXML private VBox contextContainer;

    @FXML private VBox statusContainer;
    @FXML private SectionHeaderView statusHeader;

    @Inject private PlaceService placeService;
    @Inject private TripPlaceService tripPlaceService;
    @Inject private TripService tripService;
    @Inject private ImageService imageService;
    @Inject private StoryService storyService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private String placeId;
    private String tripPlaceId;
    private TripPlaceResponse currentTripPlace;
    private Select<StatusEnum> statusSelectModel;
    private ImageFormModalView imageFormModal;
    private ImageViewModalView imageViewModal;

    @FXML
    public void initialize() {
        FontIcon icon = new FontIcon("fth-chevron-left");
        icon.setIconSize(16);
        icon.getStyleClass().add("place-details-back-icon");
        backButton.setGraphic(icon);
        Localization.bindText(backButton.textProperty(), "place.details.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "place.details.description");
        Localization.bindText(mapTitleLabel.textProperty(), "place.details.map");
        Localization.bindText(associatedTripsHeader.titleProperty(), "place.details.section.trips");
        Localization.bindText(associatedStoriesHeader.titleProperty(), "place.details.section.stories");
        Localization.bindText(associatedRoutesHeader.titleProperty(), "place.details.section.routes");
        Localization.bindText(contextHeader.titleProperty(), "story.details.section.context");
        Localization.bindText(statusHeader.titleProperty(), "filter.status");

        topRowFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());

        EditorUtils.installRoundedClip(heroContainer, 28);
        EditorUtils.initializeCoverPreview(heroImageView, heroContainer);
        EditorUtils.installRoundedClip(placeMap, 20);

        placeMap.setSelectionEnabled(false);
        placeMap.setControlsVisible(false);
        actionButtonsView.configurePrimary(fxmlLoader, Localization.textBinding("place.details.action.edit"), "fth-edit-3", this::onEditPlace);
        actionButtonsView.configureDelete(fxmlLoader, Localization.textBinding("place.details.action.delete"), "fth-trash-2", Localization.textBinding("place.details.action.delete.confirm"), this::onDeletePlace);

        setupAssociatedGrids();
        initializeTripContext();
    }

    private void setupAssociatedGrids() {
        tripsGrid.setManualLoadMore(true);
        tripsGrid.setPageSize(8);
        tripsGrid.setMinCardWidth(220);
        tripsGrid.setMaxColumns(5);
        tripsGrid.setLoadMoreKey("place.details.show.more.trips");
        tripsGrid.setEmptyTextKey("place.details.empty.trips");
        tripsGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        routesGrid.setManualLoadMore(true);
        routesGrid.setPageSize(8);
        routesGrid.setMinCardWidth(220);
        routesGrid.setMaxColumns(5);
        routesGrid.setLoadMoreKey("place.details.show.more.routes");
        routesGrid.setEmptyTextKey("place.details.empty.routes");
        routesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        storiesGrid.setManualLoadMore(true);
        storiesGrid.setPageSize(8);
        storiesGrid.setMinCardWidth(220);
        storiesGrid.setMaxColumns(5);
        storiesGrid.setLoadMoreKey("place.details.show.more.stories");
        storiesGrid.setEmptyTextKey("place.details.empty.stories");
        storiesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private void initializeTripContext() {
        statusContainer.setVisible(false);
        statusContainer.setManaged(false);

        tripPlaceAssociatedItemsContainer.setVisible(false);
        tripPlaceAssociatedItemsContainer.setManaged(false);

        contextContainerCont.setVisible(false);
        contextContainerCont.setManaged(false);

        Localization.bindText(tripImagesHeader.titleProperty(), "tripplace.context.images.header");
        Localization.bindText(tripStoriesHeader.titleProperty(), "tripplace.context.stories.header");

        statusSelectModel = Select.<StatusEnum>builder()
                .placeholder(I18n.t("tripplace.context.status.placeholder"))
                .items(FXCollections.observableArrayList(buildStatusEntries()))
                .variant(FieldVariant.GHOST)
                .size(AppComponentSize.MIDDLE)
                .onSelect(e -> onTripPlaceStatusSelected(e.getValue()))
                .build();
        tripPlaceStatusSelect.update(statusSelectModel);

        imageFormModal = new ImageFormModalView(fxmlLoader, imageService, errorHandler);
        imageViewModal = new ImageViewModalView(imageService, errorHandler, fxmlLoader);

        tripImagesGrid.setManualLoadMore(true);
        tripImagesGrid.setMinCardWidth(220);
        tripImagesGrid.setMaxColumns(4);
        tripImagesGrid.setPageSize(7);
        tripImagesGrid.setEmptyTextKey("trip.details.empty.images");
        tripImagesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tripImagesGrid.addPinnedNode(new AddCardView(
                "images.add.card.title",
                "images.add.card.subtitle",
                this::openAddImageModal
        ));
        tripImagesGrid.setCardFactory(image -> ImageCardView.create(image, () -> openImageViewModal(image)).getRoot());

        tripStoriesGrid.setManualLoadMore(true);
        tripStoriesGrid.setMinCardWidth(220);
        tripStoriesGrid.setMaxColumns(4);
        tripStoriesGrid.setPageSize(8);
        tripStoriesGrid.setEmptyTextKey("trip.details.empty.stories");
        tripStoriesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tripStoriesGrid.addPinnedNode(new AddCardView(
                "stories.add.card.title",
                "stories.add.card.subtitle",
                this::navigateToAddStory
        ));
        tripStoriesGrid.setCardFactory(story -> StoryCardView.create(story, () -> openStory(story)).getRoot());
    }

    @Override
    public void onLifecycleShow() {
        RouterArgument data = getRouter().getCurrentData();
        placeId = data == null ? null : data.getValue("placeId");
        tripPlaceId = data == null ? null : data.getValue("tripPlaceId");
        loadPlaceDetails();
    }

    @FXML
    private void onBack() {
        getRouter().popBackStack();
    }

    @FXML
    private void onEditPlace() {
        if (placeId == null || placeId.isBlank()) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", UUID.randomUUID().toString());
        args.addArgument("tripName", "");
        args.addArgument("placeId", placeId);
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    @FXML
    private void onDeletePlace() {
        if (placeId == null || placeId.isBlank()) {
            return;
        }

        var result = placeService.deletePlace(new DeletePlaceRequest(UUID.fromString(placeId)));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            return;
        }

        toast.success(I18n.t("place.details.toast.deleted.title"), I18n.t("place.details.toast.deleted.body"));
        getRouter().popBackStack();
    }

    private void loadPlaceDetails() {
        if ((placeId == null || placeId.isBlank()) && (tripPlaceId == null || tripPlaceId.isBlank())) {
            toast.warning(I18n.t("place.details.toast.notFound"));
            getRouter().popBackStack();
            return;
        }

        if(tripPlaceId != null && !tripPlaceId.isBlank()) {
            loadTripContext();
            bind(currentTripPlace.place());
            return;
        }
        resetTripContext();

        UUID placeUuid = UUID.fromString(placeId);

        var placeResult = placeService.getPlaceById(new GetPlaceByIdRequest(placeUuid));
        if (placeResult.isFailure()) {
            errorHandler.handle(placeResult.getError());
            getRouter().popBackStack();
            return;
        }
        bind(placeResult.getValue());
    }

    private void bind(PlaceResponse place) {
        if (place.coverImage() != null) {
            String coverUrl = DisplayUtils.deriveCoverUrl(place.coverImage());
            Image image = EditorUtils.loadImage(coverUrl, DEFAULT_IMAGE, PlaceDetailsController.class);
            EditorUtils.setCoverPreviewImage(heroImageView, heroContainer, image);
        }

        placeTitleLabel.setText(place.title());
        descriptionValueLabel.setText(EditorUtils.safeText(place.description(), I18n.t("place.details.empty.description")));

        DisplayUtils.bindCountry(countryRow, countryLabel, countryEmojiView, place.country(), COUNTRY_EMOJI_SIZE);

        placeMap.setMapCenter(place.latitude(), place.longitude());
        placeMap.setPinPosition(place.latitude(), place.longitude());

        UUID placeUuid = place.id();

        tripsGrid.setCardFactory(trip -> {
            String dateRange = formatDateRange(toLocalDate(trip.startedAt()), toLocalDate(trip.endedAt()));
            return (Region) TripCardView.create(trip, dateRange, () -> openTrip(trip)).getRoot();
        });
        tripsGrid.setPageLoader((page, size) -> {
            var r = placeService.getPlaceTrips(new GetPlaceTripsRequest(placeUuid, new PageRequest(page - 1, size)));
            if (r.isFailure()) return new CardGridPane.PageResult<>(List.of(), null);
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(), new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        tripsGrid.refresh();

        routesGrid.setCardFactory(route -> (Region) RouteCardView.create(route, () -> openRoute(route)).getRoot());
        routesGrid.setPageLoader((page, size) -> {
            var r = placeService.getPlaceRoutes(new GetPlaceRoutesRequest(placeUuid, new PageRequest(page - 1, size)));
            if (r.isFailure()) return new CardGridPane.PageResult<>(List.of(), null);
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(), new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        routesGrid.refresh();

        storiesGrid.setCardFactory(story -> StoryCardView.create(story, () -> openStory(story)).getRoot());
        storiesGrid.setPageLoader((page, size) -> {
            var r = storyService.getStories(new GetStoriesRequest(
                    new PageRequest(page - 1, size),
                    new GetStoriesRequest.Filter(null, null, placeUuid, null, null, null),
                    new GetStoriesRequest.OrderBy(false)));
            if (r.isFailure()) return new CardGridPane.PageResult<>(List.of(), null);
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(), new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        storiesGrid.refresh();
    }

    private void loadTripContext() {
        placeAssociatedItemsContainer.setVisible(false);
        placeAssociatedItemsContainer.setManaged(false);

        tripPlaceAssociatedItemsContainer.setVisible(true);
        tripPlaceAssociatedItemsContainer.setManaged(true);

        statusContainer.setVisible(true);
        statusContainer.setManaged(true);
        contextContainerCont.setVisible(true);
        contextContainerCont.setManaged(true);

        Result<TripPlaceResponse> result = tripPlaceService.getTripPlaceById(
                new GetTripPlaceByIdRequest(UUID.fromString(tripPlaceId)));
        if (result.isFailure()) {
            return;
        }

        currentTripPlace = result.getValue();

        Entry<StatusEnum> selectedEntry = buildStatusEntries().stream()
                .filter(e -> e.getValue() == currentTripPlace.status())
                .findFirst()
                .orElse(null);
        statusSelectModel.setSelectedItem(selectedEntry);
        tripPlaceStatusSelect.update(statusSelectModel);

        UUID tripPlaceUuid = UUID.fromString(tripPlaceId);

        tripImagesGrid.setPageLoader((page, size) -> {
            var r = imageService.getImages(new GetImagesRequest(
                    new PageRequest(page - 1, size),
                    new GetImagesRequest.Filter(tripPlaceUuid.toString(), ImageOwnerType.TRIP_PLACE, null, null),
                    null));
            if (r.isFailure()) return new CardGridPane.PageResult<>(List.of(), null);
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(),
                    new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        tripImagesGrid.refresh();

        tripStoriesGrid.setPageLoader((page, size) -> {
            var r = storyService.getStories(new GetStoriesRequest(
                    new PageRequest(page - 1, size),
                    new GetStoriesRequest.Filter(null, null, tripPlaceUuid, null, null, null),
                    new GetStoriesRequest.OrderBy(false)));
            if (r.isFailure()) return new CardGridPane.PageResult<>(List.of(), null);
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(),
                    new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        tripStoriesGrid.refresh();

        DisplayUtils.renderTripPlaceContext(getRouter(),contextContainer, currentTripPlace);
    }

    private void resetTripContext() {
        contextContainerCont.setVisible(false);
        contextContainerCont.setManaged(false);
        statusContainer.setVisible(false);
        statusContainer.setManaged(false);
        tripPlaceAssociatedItemsContainer.setVisible(false);
        tripPlaceAssociatedItemsContainer.setManaged(false);
        placeAssociatedItemsContainer.setVisible(true);
        placeAssociatedItemsContainer.setManaged(true);
        currentTripPlace = null;
    }

    private void onTripPlaceStatusSelected(StatusEnum status) {
        if (currentTripPlace == null || status == null) return;
        if (currentTripPlace.status() == status) return;
        Result<TripPlaceResponse> result = tripPlaceService.updateTripPlaceStatus(new UpdateTripPlaceStatusRequest(currentTripPlace.id(), status));
        result.onSuccess(updated -> {
            currentTripPlace = updated;
            toast.success(I18n.t("tripplace.context.status.updated"));
        });
        result.onFailure(error -> errorHandler.handle(error));
    }

    private void openAddImageModal() {
        if (currentTripPlace == null) return;
        imageFormModal.show(
                contentContainer.getScene().getWindow(),
                currentTripPlace.id(),
                ImageOwnerType.TRIP_PLACE,
                currentTripPlace.tripId(),
                image -> tripImagesGrid.refresh()
        );
    }

    private void openImageViewModal(ImageResponse image) {
        imageViewModal.show(
                contentContainer.getScene().getWindow(),
                image,
                deleted -> tripImagesGrid.refresh()
        );
    }

    private void navigateToAddStory() {
        if (currentTripPlace == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", currentTripPlace.tripId().toString());
        args.addArgument("tripPlaceId", currentTripPlace.id().toString());
        getRouter().moveto(RouteIds.ADD_STORY, args);
    }

    private List<Entry<StatusEnum>> buildStatusEntries() {
        return List.of(
                Entry.builder(StatusEnum.PLANNED, Localization.textBinding("status.planned")).build(),
                Entry.builder(StatusEnum.ONGOING, Localization.textBinding("status.ongoing")).build(),
                Entry.builder(StatusEnum.VISITED, Localization.textBinding("status.visited")).build(),
                Entry.builder(StatusEnum.CANCELED, Localization.textBinding("status.canceled")).build()
        );
    }

    private void openTrip(TripResponse trip) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", trip.id().toString());
        args.addArgument("tripStatus", trip.status());
        getRouter().moveto(RouteIds.ADD_TRIP, args);
    }

    private void openRoute(RouteResponse route) {
        if (route == null || route.id() == null) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("routeId", route.id().toString());
        getRouter().moveto(RouteIds.ROUTE_DETAILS, args);
    }

    private void openStory(StoryResponse story) {
        if (story == null || story.id() == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("storyId", story.id().toString());
        getRouter().moveto(RouteIds.STORY_DETAILS, args);
    }
}
