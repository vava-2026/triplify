package com.triplify.ui.pages.trips;

import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.List;

public class MyTripsController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(MyTripsController.class);

    @FXML private VBox searchContainer;

    @FXML
    private void initialize() {
        SearchView<String> searchView = SearchView.create(
                Search.<String>builder(query -> List.of("Prague Weekend", "Rome Adventure", "Paris Escape")
                                .stream()
                                .filter(t -> t.toLowerCase().contains(query.toLowerCase()))
                                .toList())
                        .placeholder("Search trips...")
                        .debounceMs(200)
                        .maxResults(5)
                        .onResultSelected(trip -> log.info("Trip selected: {}", trip))
                        .build()
        );

        searchContainer.getChildren().add(searchView.getRoot());
    }

    @FXML
    public void onOpenTrip() {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", 42);
        args.addArgument("tripName", "Prague Weekend");
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }
}
