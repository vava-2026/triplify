package com.triplify.ui.pages.routes;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.GetRoutesRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.add_card.view.AddCardView;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.pages.routes.view.RouteCardView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.Localization;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.List;
import java.util.UUID;

public class MyRoutesController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(MyRoutesController.class);

    @FXML private VBox searchContainer;
    @FXML private Label sortByLabel;
    @FXML private VBox sortSelectContainer;
    @FXML private CardGridPane<RouteResponse> cardGrid;

    @Inject private RouteService routeService;

    private InputItem searchInput;
    private Select<Boolean> sortByModel;
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
    public void onCreateRoute() {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", UUID.randomUUID().toString());
        args.addArgument("tripName", "");
        getRouter().moveto(RouteIds.ADD_ROUTE, args);
    }

    private void configureFilters() {
        searchInput = new InputItem("routes.search.placeholder", FieldVariant.FILLED);
        searchContainer.getChildren().setAll(searchInput);

        searchDebounce = new PauseTransition(Duration.millis(300));
        searchDebounce.setOnFinished(e -> cardGrid.refresh());
        searchInput.textProperty().addListener((obs, oldVal, newVal) -> searchDebounce.playFromStart());

        sortByModel = Select.<Boolean>builder()
                .placeholder(I18n.t("routes.sort.shortestFirst"))
                .size(AppComponentSize.BIG)
                .items(List.of(
                        Entry.builder(true, Localization.textBinding("routes.sort.shortestFirst")).build(),
                        Entry.builder(false, Localization.textBinding("routes.sort.longestFirst")).build()
                ))
                .build();
        sortByModel.setSelectedItem(sortByModel.getItems().get(0));
        sortByModel.selectedItemProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());

        SelectView<Boolean> sortView = new SelectView<>();
        sortView.update(sortByModel);
        sortView.setPrefWidth(160);
        sortView.setMaxWidth(160);
        sortView.getComboBox().setPrefWidth(160);
        sortView.getComboBox().setMaxWidth(160);
        sortView.getComboBox().getStyleClass().add("trips-filter");
        sortSelectContainer.getChildren().setAll(sortView);

        Localization.bindText(sortByLabel.textProperty(), "trips.sort.label");
    }

    private void configureGrid() {
        cardGrid.setMinCardWidth(220);
        cardGrid.setMaxColumns(5);
        cardGrid.setPageSize(9);
        cardGrid.setEmptyTextKey("routes.empty");
        cardGrid.addPinnedNode(new AddCardView("route.add.card.title", "route.add.card.subtitle", this::onCreateRoute));
        cardGrid.setCardFactory(this::buildRouteCard);
        cardGrid.setPageLoader(this::loadRoutesPage);
    }

    private CardGridPane.PageResult<RouteResponse> loadRoutesPage(int page, int pageSize) {
        String nameFilter = searchInput == null || searchInput.getText().isBlank()
                ? null
                : searchInput.getText().trim();

        boolean lengthAsc = sortByModel.getSelectedItem() != null
                && Boolean.TRUE.equals(sortByModel.getSelectedItem().getValue());

        var request = new GetRoutesRequest(
                new PageRequest(Math.max(0, page - 1), pageSize),
                new GetRoutesRequest.Filter(nameFilter),
                new GetRoutesRequest.OrderBy(lengthAsc)
        );

        var result = routeService.getRoutes(request);
        if (result.isFailure()) {
            log.warn("Failed to load routes: {}", result.getError().message());
            return new CardGridPane.PageResult<>(List.of(), Pagination.request(page, pageSize).withTotals(0));
        }

        int totalPages = result.getValue().hasNext() ? page + 1 : page;
        return new CardGridPane.PageResult<>(result.getValue().items(), new Pagination(page, pageSize, null, totalPages));
    }

    private Node buildRouteCard(RouteResponse route) {
        RouteCardView card = RouteCardView.create(route, () -> openRoute(route));
        return card.getRoot();
    }


    private void openRoute(RouteResponse route) {
        if (route == null || route.id() == null) return;
        RouterArgument args = new RouterArgument();
        args.addArgument("routeId", route.id().toString());
        getRouter().moveto(RouteIds.ROUTE_DETAILS, args);
    }
}
