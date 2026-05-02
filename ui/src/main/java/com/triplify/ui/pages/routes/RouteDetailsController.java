package com.triplify.ui.pages.routes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.kordamp.ikonli.javafx.FontIcon;

import com.gluonhq.maps.MapPoint;
import com.google.inject.Inject;
import com.triplify.application.shared.GeoCalculator;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.DeleteRouteRequest;
import com.triplify.application.usecase.route.dto.GetRouteByIdRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.story.StoryService;
import com.triplify.application.usecase.story.dto.GetStoriesRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.GetTripByIdRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.triproute.TripRouteService;
import com.triplify.application.usecase.triproute.dto.GetTripRouteByIdRequest;
import com.triplify.application.usecase.triproute.dto.GetTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.application.usecase.triproute.dto.UpdateTripRouteStatusRequest;
import com.triplify.domain.model.enums.ImageOwnerType;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.map.InteractiveMap;
import com.triplify.ui.pages.images.ImageFormModalView;
import com.triplify.ui.pages.images.ImageViewModalView;
import com.triplify.ui.pages.images.view.ImageCardView;
import com.triplify.ui.pages.stories.view.StoryCardView;
import com.triplify.ui.pages.trips.view.TripCardView;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.add_card.view.AddCardView;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.DisplayUtils;
import static com.triplify.ui.shared.util.DisplayUtils.toLocalDate;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class RouteDetailsController extends SimpleLifecycleAwareController {

    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final int COUNTRY_EMOJI_SIZE = 18;
    private static final int SCAN_PAGE_SIZE = 64;
    private static final int MAX_MATCHED_TRIP_ROUTES = 24;
    private static final int ASSOCIATED_TRIPS_LIMIT = 8;
    private static final int STORIES_PER_ROUTE_LIMIT = 4;
    private static final int ASSOCIATED_STORIES_LIMIT = 8;

    @FXML private VBox contentContainer;
    @FXML private StackPane heroContainer;
    @FXML private FlowPane topRowFlow;
    @FXML private Button backButton;
    @FXML private ImageView heroImageView;
    @FXML private Label routeTitleLabel;
    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionValueLabel;
    @FXML private Label overviewTitleLabel;
    @FXML private InteractiveMap routeMap;
    @FXML private Label distanceValueLabel;
    @FXML private Label placesValueLabel;
    @FXML private DetailActionButtonsView actionButtonsView;
    @FXML private SectionHeaderView placesHeader;
    @FXML private VBox placesListContainer;
    @FXML private SectionHeaderView associatedTripsHeader;
    @FXML private CardGridPane<TripResponse> associatedTripsGrid;
    @FXML private SectionHeaderView associatedStoriesHeader;
    @FXML private CardGridPane<StoryResponse> associatedStoriesGrid;

    @FXML private VBox contextContainerCont;
    @FXML private SectionHeaderView contextHeader;
    @FXML private VBox contextContainer;
    @FXML private VBox statusContainer;
    @FXML private SectionHeaderView statusHeader;
    @FXML private SelectView<StatusEnum> tripRouteStatusSelect;
    @FXML private VBox tripRouteAssociatedItemsContainer;
    @FXML private VBox routeAssociatedItemsContainer;
    @FXML private SectionHeaderView tripImagesHeader;
    @FXML private CardGridPane<ImageResponse> tripImagesGrid;
    @FXML private SectionHeaderView tripStoriesHeader;
    @FXML private CardGridPane<StoryResponse> tripStoriesGrid;

    @Inject private RouteService routeService;
    @Inject private TripService tripService;
    @Inject private TripRouteService tripRouteService;
    @Inject private StoryService storyService;
    @Inject private ImageService imageService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private String routeId;
    private String tripRouteId;
    private RouteResponse currentRoute;
    private TripRouteResponse currentTripRoute;
    private Select<StatusEnum> statusSelectModel;
    private ImageFormModalView imageFormModal;
    private ImageViewModalView imageViewModal;

    private final List<PlaceResponse> currentPlaces = new ArrayList<>();
    private List<TripResponse> currentTrips = List.of();
    private List<StoryResponse> currentStories = List.of();
    private PlaceResponse draggedPlace;
    private VBox draggedHandle;
    private HBox draggedRow;
    private Popup dragPreviewPopup;
    private ImageView dragPreviewImageView;
    private boolean placeOrderChanged;

    @FXML
    public void initialize() {
        FontIcon icon = new FontIcon("fth-chevron-left");
        icon.setIconSize(16);
        icon.getStyleClass().add("place-details-back-icon");
        backButton.setGraphic(icon);
        Localization.bindText(backButton.textProperty(), "route.details.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "route.details.description");
        Localization.bindText(overviewTitleLabel.textProperty(), "route.details.overview");
        Localization.bindText(associatedTripsHeader.titleProperty(), "route.details.section.trips");
        Localization.bindText(associatedStoriesHeader.titleProperty(), "route.details.section.stories");
        Localization.bindText(placesHeader.titleProperty(), "route.details.section.places");

        topRowFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());

        EditorUtils.installRoundedClip(heroContainer, 28);
        EditorUtils.initializeCoverPreview(heroImageView, heroContainer);
        EditorUtils.installRoundedClip(routeMap, 20);
        routeMap.setSelectionEnabled(false);
        routeMap.setControlsVisible(false);

        actionButtonsView.configurePrimary(fxmlLoader, Localization.textBinding("route.details.action.edit"), "fth-edit-3", this::onEditRoute);
        actionButtonsView.configureDelete(
                fxmlLoader,
                Localization.textBinding("route.details.action.delete"),
                "fth-trash-2",
                Localization.textBinding("route.details.action.delete.confirm"),
                this::onDeleteRoute
        );

        configureAssociatedTripsGrid();
        configureAssociatedStoriesGrid();
        initializeTripContext();
        initializeDragPreview();
        contentContainer.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> finishDragging());
        contentContainer.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::updateDragPreview);

        I18n.bundleProperty().addListener((obs, oldBundle, newBundle) -> localize());
    }

    private void initializeTripContext() {
        contextContainerCont.setVisible(false);
        contextContainerCont.setManaged(false);
        statusContainer.setVisible(false);
        statusContainer.setManaged(false);
        tripRouteAssociatedItemsContainer.setVisible(false);
        tripRouteAssociatedItemsContainer.setManaged(false);

        Localization.bindText(contextHeader.titleProperty(), "story.details.section.context");
        Localization.bindText(statusHeader.titleProperty(), "filter.status");
        Localization.bindText(tripImagesHeader.titleProperty(), "triproute.context.images.header");
        Localization.bindText(tripStoriesHeader.titleProperty(), "triproute.context.stories.header");

        statusSelectModel = Select.<StatusEnum>builder()
                .placeholder(I18n.t("triproute.context.status.placeholder"))
                .items(javafx.collections.FXCollections.observableArrayList(buildStatusEntries()))
                .variant(FieldVariant.GHOST)
                .size(AppComponentSize.MIDDLE)
                .onSelect(e -> onTripRouteStatusSelected(e.getValue()))
                .build();
        tripRouteStatusSelect.update(statusSelectModel);

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
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        routeId = data == null ? null : data.getValue("routeId");
        tripRouteId = data == null ? null : data.getValue("tripRouteId");
    }

    @Override
    public void onLifecycleShow() {
        loadRouteDetails();
    }

    @FXML
    private void onBack() {
        getRouter().popBackStack();
    }

    @FXML
    private void onEditRoute() {
        if (routeId == null || routeId.isBlank()) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", UUID.randomUUID().toString());
        args.addArgument("tripName", currentRoute == null || currentRoute.title() == null ? "" : currentRoute.title());
        args.addArgument("routeId", routeId);
        getRouter().moveto(RouteIds.ADD_ROUTE, args);
    }

    @FXML
    private void onDeleteRoute() {
        if (routeId == null || routeId.isBlank()) {
            return;
        }

        var result = routeService.deleteRoute(new DeleteRouteRequest(UUID.fromString(routeId)));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            return;
        }

        toast.success(I18n.t("route.details.toast.deleted.title"), I18n.t("route.details.toast.deleted.body"));
        getRouter().popBackStack();
    }

    private void loadRouteDetails() {
        if (routeId == null || routeId.isBlank()) {
            toast.warning(I18n.t("route.details.toast.notFound"));
            getRouter().popBackStack();
            return;
        }

        UUID routeUuid = UUID.fromString(routeId);
        var routeResult = routeService.getRouteById(new GetRouteByIdRequest(routeUuid));
        if (routeResult.isFailure()) {
            errorHandler.handle(routeResult.getError());
            getRouter().popBackStack();
            return;
        }

        currentRoute = routeResult.getValue();
        currentPlaces.clear();
        if (currentRoute.places() != null) {
            currentPlaces.addAll(currentRoute.places());
        }

        if (tripRouteId != null && !tripRouteId.isBlank()) {
            loadTripContext();
        } else {
            List<TripRouteResponse> matchedTripRoutes = loadMatchedTripRoutes(routeUuid);
            currentTrips = loadAssociatedTrips(matchedTripRoutes);
            currentStories = loadAssociatedStories(matchedTripRoutes);
        }

        bind();
    }

    private void loadTripContext() {
        routeAssociatedItemsContainer.setVisible(false);
        routeAssociatedItemsContainer.setManaged(false);

        tripRouteAssociatedItemsContainer.setVisible(true);
        tripRouteAssociatedItemsContainer.setManaged(true);
        statusContainer.setVisible(true);
        statusContainer.setManaged(true);
        contextContainerCont.setVisible(true);
        contextContainerCont.setManaged(true);

        var result = tripRouteService.getTripRouteById(
                new GetTripRouteByIdRequest(UUID.fromString(tripRouteId)));
        if (result.isFailure()) {
            return;
        }

        currentTripRoute = result.getValue();

        Entry<StatusEnum> selectedEntry = buildStatusEntries().stream()
                .filter(e -> e.getValue() == currentTripRoute.status())
                .findFirst()
                .orElse(null);
        statusSelectModel.setSelectedItem(selectedEntry);
        tripRouteStatusSelect.update(statusSelectModel);

        UUID tripRouteUuid = UUID.fromString(tripRouteId);

        tripImagesGrid.setPageLoader((page, size) -> {
            var r = imageService.getImages(new GetImagesRequest(
                    new PageRequest(page - 1, size),
                    new GetImagesRequest.Filter(tripRouteUuid.toString(), ImageOwnerType.TRIP_ROUTE, null, null),
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
                    new GetStoriesRequest.Filter(null, tripRouteUuid, null, null, null, null),
                    new GetStoriesRequest.OrderBy(false)));
            if (r.isFailure()) return new CardGridPane.PageResult<>(List.of(), null);
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(),
                    new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        tripStoriesGrid.refresh();

        DisplayUtils.renderTripRouteContext(getRouter(), contextContainer, currentTripRoute);
    }

    private void onTripRouteStatusSelected(StatusEnum status) {
        if (currentTripRoute == null || status == null) return;
        if (currentTripRoute.status() == status) return;
        var result = tripRouteService.updateStatus(
                new UpdateTripRouteStatusRequest(currentTripRoute.id(), status, null, null));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
        } else {
            currentTripRoute = result.getValue();
            toast.success(I18n.t("triproute.context.status.updated"));
        }
    }

    private void openAddImageModal() {
        if (currentTripRoute == null) return;
        imageFormModal.show(
                contentContainer.getScene().getWindow(),
                currentTripRoute.id(),
                ImageOwnerType.TRIP_ROUTE,
                currentTripRoute.tripId(),
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
        if (currentTripRoute == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", currentTripRoute.tripId().toString());
        args.addArgument("tripRouteId", currentTripRoute.id().toString());
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

    private List<TripRouteResponse> loadMatchedTripRoutes(UUID routeUuid) {
        List<TripRouteResponse> matches = new ArrayList<>();
        int page = 0;

        while (matches.size() < MAX_MATCHED_TRIP_ROUTES) {
            var result = tripRouteService.getTripRoutes(new GetTripRoutesRequest(new PageRequest(page, SCAN_PAGE_SIZE), null));
            if (result.isFailure()) {
                return List.of();
            }

            var pageResult = result.getValue();
            for (TripRouteResponse tripRoute : pageResult.items()) {
                if (tripRoute.route() != null && routeUuid.equals(tripRoute.route().id())) {
                    matches.add(tripRoute);
                    if (matches.size() >= MAX_MATCHED_TRIP_ROUTES) {
                        break;
                    }
                }
            }

            if (!pageResult.hasNext()) {
                break;
            }
            page++;
        }

        return matches;
    }

    private List<TripResponse> loadAssociatedTrips(List<TripRouteResponse> matchedTripRoutes) {
        LinkedHashMap<UUID, TripResponse> trips = new LinkedHashMap<>();
        for (TripRouteResponse tripRoute : matchedTripRoutes) {
            if (tripRoute.tripId() == null || trips.containsKey(tripRoute.tripId())) {
                continue;
            }

            var result = tripService.getTripById(new GetTripByIdRequest(tripRoute.tripId()));
            if (result.isFailure()) {
                continue;
            }

            trips.put(tripRoute.tripId(), result.getValue());
            if (trips.size() >= ASSOCIATED_TRIPS_LIMIT) {
                break;
            }
        }
        return new ArrayList<>(trips.values());
    }

    private List<StoryResponse> loadAssociatedStories(List<TripRouteResponse> matchedTripRoutes) {
        LinkedHashMap<UUID, StoryResponse> storiesById = new LinkedHashMap<>();

        for (TripRouteResponse tripRoute : matchedTripRoutes) {
            var filter = new GetStoriesRequest.Filter(
                    null,
                    tripRoute.id(),
                    null,
                    null,
                    null,
                    null
            );
            var orderBy = new GetStoriesRequest.OrderBy(false);
            var request = new GetStoriesRequest(new PageRequest(0, STORIES_PER_ROUTE_LIMIT), filter, orderBy);
            var result = storyService.getStories(request);
            if (result.isFailure()) {
                continue;
            }

            for (StoryResponse story : result.getValue().items()) {
                storiesById.putIfAbsent(story.id(), story);
            }
        }

        return storiesById.values().stream()
                .sorted(Comparator.comparing(StoryResponse::storyTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(ASSOCIATED_STORIES_LIMIT)
                .toList();
    }

    private void bind() {
        routeTitleLabel.setText(currentRoute.title());
        if (currentRoute.coverImage() != null) {
            String coverUrl = DisplayUtils.deriveCoverUrl(currentRoute.coverImage());
            Image image = EditorUtils.loadImage(coverUrl, DEFAULT_IMAGE, RouteDetailsController.class);
            EditorUtils.setCoverPreviewImage(heroImageView, heroContainer, image);
        }

        renderMap(currentRoute);
        renderPlaces(currentPlaces);
        localize();

        if (tripRouteId == null || tripRouteId.isBlank()) {
            associatedTripsGrid.refresh();
            associatedStoriesGrid.refresh();
        }
    }

    private void localize(){
        descriptionValueLabel.setText(EditorUtils.safeText(currentRoute.description(), I18n.t("route.details.empty.description")));
        distanceValueLabel.setText(String.format(Locale.US, I18n.t("route.details.metric.distance"), currentRoute.length()));
        placesValueLabel.setText(formatPlacesCount(currentPlaces.size()));
    }

    private void configureAssociatedTripsGrid() {
        associatedTripsGrid.setMinCardWidth(240);
        associatedTripsGrid.setMaxColumns(4);
        associatedTripsGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        associatedTripsGrid.setEmptyTextKey("route.details.empty.trips");
        associatedTripsGrid.setCardFactory(trip -> {
            String dateRange = DisplayUtils.formatDateRange(toLocalDate(trip.startedAt()), toLocalDate(trip.endedAt()));
            return TripCardView.create(trip, dateRange, () -> openTrip(trip)).getRoot();
        });
        associatedTripsGrid.setPageLoader((page, size) -> new CardGridPane.PageResult<>(currentTrips, null));
    }

    private void configureAssociatedStoriesGrid() {
        associatedStoriesGrid.setMinCardWidth(257);
        associatedStoriesGrid.setMaxColumns(4);
        associatedStoriesGrid.setVScrollPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        associatedStoriesGrid.setEmptyTextKey("route.details.empty.stories");
        associatedStoriesGrid.setCardFactory(story -> StoryCardView.create(story, () -> openStory(story)).getRoot());
        associatedStoriesGrid.setPageLoader((page, size) -> new CardGridPane.PageResult<>(currentStories, null));
    }

    private void renderMap(RouteResponse route) {
        List<PlaceResponse> places = route.places() == null ? List.of() : route.places().stream().toList();

        if (places.isEmpty()) {
            routeMap.setMapCenter(48.1486, 17.1077);
        } else {
            double latitudeAverage = places.stream().mapToDouble(PlaceResponse::latitude).average().orElse(places.getFirst().latitude());
            double longitudeAverage = places.stream().mapToDouble(PlaceResponse::longitude).average().orElse(places.getFirst().longitude());
            routeMap.setMapCenter(latitudeAverage, longitudeAverage);
        }

        renderMapWaypoints(currentPlaces);
    }

    private void renderMapWaypoints(List<PlaceResponse> places) {
        routeMap.clearMarkers();
        routeMap.clearPolyline();
        if (places.isEmpty()) {
            return;
        }

        for (int i = 0; i < places.size(); i++) {
            PlaceResponse place = places.get(i);
            routeMap.addMarker(new MapPoint(place.latitude(), place.longitude()), i + 1);
        }

        if (places.size() >= 2) {
            List<MapPoint> waypoints = places.stream()
                    .map(p -> new MapPoint(p.latitude(), p.longitude()))
                    .toList();
            routeMap.setPolyline(waypoints);
        }
    }

    private void renderPlaces(List<PlaceResponse> places) {
        placesListContainer.getChildren().clear();
        for (int index = 0; index < places.size(); index++) {
            PlaceResponse place = places.get(index);
            placesListContainer.getChildren().add(buildPlaceRow(place, index + 1));
        }
    }

    private HBox buildPlaceRow(PlaceResponse place, int index) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("route-details-place-row");
        if (place.equals(draggedPlace)) {
            row.getStyleClass().add("add-route-place-row-dragging");
        }

        Label orderLabel = new Label(String.valueOf(index));
        orderLabel.getStyleClass().add("route-details-place-index");

        String path = DisplayUtils.deriveCoverUrl(place.coverImage());
        ImageView preview = new ImageView(EditorUtils.loadImage(path, "", getClass()));
        preview.setFitWidth(92);
        preview.setFitHeight(58);
        preview.setPreserveRatio(false);
        preview.getStyleClass().add("route-details-place-thumb");
        Rectangle clip = new Rectangle(92, 58);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        preview.setClip(clip);

        VBox copy = new VBox(4);
        Label title = new Label(place.title());
        title.getStyleClass().add("route-details-place-title");
        title.setWrapText(true);

        Label subtitle = new Label();
        Localization.bindLocalizedText(subtitle.textProperty(), place.country());
        subtitle.getStyleClass().add("route-details-place-subtitle");
        subtitle.setWrapText(true);
        copy.getChildren().addAll(title, subtitle);
        HBox.setHgrow(copy, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Region handle = createHandle(place, row);
        actions.getChildren().add(handle);

        row.setOnMouseDragEntered(event -> reorderPlaces(place));
        row.setOnMouseReleased(event -> finishDragging());
        row.setOnMouseClicked(event -> {
            if (isClickOnHandle(event.getTarget())) {
                return;
            }
            openPlace(place);
        });
        if (place.coverImage()==null) {
            row.getChildren().addAll(orderLabel, copy, actions);
        }
        else {
            row.getChildren().addAll(orderLabel, preview, copy, actions);
        }
        return row;
    }

    private Region createHandle(PlaceResponse place, HBox row) {
        VBox handle = new VBox(3);
        handle.setAlignment(Pos.CENTER);
        handle.getStyleClass().add("add-route-handle");
        handle.setCursor(Cursor.OPEN_HAND);

        for (int index = 0; index < 3; index++) {
            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(1.8);
            dot.getStyleClass().add("add-route-handle-dot");
            handle.getChildren().add(dot);
        }

        handle.setOnMousePressed(event -> handle.setCursor(Cursor.CLOSED_HAND));
        handle.setOnDragDetected(event -> beginDragging(place, handle, row, event));
        handle.setOnMouseReleased(event -> {
            handle.setCursor(Cursor.OPEN_HAND);
            handle.getStyleClass().remove("add-route-handle-dragging");
            finishDragging();
        });
        return handle;
    }

    private void beginDragging(PlaceResponse place, VBox handle, HBox row, MouseEvent event) {
        draggedPlace = place;
        draggedHandle = handle;
        draggedRow = row;
        placeOrderChanged = false;
        handle.setCursor(Cursor.CLOSED_HAND);
        handle.getStyleClass().add("add-route-handle-dragging");
        if (!row.getStyleClass().contains("add-route-place-row-dragging")) {
            row.getStyleClass().add("add-route-place-row-dragging");
        }
        showDragPreview(row, event);
        handle.startFullDrag();
        event.consume();
    }

    private void reorderPlaces(PlaceResponse targetPlace) {
        if (draggedPlace == null || draggedPlace.equals(targetPlace)) {
            return;
        }

        int draggedIndex = currentPlaces.indexOf(draggedPlace);
        int targetIndex = currentPlaces.indexOf(targetPlace);
        if (draggedIndex < 0 || targetIndex < 0 || draggedIndex == targetIndex) {
            return;
        }

        Collections.swap(currentPlaces, draggedIndex, targetIndex);
        placeOrderChanged = true;
        renderPlaces(currentPlaces);
        renderMapWaypoints(currentPlaces);
        distanceValueLabel.setText(String.format(Locale.US, I18n.t("route.details.metric.distance"), calculateRouteLength(currentPlaces)));
    }

    private void finishDragging() {
        if (draggedPlace == null) {
            return;
        }

        if (draggedHandle != null) {
            draggedHandle.setCursor(Cursor.OPEN_HAND);
            draggedHandle.getStyleClass().remove("add-route-handle-dragging");
            draggedHandle = null;
        }
        if (draggedRow != null) {
            draggedRow.getStyleClass().remove("add-route-place-row-dragging");
            draggedRow = null;
        }

        PlaceResponse previousDraggedPlace = draggedPlace;
        boolean shouldPersist = placeOrderChanged;
        hideDragPreview();
        draggedPlace = null;
        placeOrderChanged = false;
        renderPlaces(currentPlaces);

        if (shouldPersist) {
            persistPlaceOrder(previousDraggedPlace);
        }
    }

    private void persistPlaceOrder(PlaceResponse focusPlace) {
        if (routeId == null || routeId.isBlank()) {
            return;
        }

        var result = routeService.rearrangePlacesInRoute(
                new com.triplify.application.usecase.route.dto.RearrangePlacesInRouteRequest(
                        UUID.fromString(routeId),
                        currentPlaces.stream().map(PlaceResponse::id).toList()
                )
        );
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            loadRouteDetails();
            return;
        }

        currentRoute = result.getValue();
        currentPlaces.clear();
        if (currentRoute.places() != null) {
            currentPlaces.addAll(currentRoute.places());
        }
        renderPlaces(currentPlaces);
        renderMapWaypoints(currentPlaces);
        placesValueLabel.setText(formatPlacesCount(currentPlaces.size()));
        distanceValueLabel.setText(String.format(Locale.US, I18n.t("route.details.metric.distance"), currentRoute.length()));
    }

    private void initializeDragPreview() {
        dragPreviewImageView = new ImageView();
        dragPreviewImageView.setMouseTransparent(true);
        dragPreviewImageView.getStyleClass().add("add-route-drag-preview");

        StackPane previewRoot = new StackPane(dragPreviewImageView);
        previewRoot.setMouseTransparent(true);
        previewRoot.getStyleClass().add("add-route-drag-preview-shell");

        dragPreviewPopup = new Popup();
        dragPreviewPopup.setAutoHide(false);
        dragPreviewPopup.setAutoFix(false);
        dragPreviewPopup.setHideOnEscape(false);
        dragPreviewPopup.getContent().add(previewRoot);
    }

    private void showDragPreview(HBox row, MouseEvent event) {
        if (contentContainer.getScene() == null || contentContainer.getScene().getWindow() == null) {
            return;
        }

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage snapshot = row.snapshot(parameters, null);
        dragPreviewImageView.setImage(snapshot);

        if (!dragPreviewPopup.isShowing()) {
            dragPreviewPopup.show(
                    contentContainer.getScene().getWindow(),
                    event.getScreenX() - snapshot.getWidth() - 18,
                    event.getScreenY() - 12
            );
        }
        updateDragPreviewPosition(event);
    }

    private void updateDragPreview(MouseEvent event) {
        if (draggedPlace == null) {
            return;
        }
        updateDragPreviewPosition(event);
    }

    private void updateDragPreviewPosition(MouseEvent event) {
        if (dragPreviewPopup == null || !dragPreviewPopup.isShowing()) {
            return;
        }

        double previewWidth = dragPreviewImageView.getImage() == null ? 0 : dragPreviewImageView.getImage().getWidth();
        dragPreviewPopup.setX(event.getScreenX() - previewWidth - 18);
        dragPreviewPopup.setY(event.getScreenY() - 12);
    }

    private void hideDragPreview() {
        if (dragPreviewPopup != null) {
            dragPreviewPopup.hide();
        }
        if (dragPreviewImageView != null) {
            dragPreviewImageView.setImage(null);
        }
    }

    private void openTrip(TripResponse trip) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", trip.id().toString());
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }

    private void openStory(StoryResponse story) {
        if (story == null || story.id() == null) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("storyId", story.id().toString());
        getRouter().moveto(RouteIds.STORY_DETAILS, args);
    }

    private void openPlace(PlaceResponse place) {
        if (place == null || place.id() == null) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("placeId", place.id().toString());
        getRouter().moveto(RouteIds.PLACE_DETAILS, args);
    }

    private boolean isClickOnHandle(Object target) {
        Node current = target instanceof Node node ? node : null;
        while (current != null) {
            if (current.getStyleClass().contains("add-route-handle")) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private String formatPlacesCount(int count) {
        String key = count == 1
                ? "route.details.metric.places.one"
                : "route.details.metric.places.other";
        return String.format(Locale.US, I18n.t(key), count);
    }

    private double calculateRouteLength(List<PlaceResponse> places) {
        double total = 0;
        for (int i = 1; i < places.size(); i++) {
            PlaceResponse from = places.get(i - 1);
            PlaceResponse to = places.get(i);
            total += GeoCalculator.distanceKm(from.latitude(), from.longitude(), to.latitude(), to.longitude());
        }
        return total;
    }
}
