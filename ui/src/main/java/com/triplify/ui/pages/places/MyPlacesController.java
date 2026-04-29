package com.triplify.ui.pages.places;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.add_card.view.AddCardView;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.pages.countries.model.Countries;
import com.triplify.ui.pages.countries.view.CountriesView;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.pages.places.view.PlaceCardView;
import com.triplify.ui.shared.model.FieldVariant;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.List;
import java.util.UUID;

public class MyPlacesController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(MyPlacesController.class);

    @FXML private VBox searchContainer;
    @FXML private VBox countryFilterContainer;
    @FXML private CardGridPane<PlaceResponse> cardGrid;

    @Inject private PlaceService placeService;
    @Inject private CountryService countryService;

    private InputItem searchInput;
    private CountriesView countryFilterView;
    private PauseTransition searchDebounce;

    @FXML
    private void initialize() {
        configureFilters();
        configureGrid();
        cardGrid.refresh();
    }

    @Override
    public void onLifecycleShow() {
        if (cardGrid != null) {
            cardGrid.refresh();
        }
    }

    @FXML
    public void onCreatePlace() {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", UUID.randomUUID().toString());
        args.addArgument("tripName", "");
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    private void configureFilters() {
        searchInput = new InputItem("places.search.placeholder", FieldVariant.FILLED);
        searchContainer.getChildren().setAll(searchInput);

        searchDebounce = new PauseTransition(Duration.millis(300));
        searchDebounce.setOnFinished(e -> cardGrid.refresh());
        searchInput.textProperty().addListener((obs, oldVal, newVal) -> searchDebounce.playFromStart());

        Countries countryFilterModel = Countries.builder(countryService)
                .placeholderKey("input.placeholder.country")
                .noResultKey("search.noResult")
                .searchOnTyping(true)
                .onResultSelected(entry -> cardGrid.refresh())
                .onLoadFailed(error -> log.warn("Failed to load country filter options: {}", error.code()))
                .build();
        countryFilterView = new CountriesView(countryFilterModel);
        countryFilterContainer.getChildren().setAll(countryFilterView);
    }

    private void configureGrid() {
        cardGrid.setMinCardWidth(257);
        cardGrid.setMaxColumns(4);
        cardGrid.setPageSize(8);
        cardGrid.setEmptyText("No places found");
        cardGrid.addPinnedNode(new AddCardView("place.add.card.title", "place.add.card.subtitle", this::onCreatePlace));
        cardGrid.setCardFactory(this::buildPlaceCard);
        cardGrid.setPageLoader(this::loadPlacesPage);
    }

    private CardGridPane.PageResult<PlaceResponse> loadPlacesPage(int page, int pageSize) {
        String countryId = countryFilterView == null ? null : countryFilterView.getSelectedCountryId();
        String normalizedCountryId = (countryId == null || countryId.isBlank()) ? null : countryId;

        String nameFilter = searchInput == null || searchInput.getText().isBlank()
                ? null
                : searchInput.getText().trim();

        var request = new GetPlacesRequest(
                new PageRequest(Math.max(0, page - 1), pageSize),
                new PlaceFilter(nameFilter, normalizedCountryId)
        );

        var result = placeService.getPlaces(request);
        if (result.isFailure()) {
            log.warn("Failed to load places: {}", result.getError().message());
            return new CardGridPane.PageResult<>(List.of(), Pagination.request(page, pageSize).withTotals(0));
        }

        int totalPages = result.getValue().hasNext() ? page + 1 : page;
        return new CardGridPane.PageResult<>(result.getValue().items(), new Pagination(page, pageSize, null, totalPages));
    }

    private Node buildPlaceCard(PlaceResponse place) {
        PlaceCardView card = PlaceCardView.create(place, () -> openPlace(place));
        return card.getRoot();
    }


    private void openPlace(PlaceResponse place) {
        if (place == null || place.id() == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("placeId", place.id().toString());
        getRouter().moveto(RouteIds.PLACE_DETAILS, args);
    }
}
