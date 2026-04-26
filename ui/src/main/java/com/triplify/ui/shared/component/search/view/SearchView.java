package com.triplify.ui.shared.component.search.view;

import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.entry.view.EntryCell;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.model.SearchDisplayMode;
import com.triplify.ui.shared.component.search.model.SearchSize;
import com.triplify.ui.shared.model.FieldVariant;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.util.Duration;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.List;

public class SearchView<T> extends VBox {

    private static final double ROW_HEIGHT = 32.0;

    @FXML @Getter private TextField searchField;
    @FXML private HBox searchBox;
    @FXML private FontIcon searchIcon;

    @Getter private final ListView<Entry<T>> resultsListView = new ListView<>();
    private final Label noResultsLabel = new Label();
    private final VBox inlineContent = new VBox();
    private final VBox popupContent = new VBox();
    private final Popup popup = new Popup();

    private Search<T> model;
    private PauseTransition debounce;

    private FieldVariant lastVariant = null;
    private SearchSize lastSize = null;
    private boolean isFocused = false;
    private ScrollBar verticalScrollBar;
    private String activeQuery = "";
    private boolean loadingMore = false;

    private final ChangeListener<Number> scrollListener = (obs, oldVal, newVal) -> {
        if (newVal.doubleValue() >= model.getLoadMoreThreshold()) {
            runLoadMore();
        }
    };

    private final EventHandler<MouseEvent> outsideClickFilter = this::handleOutsideClick;

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
        setupInlineContent();
        update(model);
    }

    private void setupPopup() {
        resultsListView.getStyleClass().add("search-results");
        resultsListView.setFixedCellSize(ROW_HEIGHT);
        resultsListView.setPrefWidth(Double.MAX_VALUE);

        noResultsLabel.getStyleClass().add("search-no-results");

        popupContent.getStyleClass().add("search-popup-content");
        popupContent.getChildren().addAll(resultsListView, noResultsLabel);

        popup.getContent().add(popupContent);
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setConsumeAutoHidingEvents(false);
        popup.setHideOnEscape(true);
        popup.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (!isShowing) {
                getStyleClass().remove("search-showing");
                popupContent.getStyleClass().remove("search-showing");
            }
        });

        searchField.setFocusTraversable(false);
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickFilter);
            }
        });
    }

    private void setupInlineContent() {
        inlineContent.getStyleClass().add("search-inline-content");
        inlineContent.setVisible(false);
        inlineContent.setManaged(false);
        inlineContent.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(inlineContent, Priority.NEVER);
        getChildren().add(inlineContent);
    }

    private boolean isInlineMode() {
        return model != null && model.getDisplayMode() == SearchDisplayMode.INLINE;
    }

    private void attachResultsHost() {
        VBox target = isInlineMode() ? inlineContent : popupContent;
        if (resultsListView.getParent() != target || noResultsLabel.getParent() != target) {
            target.getChildren().setAll(resultsListView, noResultsLabel);
        }
    }

    private void showPopup() {
        if (isInlineMode()) {
            inlineContent.setVisible(true);
            inlineContent.setManaged(true);
            return;
        }

        if (getScene() == null || getScene().getWindow() == null || !getScene().getWindow().isShowing()) return;
        Bounds fieldBounds = searchBox.localToScreen(searchBox.getBoundsInLocal());
        if (fieldBounds == null || fieldBounds.getWidth() <= 0) return;

        double x = fieldBounds.getMinX();
        double y = fieldBounds.getMaxY();
        double width = fieldBounds.getWidth();

        popupContent.setPrefWidth(width);
        popupContent.setMinWidth(width);
        popupContent.setMaxWidth(width);

        if (!popup.isShowing()) {
            popup.show(searchBox, x, y);
        } else {
            popup.setX(x);
            popup.setY(y);
        }
    }

    private void handleOutsideClick(MouseEvent e) {
        if (!isFocused) return;

        Bounds searchBounds = searchBox.localToScene(searchBox.getBoundsInLocal());
        if (searchBounds != null && searchBounds.contains(e.getSceneX(), e.getSceneY())) {
            return;
        }

        if (getScene() != null && getScene().getRoot() != null) {
            getScene().getRoot().requestFocus();
        }
    }

    private void update(Search<T> model) {
        this.model = model;

        searchField.promptTextProperty().bind(model.getPlaceholder());
        noResultsLabel.textProperty().bind(model.getNoResult());

        debounce = new PauseTransition(Duration.millis(model.getDebounceMs()));
        debounce.setOnFinished(e -> runSearch(searchField.getText()));

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (model.isSearchOnTyping()) {
                debounce.playFromStart();
            }
        });
        searchField.setOnAction(event -> runSearch(searchField.getText()));

        searchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            this.isFocused = isFocused;
            if (!isFocused) {
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

        resultsListView.skinProperty().addListener((obs, oldSkin, newSkin) -> installVerticalScrollListener());
        resultsListView.sceneProperty().addListener((obs, oldScene, newScene) -> installVerticalScrollListener());

        applyVariant(model.getVariant());
        applySize(model.getSize());
        attachResultsHost();
        runSearch(searchField.getText());
    }

    private void runSearch(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        activeQuery = normalizedQuery;

        // IMPORTANT: always run search even if the query is empty, application logic hardly relies on this
        List<Entry<T>> results = model.search(normalizedQuery);

        if (normalizedQuery.isEmpty() && !model.isShowOnEmptyQuery()) {
            resultsListView.getSelectionModel().clearSelection();
            resultsListView.getItems().clear();
            showNothing();
            return;
        }

        resultsListView.getSelectionModel().clearSelection();
        resultsListView.getItems().setAll(results);
        updateListViewHeight();
        installVerticalScrollListener();

        if (!isFocused) return;

        if (results.isEmpty()) {
            showNoResults();
        } else {
            showResults();
        }
    }

    private void runLoadMore() {
        if (!model.canLoadMore() || loadingMore) {
            return;
        }

        loadingMore = true;
        try {
            List<Entry<T>> more = model.loadMore(activeQuery);
            if (!more.isEmpty()) {
                resultsListView.getItems().addAll(more);
                updateListViewHeight();
                if (isFocused) {
                    showResults();
                }
            }
        } finally {
            loadingMore = false;
        }
    }

    private void installVerticalScrollListener() {
        if (!model.canLoadMore()) {
            return;
        }

        ScrollBar newVerticalBar = null;
        for (Node node : resultsListView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == Orientation.VERTICAL) {
                newVerticalBar = scrollBar;
                break;
            }
        }

        if (newVerticalBar == null || newVerticalBar == verticalScrollBar) {
            return;
        }

        if (verticalScrollBar != null) {
            verticalScrollBar.valueProperty().removeListener(scrollListener);
        }

        verticalScrollBar = newVerticalBar;
        verticalScrollBar.valueProperty().addListener(scrollListener);
    }

    private void updateListViewHeight() {
        int max = model.getMaxVisibleResults() > 0
                ? model.getMaxVisibleResults()
                : (model.getMaxResults() > 0 ? model.getMaxResults() : Integer.MAX_VALUE);
        int count = Math.min(resultsListView.getItems().size(), max);
        double height = count * ROW_HEIGHT + 6;
        resultsListView.setPrefHeight(height);
        resultsListView.setMinHeight(height);
        resultsListView.setMaxHeight(height);
    }

    private void applyVariant(FieldVariant variant) {
        if (lastVariant == variant) return;
        if (lastVariant != null) {
            String cls = toStyleClass(lastVariant);
            getStyleClass().remove(cls);
            popupContent.getStyleClass().remove(cls);
        }
        if (variant != null) {
            String cls = toStyleClass(variant);
            getStyleClass().add(cls);
            popupContent.getStyleClass().add(cls);
        }
        lastVariant = variant;
    }

    private static String toStyleClass(FieldVariant variant) {
        return switch (variant) {
            case OUTLINED -> "app-search-variant-outlined";
            case FILLED -> "app-search-variant-filled";
            case GHOST -> "app-search-variant-ghost";
        };
    }

    private void applySize(SearchSize size) {
        if (lastSize == size) return;
        if (lastSize != null) {
            String cls = toStyleClass(lastSize);
            getStyleClass().remove(cls);
            popupContent.getStyleClass().remove(cls);
        }
        SearchSize effective = size == null ? SearchSize.SMALL : size;
        String cls = toStyleClass(effective);
        getStyleClass().add(cls);
        popupContent.getStyleClass().add(cls);
        lastSize = effective;
    }

    private static String toStyleClass(SearchSize size) {
        return switch (size) {
            case SMALL -> "app-search-size-small";
            case MIDDLE -> "app-search-size-middle";
        };
    }

    private void showNothing() {
        getStyleClass().remove("search-showing");
        popupContent.getStyleClass().remove("search-showing");
        inlineContent.setVisible(false);
        inlineContent.setManaged(false);
        popup.hide();
    }

    private void showNoResults() {
        attachResultsHost();
        if (!getStyleClass().contains("search-showing"))
            getStyleClass().add("search-showing");
        if (!popupContent.getStyleClass().contains("search-showing"))
            popupContent.getStyleClass().add("search-showing");
        noResultsLabel.setVisible(true);
        noResultsLabel.setManaged(true);
        noResultsLabel.setPrefWidth(Double.MAX_VALUE);
        resultsListView.setVisible(false);
        resultsListView.setManaged(false);
        showPopup();
    }

    private void showResults() {
        attachResultsHost();
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
}
