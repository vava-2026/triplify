package com.triplify.bootstrap;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.triplify.ui.InjectorHolder;
import com.triplify.ui.MainApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        log.info("Creating Guice injector");
        Injector injector = Guice.createInjector(new BootstrapModule());
        log.info("Guice injector created successfully");
        InjectorHolder.setInjector(injector);
        log.info("Launching UI");
        MainApp.launch(MainApp.class, args);
    }
}
