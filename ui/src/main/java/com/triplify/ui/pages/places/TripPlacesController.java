package com.triplify.ui.pages.places;

import com.triplify.application.pagination.Pagination;
import com.triplify.application.request.PlaceSort;
import com.triplify.application.request.SearchPlacesRequest;
import com.triplify.application.response.PlaceResponse;
import com.triplify.application.response.SearchPlacesResponse;
import com.triplify.application.service.PlaceService;
import com.triplify.application.service.TripPlaceServiceImpl;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.place.view.PlaceCardView;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class TripPlacesController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(TripPlacesController.class);

    @FXML private Label tripNameLabel;
    @FXML private ComboBox<String> statusSelect;
    @FXML private ComboBox<PlaceSort> sortSelect;
    @FXML private CardGridPane<PlaceResponse> cardGrid;

    private Integer tripId;
    private String  tripName;

    private final PlaceService placeService = new TripPlaceServiceImpl();

    @FXML
    private void initialize() {
        sortSelect.setItems(FXCollections.observableArrayList(PlaceSort.values()));
        sortSelect.getSelectionModel().select(PlaceSort.NEWEST_FIRST);

        cardGrid.setMinCardWidth(220);
        cardGrid.setMaxColumns(4);
        cardGrid.setPageSize(8);
        cardGrid.setEmptyText("No places found");
        cardGrid.addPinnedNode(buildAddPlaceCard());
        cardGrid.setCardFactory(this::buildPlaceCard);
        cardGrid.setPageLoader(this::loadPlacesPage);

        sortSelect.valueProperty().addListener((obs, o, n) -> cardGrid.refresh());
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        tripId   = data == null ? null : data.getValue("tripId");
        tripName = data == null ? null : data.getValue("tripName");

        tripNameLabel.setText(tripName != null ? tripName : "Trip Places");
        log.info("TripPlaces opened: tripId={}, tripName={}", tripId, tripName);
        cardGrid.refresh();
    }

    @FXML
    private void onBack() {
        getRouter().popBackStack();
    }

    private CardGridPane.PageResult<PlaceResponse> loadPlacesPage(int page, int pageSize) {
        SearchPlacesRequest request = new SearchPlacesRequest(
                tripId,
                null, null,   // filters — add ComboBoxes to FXML when needed
                sortSelect.getValue(),
                Pagination.request(page, pageSize)
        );
        log.info("Places search: tripId={}, page={}, sort={}", tripId, page, request.sort());

        SearchPlacesResponse response = placeService.searchPlaces(request);
        return new CardGridPane.PageResult<>(response.places(), response.pagination());
    }

    private Node buildPlaceCard(PlaceResponse place) {
        PlaceCardView card = PlaceCardView.create(place, () -> openPlace(place));
        return card.getRoot();
    }

    private Node buildAddPlaceCard() {
        StackPane card = new StackPane();
        card.getStyleClass().add("place-create-card");
        card.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(6);
        content.setAlignment(Pos.CENTER);

        FontIcon icon = new FontIcon("fth-map-pin");
        icon.getStyleClass().add("trip-create-icon");

        Label title    = new Label("Add New Place");
        title.getStyleClass().add("trip-create-title");

        content.getChildren().addAll(icon, title);
        card.getChildren().add(content);
        return card;
    }

    private void openPlace(PlaceResponse place) {
        log.info("Open place: id={}, name={}", place.id(), place.name());

        // For place details page
        // RouterArgument args = new RouterArgument();
        // args.addArgument("placeId", place.id());
        // getRouter().moveto(RouteIds.PLACE_DETAILS, args);
    }
}