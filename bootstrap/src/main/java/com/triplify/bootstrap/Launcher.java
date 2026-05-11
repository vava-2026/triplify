package com.triplify.bootstrap;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.triplify.infrastructure.repository.persistence.DatabaseMigrationInitializer;
import com.triplify.ui.MainApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    static void main(String[] args) {
        log.info("Creating Guice injector");
        Injector injector = Guice.createInjector(new BootstrapModule());
        log.info("Guice injector created successfully");
        injector.getInstance(DatabaseMigrationInitializer.class).initialize();
        log.info("Launching UI");
        MainApp.launch(injector, args);
    }
}