package com.triplify.ui.pages.places;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
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
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.pages.routes.view.RouteCardView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.pages.trips.view.TripCardView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
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

import java.util.UUID;

public class PlaceDetailsController extends SimpleLifecycleAwareController {

    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";

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
    @FXML private CardGridPane<TripResponse> tripsGrid;
    @FXML private CardGridPane<RouteResponse> routesGrid;
    @FXML private CardGridPane<StoryResponse> storiesGrid;

    @Inject private PlaceService placeService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

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

        heroImageView.fitWidthProperty().bind(heroContainer.widthProperty());
        heroImageView.fitHeightProperty().bind(heroContainer.heightProperty());
        installRoundedPaneClip(heroContainer, 28);
        installRoundedImageClip(heroImageView, 28);
        installRoundedPaneClip(placeMap, 20);
        placeMap.setSelectionEnabled(false);
        placeMap.setControlsVisible(false);
        actionButtonsView.configurePrimary(fxmlLoader, Localization.textBinding("place.details.action.edit"), "fth-edit-3", this::onEditPlace);
        actionButtonsView.configureDelete(fxmlLoader, Localization.textBinding("place.details.action.delete"), "fth-trash-2", Localization.textBinding("place.details.action.delete.confirm"), this::onDeletePlace);

        setupAssociatedGrids();
    }

    private void setupAssociatedGrids() {
        tripsGrid.setManualLoadMore(true);
        tripsGrid.setPageSize(8);
        tripsGrid.setMinCardWidth(220);
        tripsGrid.setMaxColumns(3);
        tripsGrid.setLoadMoreKey("place.details.show.more.trips");
        tripsGrid.setEmptyText(I18n.t("place.details.empty.trips"));

        routesGrid.setManualLoadMore(true);
        routesGrid.setPageSize(8);
        routesGrid.setMinCardWidth(220);
        routesGrid.setMaxColumns(3);
        routesGrid.setLoadMoreKey("place.details.show.more.routes");
        routesGrid.setEmptyText(I18n.t("place.details.empty.routes"));

        storiesGrid.setManualLoadMore(true);
        storiesGrid.setPageSize(8);
        storiesGrid.setMinCardWidth(220);
        storiesGrid.setMaxColumns(3);
        storiesGrid.setLoadMoreKey("place.details.show.more.stories");
        storiesGrid.setEmptyText(I18n.t("place.details.empty.stories"));
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

        bind(placeResult.getValue());
    }

    private void bind(PlaceResponse place) {
        heroImageView.setImage(loadImage(imagePath(place)));
        placeTitleLabel.setText(safeText(place.title(), I18n.t("trip.add.fallback.place")));
        placeCountryLabel.setText(place.country() == null ? "" : safeText(place.country().name(), ""));
        descriptionValueLabel.setText(safeText(place.description(), I18n.t("place.details.empty.description")));

        placeMap.setMapCenter(place.latitude(), place.longitude());
        placeMap.setPinPosition(place.latitude(), place.longitude());

        UUID placeUuid = place.id();

        tripsGrid.setCardFactory(trip -> {
            String dateRange = formatDateRange(toLocalDate(trip.startedAt()), toLocalDate(trip.endedAt()));
            return (Region) TripCardView.create(trip, dateRange, () -> openTrip(trip)).getRoot();
        });
        tripsGrid.setPageLoader((page, size) -> {
            var r = placeService.getPlaceTrips(new GetPlaceTripsRequest(placeUuid, new PageRequest(page - 1, size)));
            if (r.isFailure()) return new CardGridPane.PageResult<>(java.util.List.of(), null);
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(),
                    new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        tripsGrid.refresh();

        routesGrid.setCardFactory(route -> (Region) RouteCardView.create(route, () -> openRoute(route)).getRoot());
        routesGrid.setPageLoader((page, size) -> {
            var r = placeService.getPlaceRoutes(new GetPlaceRoutesRequest(placeUuid, new PageRequest(page - 1, size)));
            if (r.isFailure()) return new CardGridPane.PageResult<>(java.util.List.of(), null);
            var p = r.getValue();
            return new CardGridPane.PageResult<>(p.items(),
                    new Pagination(page, size, null, p.hasNext() ? page + 1 : page));
        });
        routesGrid.refresh();

        storiesGrid.setCardFactory(story -> new Label());
        storiesGrid.setPageLoader((page, size) -> new CardGridPane.PageResult<>(java.util.List.of(), null));
        storiesGrid.refresh();
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
