package com.triplify.ui.pages.routes;

import com.google.inject.Inject;
import com.triplify.application.response.TripStatus;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.DeleteRouteRequest;
import com.triplify.application.usecase.route.dto.GetRouteByIdRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.story.StoryService;
import com.triplify.application.usecase.story.dto.GetStoriesRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.GetTripsRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.triproute.TripRouteService;
import com.triplify.application.usecase.triproute.dto.GetTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.result.Result;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.shared.component.empty_state.view.EmptyStateCardView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.trip.view.TripCardView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.Localization;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.io.File;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RouteDetailsController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(RouteDetailsController.class);
    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML private StackPane heroContainer;
    @FXML private Button backButton;
    @FXML private ImageView heroImageView;
    @FXML private Label routeTitleLabel;
    @FXML private Label routeMetaLabel;
    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionValueLabel;
    @FXML private Canvas routePreviewCanvas;
    @FXML private Label summaryDistanceValueLabel;
    @FXML private Label summaryDistanceCaptionLabel;
    @FXML private Label summaryPlacesValueLabel;
    @FXML private Label summaryPlacesCaptionLabel;
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

    private String routeId;
    private RouteResponse currentRoute;

    @FXML
    public void initialize() {
        installRoundedImageClip(heroImageView, 28);
        installRoundedPaneClip(heroContainer, 28);
        configureButtonIcon(backButton, "fth-chevron-left", "route-details-back-icon");
        configureButtonIcon(actionButtonsView.getPrimaryButton(), "fth-edit-3", "app-btn-icon");
        configureButtonIcon(actionButtonsView.getSecondaryButton(), "fth-trash-2", "app-btn-icon");

        Localization.bindText(backButton.textProperty(), "button.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "route.details.section.description");
        Localization.bindText(summaryDistanceCaptionLabel.textProperty(), "route.details.summary.distance");
        Localization.bindText(summaryPlacesCaptionLabel.textProperty(), "route.details.summary.places");
        Localization.bindText(placesHeader.titleProperty(), "route.details.section.places");
        Localization.bindText(associatedTripsHeader.titleProperty(), "route.details.section.associatedTrips");
        Localization.bindText(associatedStoriesHeader.titleProperty(), "route.details.section.associatedStories");
        Localization.bindText(actionButtonsView.getPrimaryButton().textProperty(), "route.details.action.edit");
        Localization.bindText(actionButtonsView.getSecondaryButton().textProperty(), "route.details.action.delete");

        actionButtonsView.getPrimaryButton().setOnAction(event -> onEditRoute());
        actionButtonsView.getSecondaryButton().setOnAction(event -> onDeleteRoute());
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        routeId = data == null ? null : data.getValue("routeId");
        loadRouteDetails();
    }

    @Override
    public void onLifecycleShow() {
        if (routeId != null && !routeId.isBlank()) {
            loadRouteDetails();
        }
    }

    @FXML
    private void onBack() {
        getRouter().popBackStack();
    }

    private void onEditRoute() {
        if (currentRoute == null) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", "0");
        args.addArgument("tripName", safeText(currentRoute.title(), I18n.t("route.details.fallback.title")));
        args.addArgument("routeId", currentRoute.id());
        getRouter().moveto(RouteIds.ADD_ROUTE, args);
    }

    private void onDeleteRoute() {
        if (currentRoute == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText(I18n.t("route.details.delete.confirm"));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        var deleteResult = routeService.deleteRoute(new DeleteRouteRequest(currentRoute.id()));
        deleteResult.onSuccess(ignored -> {
            toast.success(
                    I18n.t("route.details.toast.deleted.title"),
                    formatMessage("route.details.toast.deleted.body", safeText(currentRoute.title(), I18n.t("route.details.fallback.title")))
            );
            getRouter().popBackStack();
        });
        deleteResult.onFailure(errorHandler::handle);
    }

    private void loadRouteDetails() {
        if (routeId == null || routeId.isBlank()) {
            toast.warning(I18n.t("route.details.error.loadFailed"));
            getRouter().popBackStack();
            return;
        }

        var result = routeService.getRouteById(new GetRouteByIdRequest(routeId));
        result.onSuccess(route -> {
            currentRoute = route;
            bind(route);
            loadAssociatedContent(route.id());
        });
        result.onFailure(error -> {
            errorHandler.handle(error);
            if (currentRoute == null) {
                getRouter().popBackStack();
            }
        });
    }

    private void bind(RouteResponse route) {
        heroImageView.setImage(loadImage(imagePath(route.coverImage())));
        routeTitleLabel.setText(safeText(route.title(), I18n.t("route.details.fallback.title")));
        routeMetaLabel.setText(formatMessage(
                "route.details.hero.meta",
                formatPlacesCount(route.places() == null ? 0 : route.places().size()),
                formatDistance(route.length())
        ));
        descriptionValueLabel.setText(safeText(route.description(), I18n.t("route.details.fallback.description")));
        summaryDistanceValueLabel.setText(formatDistance(route.length()));
        summaryPlacesValueLabel.setText(String.valueOf(route.places() == null ? 0 : route.places().size()));

        drawRoutePreview(route);
        renderPlaces(route.places() == null ? List.of() : List.copyOf(route.places()));
    }

    private void loadAssociatedContent(String targetRouteId) {
        List<AssociatedTripItem> associatedTrips = loadAssociatedTrips(targetRouteId);
        renderAssociatedTrips(associatedTrips.stream().map(AssociatedTripItem::trip).toList());

        Set<String> tripRouteIds = associatedTrips.stream()
                .map(AssociatedTripItem::tripRouteId)
                .filter(id -> id != null && !id.isBlank())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        renderAssociatedStories(loadAssociatedStories(tripRouteIds));
    }

    private void renderPlaces(List<com.triplify.application.usecase.place.dto.PlaceResponse> places) {
        placesListContainer.getChildren().clear();
        if (places.isEmpty()) {
            placesListContainer.getChildren().add(createEmptyState(I18n.t("route.details.empty.places")));
            return;
        }

        for (int index = 0; index < places.size(); index++) {
            placesListContainer.getChildren().add(buildPlaceRow(places.get(index), index + 1));
        }
    }

    private void renderAssociatedTrips(List<TripResponse> trips) {
        associatedTripsFlow.getChildren().clear();
        if (trips.isEmpty()) {
            associatedTripsFlow.getChildren().add(createEmptyState(I18n.t("route.details.empty.trips")));
            return;
        }

        for (TripResponse trip : trips) {
            String dateRange = formatDateRange(toLocalDate(trip.startedAt()), toLocalDate(trip.endedAt()));
            TripCardView card = TripCardView.create(toLegacyTrip(trip), dateRange, () -> openTrip(trip, dateRange));
            associatedTripsFlow.getChildren().add(card.getRoot());
        }
    }

    private void renderAssociatedStories(List<StoryResponse> stories) {
        associatedStoriesFlow.getChildren().clear();
        if (stories.isEmpty()) {
            associatedStoriesFlow.getChildren().add(createEmptyState(I18n.t("route.details.empty.stories")));
            return;
        }

        for (StoryResponse story : stories) {
            associatedStoriesFlow.getChildren().add(buildStoryCard(story));
        }
    }

    private Node buildPlaceRow(com.triplify.application.usecase.place.dto.PlaceResponse place, int index) {
        HBox row = new HBox(14);
        row.getStyleClass().add("route-details-place-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label orderLabel = new Label(String.valueOf(index));
        orderLabel.getStyleClass().add("route-details-place-index");

        ImageView preview = new ImageView(loadImage(imagePath(place.coverImage())));
        preview.setFitWidth(112);
        preview.setFitHeight(72);
        preview.setPreserveRatio(false);
        preview.getStyleClass().add("route-details-place-thumb");
        Rectangle clip = new Rectangle(112, 72);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        preview.setClip(clip);

        VBox copy = new VBox(4);
        Label title = new Label(safeText(place.title(), I18n.t("route.details.fallback.place")));
        title.getStyleClass().add("route-details-place-title");
        Label subtitle = new Label(place.country() != null && place.country().name() != null && !place.country().name().isBlank()
                ? place.country().name()
                : safeText(place.description(), I18n.t("route.details.fallback.placeSubtitle")));
        subtitle.getStyleClass().add("route-details-place-subtitle");
        copy.getChildren().addAll(title, subtitle);

        row.getChildren().addAll(orderLabel, preview, copy);
        return row;
    }

    private Node buildStoryCard(StoryResponse story) {
        VBox card = new VBox(10);
        card.getStyleClass().add("route-details-story-card");

        ImageView preview = new ImageView(loadImage(imagePath(story.images())));
        preview.setFitWidth(244);
        preview.setFitHeight(126);
        preview.setPreserveRatio(false);
        Rectangle clip = new Rectangle(244, 126);
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        preview.setClip(clip);

        Label title = new Label(safeText(story.title(), I18n.t("route.details.fallback.story")));
        title.getStyleClass().add("route-details-story-title");

        Label meta = new Label(formatStoryDate(story.storyTime()));
        meta.getStyleClass().add("route-details-story-meta");

        Label description = new Label(safeText(story.description(), I18n.t("route.details.fallback.storyDescription")));
        description.setWrapText(true);
        description.getStyleClass().add("route-details-story-description");

        card.getChildren().addAll(preview, title, meta, description);
        return card;
    }

    private EmptyStateCardView createEmptyState(String text) {
        EmptyStateCardView view = new EmptyStateCardView();
        view.setText(text);
        FlowPane.setMargin(view, new Insets(0));
        return view;
    }

    private List<AssociatedTripItem> loadAssociatedTrips(String targetRouteId) {
        List<AssociatedTripItem> items = new ArrayList<>();
        for (TripResponse trip : loadAllTrips()) {
            Result<List<TripRouteResponse>> tripRoutesResult = loadAllTripRoutes(trip.id());
            if (tripRoutesResult.isFailure()) {
                log.warn("Failed to load trip routes for trip '{}': {}", trip.id(), tripRoutesResult.getError().message());
                continue;
            }

            for (TripRouteResponse tripRoute : tripRoutesResult.getValue()) {
                if (tripRoute.route() == null || !targetRouteId.equals(tripRoute.route().id())) {
                    continue;
                }
                items.add(new AssociatedTripItem(trip, tripRoute.id()));
                break;
            }
        }
        return items;
    }

    private List<StoryResponse> loadAssociatedStories(Set<String> tripRouteIds) {
        if (tripRouteIds.isEmpty()) {
            return List.of();
        }

        Map<String, StoryResponse> storiesById = new LinkedHashMap<>();
        for (String tripRouteId : tripRouteIds) {
            PageRequest pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE);
            while (true) {
                var result = storyService.getStories(new GetStoriesRequest(
                        pageRequest,
                        new GetStoriesRequest.Filter(null, tripRouteId, null, null, null, null),
                        new GetStoriesRequest.OrderBy(false)
                ));
                if (result.isFailure()) {
                    log.warn("Failed to load stories for tripRoute '{}': {}", tripRouteId, result.getError().message());
                    break;
                }

                Page<StoryResponse> page = result.getValue();
                for (StoryResponse story : page.items()) {
                    storiesById.putIfAbsent(story.id(), story);
                }

                if (!page.hasNext()) {
                    break;
                }
                pageRequest = pageRequest.next();
            }
        }

        return storiesById.values().stream()
                .sorted(Comparator.comparing(StoryResponse::storyTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<TripResponse> loadAllTrips() {
        List<TripResponse> items = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE);

        while (true) {
            var result = tripService.getTrips(new GetTripsRequest(pageRequest, null, new GetTripsRequest.OrderBy(false)));
            if (result.isFailure()) {
                log.warn("Failed to load trips for route details: {}", result.getError().message());
                return items;
            }

            Page<TripResponse> page = result.getValue();
            items.addAll(page.items());
            if (!page.hasNext()) {
                return items;
            }
            pageRequest = pageRequest.next();
        }
    }

    private Result<List<TripRouteResponse>> loadAllTripRoutes(String tripId) {
        List<TripRouteResponse> items = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE);

        while (true) {
            var result = tripRouteService.getTripRoutes(new GetTripRoutesRequest(
                    pageRequest,
                    new GetTripRoutesRequest.Filter(tripId, null)
            ));
            if (result.isFailure()) {
                return Result.fail(result.getError());
            }

            Page<TripRouteResponse> page = result.getValue();
            items.addAll(page.items());
            if (!page.hasNext()) {
                return Result.ok(items);
            }
            pageRequest = pageRequest.next();
        }
    }

    private void openTrip(TripResponse trip, String dateRange) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", trip.id());
        args.addArgument("tripName", trip.title());
        args.addArgument("tripCountry", deriveCountryLabel(trip.countries()));
        args.addArgument("tripCategory", trip.category() == null ? "" : trip.category().name());
        args.addArgument("tripStatus", toLegacyStatus(trip.status()));
        args.addArgument("tripDates", dateRange);
        args.addArgument("tripStartDate", trip.startedAt() == null ? null : trip.startedAt().toString());
        args.addArgument("tripEndDate", trip.endedAt() == null ? null : trip.endedAt().toString());
        args.addArgument("tripCoverUrl", deriveCoverUrl(trip.images()));
        args.addArgument("tripTags", trip.tags() == null ? "" : String.join(",", trip.tags().stream().map(tag -> tag.name()).toList()));
        getRouter().moveto(RouteIds.ADD_TRIP, args);
    }

    private com.triplify.application.response.TripResponse toLegacyTrip(TripResponse trip) {
        return new com.triplify.application.response.TripResponse(
                trip.id(),
                trip.title(),
                deriveCountryLabel(trip.countries()),
                trip.category() == null ? "" : trip.category().name(),
                toLegacyStatus(trip.status()),
                toLocalDate(trip.startedAt()),
                toLocalDate(trip.endedAt()),
                null,
                deriveCoverUrl(trip.images()),
                trip.tags() == null ? List.of() : trip.tags().stream().map(tag -> tag.name()).toList()
        );
    }

    private void drawRoutePreview(RouteResponse route) {
        GraphicsContext graphics = routePreviewCanvas.getGraphicsContext2D();
        double width = routePreviewCanvas.getWidth();
        double height = routePreviewCanvas.getHeight();

        graphics.clearRect(0, 0, width, height);
        graphics.setFill(Color.web("#eef3ea"));
        graphics.fillRoundRect(0, 0, width, height, 24, 24);

        graphics.setStroke(Color.web("#d6ded0"));
        graphics.setLineWidth(2);
        for (int index = 0; index < 4; index++) {
            double y = 18 + (index * 34);
            graphics.strokeLine(14, y, width - 14, y + 12);
        }

        List<com.triplify.application.usecase.place.dto.PlaceResponse> places = route.places() == null
                ? List.of()
                : route.places().stream().filter(place -> place != null).toList();
        if (places.isEmpty()) {
            graphics.setFill(Color.web("#8e98a4"));
            graphics.fillText(I18n.t("route.details.preview.empty"), 18, height / 2.0);
            return;
        }

        double minLatitude = places.stream().mapToDouble(com.triplify.application.usecase.place.dto.PlaceResponse::latitude).min().orElse(0);
        double maxLatitude = places.stream().mapToDouble(com.triplify.application.usecase.place.dto.PlaceResponse::latitude).max().orElse(0);
        double minLongitude = places.stream().mapToDouble(com.triplify.application.usecase.place.dto.PlaceResponse::longitude).min().orElse(0);
        double maxLongitude = places.stream().mapToDouble(com.triplify.application.usecase.place.dto.PlaceResponse::longitude).max().orElse(0);

        if (Double.compare(minLatitude, maxLatitude) == 0) {
            minLatitude -= 0.01;
            maxLatitude += 0.01;
        }
        if (Double.compare(minLongitude, maxLongitude) == 0) {
            minLongitude -= 0.01;
            maxLongitude += 0.01;
        }

        double padding = 18;
        List<Point> points = new ArrayList<>();
        for (com.triplify.application.usecase.place.dto.PlaceResponse place : places) {
            double x = padding + ((place.longitude() - minLongitude) / (maxLongitude - minLongitude)) * (width - padding * 2);
            double y = height - padding - ((place.latitude() - minLatitude) / (maxLatitude - minLatitude)) * (height - padding * 2);
            points.add(new Point(x, y));
        }

        graphics.setStroke(Color.web("#d94d4d"));
        graphics.setLineWidth(5);
        graphics.setLineCap(StrokeLineCap.ROUND);
        for (int index = 1; index < points.size(); index++) {
            Point previous = points.get(index - 1);
            Point current = points.get(index);
            graphics.strokeLine(previous.x(), previous.y(), current.x(), current.y());
        }

        for (int index = 0; index < points.size(); index++) {
            Point point = points.get(index);
            graphics.setFill(index == 0 ? Color.web("#234b81") : index == points.size() - 1 ? Color.web("#d94d4d") : Color.WHITE);
            graphics.fillOval(point.x() - 5, point.y() - 5, 10, 10);
            graphics.setStroke(Color.WHITE);
            graphics.setLineWidth(2);
            graphics.strokeOval(point.x() - 5, point.y() - 5, 10, 10);
        }
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String imagePath(ImageResponse image) {
        return image == null || image.url() == null ? null : image.url().toString();
    }

    private String imagePath(Set<ImageResponse> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .map(this::imagePath)
                .filter(path -> path != null && !path.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String deriveCountryLabel(Set<com.triplify.application.usecase.country.dto.CountryResponse> countries) {
        if (countries == null || countries.isEmpty()) {
            return "";
        }
        if (countries.size() == 1) {
            return countries.iterator().next().name();
        }
        return countries.iterator().next().name() + " +" + (countries.size() - 1);
    }

    private String deriveCoverUrl(Set<ImageResponse> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .map(ImageResponse::url)
                .filter(path -> path != null)
                .map(path -> path.toUri().toString())
                .findFirst()
                .orElse(null);
    }

    private LocalDate toLocalDate(Instant value) {
        return value == null ? null : value.atZone(ZoneOffset.UTC).toLocalDate();
    }

    private TripStatus toLegacyStatus(StatusEnum status) {
        if (status == null) {
            return TripStatus.PLANNED;
        }
        return switch (status) {
            case VISITED -> TripStatus.VISITED;
            case ONGOING -> TripStatus.ONGOING;
            case PLANNED, CANCELED -> TripStatus.PLANNED;
        };
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return "Dates TBA";
        }
        if (start != null && (end == null || start.equals(end))) {
            return start.format(DATE_FORMAT);
        }
        if (start != null && end != null) {
            if (start.getYear() == end.getYear() && start.getMonth() == end.getMonth()) {
                return String.format(
                        "%s %d - %d, %d",
                        start.getMonth().name().substring(0, 1) + start.getMonth().name().substring(1).toLowerCase(Locale.ROOT),
                        start.getDayOfMonth(),
                        end.getDayOfMonth(),
                        start.getYear()
                );
            }
            return start.format(DATE_FORMAT) + " - " + end.format(DATE_FORMAT);
        }
        return start == null ? end.format(DATE_FORMAT) : start.format(DATE_FORMAT);
    }

    private String formatDistance(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private String formatPlacesCount(int value) {
        return value == 1 ? "1 place" : value + " places";
    }

    private String formatStoryDate(Instant value) {
        return value == null ? I18n.t("route.details.story.date.unknown") : value.atZone(ZoneOffset.UTC).toLocalDate().format(DATE_FORMAT);
    }

    private String formatMessage(String key, Object... args) {
        return MessageFormat.format(I18n.t(key), args);
    }

    private Image loadImage(String imagePath) {
        String resolvedPath = imagePath == null || imagePath.isBlank() ? DEFAULT_IMAGE : imagePath;
        if (resolvedPath.startsWith("/")) {
            var resource = getClass().getResource(resolvedPath);
            if (resource != null) {
                return new Image(resource.toExternalForm(), true);
            }
        }

        File file = new File(resolvedPath);
        if (file.exists()) {
            return new Image(file.toURI().toString(), true);
        }

        var fallback = getClass().getResource(DEFAULT_IMAGE);
        return new Image(fallback.toExternalForm(), true);
    }

    private void configureButtonIcon(Button button, String iconLiteral, String styleClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(14);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
    }

    private void installRoundedImageClip(ImageView imageView, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(imageView.fitWidthProperty());
        clip.heightProperty().bind(imageView.fitHeightProperty());
        imageView.setClip(clip);
    }

    private void installRoundedPaneClip(StackPane pane, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(pane.widthProperty());
        clip.heightProperty().bind(pane.heightProperty());
        pane.setClip(clip);
    }

    private record AssociatedTripItem(TripResponse trip, String tripRouteId) { }

    private record Point(double x, double y) { }
}
