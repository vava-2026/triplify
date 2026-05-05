package com.triplify.ui.map;

import com.gluonhq.maps.tile.TileRetriever;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class VoyagerTileRetriever implements TileRetriever {

    private static final String VOYAGER_URL =
            "https://basemaps.cartocdn.com/rastertiles/voyager/%d/%d/%d.png";
    private static final String OSM_FALLBACK_URL =
            "https://tile.openstreetmap.org/%d/%d/%d.png";

    private final Map<String, CompletableFuture<Image>> cache = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Image> loadTile(int zoom, long i, long j) {
        String cacheKey = zoom + ":" + i + ":" + j;
        return cache.computeIfAbsent(cacheKey, ignored ->
                CompletableFuture.supplyAsync(() -> loadPreferredTile(zoom, i, j))
        );
    }

    private Image loadPreferredTile(int zoom, long i, long j) {
        try {
            return new Image(VOYAGER_URL.formatted(zoom, i, j), false);
        } catch (RuntimeException ex) {
            return new Image(OSM_FALLBACK_URL.formatted(zoom, i, j), false);
        }
    }
}
