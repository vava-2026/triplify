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
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.ui.routing.AppPage;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.pages.trips.model.Trips;
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
    @Setter
    private Consumer<String> navigationHandler;

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
        Trips tripsModel = Trips.builder(tripService)
                .onLoadFailed(error -> log.warn("Header trip search failed [code={}, message={}]", error.code(), error.message()))
                .build();

        Search<TripResponse> search = Search.builder(query -> toEntries(tripsModel.search(query)))
                .onLoadMore(query -> toEntries(tripsModel.loadMore(query)))
                .placeholderKey("search.tripsPlaceholder")
                .noResultKey("search.noResult")
                .debounceMs(250)
                .maxVisibleResults(7)
                .variant(FieldVariant.GHOST)
                .onResultSelected(entry -> {
                    TripResponse trip = entry.getValue();
                    tripsModel.reset();
                    if (navigationHandler != null) {
                        navigationHandler.accept(trip.id().toString());
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

    private List<Entry<TripResponse>> toEntries(List<TripResponse> trips) {
        return trips.stream()
                .map(trip -> Entry.<TripResponse>builder(
                                trip,
                                trip.title() != null && !trip.title().isBlank() ? trip.title() : "—")
                        .icon("fth-map")
                        .colorTheme(colorThemeForStatus(trip.status()))
                        .build())
                .toList();
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
