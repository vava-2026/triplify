package com.triplify.ui.shared.component.search.view;

import com.triplify.ui.shared.component.search.model.Search;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.List;

public class SearchView<T> {

    private static final double ROW_HEIGHT = 32.0;

    @FXML private TextField  searchField;
    @FXML private Button clearButton;
    @FXML private ListView<T> resultsListView;
    @FXML private Label noResultsLabel;

    private Search<T> model;
    private Node root;
    private PauseTransition debounce;

    private boolean isFocused = false;

    public static <T> SearchView<T> create(Search<T> model) {
        SearchView<T> controller = new SearchView<>();
        FXMLLoader loader = new FXMLLoader(
            SearchView.class.getResource("/com/triplify/ui/shared/component/search/view/AppSearch.fxml"));
        loader.setController(controller);
        try {
            controller.root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AppSearch.fxml", e);
        }
        controller.update(model);
        return controller;
    }

    @FXML
    private void initialize() {
        // Wired after load; real setup happens in bindModel()
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

        resultsListView.getItems().addListener((javafx.collections.ListChangeListener<T>) c ->
            updateListViewHeight());

        // update UI state immediately after loading
        runSearch(searchField.getText());
    }

    private void runSearch(String query) {
        if (query == null) {
            resultsListView.getItems().clear();
            return;
        }

        List<T> results = model.search(query);
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

    private void updateListViewHeight() {
        int max = model.getMaxResults() > 0 ? model.getMaxResults() : Integer.MAX_VALUE;
        int count = Math.min(resultsListView.getItems().size(), max);
        double height = count * ROW_HEIGHT + 6; // +6 for borders or whatever
        resultsListView.setPrefHeight(height);
        resultsListView.setMinHeight(height);
        resultsListView.setMaxHeight(height);
    }

    private void showNothing()
    {
        noResultsLabel.setVisible(false);
        noResultsLabel.setManaged(false);
        resultsListView.setVisible(false);
        resultsListView.setManaged(false);
    }

    // operations on to show/hide UI components
    private void showNoResults() {
        noResultsLabel.setVisible(true);
        noResultsLabel.setManaged(true);
        resultsListView.setVisible(false);
        resultsListView.setManaged(false);
    }

    private void showResults() {
        noResultsLabel.setVisible(false);
        noResultsLabel.setManaged(false);
        resultsListView.setVisible(true);
        resultsListView.setManaged(true);
    }

    // Event handlers
    @FXML
    private void onClearClicked() {
        searchField.clear();
    }

    // Public API
    public Node getRoot() {
        return root;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public ListView<T> getResultsListView() {
        return resultsListView;
    }
}
