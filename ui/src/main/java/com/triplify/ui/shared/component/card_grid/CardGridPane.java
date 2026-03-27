package com.triplify.ui.shared.component.card_grid;

import com.triplify.application.pagination.Pagination;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Reusable responsive card grid with automatic scroll-based pagination.
 *
 * @param <T> the item type
 */
public class CardGridPane<T> extends VBox {

    @FunctionalInterface
    public interface PageLoader<T> {
        PageResult<T> load(int page, int pageSize);
    }

    public record PageResult<T>(List<T> items, Pagination pagination) {}

    private final ScrollPane scrollPane;
    private final GridPane grid;
    private final StackPane emptyPane;
    private final Label emptyLabel;

    private PageLoader<T> pageLoader;
    private Function<T, Node> cardFactory;
    private final List<Node> pinnedNodes = new ArrayList<>();

    private int page = 1;
    private int pageSize = 12;
    private boolean loading = false;
    private boolean hasMore = true;
    private int currentColumns = 0;
    private double lastViewportWidth = 0;

    private double gap = 16;
    private double minCardWidth = 200;
    private int maxColumns = 5;
    private String emptyText = "Nothing found";

    public CardGridPane() {
        getStyleClass().add("card-grid-root");

        grid = new GridPane();
        grid.getStyleClass().add("card-grid");
        grid.setHgap(gap);
        grid.setVgap(gap);
        grid.setPadding(new Insets(4, 2, 10, 2));

        emptyLabel = new Label(emptyText);
        emptyLabel.getStyleClass().addAll("card-grid-empty", "page-subtitle");
        emptyPane = new StackPane(emptyLabel);
        emptyPane.setAlignment(Pos.CENTER);
        emptyPane.setMinHeight(120);
        emptyPane.setVisible(false);
        emptyPane.setManaged(false);

        scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.getStyleClass().add("card-grid-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(emptyPane, scrollPane);

        scrollPane.vvalueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.doubleValue() >= 0.85) {
                loadNextPage();
            }
        });

        scrollPane.viewportBoundsProperty().addListener((obs, oldB, newB) -> {
            if (newB != null && newB.getWidth() > 0) {
                lastViewportWidth = newB.getWidth();
                int cols = computeColumns(newB.getWidth());
                if (cols != currentColumns) {
                    currentColumns = cols;
                }
                relayout();
            }
        });
    }

    // ── Configuration ──

    public void setPageLoader(PageLoader<T> pageLoader) {
        this.pageLoader = pageLoader;
    }

    public void setCardFactory(Function<T, Node> cardFactory) {
        this.cardFactory = cardFactory;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(1, pageSize);
    }

    public void setGap(double gap) {
        this.gap = gap;
        grid.setHgap(gap);
        grid.setVgap(gap);
    }

    public void setMinCardWidth(double minCardWidth) {
        this.minCardWidth = minCardWidth;
    }

    public void setMaxColumns(int maxColumns) {
        this.maxColumns = Math.max(1, maxColumns);
    }

    public void setEmptyText(String text) {
        this.emptyText = text;
        emptyLabel.setText(text);
    }

    public void addPinnedNode(Node node) {
        pinnedNodes.add(node);
    }

    public void clearPinnedNodes() {
        pinnedNodes.clear();
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    // ── Data ──

    private final List<Node> cardNodes = new ArrayList<>();

    public void refresh() {
        page = 1;
        hasMore = true;
        loading = false;
        cardNodes.clear();
        scrollPane.setVvalue(0);
        showEmpty(false);
        loadNextPage();
    }

    private void loadNextPage() {
        if (loading || !hasMore || pageLoader == null || cardFactory == null) return;
        loading = true;

        PageResult<T> result = pageLoader.load(page, pageSize);
        List<T> items = result.items();

        if (items == null || items.isEmpty()) {
            hasMore = false;
            if (page == 1 && cardNodes.isEmpty() && pinnedNodes.isEmpty()) {
                showEmpty(true);
            }
            loading = false;
            return;
        }

        for (T item : items) {
            Node card = cardFactory.apply(item);
            cardNodes.add(card);
        }

        updatePagination(result.pagination());
        relayout();
        loading = false;
        ensureScrollable();
    }

    private void updatePagination(Pagination pagination) {
        if (pagination == null) {
            hasMore = false;
            return;
        }
        int totalPages = pagination.totalPages() == null ? 1 : pagination.totalPages();
        hasMore = pagination.page() < totalPages;
        page = pagination.page() + 1;
    }

    private void ensureScrollable() {
        Platform.runLater(() -> {
            if (loading || !hasMore) return;
            double viewportH = scrollPane.getViewportBounds().getHeight();
            if (viewportH <= 0) return;
            double contentH = grid.getBoundsInLocal().getHeight();
            if (contentH <= viewportH + 1) {
                loadNextPage();
            }
        });
    }

    // ── Layout ──

    private int computeColumns(double availableWidth) {
        if (availableWidth <= 0) return 1;
        double usable = availableWidth - grid.getPadding().getLeft() - grid.getPadding().getRight();
        int cols = Math.max(1, (int) ((usable + gap) / (minCardWidth + gap)));
        return Math.min(cols, maxColumns);
    }

    private void relayout() {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();

        int cols = currentColumns > 0 ? currentColumns : 1;

        double padH = grid.getPadding().getLeft() + grid.getPadding().getRight();
        double usable = lastViewportWidth - padH - (cols - 1) * gap;
        double colWidth = Math.max(minCardWidth, usable / cols);

        for (int i = 0; i < cols; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(colWidth);
            cc.setPrefWidth(colWidth);
            cc.setMaxWidth(colWidth);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);
        }

        List<Node> all = new ArrayList<>(pinnedNodes.size() + cardNodes.size());
        all.addAll(pinnedNodes);
        all.addAll(cardNodes);

        int row = 0;
        int col = 0;
        for (Node node : all) {
            GridPane.setFillWidth(node, true);
            grid.add(node, col, row);
            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }
    }

    private void showEmpty(boolean show) {
        emptyPane.setVisible(show);
        emptyPane.setManaged(show);
        scrollPane.setVisible(!show);
        scrollPane.setManaged(!show);
    }
}
