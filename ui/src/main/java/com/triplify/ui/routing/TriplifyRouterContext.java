package com.triplify.ui.routing;

import rahulstech.jfx.routing.BaseRouterContext;

import java.io.InputStream;
import java.net.URL;

public class TriplifyRouterContext extends BaseRouterContext {

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
}
