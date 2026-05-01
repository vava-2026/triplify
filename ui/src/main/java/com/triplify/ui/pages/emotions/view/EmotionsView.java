package com.triplify.ui.pages.emotions.view;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.pages.emotions.model.Emotions;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class EmotionsView extends VBox {

    private final Emotions model;
    private final SearchView<String> searchView;
    private final Label errorLabel = new Label();

    private Entry<String> selectedEntry;
    private boolean updatingSelection;

    public EmotionsView(Emotions model) {
        this.model = model;

        getStyleClass().add("emotions-view");

        Search<String> searchModel = Search.<String>builder(model::search)
                .placeholderKey(model.getPlaceholderKey())
                .noResultKey(model.getNoResultKey())
                .debounceMs(model.getDebounceMs())
                .maxVisibleResults(8)
                .searchOnTyping(model.isSearchOnTyping())
                .showOnEmptyQuery(true)
                .variant(model.getVariant())
                .size(AppComponentSize.MIDDLE)
                .onResultSelected(this::handleEmotionSelected)
                .build();

        this.searchView = new SearchView<>(searchModel);

        errorLabel.getStyleClass().addAll("input-error-label", "emotions-error-label");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        getChildren().setAll(searchView, errorLabel);

        bindSelectionResetOnTyping();
        bindLanguageSync();
    }

    public String getSelectedEmotionId() {
        return selectedEntry == null ? null : selectedEntry.getValue();
    }

    public void clearSearch() {
        selectedEntry = null;
        searchView.getSearchField().clear();
    }

    public boolean selectEmotionById(String emotionId) {
        Entry<String> entry = model.findById(emotionId);
        if (entry == null) return false;
        applySelectedEntry(entry);
        return true;
    }

    public void clearError() {
        getStyleClass().remove("emotions-has-error");
        searchView.clearError();
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }

    public void showError(String message) {
        if (!getStyleClass().contains("emotions-has-error")) {
            getStyleClass().add("emotions-has-error");
        }
        searchView.showError(message);
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private void handleEmotionSelected(Entry<String> entry) {
        applySelectedEntry(entry);
        model.selectResult(entry);
    }

    private void bindLanguageSync() {
        I18n.languageProperty().addListener((obs, oldLang, newLang) -> {
            if (selectedEntry == null) return;
            updatingSelection = true;
            try {
                searchView.getSearchField().setText(selectedEntry.getLabel());
            } finally {
                updatingSelection = false;
            }
        });
    }

    private void bindSelectionResetOnTyping() {
        searchView.getSearchField().textProperty().addListener((obs, oldValue, newValue) -> {
            if (updatingSelection) return;
            if (selectedEntry != null && (newValue == null || !newValue.equals(selectedEntry.getLabel()))) {
                selectedEntry = null;
                model.selectResult(null);
            }
        });
    }

    private void applySelectedEntry(Entry<String> entry) {
        updatingSelection = true;
        try {
            selectedEntry = entry;
            searchView.getSearchField().setText(entry.getLabel());
            clearError();
        } finally {
            updatingSelection = false;
        }
    }
}
