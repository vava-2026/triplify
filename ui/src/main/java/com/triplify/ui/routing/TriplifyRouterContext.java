package com.triplify.ui.routing;

import com.google.inject.Injector;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import rahulstech.jfx.routing.BaseRouterContext;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;

public class TriplifyRouterContext extends BaseRouterContext {

    private final BooleanProperty fullScreenContent = new SimpleBooleanProperty(false);
    private final Injector injector;

    public TriplifyRouterContext(Injector injector) {
        this.injector = injector;
    }

    @Override
    public FXMLLoader getFxmlLoader(String name, Class<?> controllerClass, ResourceBundle resources, Charset charset) {
        URL url = getResource(name);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(url);
        if (resources != null) loader.setResources(resources);
        if (charset != null) loader.setCharset(charset);
        loader.setControllerFactory(injector::getInstance);
        return loader;
    }

    @Override
    public URL getResource(String name, String type) {
        String path = normalize(name);
        return path == null ? null : TriplifyRouterContext.class.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String name, String type) {
        String path = normalize(name);
        return path == null ? null : TriplifyRouterContext.class.getResourceAsStream(path);
    }

    private String normalize(String name) {
        if (name == null || name.isEmpty()){
            return null;
        }
        return name.startsWith("/") ? name : "/" + name;
    }

    public BooleanProperty fullScreenContentProperty() {
        return fullScreenContent;
    }

    public boolean isFullScreenContent() {
        return fullScreenContent.get();
    }

    public void setFullScreenContent(boolean value) {
        fullScreenContent.set(value);
    }
}
