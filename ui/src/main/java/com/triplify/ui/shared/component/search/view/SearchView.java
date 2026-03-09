package com.triplify.ui.shared.component.search.view;

import com.triplify.ui.shared.component.entry.model.Entry;
import com.triplify.ui.shared.component.entry.view.EntryCell;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.model.SearchVariant;
import com.triplify.ui.shared.component.select.model.SelectVariant;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.List;

public class SearchView<T> extends VBox {

    private static final double ROW_HEIGHT = 32.0;

    @FXML @Getter private TextField  searchField;
    @FXML @Getter private ListView<Entry<T>> resultsListView;

    @FXML private Button clearButton;
    @FXML private Label noResultsLabel;

    private Search<T> model;
    private PauseTransition debounce;

    private SearchVariant lastVariant = null;
    private boolean isFocused = false;

    public SearchView(Search<T> model) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/triplify/ui/shared/component/search/view/AppSearch.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AppSearch.fxml", e);
        }

        resultsListView.setCellFactory(lv -> new EntryCell<>());
        update(model);
    }

    private void update(Search<T> model) {
        this.model = model;

        searchField.setPromptText(model.getPlaceholder());
        noResultsLabel.setText(model.getNoResultsMessage());

        // perform the search
        debounce = new PauseTransition(Duration.millis(model.getDebounceMs()));
        debounce.setOnFinished(e -> runSearch(searchField.getText()));

        // Search on the text change with debounce
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasText = newVal != null && !newVal.isEmpty();
            clearButton.setVisible(hasText);
            clearButton.setManaged(hasText);

            // triggers search in 300ms
            debounce.playFromStart();
        });

        // Show/hide results based on focus
        searchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            this.isFocused = isFocused;
            if (!isFocused) {
                showNothing();
            }
            else {
                // search immediately when focused
                runSearch(searchField.getText());
            }
        });

        resultsListView.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    model.selectResult(newVal);
                    resultsListView.getScene().getRoot().requestFocus();    // deselect the input
                    showNothing();
                }
            });

        resultsListView.getItems().addListener((javafx.collections.ListChangeListener<Entry<T>>) c ->
            updateListViewHeight());

        applyVariant(model.getVariant());

        // update UI state immediately after loading
        runSearch(searchField.getText());
    }

    private void runSearch(String query) {
        if (query == null) {
            resultsListView.getItems().clear();
            return;
        }

        List<Entry<T>> results = model.search(query);
        resultsListView.getItems().setAll(results);
        updateListViewHeight();

        if (!isFocused)
            return;

        if (results.isEmpty()) {
            showNoResults();
        }
        else {
            showResults();
        }
    }

    // UI helpers
    private void updateListViewHeight() {
        int max = model.getMaxResults() > 0 ? model.getMaxResults() : Integer.MAX_VALUE;
        int count = Math.min(resultsListView.getItems().size(), max);
        double height = count * ROW_HEIGHT + 6; // +6 for borders or whatever
        resultsListView.setPrefHeight(height);
        resultsListView.setMinHeight(height);
        resultsListView.setMaxHeight(height);
    }

    private void applyVariant(SearchVariant variant) {
        if (lastVariant == variant) return;
        if (lastVariant != null) {
            getStyleClass().remove(lastVariant.getStyleClass());
        }
        if (variant != null) {
            getStyleClass().add(variant.getStyleClass());
        }
        lastVariant = variant;
    }

    // show/hide UI
    private void showNothing() {
        getStyleClass().remove("search-showing");
        noResultsLabel.setVisible(false);
        noResultsLabel.setManaged(false);
        resultsListView.setVisible(false);
        resultsListView.setManaged(false);
    }

    private void showNoResults() {
        getStyleClass().remove("search-showing");
        noResultsLabel.setVisible(true);
        noResultsLabel.setManaged(true);
        resultsListView.setVisible(false);
        resultsListView.setManaged(false);
    }

    private void showResults() {
        if (!getStyleClass().contains("search-showing"))
            getStyleClass().add("search-showing");
        noResultsLabel.setVisible(false);
        noResultsLabel.setManaged(false);
        resultsListView.setVisible(true);
        resultsListView.setManaged(true);
    }

    @FXML
    private void onClearClicked() {
        searchField.clear();
    }
}
