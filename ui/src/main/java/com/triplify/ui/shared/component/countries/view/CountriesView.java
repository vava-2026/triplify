package com.triplify.ui.shared.component.countries.view;

import com.triplify.ui.shared.component.countries.model.Countries;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CountriesView extends VBox {

    private final Countries model;
    private final SearchView<String> searchView;
    private final Label errorLabel = new Label();

    private Entry<String> selectedEntry;
    private boolean updatingSelection;

    public CountriesView(Countries model) {
        this.model = model;

        getStyleClass().add("countries-view");

        Search<String> searchModel = Search.<String>builder(model::search)
                .placeholderKey(model.getPlaceholderKey())
                .noResultKey(model.getNoResultKey())
                .debounceMs(model.getDebounceMs())
                .maxVisibleResults(8)
                .searchOnTyping(model.isSearchOnTyping())
                .showOnEmptyQuery(true)
                .onLoadMore(model::loadMore)
                .loadMoreThreshold(0.82)
                .variant(model.getVariant())
                .size(AppComponentSize.MIDDLE)
                .onResultSelected(this::handleCountrySelected)
                .build();

        this.searchView = new SearchView<>(searchModel);

        errorLabel.getStyleClass().addAll("input-error-label", "countries-error-label");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        getChildren().setAll(searchView, errorLabel);

        bindSelectionResetOnTyping();
    }

    public String getSelectedCountryId() {
        return selectedEntry == null ? null : selectedEntry.getValue();
    }

    public void clearSearch() {
        selectedEntry = null;
        searchView.getSearchField().clear();
    }

    public boolean selectCountryByName(String countryName) {
        Entry<String> entry = model.findBestMatch(countryName);
        if (entry == null) {
            return false;
        }
        applySelectedEntry(entry);
        return true;
    }

    public void clearError() {
        getStyleClass().remove("countries-has-error");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }

    public void showError(String message) {
        if (!getStyleClass().contains("countries-has-error")) {
            getStyleClass().add("countries-has-error");
        }
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private void handleCountrySelected(Entry<String> countryEntry) {
        applySelectedEntry(countryEntry);
        model.selectResult(countryEntry);
    }

    private void bindSelectionResetOnTyping() {
        searchView.getSearchField().textProperty().addListener((obs, oldValue, newValue) -> {
            if (updatingSelection) {
                return;
            }
            if (selectedEntry != null && (newValue == null || !newValue.equals(selectedEntry.getLabel()))) {
                selectedEntry = null;
            }
        });
    }

    private void applySelectedEntry(Entry<String> countryEntry) {
        updatingSelection = true;
        try {
            selectedEntry = countryEntry;
            searchView.getSearchField().setText(countryEntry.getLabel());
            clearError();
        } finally {
            updatingSelection = false;
        }
    }
}







