package com.triplify.ui.shared.component.search.view;

import com.triplify.ui.shared.component.entry.model.Entry;
import com.triplify.ui.shared.component.entry.view.EntryCell;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.model.SearchVariant;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

public class SearchView<T> extends VBox {

    private static final double ROW_HEIGHT = 32.0;

    @FXML @Getter private TextField searchField;
    @FXML private HBox searchBox;
    @FXML private Button clearButton;

    @Getter private final ListView<Entry<T>> resultsListView = new ListView<>();
    private final Label noResultsLabel = new Label();
    private final VBox popupContent = new VBox();
    private final Popup popup = new Popup();

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

        setupPopup();
        resultsListView.setCellFactory(lv -> new EntryCell<>());
        update(model);
    }

    private void setupPopup() {
        resultsListView.getStyleClass().add("search-results");
        resultsListView.setFixedCellSize(ROW_HEIGHT);

        // Style the no-results label
        noResultsLabel.getStyleClass().add("search-no-results");
        //noResultsLabel.setPadding(new Insets(6, 10, 6, 10));

        popupContent.getStyleClass().add("search-popup-content");
        popupContent.getChildren().addAll(resultsListView, noResultsLabel);

        popup.getContent().add(popupContent);
        popup.setAutoHide(false);  // manage hide manually
    }

    private void showPopup() {
        if (getScene() == null || getScene().getWindow() == null) return;
        Bounds fieldBounds = searchBox.localToScreen(searchBox.getBoundsInLocal());
        if (fieldBounds == null) return;

        double x = fieldBounds.getMinX() - 2;
        double y = fieldBounds.getMaxY();
        double width = fieldBounds.getWidth();

        popupContent.setPrefWidth(width);
        popupContent.setMinWidth(width);
        popupContent.setMaxWidth(width);

        if (!popup.isShowing()) {
            popup.show(getScene().getWindow(), x, y);
        } else {
            popup.setX(x);
            popup.setY(y);
        }
    }

    private void update(Search<T> model) {
        this.model = model;

        searchField.setPromptText(model.getPlaceholder());
        noResultsLabel.setText(model.getNoResultsMessage());

        debounce = new PauseTransition(Duration.millis(model.getDebounceMs()));
        debounce.setOnFinished(e -> runSearch(searchField.getText()));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasText = newVal != null && !newVal.isEmpty();
            clearButton.setVisible(hasText);
            clearButton.setManaged(hasText);
            debounce.playFromStart();
        });

        searchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            this.isFocused = isFocused;
            if (!isFocused) {
                // Delay hiding so a click on a list item can register first
                PauseTransition hideDelay = new PauseTransition(Duration.millis(150));
                hideDelay.setOnFinished(e -> showNothing());
                hideDelay.play();
            } else {
                runSearch(searchField.getText());
            }
        });

        resultsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                javafx.application.Platform.runLater(() -> {
                    model.selectResult(newVal);
                    resultsListView.getSelectionModel().clearSelection();
                    if (getScene() != null) getScene().getRoot().requestFocus();
                    showNothing();
                });
            }
        });

        resultsListView.getItems().addListener((javafx.collections.ListChangeListener<Entry<T>>) c ->
            updateListViewHeight());

        applyVariant(model.getVariant());
        runSearch(searchField.getText());
    }

    private void runSearch(String query) {
        if (query == null) {
            resultsListView.getSelectionModel().clearSelection();
            resultsListView.getItems().clear();
            return;
        }

        List<Entry<T>> results = model.search(query);
        resultsListView.getSelectionModel().clearSelection();
        resultsListView.getItems().setAll(results);
        updateListViewHeight();

        if (!isFocused) return;

        if (results.isEmpty()) {
            showNoResults();
        } else {
            showResults();
        }
    }

    private void updateListViewHeight() {
        int max = model.getMaxResults() > 0 ? model.getMaxResults() : Integer.MAX_VALUE;
        int count = Math.min(resultsListView.getItems().size(), max);
        double height = count * ROW_HEIGHT + 6;
        resultsListView.setPrefHeight(height);
        resultsListView.setMinHeight(height);
        resultsListView.setMaxHeight(height);
    }

    private void applyVariant(SearchVariant variant) {
        if (lastVariant == variant) return;
        if (lastVariant != null) {
            getStyleClass().remove(lastVariant.getStyleClass());
            popupContent.getStyleClass().remove(lastVariant.getStyleClass());
        }
        if (variant != null) {
            getStyleClass().add(variant.getStyleClass());
            popupContent.getStyleClass().add(variant.getStyleClass());
        }
        lastVariant = variant;
    }

    private void showNothing() {
        getStyleClass().remove("search-showing");
        popupContent.getStyleClass().remove("search-showing");
        popup.hide();
    }

    private void showNoResults() {
        getStyleClass().remove("search-showing");
        popupContent.getStyleClass().remove("search-showing");
        noResultsLabel.setVisible(true);
        noResultsLabel.setManaged(true);
        resultsListView.setVisible(false);
        resultsListView.setManaged(false);
        showPopup();
    }

    private void showResults() {
        if (!getStyleClass().contains("search-showing"))
            getStyleClass().add("search-showing");
        if (!popupContent.getStyleClass().contains("search-showing"))
            popupContent.getStyleClass().add("search-showing");
        noResultsLabel.setVisible(false);
        noResultsLabel.setManaged(false);
        resultsListView.setVisible(true);
        resultsListView.setManaged(true);
        showPopup();
    }

    @FXML
    private void onClearClicked() {
        searchField.clear();
    }
}
