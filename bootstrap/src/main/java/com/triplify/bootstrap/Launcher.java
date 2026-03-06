package com.triplify.bootstrap;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.triplify.ui.InjectorHolder;
import com.triplify.ui.MainApp;

public class Launcher {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new BootstrapModule());
        InjectorHolder.setInjector(injector);
        MainApp.launch(MainApp.class, args);
    }
}
