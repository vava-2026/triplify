package com.triplify.ui.map;

import java.util.List;
import java.util.Objects;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polyline;

public class MapRouteHighlightLayer extends MapLayer {

    private List<MapPoint> waypoints = List.of();
    private long waypointsRevision = 0L;
    private long renderedWaypointsRevision = -1L;
    private String renderedViewportKey = "";

    @Override
    protected void initialize() {
        markDirty();
    }

    public void setWaypoints(List<MapPoint> waypoints) {
        this.waypoints = List.copyOf(waypoints);
        waypointsRevision++;
        markDirty();
    }

    public void clear() {
        this.waypoints = List.of();
        waypointsRevision++;
        markDirty();
    }

    @Override
    protected void layoutLayer() {
        String viewportKey = viewportKey();
        if (viewportKey == null) return;
        if (renderedWaypointsRevision == waypointsRevision && Objects.equals(renderedViewportKey, viewportKey)) {
            return;
        }

        getChildren().clear();
        if (waypoints.size() < 2) return;

        Polyline polyline = new Polyline();
        polyline.getStyleClass().add("map-route-polyline");
        polyline.setMouseTransparent(true);

        for (MapPoint wp : waypoints) {
            Point2D pixel = getMapPoint(wp.getLatitude(), wp.getLongitude());
            if (pixel == null) return;
            polyline.getPoints().addAll(pixel.getX(), pixel.getY());
        }

        getChildren().add(polyline);

        renderedWaypointsRevision = waypointsRevision;
        renderedViewportKey = viewportKey;
    }

    private String viewportKey() {
        Point2D anchorA = getMapPoint(0, 0);
        Point2D anchorB = getMapPoint(45, 45);
        if (anchorA == null || anchorB == null) return null;
        return anchorA.getX() + ":" + anchorA.getY() + "|" + anchorB.getX() + ":" + anchorB.getY();
    }
}
