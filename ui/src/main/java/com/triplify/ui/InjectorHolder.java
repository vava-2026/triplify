package com.triplify.ui;

import com.google.inject.Injector;

public final class InjectorHolder {

    private static volatile Injector injector;

    private InjectorHolder() {}

    public static void setInjector(Injector injector) {
        InjectorHolder.injector = injector;
    }

    public static Injector getInjector() {
        if (injector == null) {
            throw new IllegalStateException("Injector has not been set. Make sure Launcher.main() is used as the entry point");
        }
        return injector;
    }
}
