package com.triplify.ui.shared.util;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

/**
 * Utility class that simplifies FXML loading with Guice dependency injection.
 *
 * <p>The Guice {@link Injector} is always used as the controller factory, which means:</p>
 * <ul>
 *   <li>{@link #load} – controller is declared in the FXML; Guice creates and injects it.</li>
 *   <li>{@link #loadComponent} – the caller is both root and controller; the explicit
 *       {@code setController} call takes precedence over the factory, so the pre-existing
 *       instance is used, but Guice still resolves any nested {@code @Inject} fields if needed.</li>
 * </ul>
 */
@Singleton
public final class FxmlLoaderHelper {

    private final Injector injector;

    @Inject
    public FxmlLoaderHelper(Injector injector) {
        this.injector = injector;
    }

    /**
     * Loads an FXML file by classpath resource path.
     * The controller is created and injected by Guice.
     *
     * @param resourcePath absolute classpath resource path, e.g.
     *                     {@code "/com/triplify/ui/pages/map/MapView.fxml"}
     * @param <N>          expected root node type
     * @param <C>          expected controller type
     * @return {@link FxmlLoadResult} containing the root node and the controller
     * @throws IllegalStateException if the resource is not found on the classpath
     * @throws RuntimeException      if loading fails
     */
    public <N extends Node, C> FxmlLoadResult<N, C> load(String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        if (url == null) throw new IllegalStateException("FXML not found: " + resourcePath);
        return load(url);
    }

    /**
     * Loads an FXML file from the given {@link URL}.
     * The controller is created and injected by Guice.
     *
     * @param fxmlUrl URL pointing to the FXML resource
     * @param <N>     expected root node type
     * @param <C>     expected controller type
     * @return {@link FxmlLoadResult} containing the root node and the controller
     * @throws RuntimeException if loading fails
     */
    public <N extends Node, C> FxmlLoadResult<N, C> load(URL fxmlUrl) {
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setControllerFactory(injector::getInstance);
        try {
            N node = loader.load();
            C controller = loader.getController();
            return new FxmlLoadResult<>(node, controller);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlUrl, e);
        }
    }

    /**
     * Loads an FXML file for a <em>custom component</em> where {@code rootController}
     * acts as both the root pane and the controller (the self-as-root pattern used by
     * custom controls that extend a layout pane, e.g. {@code HBox}).
     *
     * <p>The explicit {@link FXMLLoader#setController} call takes precedence over the
     * controller factory, so the provided instance is always used as-is.</p>
     *
     * @param resourcePath   absolute classpath resource path
     * @param rootController the component instance used as both root and controller
     * @param <T>            the component type (must extend {@link Node})
     * @throws IllegalStateException if the resource is not found on the classpath
     * @throws RuntimeException      if loading fails
     */
    public <T extends Node> void loadComponent(String resourcePath, T rootController) {
        URL url = rootController.getClass().getResource(resourcePath);
        if (url == null) throw new IllegalStateException("FXML not found: " + resourcePath);
        loadComponent(url, rootController);
    }

    /**
     * Loads an FXML file for a custom component from a {@link URL}.
     *
     * @param fxmlUrl        URL of the FXML resource
     * @param rootController the component instance used as both root and controller
     * @param <T>            the component type (must extend {@link Node})
     * @throws RuntimeException if loading fails
     */
    public <T extends Node> void loadComponent(URL fxmlUrl, T rootController) {
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setRoot(rootController);
        loader.setController(rootController);
        loader.setControllerFactory(injector::getInstance);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load component FXML: " + fxmlUrl, e);
        }
    }
}
