package com.triplify.ui.pages.places;

import com.google.inject.Inject;
import com.triplify.application.response.TripStatus;
import com.triplify.application.usecase.place.details.PlaceDetailsService;
import com.triplify.application.usecase.place.details.dto.GetPlaceDetailsRequest;
import com.triplify.application.usecase.place.details.dto.PlaceDetailsResponse;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.DeletePlaceRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.map.InteractiveMap;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.shared.component.empty_state.view.EmptyStateCardView;
import com.triplify.ui.shared.component.route.view.RouteCardView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.trip.view.TripCardView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class PlaceDetailsController extends SimpleLifecycleAwareController {

    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    @FXML private VBox contentContainer;
    @FXML private StackPane heroContainer;
    @FXML private FlowPane topRowFlow;
    @FXML private Button backButton;
    @FXML private ImageView heroImageView;
    @FXML private Label placeTitleLabel;
    @FXML private Label placeCountryLabel;
    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionValueLabel;
    @FXML private Label mapTitleLabel;
    @FXML private InteractiveMap placeMap;
    @FXML private DetailActionButtonsView actionButtonsView;
    @FXML private SectionHeaderView associatedTripsHeader;
    @FXML private SectionHeaderView associatedStoriesHeader;
    @FXML private SectionHeaderView associatedRoutesHeader;
    @FXML private FlowPane associatedTripsFlow;
    @FXML private FlowPane associatedStoriesFlow;
    @FXML private FlowPane associatedRoutesFlow;

    @Inject private PlaceDetailsService placeDetailsService;
    @Inject private PlaceService placeService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;

    private String placeId;

    @FXML
    public void initialize() {
        configureButtonIcon(backButton, "fth-chevron-left", "place-details-back-icon");
        Localization.bindText(backButton.textProperty(), "place.details.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "place.details.description");
        Localization.bindText(mapTitleLabel.textProperty(), "place.details.map");
        Localization.bindText(associatedTripsHeader.titleProperty(), "place.details.section.trips");
        Localization.bindText(associatedStoriesHeader.titleProperty(), "place.details.section.stories");
        Localization.bindText(associatedRoutesHeader.titleProperty(), "place.details.section.routes");

        topRowFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        associatedTripsFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        associatedRoutesFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        associatedStoriesFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());

        heroImageView.fitWidthProperty().bind(heroContainer.widthProperty());
        heroImageView.fitHeightProperty().bind(heroContainer.heightProperty());
        installRoundedPaneClip(heroContainer, 28);
        installRoundedImageClip(heroImageView, 28);
        installRoundedPaneClip(placeMap, 20);
        placeMap.setSelectionEnabled(false);
        placeMap.setControlsVisible(false);
        Localization.bindText(actionButtonsView.getPrimaryButton().textProperty(), "place.details.action.edit");
        Localization.bindText(actionButtonsView.getSecondaryButton().textProperty(), "place.details.action.delete");
        actionButtonsView.getPrimaryButton().setOnAction(event -> onEditPlace());
        actionButtonsView.getSecondaryButton().setOnAction(event -> onDeletePlace());
        configureButtonIcon(actionButtonsView.getPrimaryButton(), "fth-edit-3", "app-btn-icon");
        configureButtonIcon(actionButtonsView.getSecondaryButton(), "fth-trash-2", "app-btn-icon");
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        placeId = data == null ? null : data.getValue("placeId");
    }

    @Override
    public void onLifecycleShow() {
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
        args.addArgument("tripId", 0);
        args.addArgument("tripName", "");
        args.addArgument("placeId", placeId);
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    @FXML
    private void onDeletePlace() {
        if (placeId == null || placeId.isBlank()) {
            return;
        }

        var result = placeService.deletePlace(new DeletePlaceRequest(placeId));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            return;
        }

        toast.success(I18n.t("place.details.toast.deleted.title"), I18n.t("place.details.toast.deleted.body"));
        getRouter().popBackStack();
    }

    private void loadPlaceDetails() {
        if (placeId == null || placeId.isBlank()) {
            toast.warning(I18n.t("place.details.toast.notFound"));
            getRouter().popBackStack();
            return;
        }

        var result = placeDetailsService.getPlaceDetails(new GetPlaceDetailsRequest(placeId));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            getRouter().popBackStack();
            return;
        }

        bind(result.getValue());
    }

    private void bind(PlaceDetailsResponse details) {
        PlaceResponse place = details.place();

        heroImageView.setImage(loadImage(imagePath(place)));
        placeTitleLabel.setText(safeText(place.title(), I18n.t("trip.add.fallback.place")));
        placeCountryLabel.setText(place.country() == null ? "" : safeText(place.country().name(), ""));
        descriptionValueLabel.setText(safeText(place.description(), I18n.t("place.details.empty.description")));

        placeMap.setMapCenter(place.latitude(), place.longitude());
        placeMap.setPinPosition(place.latitude(), place.longitude());

        renderAssociatedTrips(details.associatedTrips());
        renderAssociatedRoutes(details.associatedRoutes());
        renderAssociatedStories(details.associatedStories());
    }

    private void renderAssociatedTrips(java.util.List<com.triplify.application.usecase.trip.dto.TripResponse> trips) {
        associatedTripsFlow.getChildren().clear();
        if (trips.isEmpty()) {
            associatedTripsFlow.getChildren().add(createEmptyState(I18n.t("place.details.empty.trips"), associatedTripsFlow));
            return;
        }

        for (com.triplify.application.usecase.trip.dto.TripResponse trip : trips) {
            String dateRange = formatDateRange(toLocalDate(trip.startedAt()), toLocalDate(trip.endedAt()));
            TripCardView card = TripCardView.createForDetails(
                    toLegacyTrip(trip),
                    dateRange,
                    () -> openTrip(trip, dateRange)
            );
            associatedTripsFlow.getChildren().add((Region) card.getRoot());
        }
    }

    private void renderAssociatedRoutes(java.util.List<RouteResponse> routes) {
        associatedRoutesFlow.getChildren().clear();
        if (routes.isEmpty()) {
            associatedRoutesFlow.getChildren().add(createEmptyState(I18n.t("place.details.empty.routes"), associatedRoutesFlow));
            return;
        }

        for (RouteResponse route : routes) {
            RouteCardView card = RouteCardView.createForDetails(route, null);
            associatedRoutesFlow.getChildren().add((Region) card.getRoot());
        }
    }

    private void renderAssociatedStories(java.util.List<StoryResponse> stories) {
        associatedStoriesFlow.getChildren().clear();
        if (stories.isEmpty()) {
            associatedStoriesFlow.getChildren().add(createEmptyState(I18n.t("place.details.empty.stories"), associatedStoriesFlow));
            return;
        }

        for (StoryResponse story : stories) {
            VBox card = new VBox(6);
            card.getStyleClass().add("place-details-story-card");

            Label title = new Label(safeText(story.title(), I18n.t("place.details.story.fallback")));
            title.getStyleClass().add("place-details-story-title");
            title.setWrapText(true);

            Label description = new Label(safeText(story.description(), I18n.t("place.details.empty.description")));
            description.getStyleClass().add("place-details-story-description");
            description.setWrapText(true);

            card.getChildren().addAll(title, description);
            associatedStoriesFlow.getChildren().add(card);
        }
    }

    private EmptyStateCardView createEmptyState(String text, FlowPane parent) {
        EmptyStateCardView card = new EmptyStateCardView();
        card.setText(text);
        card.prefWidthProperty().bind(parent.widthProperty());
        card.maxWidthProperty().bind(parent.widthProperty());
        return card;
    }

    private void openTrip(com.triplify.application.usecase.trip.dto.TripResponse trip, String dateRange) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", trip.id());
        args.addArgument("tripName", trip.title());
        args.addArgument("tripCountry", deriveCountryLabel(trip.countries()));
        args.addArgument("tripCategory", trip.category() == null ? "" : trip.category().name());
        args.addArgument("tripStatus", toLegacyStatus(trip.status()));
        args.addArgument("tripDates", dateRange);
        args.addArgument("tripStartDate", trip.startedAt() == null ? null : toLocalDate(trip.startedAt()).toString());
        args.addArgument("tripEndDate", trip.endedAt() == null ? null : toLocalDate(trip.endedAt()).toString());
        args.addArgument("tripCoverUrl", deriveCoverUrl(trip.images()));
        args.addArgument("tripTags", trip.tags() == null ? "" : String.join(",", trip.tags().stream().map(TagResponse::name).toList()));
        getRouter().moveto(RouteIds.ADD_TRIP, args);
    }

    private String imagePath(PlaceResponse place) {
        return place.coverImage() == null || place.coverImage().url() == null
                ? DEFAULT_IMAGE
                : place.coverImage().url().toString();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private com.triplify.application.response.TripResponse toLegacyTrip(
            com.triplify.application.usecase.trip.dto.TripResponse trip
    ) {
        return new com.triplify.application.response.TripResponse(
                trip.id(),
                safeText(trip.title(), I18n.t("trip.add.fallback.trip")),
                deriveCountryLabel(trip.countries()),
                trip.category() == null ? "" : trip.category().name(),
                toLegacyStatus(trip.status()),
                toLocalDate(trip.startedAt()),
                toLocalDate(trip.endedAt()),
                null,
                deriveCoverUrl(trip.images()),
                trip.tags() == null ? java.util.List.of() : trip.tags().stream().map(TagResponse::name).toList()
        );
    }

    private String deriveCountryLabel(java.util.Set<com.triplify.application.usecase.country.dto.CountryResponse> countries) {
        if (countries == null || countries.isEmpty()) {
            return "";
        }
        if (countries.size() == 1) {
            return countries.iterator().next().name();
        }
        return countries.iterator().next().name() + " +" + (countries.size() - 1);
    }

    private String deriveCoverUrl(java.util.Set<ImageResponse> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
                .filter(image -> image.url() != null)
                .map(image -> image.url().toUri().toString())
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
                        start.getMonth().name().substring(0, 1) + start.getMonth().name().substring(1).toLowerCase(),
                        start.getDayOfMonth(),
                        end.getDayOfMonth(),
                        start.getYear()
                );
            }
            return start.format(DATE_FORMAT) + " - " + end.format(DATE_FORMAT);
        }
        return start == null ? end.format(DATE_FORMAT) : start.format(DATE_FORMAT);
    }

    private void configureButtonIcon(Button button, String iconLiteral, String styleClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
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

    private void installRoundedImageClip(ImageView target, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(target.fitWidthProperty());
        clip.heightProperty().bind(target.fitHeightProperty());
        target.setClip(clip);
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
