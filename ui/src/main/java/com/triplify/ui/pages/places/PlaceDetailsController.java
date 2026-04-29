package com.triplify.ui.pages.places;

import com.google.inject.Inject;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.DeletePlaceRequest;
import com.triplify.application.usecase.place.dto.GetPlaceByIdRequest;
import com.triplify.application.usecase.place.dto.GetPlaceRoutesRequest;
import com.triplify.application.usecase.place.dto.GetPlaceTripsRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.map.InteractiveMap;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.shared.component.empty_state.view.EmptyStateCardView;
import com.triplify.ui.pages.routes.view.RouteCardView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.pages.trips.view.TripCardView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.DisplayUtils.*;

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

import java.util.List;
import java.util.UUID;

public class PlaceDetailsController extends SimpleLifecycleAwareController {

    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final int ASSOCIATED_ITEMS_PAGE_SIZE = 8;
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
        if (placeId == null || placeId.isBlank()) {
            toast.warning(I18n.t("place.details.toast.notFound"));
            getRouter().popBackStack();
            return;
        }

        UUID placeUuid = UUID.fromString(placeId);

        var placeResult = placeService.getPlaceById(new GetPlaceByIdRequest(placeUuid));
        if (placeResult.isFailure()) {
            errorHandler.handle(placeResult.getError());
            getRouter().popBackStack();
            return;
        }

        var tripsResult = placeService.getPlaceTrips(new GetPlaceTripsRequest(
                placeUuid,
                new PageRequest(0, ASSOCIATED_ITEMS_PAGE_SIZE)
        ));
        if (tripsResult.isFailure()) {
            errorHandler.handle(tripsResult.getError());
            getRouter().popBackStack();
            return;
        }

        var routesResult = placeService.getPlaceRoutes(new GetPlaceRoutesRequest(
                placeUuid,
                new PageRequest(0, ASSOCIATED_ITEMS_PAGE_SIZE)
        ));
        if (routesResult.isFailure()) {
            errorHandler.handle(routesResult.getError());
            getRouter().popBackStack();
            return;
        }

        bind(placeResult.getValue(), tripsResult.getValue().items(), routesResult.getValue().items());
    }

    private void bind(PlaceResponse place, List<TripResponse> trips, List<RouteResponse> routes) {
        heroImageView.setImage(loadImage(imagePath(place)));
        placeTitleLabel.setText(safeText(place.title(), I18n.t("trip.add.fallback.place")));
        placeCountryLabel.setText(place.country() == null ? "" : safeText(place.country().name(), ""));
        descriptionValueLabel.setText(safeText(place.description(), I18n.t("place.details.empty.description")));

        placeMap.setMapCenter(place.latitude(), place.longitude());
        placeMap.setPinPosition(place.latitude(), place.longitude());

        renderAssociatedTrips(trips);
        renderAssociatedRoutes(routes);
        renderAssociatedStories(List.of());
    }

    private void renderAssociatedTrips(List<TripResponse> trips) {
        associatedTripsFlow.getChildren().clear();
        if (trips.isEmpty()) {
            associatedTripsFlow.getChildren().add(createEmptyState(I18n.t("place.details.empty.trips"), associatedTripsFlow));
            return;
        }

        for (TripResponse trip : trips) {
            String dateRange = formatDateRange(toLocalDate(trip.startedAt()), toLocalDate(trip.endedAt()));
            TripCardView card = TripCardView.create(
                    trip,
                    dateRange,
                    () -> openTrip(trip)
            );
            associatedTripsFlow.getChildren().add((Region) card.getRoot());
        }
    }

    private void renderAssociatedRoutes(List<RouteResponse> routes) {
        associatedRoutesFlow.getChildren().clear();
        if (routes.isEmpty()) {
            associatedRoutesFlow.getChildren().add(createEmptyState(I18n.t("place.details.empty.routes"), associatedRoutesFlow));
            return;
        }

        for (RouteResponse route : routes) {
            RouteCardView card = RouteCardView.create(route, () -> openRoute(route));
            associatedRoutesFlow.getChildren().add((Region) card.getRoot());
        }
    }

    private void renderAssociatedStories(List<StoryResponse> stories) {
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

    private String imagePath(PlaceResponse place) {
        return place.coverImage() == null || place.coverImage().url() == null
                ? DEFAULT_IMAGE
                : place.coverImage().url().toString();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void configureButtonIcon(Button button, String iconLiteral, String styleClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
    }

    private Image loadImage(String imagePath) {
        return EditorUtils.loadImage(imagePath, DEFAULT_IMAGE, getClass());
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
