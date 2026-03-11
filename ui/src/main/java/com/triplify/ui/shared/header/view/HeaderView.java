package com.triplify.ui.shared.header.view;

import com.triplify.application.model.ColorTheme;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.pages.trips.MyTripsController;
import com.triplify.ui.shared.component.entry.model.Entry;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.model.SearchVariant;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.header.viewmodel.HeaderViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HeaderView implements Initializable {

    @FXML private StackPane headerRoot;
    @FXML private Label pageTitle;
    @FXML private StackPane searchContainer;

    @Getter private final HeaderViewModel viewModel = new HeaderViewModel();
    private static final Logger log = LoggerFactory.getLogger(MyTripsController.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pageTitle.textProperty().bind(viewModel.pageTitleBinding());

        SearchView<String> searchView = new SearchView<>(
                Search.<String>builder(query -> List.of(
                                Entry.builder("prague-weekend", "Prague Weekend").icon("fth-globe").build(),
                                Entry.builder("rome-adventure", "Rome Adventure").icon("fth-globe").colorTheme(ColorTheme.GREEN).build(),
                                Entry.builder("paris-escape", "Paris escape").colorTheme(ColorTheme.ORANGE).build()))
                        .placeholder("Search trips...")
                        .debounceMs(200)
                        .maxResults(5)
                        .onResultSelected(trip -> log.info("Trip selected: {}", trip))
                        .variant(SearchVariant.OUTLINED)
                        .build()
        );

//        headerRoot.setClip(null);
//        searchContainer.setClip(null);
//        searchView.setClip(null);

        searchContainer.getChildren().add(searchView);
    }
}
