package com.triplify.ui.pages.places;

import com.google.inject.Inject;
import com.triplify.application.usecase.place.dto.PlaceSort;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.PlaceResponse;
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

import java.util.Comparator;
import java.util.List;

public class TripPlacesController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(TripPlacesController.class);

    @FXML private Label tripNameLabel;
    @FXML private ComboBox<PlaceSort> sortSelect;
    @FXML private CardGridPane<PlaceResponse> cardGrid;

    private Integer tripId;
    private String  tripName;

    /* Mock data */
    private static final List<PlaceResponse> MOCK_PLACES = List.of(
            mockPlace("1", "Eiffel Tower",        "France",  "2024-03-10"),
            mockPlace("2", "Louvre Museum",       "France",  "2024-03-11"),
            mockPlace("3", "Notre-Dame",          "France",  "2024-03-11"),
            mockPlace("4", "Montmartre",          "France",  "2024-03-12"),
            mockPlace("5", "Palace of Versailles","France",  "2024-03-13"),
            mockPlace("6", "Sainte-Chapelle",     "France",  "2024-03-12"),
            mockPlace("7", "Musée d'Orsay",       "France",  "2024-03-14"),
            mockPlace("8", "Seine River Cruise",  "France",  "2024-03-10"),
            mockPlace("9", "Champs-Élysées",      "France",  "2024-03-11"),
            mockPlace("10","Père Lachaise",       "France",  "2024-03-15")
    );

    private static PlaceResponse mockPlace(String id, String title, String country, String date) {
        var countryDto = new com.triplify.application.usecase.country.dto.CountryResponse(
                id, country, country, "", "", true
        );
        var image = new com.triplify.application.usecase.image.dto.ImageResponse(
                id,
                java.nio.file.Path.of("/com/triplify/ui/pages/trips/images/two.png"),
                null,
                java.time.Instant.now()
        );
        return new PlaceResponse(
                id, null, countryDto, image,
                title, null,
                0.0, 0.0,
                java.time.Instant.parse(date + "T00:00:00Z"),
                java.time.Instant.parse(date + "T00:00:00Z")
        );
    }

    @Inject private PlaceService placeService;

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

    /*
    private CardGridPane.PageResult<PlaceResponse> loadPlacesPage(int page, int pageSize) {
        PageRequest pageRequest = new PageRequest(page, pageSize);
        PlaceFilter filter = new PlaceFilter(null, null);

        Result<Page<PlaceResponse>> result = placeService.getPlaces(new GetPlacesRequest(pageRequest, filter));

        if (!result.isSuccess()) {
            log.error("Failed to load places: {}", result.getError());
            return new CardGridPane.PageResult<>(List.of(), null);
        }

        Page<PlaceResponse> domainPage = result.getValue();
        List<PlaceResponse> sorted = applySort(domainPage.items(), sortSelect.getValue());

        // PageResult expects our Pagination wrapper — adapt from domain Page
        com.triplify.application.pagination.Pagination pagination =
                com.triplify.application.pagination.Pagination.response(page, pageSize, (int) domainPage.totalElements());

        return new CardGridPane.PageResult<>(sorted, pagination);
    }
    */

    // Mock implementation for demonstration */
    private CardGridPane.PageResult<PlaceResponse> loadPlacesPage(int page, int pageSize) {
        List<PlaceResponse> all = applySort(MOCK_PLACES, sortSelect.getValue());

        int from = (page - 1) * pageSize;
        int to   = Math.min(from + pageSize, all.size());
        List<PlaceResponse> slice = from >= all.size() ? List.of() : all.subList(from, to);

        com.triplify.application.pagination.Pagination pagination =
                com.triplify.application.pagination.Pagination.response(page, pageSize, all.size());

        return new CardGridPane.PageResult<>(slice, pagination);
    }
    private List<PlaceResponse> applySort(List<PlaceResponse> items, PlaceSort sort) {
        if (sort == null || items == null) return items;
        Comparator<PlaceResponse> cmp = switch (sort) {
            case NEWEST_FIRST -> Comparator.comparing(PlaceResponse::createdAt,
                    Comparator.nullsLast(Comparator.reverseOrder()));
            case OLDEST_FIRST -> Comparator.comparing(PlaceResponse::createdAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case NAME_AZ      -> Comparator.comparing(PlaceResponse::title);
            case NAME_ZA      -> Comparator.comparing(PlaceResponse::title).reversed();
        };
        return items.stream().sorted(cmp).toList();
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

        Label title = new Label("Add New Place");
        title.getStyleClass().add("trip-create-title");

        content.getChildren().addAll(icon, title);
        card.getChildren().add(content);
        return card;
    }

    private void openPlace(PlaceResponse place) {
        log.info("Open place: id={}, name={}", place.id(), place.title());
        // RouterArgument args = new RouterArgument();
        // args.addArgument("placeId", place.id());
        // args.addArgument("placeName", place.title());
        // getRouter().moveto(RouteIds.PLACE_DETAILS, args);
    }
}