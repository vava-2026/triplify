package com.triplify.ui.pages.trips;

import com.triplify.application.model.ColorTheme;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.model.FieldVariant;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.Arrays;
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
                        .variant(FieldVariant.FILLED)
                        .build()
        );

        SearchView<String> searchView2 = new SearchView<>(
                Search.<String>builder(query -> List.of())
                        .placeholderKey("search.tripsPlaceholder")
                        .debounceMs(200)
                        .maxResults(5)
                        .onResultSelected(trip -> log.info("Trip selected: {}", trip))
                        .variant(FieldVariant.OUTLINED)
                        .build()
        );

        SearchView<String> searchView3 = new SearchView<>(
                Search.<String>builder(query -> List.of(
                                Entry.builder("prague-weekend", "Prague Weekend").icon("fth-globe").build(),
                                Entry.builder("rome-adventure", "Rome Adventure").icon("fth-globe").colorTheme(ColorTheme.GREEN).build(),
                                Entry.builder("paris-escape", "Paris escape").colorTheme(ColorTheme.ORANGE).build()))
                        .placeholderKey("search.tripsPlaceholder")
                        .debounceMs(200)
                        .maxResults(5)
                        .onResultSelected(trip -> log.info("Trip selected: {}", trip))
                        .variant(FieldVariant.GHOST)
                        .build()
        );

        SelectView<Integer> selectView = new SelectView<>();
        selectView.update(Select.<Integer>builder()
                .placeholder("Choose a number...")
                .variant(FieldVariant.FILLED)
                .items(Arrays.asList(
                        Entry.builder(1, "One").colorTheme(ColorTheme.BLUE).icon("fth-globe").build(),
                        Entry.builder(2, "Two").colorTheme(ColorTheme.GREEN).emoji("\uD83C\uDFD6\uFE0F").build(),
                        Entry.builder(3, "Three").colorTheme(ColorTheme.RED).emoji("\uD83D\uDD25").build(),
                        Entry.builder(1, "Option A").build(),
                        Entry.builder(2, "Option B").colorTheme(ColorTheme.ORANGE).build(),
                        Entry.builder(3, "Option C").colorTheme(ColorTheme.PURPLE).build(),
                        Entry.builder(4, "Option D").colorTheme(ColorTheme.TEAL).build(),
                        Entry.builder(5, "Option E").colorTheme(ColorTheme.PINK).build()
                ))
                .onSelect(entry -> System.out.println("Selected: " + entry.getValue()))
                .build());

        SelectView<Integer> selectView2 = new SelectView<>();
        selectView2.update(Select.<Integer>builder()
                .placeholder("Choose a number...")
                .variant(FieldVariant.OUTLINED)
                .items(Arrays.asList(
                        Entry.builder(1, "One").colorTheme(ColorTheme.BLUE).icon("fth-globe").build(),
                        Entry.builder(2, "Two").colorTheme(ColorTheme.GREEN).emoji("\uD83C\uDFD6\uFE0F").build(),
                        Entry.builder(3, "Three").colorTheme(ColorTheme.RED).emoji("\uD83D\uDD25").build(),
                        Entry.builder(1, "Option A").build(),
                        Entry.builder(2, "Option B").colorTheme(ColorTheme.ORANGE).build(),
                        Entry.builder(3, "Option C").colorTheme(ColorTheme.PURPLE).build(),
                        Entry.builder(4, "Option D").colorTheme(ColorTheme.TEAL).build(),
                        Entry.builder(5, "Option E").colorTheme(ColorTheme.PINK).build()
                ))
                .onSelect(entry -> System.out.println("Selected: " + entry.getValue()))
                .build());

        SelectView<Integer> selectView3 = new SelectView<>();
        selectView3.update(Select.<Integer>builder()
                .placeholder("Choose a number...")
                .variant(FieldVariant.GHOST)
                .items(Arrays.asList(
                        Entry.builder(1, "One").colorTheme(ColorTheme.BLUE).icon("fth-globe").build(),
                        Entry.builder(2, "Two").colorTheme(ColorTheme.GREEN).emoji("\uD83C\uDFD6\uFE0F").build(),
                        Entry.builder(3, "Three").colorTheme(ColorTheme.RED).emoji("\uD83D\uDD25").build(),
                        Entry.builder(1, "Option A").build(),
                        Entry.builder(2, "Option B").colorTheme(ColorTheme.ORANGE).build(),
                        Entry.builder(3, "Option C").colorTheme(ColorTheme.PURPLE).build(),
                        Entry.builder(4, "Option D").colorTheme(ColorTheme.TEAL).build(),
                        Entry.builder(5, "Option E").colorTheme(ColorTheme.PINK).build()
                ))
                .onSelect(entry -> System.out.println("Selected: " + entry.getValue()))
                .build());

        searchContainer.getChildren().add(searchView);
        searchContainer.getChildren().add(searchView2);
        searchContainer.getChildren().add(searchView3);
        searchContainer.getChildren().add(selectView);
        searchContainer.getChildren().add(selectView2);
        searchContainer.getChildren().add(selectView3);
    }

    @FXML
    public void onOpenTrip() {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", 42);
        args.addArgument("tripName", "Prague Weekend");
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }
}
