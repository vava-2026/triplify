package com.triplify.ui.pages.trips;

import com.triplify.application.model.ColorTheme;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.entry.model.Entry;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.model.SearchVariant;
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
        SearchView<String> searchView = new SearchView<>(
                Search.<String>builder(query -> List.of(
                                        Entry.builder("prague-weekend", "Prague Weekend").icon("fth-globe").build(),
                                        Entry.builder("rome-adventure", "Rome Adventure").icon("fth-globe").colorTheme(ColorTheme.GREEN).build(),
                                        Entry.builder("paris-escape", "Paris escape").colorTheme(ColorTheme.ORANGE).build()))
                        .placeholderKey("search.tripsPlaceholder")
                        .debounceMs(200)
                        .maxResults(5)
                        .onResultSelected(trip -> log.info("Trip selected: {}", trip))
                        .variant(SearchVariant.OUTLINED)
                        .build()
        );

        searchContainer.getChildren().add(searchView);
    }

    @FXML
    public void onOpenTrip() {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", 42);
        args.addArgument("tripName", "Prague Weekend");
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }
}
