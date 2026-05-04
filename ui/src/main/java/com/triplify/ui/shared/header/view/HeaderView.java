package com.triplify.ui.shared.header.view;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.triplify.application.shared.ColorTheme;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.ui.routing.AppPage;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.header.model.AppSearchModel;
import com.triplify.ui.shared.header.model.GlobalSearchItem;
import com.triplify.ui.shared.header.viewmodel.HeaderViewModel;
import com.triplify.ui.shared.model.FieldVariant;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;

public class HeaderView implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(HeaderView.class);
    private static final double TITLE_HIDE_BREAKPOINT = 980.0;

    @FXML private StackPane headerRoot;
    @FXML private Label pageTitle;
    @FXML private StackPane searchContainer;

    @Getter private final HeaderViewModel viewModel = new HeaderViewModel();

    @Inject private TripService tripService;
    @Inject private RouteService routeService;
    @Inject private PlaceService placeService;
    
    @Setter
    private Consumer<GlobalSearchItem> navigationHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pageTitle.textProperty().bind(viewModel.pageTitleBinding());
        bindSearchWidth();
        bindTitleVisibility();
        buildSearch();
    }

    public void setActivePage(AppPage page) {
        viewModel.activePageProperty().set(page);
    }

    private void buildSearch() {
        var searchModel = new AppSearchModel(tripService, routeService, placeService, 
            error -> log.warn("Header search failed [code={}, message={}]", error.code(), error.message()));

        Search<GlobalSearchItem> search = Search.builder(query -> toEntries(searchModel.search(query)))
                .placeholderKey("search.placeholder")
                .noResultKey("search.noResult")
                .debounceMs(250)
                .maxVisibleResults(7)
                .variant(FieldVariant.GHOST)
                .onResultSelected(entry -> {
                    var item = entry.getValue();
                    if (navigationHandler != null) {
                        navigationHandler.accept(item);
                    }
                })
                .build();

        searchContainer.getChildren().add(new SearchView<>(search));
    }

    private void bindSearchWidth() {
        searchContainer.setMinWidth(0);
        searchContainer.setMaxWidth(Double.MAX_VALUE);
        searchContainer.prefWidthProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(460, Math.max(220, headerRoot.getWidth() * 0.33)),
                        headerRoot.widthProperty()
                )
        );
    }

    private void bindTitleVisibility() {
        updateTitleVisibility(headerRoot.getWidth());
        headerRoot.widthProperty().addListener((obs, oldWidth, newWidth) -> updateTitleVisibility(newWidth == null ? 0 : newWidth.doubleValue()));
    }

    private void updateTitleVisibility(double width) {
        boolean showTitle = width >= TITLE_HIDE_BREAKPOINT;
        pageTitle.setManaged(showTitle);
        pageTitle.setVisible(showTitle);
    }

    private List<Entry<GlobalSearchItem>> toEntries(List<GlobalSearchItem> items) {
        return items.stream()
                .map(item -> Entry.<GlobalSearchItem>builder(
                                item,
                                item.getTitle())
                        .icon(iconForType(item.getType()))
                        .colorTheme(item.getColorTheme())
                        .build())
                .toList();
    }
    
    private String iconForType(GlobalSearchItem.Type type) {
        if (type == null) return "fth-map";
        return switch (type) {
            case TRIP -> "fth-map";
            case ROUTE -> "fth-corner-up-right";
            case PLACE -> "fth-map-pin";
        };
    }

    private ColorTheme colorThemeForStatus(StatusEnum status) {
        if (status == null) return null;
        return switch (status) {
            case PLANNED -> ColorTheme.BLUE;
            case ONGOING -> ColorTheme.GREEN;
            case VISITED -> ColorTheme.TEAL;
            case CANCELED -> ColorTheme.GRAY;
        };
    }
}
