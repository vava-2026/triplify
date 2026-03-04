package com.triplify.ui.routing;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import rahulstech.jfx.routing.BaseRouterContext;

import java.io.InputStream;
import java.net.URL;

public class TriplifyRouterContext extends BaseRouterContext {

    private final BooleanProperty fullScreenContent = new SimpleBooleanProperty(false);

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
        if (name == null || name.isEmpty()) {
            return null;
        }
        return name.startsWith("/") ? name : "/" + name;
    }

    public BooleanProperty fullScreenContentProperty() { return fullScreenContent; }
    public boolean isFullScreenContent() { return fullScreenContent.get(); }
    public void setFullScreenContent(boolean value) { fullScreenContent.set(value); }
}
