package com.triplify.ui.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.kordamp.ikonli.javafx.FontIcon;

import com.gluonhq.maps.MapLayer;
import com.triplify.application.usecase.map.dto.MapObjectResponse;
import com.triplify.domain.map.MapObjectType;
import com.triplify.ui.shared.util.EditorUtils;

import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Setter;

public class MapMarkersLayer extends MapLayer {

    private static final int MARKER_SIZE = 36;
    private static final int MARKER_INNER_SIZE = 30;
    private static final int CLUSTER_SIZE = 40;
    private static final int CLUSTER_LARGE_SIZE = 52;
    private static final int CLUSTER_LARGE_THRESHOLD = 10;
    private static final int PLACE_NODE_WIDTH = 160;
    private static final int PLACE_LABEL_HEIGHT = 18;
    private static final int PLACE_LABEL_MAX_CHARS = 25;
    private static final int CARD_WIDTH = 180;
    private static final int CARD_IMAGE_HEIGHT = 90;
    private static final int CARD_TITLE_HEIGHT = 38;
    private static final String FALLBACK_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final BackgroundSize FILL = new BackgroundSize(1, 1, true, true, false, true);

    private List<MapObjectResponse> markers = List.of();
    private final Map<String, Image> imageCache = new HashMap<>();
    private long markersRevision = 0;
    private long renderedRevision = -1;
    private String renderedViewport = "";

    private final StackPane hoverCard;
    private Region hoverCardImage;
    private Label hoverCardLabel;

    private final PauseTransition hideDelay = new PauseTransition(Duration.millis(100));

    @Setter private Consumer<MapObjectResponse> onMarkerClick = ignore -> {};
    @Setter private Consumer<MapObjectResponse> onClusterClick = ignore -> {};
    @Setter private Consumer<String> onRouteHover = ignore -> {};

    public MapMarkersLayer() {
        hoverCard = buildHoverCard();
        hideDelay.setOnFinished(e -> hoverCard.setVisible(false));
    }

    public void setMarkers(List<MapObjectResponse> markers) {
        this.markers = new ArrayList<>(markers);
        markersRevision++;
        markDirty();
    }

    @Override
    protected void initialize() {
        getChildren().add(hoverCard);
        markDirty();
    }

    @Override
    protected void layoutLayer() {
        String viewport = viewportKey();
        if (viewport != null
                && renderedRevision == markersRevision
                && Objects.equals(renderedViewport, viewport)) {
            return;
        }

        getChildren().removeIf(node -> node != hoverCard);
        hideHoverCard();

        for (MapObjectResponse marker : markers) {
            Point2D point = getMapPoint(marker.latitude(), marker.longitude());
            if (point == null) continue;

            Region node = buildNode(marker, point);
            node.setManaged(false);
            node.applyCss();

            if (marker.count() <= 1 && marker.objectType() == MapObjectType.PLACE) {
                node.resize(PLACE_NODE_WIDTH, MARKER_SIZE + PLACE_LABEL_HEIGHT);
                node.relocate(point.getX() - PLACE_NODE_WIDTH / 2.0, point.getY() - MARKER_SIZE / 2.0);
            } else {
                double size = nodeSize(marker);
                node.resize(size, size);
                node.relocate(point.getX() - size / 2.0, point.getY() - size / 2.0);
            }

            getChildren().add(node);
        }

        hoverCard.toFront();
        renderedRevision = markersRevision;
        renderedViewport = viewport;
    }

    private Region buildNode(MapObjectResponse marker, Point2D point) {
        if (marker.count() > 1) return buildClusterNode(marker);
        if (marker.objectType() == MapObjectType.PLACE) return buildPlaceNode(marker, point);
        return buildMarkerNode(marker);
    }

    private String viewportKey() {
        Point2D a = getMapPoint(0, 0);
        Point2D b = getMapPoint(45, 45);
        if (a == null || b == null) return null;
        return a.getX() + ":" + a.getY() + "|" + b.getX() + ":" + b.getY();
    }

    private VBox buildPlaceNode(MapObjectResponse marker, Point2D point) {
        Circle bg = new Circle(MARKER_SIZE / 2.0);
        bg.setFill(colorFor(MapObjectType.PLACE));

        FontIcon icon = new FontIcon("fth-map-pin");
        icon.setIconSize(15);
        icon.getStyleClass().add("map-place-marker-icon");
        icon.setMouseTransparent(true);

        StackPane pin = new StackPane(bg, icon);
        pin.getStyleClass().addAll("map-marker", "map-marker-place");
        pin.setMinSize(MARKER_SIZE, MARKER_SIZE);
        pin.setMaxSize(MARKER_SIZE, MARKER_SIZE);

        Label label = new Label(truncate(marker.title(), PLACE_LABEL_MAX_CHARS));
        label.getStyleClass().add("map-place-title");
        label.setMouseTransparent(true);

        VBox container = new VBox(2, pin, label);
        container.setAlignment(Pos.TOP_CENTER);
        container.setBackground(Background.EMPTY);
        container.setPickOnBounds(false);

        container.setOnMouseEntered(e -> showHoverCard(marker, point));
        container.setOnMouseExited(e -> hideDelay.playFromStart());
        container.setOnMouseClicked(e -> {
            onMarkerClick.accept(marker);
            e.consume();
        });

        return container;
    }

    private StackPane buildMarkerNode(MapObjectResponse marker) {
        Circle bg = new Circle(MARKER_SIZE / 2.0);
        bg.setFill(colorFor(marker.objectType()));

        StackPane pane = new StackPane(bg);
        pane.getStyleClass().addAll("map-marker", styleClassFor(marker.objectType()));

        if (marker.coverImage() != null && marker.coverImage().url() != null) {
            pane.getChildren().add(buildCircularImage(marker.coverImage().url().toString()));
        } else {
            Circle inner = new Circle(MARKER_INNER_SIZE / 2.0);
            inner.setFill(Color.web("#ffffff", 0.9));
            inner.setMouseTransparent(true);
            pane.getChildren().add(inner);
        }

        pane.setOnMouseClicked(e -> { onMarkerClick.accept(marker); e.consume(); });

        if (marker.objectType() == MapObjectType.ROUTE) {
            pane.setOnMouseEntered(e -> onRouteHover.accept(marker.id()));
            pane.setOnMouseExited(e -> onRouteHover.accept(null));
        }

        return pane;
    }

    private StackPane buildCircularImage(String imagePath) {
        Image image = imageCache.computeIfAbsent(
                imagePath, path -> EditorUtils.loadImage(path, FALLBACK_IMAGE, getClass()));

        StackPane pane = new StackPane();
        pane.setMinSize(MARKER_INNER_SIZE, MARKER_INNER_SIZE);
        pane.setMaxSize(MARKER_INNER_SIZE, MARKER_INNER_SIZE);
        pane.setMouseTransparent(true);
        pane.setBackground(new Background(new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, FILL)));

        Circle clip = new Circle(MARKER_INNER_SIZE / 2.0, MARKER_INNER_SIZE / 2.0, MARKER_INNER_SIZE / 2.0);
        pane.setClip(clip);
        return pane;
    }

    private StackPane buildClusterNode(MapObjectResponse cluster) {
        boolean large = cluster.count() >= CLUSTER_LARGE_THRESHOLD;
        String text = cluster.count() >= 1000 ? "999+" : String.valueOf(cluster.count());

        Label label = new Label(text);
        label.getStyleClass().add("map-cluster-label");

        StackPane pane = new StackPane(label);
        pane.getStyleClass().addAll("map-cluster", large ? "map-cluster-large" : "map-cluster-small");
        pane.setOnMouseClicked(e -> { onClusterClick.accept(cluster); e.consume(); });
        return pane;
    }

    private StackPane buildHoverCard() {
        Region imageSlot = new Region();
        imageSlot.getStyleClass().add("map-place-card-image");
        imageSlot.setPrefSize(CARD_WIDTH, CARD_IMAGE_HEIGHT);
        imageSlot.setMinSize(CARD_WIDTH, CARD_IMAGE_HEIGHT);
        imageSlot.setMaxSize(CARD_WIDTH, CARD_IMAGE_HEIGHT);
        hoverCardImage = imageSlot;

        Label title = new Label();
        title.getStyleClass().add("map-place-card-title");
        title.setMaxWidth(CARD_WIDTH - 20);
        title.setWrapText(false);
        hoverCardLabel = title;

        VBox content = new VBox(0, imageSlot, title);
        content.getStyleClass().add("map-place-card");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(content.widthProperty());
        clip.heightProperty().bind(content.heightProperty());
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        content.setClip(clip);

        StackPane card = new StackPane(content);
        card.setManaged(false);
        card.setVisible(false);
        card.setMouseTransparent(true);
        card.setPickOnBounds(false);
        return card;
    }

    private void showHoverCard(MapObjectResponse marker, Point2D point) {
        hideDelay.stop();

        boolean hasImage = marker.coverImage() != null && marker.coverImage().url() != null;
        if (hasImage) {
            Image image = imageCache.computeIfAbsent(
                    marker.coverImage().url().toString(),
                    path -> EditorUtils.loadImage(path, FALLBACK_IMAGE, getClass()));
            hoverCardImage.setBackground(new Background(new BackgroundImage(
                    image,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, FILL)));
        }
        hoverCardImage.setVisible(hasImage);
        hoverCardImage.setManaged(hasImage);
        hoverCardLabel.setText(marker.title() != null ? marker.title() : "");

        int cardHeight = hasImage ? CARD_IMAGE_HEIGHT + CARD_TITLE_HEIGHT : CARD_TITLE_HEIGHT;
        hoverCard.applyCss();
        hoverCard.resize(CARD_WIDTH, cardHeight);
        hoverCard.relocate(
                point.getX() - CARD_WIDTH / 2.0,
                point.getY() - MARKER_SIZE / 2.0 - cardHeight - 8);
        hoverCard.setVisible(true);
    }

    private void hideHoverCard() {
        hideDelay.stop();
        hoverCard.setVisible(false);
    }

    private double nodeSize(MapObjectResponse marker) {
        if (marker.count() > 1) {
            return marker.count() >= CLUSTER_LARGE_THRESHOLD ? CLUSTER_LARGE_SIZE : CLUSTER_SIZE;
        }
        return MARKER_SIZE;
    }

    private String styleClassFor(MapObjectType type) {
        if (type == null) return "map-marker-place";
        return switch (type) {
            case ROUTE -> "map-marker-route";
            case STORY -> "map-marker-story";
            default    -> "map-marker-place";
        };
    }

    private Color colorFor(MapObjectType type) {
        if (type == null) return Color.web("#2f6690");
        return switch (type) {
            case ROUTE -> Color.web("#2a9d8f");
            case STORY -> Color.web("#8338ec");
            default    -> Color.web("#2f6690");
        };
    }

    private static String truncate(String text, int max) {
        if (text == null || text.length() <= max) return text != null ? text : "";
        return text.substring(0, max - 1) + "…";
    }
}
