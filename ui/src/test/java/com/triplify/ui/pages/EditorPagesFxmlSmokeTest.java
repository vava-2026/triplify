package com.triplify.ui.pages;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EditorPagesFxmlSmokeTest {

    private static final AtomicBoolean TOOLKIT_STARTED = new AtomicBoolean(false);

    @BeforeAll
    static void initToolkit() throws InterruptedException {
        if (TOOLKIT_STARTED.compareAndSet(false, true)) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX toolkit failed to start");
            }
        }
    }

    @Test
    void addTripViewLoads() throws Exception {
        assertLoads("/com/triplify/ui/pages/trips/AddTripView.fxml");
    }

    @Test
    void addRouteViewLoads() throws Exception {
        assertLoads("/com/triplify/ui/pages/routes/AddRouteView.fxml");
    }

    @Test
    void routeDetailsViewLoads() throws Exception {
        assertLoads("/com/triplify/ui/pages/routes/RouteDetailsView.fxml");
    }

    @Test
    void addPlaceViewLoads() throws Exception {
        assertLoads("/com/triplify/ui/pages/places/AddPlaceView.fxml");
    }

    private void assertLoads(String resourcePath) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean loaded = new AtomicBoolean(false);
        AtomicBoolean failed = new AtomicBoolean(false);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
                Object node = loader.load();
                assertNotNull(node, resourcePath + " should load");
                loaded.set(true);
            } catch (Throwable throwable) {
                failed.set(true);
                error[0] = throwable;
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out while loading " + resourcePath);
        }
        if (failed.get()) {
            throw new RuntimeException("Failed to load " + resourcePath, error[0]);
        }
        if (!loaded.get()) {
            throw new IllegalStateException(resourcePath + " was not loaded");
        }
    }
}
