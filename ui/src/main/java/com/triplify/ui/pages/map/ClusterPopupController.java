package com.triplify.ui.pages.map;

import com.google.inject.Inject;
import com.triplify.application.usecase.map.MapService;
import com.triplify.application.usecase.map.dto.GetClusterItemsRequest;
import com.triplify.application.usecase.map.dto.MapObjectResponse;
import com.triplify.domain.map.MapObjectType;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.util.Localization;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ClusterPopupController extends StackPane {

    private static final int PAGE_SIZE = 20;

    private final MapService mapService;

    private final VBox itemsContainer = new VBox();
    private final Button loadMoreButton = new Button();
    private final Label titleLabel = new Label();

    private MapObjectResponse currentCluster;
    private double currentZoomLevel;
    private Set<MapObjectType> currentFilter;
    private Consumer<MapObjectResponse> onItemClick;
    private int currentPage = 0;

    @Inject
    public ClusterPopupController(MapService mapService) {
        this.mapService = mapService;
        buildUi();
    }

    private void buildUi() {
        getStyleClass().add("map-cluster-popup");
        setVisible(false);
        setManaged(false);
        StackPane.setAlignment(this, Pos.CENTER_RIGHT);
        StackPane.setMargin(this, new Insets(0, 12, 0, 0));

        Button closeBtn = new Button();
        closeBtn.getStyleClass().addAll("app-btn", "app-btn-ghost", "map-cluster-popup-close");
        FontIcon closeIcon = new FontIcon("fth-x");
        closeIcon.setIconSize(14);
        closeBtn.setGraphic(closeIcon);
        closeBtn.setOnAction(e -> hide());

        titleLabel.getStyleClass().add("map-cluster-popup-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        HBox header = new HBox(8, titleLabel, closeBtn);
        header.getStyleClass().add("map-cluster-popup-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 12, 8, 16));

        itemsContainer.getStyleClass().add("map-cluster-popup-items");
        itemsContainer.setSpacing(0);

        loadMoreButton.getStyleClass().addAll("app-btn", "app-btn-ghost", "map-cluster-popup-load-more");
        Localization.bindText(loadMoreButton.textProperty(), "map.cluster.load_more");
        loadMoreButton.setMaxWidth(Double.MAX_VALUE);
        loadMoreButton.setVisible(false);
        loadMoreButton.setManaged(false);
        loadMoreButton.setOnAction(e -> loadPage(currentPage));

        VBox itemsWithMore = new VBox(itemsContainer, loadMoreButton);
        itemsWithMore.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(itemsWithMore);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("map-cluster-popup-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(header, scroll);
        root.getStyleClass().add("map-cluster-popup-root");
        root.setMaxWidth(320);
        root.setMaxHeight(420);
        getChildren().add(root);
    }

    public void show(MapObjectResponse cluster, double zoomLevel, Set<MapObjectType> filter, Consumer<MapObjectResponse> onItemClick) {
        this.currentCluster = cluster;
        this.currentZoomLevel = zoomLevel;
        this.currentFilter = filter;
        this.onItemClick = onItemClick;
        this.currentPage = 0;

        titleLabel.setText(MessageFormat.format(I18n.t("map.cluster.title"), cluster.count()));
        itemsContainer.getChildren().clear();
        loadMoreButton.setVisible(false);
        loadMoreButton.setManaged(false);

        setVisible(true);
        setManaged(true);

        loadPage(0);
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
        itemsContainer.getChildren().clear();
    }

    private void loadPage(int page) {
        GetClusterItemsRequest request = new GetClusterItemsRequest(
                currentCluster.latitude(),
                currentCluster.longitude(),
                currentZoomLevel,
                currentFilter,
                new PageRequest(page, PAGE_SIZE)
        );

        CompletableFuture.supplyAsync(() -> mapService.getClusterItems(request))
                .thenAccept(result -> Platform.runLater(() -> result.onSuccess(this::appendPage)));
    }

    private void appendPage(Page<MapObjectResponse> page) {
        appendItems(page.items());
        currentPage = page.page() + 1;

        boolean hasMore = page.hasNext();
        loadMoreButton.setVisible(hasMore);
        loadMoreButton.setManaged(hasMore);
    }

    private void appendItems(List<MapObjectResponse> items) {
        for (MapObjectResponse item : items) {
            HBox row = buildItemRow(item);
            itemsContainer.getChildren().add(row);
        }
    }

    private HBox buildItemRow(MapObjectResponse item) {
        FontIcon icon = new FontIcon(iconForType(item.objectType()));
        icon.setIconSize(16);
        icon.getStyleClass().add("map-cluster-popup-item-icon");

        Label titleLabel = new Label(item.title());
        titleLabel.getStyleClass().add("map-cluster-popup-item-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        HBox row = new HBox(10, icon, titleLabel);
        row.getStyleClass().add("map-cluster-popup-item");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setOnMouseClicked(e -> {
            if (onItemClick != null) onItemClick.accept(item);
        });

        return row;
    }

    private String iconForType(MapObjectType type) {
        if (type == null) return "fth-map-pin";
        return switch (type) {
            case ROUTE -> "fth-navigation";
            case STORY -> "fth-book-open";
            default    -> "fth-map-pin";
        };
    }
}
