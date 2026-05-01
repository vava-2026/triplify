package com.triplify.ui.pages.routes;

import com.google.inject.Inject;
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
import com.triplify.application.usecase.triproute.dto.GetTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.map.InteractiveMap;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.shared.component.empty_state.view.EmptyStateCardView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.DisplayUtils;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;
import com.triplify.ui.pages.trips.view.TripCardView;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

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
    @FXML private HBox countryRow;
    @FXML private ImageView countryEmojiView;
    @FXML private Label countryLabel;
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
    @FXML private FlowPane associatedTripsFlow;
    @FXML private SectionHeaderView associatedStoriesHeader;
    @FXML private FlowPane associatedStoriesFlow;

    @Inject private RouteService routeService;
    @Inject private TripService tripService;
    @Inject private TripRouteService tripRouteService;
    @Inject private StoryService storyService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private String routeId;
    private RouteResponse currentRoute;
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
        configureButtonIcon(backButton, "fth-chevron-left", "place-details-back-icon");
        Localization.bindText(backButton.textProperty(), "route.details.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "route.details.description");
        Localization.bindText(overviewTitleLabel.textProperty(), "route.details.overview");
        Localization.bindText(associatedTripsHeader.titleProperty(), "route.details.section.trips");
        Localization.bindText(associatedStoriesHeader.titleProperty(), "route.details.section.stories");
        Localization.bindText(placesHeader.titleProperty(), "route.details.section.places");

        topRowFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        associatedTripsFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        associatedStoriesFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());

        EditorUtils.installRoundedClip(heroContainer, 28);
        EditorUtils.initializeCoverPreview(heroImageView, heroContainer);
        installRoundedPaneClip(routeMap, 20);
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
        initializeDragPreview();
        contentContainer.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> finishDragging());
        contentContainer.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::updateDragPreview);

        I18n.bundleProperty().addListener((obs, oldBundle, newBundle) -> refreshLocalizedContent());
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        routeId = data == null ? null : data.getValue("routeId");
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
        List<TripRouteResponse> matchedTripRoutes = loadMatchedTripRoutes(routeUuid);
        currentTrips = loadAssociatedTrips(matchedTripRoutes);
        currentStories = loadAssociatedStories(matchedTripRoutes);
        currentPlaces.clear();
        if (currentRoute.places() != null) {
            currentPlaces.addAll(currentRoute.places());
        }
        bindCurrentState();
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

    private void refreshLocalizedContent() {
        if (currentRoute != null) {
            bindCurrentState();
        }
    }

    private void bindCurrentState() {
        routeTitleLabel.setText(safeText(currentRoute.title(), I18n.t("trip.add.fallback.route")));
        bindRouteCountry(currentRoute);
        EditorUtils.setCoverPreviewImage(
                heroImageView,
                heroContainer,
                EditorUtils.loadImage(routeImagePath(currentRoute), DEFAULT_IMAGE, getClass())
        );
        descriptionValueLabel.setText(safeText(currentRoute.description(), I18n.t("route.details.empty.description")));
        distanceValueLabel.setText(String.format(Locale.US, I18n.t("route.details.metric.distance"), currentRoute.length()));
        placesValueLabel.setText(formatPlacesCount(currentPlaces.size()));

        renderMap(currentRoute);
        renderPlaces(currentPlaces);
        renderAssociatedTrips(currentTrips);
        renderAssociatedStories(currentStories);
    }

    private void renderMap(RouteResponse route) {
        List<PlaceResponse> places = route.places() == null ? List.of() : route.places().stream().toList();

        if (places.isEmpty()) {
            routeMap.setMapCenter(48.1486, 17.1077);
            routeMap.setPinPosition(48.1486, 17.1077);
            return;
        }

        double latitudeAverage = places.stream().mapToDouble(PlaceResponse::latitude).average().orElse(places.getFirst().latitude());
        double longitudeAverage = places.stream().mapToDouble(PlaceResponse::longitude).average().orElse(places.getFirst().longitude());
        routeMap.setMapCenter(latitudeAverage, longitudeAverage);
        routeMap.setPinPosition(places.getFirst().latitude(), places.getFirst().longitude());
    }

    private void renderPlaces(List<PlaceResponse> places) {
        placesListContainer.getChildren().clear();
        if (places.isEmpty()) {
            placesListContainer.getChildren().add(createEmptyState(I18n.t("route.details.empty.places"), placesListContainer));
            return;
        }

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

        ImageView preview = new ImageView(EditorUtils.loadImage(placeImagePath(place), DEFAULT_IMAGE, getClass()));
        preview.setFitWidth(92);
        preview.setFitHeight(58);
        preview.setPreserveRatio(false);
        preview.getStyleClass().add("route-details-place-thumb");
        Rectangle clip = new Rectangle(92, 58);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        preview.setClip(clip);

        VBox copy = new VBox(4);
        Label title = new Label(safeText(place.title(), I18n.t("route.details.place.fallback")));
        title.getStyleClass().add("route-details-place-title");
        title.setWrapText(true);

        String subtitleText = place.country() == null
                ? safeText(place.description(), "")
                : Localization.localize(place.country());
        Label subtitle = new Label(safeText(subtitleText, ""));
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
        row.getChildren().addAll(orderLabel, preview, copy, actions);
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
        placesValueLabel.setText(formatPlacesCount(currentPlaces.size()));
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

    private void renderAssociatedTrips(List<TripResponse> trips) {
        associatedTripsFlow.getChildren().clear();
        if (trips.isEmpty()) {
            associatedTripsFlow.getChildren().add(createEmptyState(I18n.t("route.details.empty.trips"), associatedTripsFlow));
            return;
        }

        for (TripResponse trip : trips) {
            String dateRange = formatTripDateRange(trip.startedAt(), trip.endedAt());
            TripCardView card = TripCardView.create(trip, dateRange, () -> openTrip(trip));
            associatedTripsFlow.getChildren().add((Region) card.getRoot());
        }
    }

    private void renderAssociatedStories(List<StoryResponse> stories) {
        associatedStoriesFlow.getChildren().clear();
        if (stories.isEmpty()) {
            associatedStoriesFlow.getChildren().add(createEmptyState(I18n.t("route.details.empty.stories"), associatedStoriesFlow));
            return;
        }

        for (StoryResponse story : stories) {
            associatedStoriesFlow.getChildren().add(buildStoryCard(story));
        }
    }

    private VBox buildStoryCard(StoryResponse story) {
        VBox card = new VBox(10);
        card.getStyleClass().add("route-details-story-card");

        ImageView preview = new ImageView(EditorUtils.loadImage(storyImagePath(story), DEFAULT_IMAGE, getClass()));
        preview.setFitWidth(257);
        preview.setFitHeight(146);
        preview.setPreserveRatio(false);
        preview.getStyleClass().add("route-details-story-image");
        Rectangle clip = new Rectangle(257, 146);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        preview.setClip(clip);

        VBox body = new VBox(8);
        body.getStyleClass().add("route-details-story-body");

        Label title = new Label(safeText(story.title(), I18n.t("route.details.story.fallback")));
        title.getStyleClass().add("route-details-story-title");
        title.setWrapText(true);

        Label meta = new Label(formatStoryDate(story.storyTime()));
        meta.getStyleClass().add("route-details-story-meta");

        body.getChildren().addAll(title, meta);

        if (story.emotion() != null) {
            Label emotion = new Label(buildEmotionLabel(story));
            emotion.getStyleClass().add("route-details-story-emotion");
            body.getChildren().add(emotion);
        }

        card.getChildren().addAll(preview, body);
        return card;
    }

    private EmptyStateCardView createEmptyState(String text, Region parent) {
        EmptyStateCardView card = new EmptyStateCardView();
        card.setText(text);
        card.prefWidthProperty().bind(parent.widthProperty());
        card.maxWidthProperty().bind(parent.widthProperty());
        return card;
    }

    private void openTrip(TripResponse trip) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", trip.id().toString());
        args.addArgument("tripStatus", trip.status());
        getRouter().moveto(RouteIds.ADD_TRIP, args);
    }

    private void openPlace(PlaceResponse place) {
        if (place == null || place.id() == null) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("placeId", place.id().toString());
        getRouter().moveto(RouteIds.PLACE_DETAILS, args);
    }

    private String routeImagePath(RouteResponse route) {
        String coverUrl = DisplayUtils.deriveCoverUrl(route.coverImage());
        return coverUrl == null || coverUrl.isBlank() ? DEFAULT_IMAGE : coverUrl;
    }

    private String placeImagePath(PlaceResponse place) {
        return place.coverImage() == null || place.coverImage().url() == null
                ? DEFAULT_IMAGE
                : place.coverImage().url().toString();
    }

    private String storyImagePath(StoryResponse story) {
        if (story.images() == null || story.images().isEmpty()) {
            return DEFAULT_IMAGE;
        }

        return story.images().stream()
                .findFirst()
                .filter(image -> image.url() != null)
                .map(image -> image.url().toString())
                .orElse(DEFAULT_IMAGE);
    }

    private String buildEmotionLabel(StoryResponse story) {
        String emoji = story.emotion().emojiUnicode() == null ? "" : story.emotion().emojiUnicode() + " ";
        return emoji + Localization.localize(story.emotion());
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

    private String formatTripDateRange(Instant startedAt, Instant endedAt) {
        LocalDate start = startedAt == null ? null : startedAt.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate end = endedAt == null ? null : endedAt.atZone(ZoneOffset.UTC).toLocalDate();
        if (start == null && end == null) {
            return I18n.t("route.details.trip.dates.tba");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(I18n.getLanguage().getLocale());

        if (start != null && end != null) {
            return start.format(formatter) + " - " + end.format(formatter);
        }

        return (start == null ? end : start).format(formatter);
    }

    private String formatStoryDate(Instant storyTime) {
        if (storyTime == null) {
            return I18n.t("route.details.story.date.unknown");
        }

        return storyTime.atZone(ZoneOffset.UTC)
                .toLocalDate()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(I18n.getLanguage().getLocale()));
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void bindRouteCountry(RouteResponse route) {
        if (route == null || route.places() == null || route.places().isEmpty()) {
            countryRow.setVisible(true);
            countryRow.setManaged(true);
            countryLabel.textProperty().unbind();
            countryLabel.setText(formatPlacesCount(0));
            countryEmojiView.setVisible(false);
            countryEmojiView.setManaged(false);
            countryEmojiView.setImage(null);
            return;
        }

        String firstEmoji = null;
        Set<String> countries = new LinkedHashSet<>();
        for (PlaceResponse place : route.places()) {
            if (place != null && place.country() != null) {
                if (firstEmoji == null) {
                    firstEmoji = place.country().emojiUnicode();
                }
                String localizedCountry = Localization.localize(place.country());
                if (localizedCountry != null && !localizedCountry.isBlank()) {
                    countries.add(localizedCountry);
                }
            }
        }

        if (countries.isEmpty()) {
            countryRow.setVisible(true);
            countryRow.setManaged(true);
            countryLabel.textProperty().unbind();
            countryLabel.setText(formatPlacesCount(route.places().size()));
            countryEmojiView.setVisible(false);
            countryEmojiView.setManaged(false);
            countryEmojiView.setImage(null);
            return;
        }

        String firstCountry = countries.iterator().next();
        countryRow.setVisible(true);
        countryRow.setManaged(true);
        EditorUtils.applyEmojiImage(countryEmojiView, firstEmoji, COUNTRY_EMOJI_SIZE);
        countryLabel.textProperty().unbind();
        countryLabel.setText(countries.size() == 1 ? firstCountry : firstCountry + " +" + (countries.size() - 1));
    }

    private void configureButtonIcon(Button button, String iconLiteral, String styleClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
    }

    private void installRoundedPaneClip(StackPane target, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(target.widthProperty());
        clip.heightProperty().bind(target.heightProperty());
        target.setClip(clip);
    }
}
