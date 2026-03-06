package com.triplify.bootstrap;

import com.google.inject.AbstractModule;
import com.triplify.application.di.ApplicationModule;
import com.triplify.infrastructure.di.InfrastructureModule;

public class BootstrapModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new InfrastructureModule());
        install(new ApplicationModule());
    }
}
