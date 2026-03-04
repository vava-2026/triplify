package com.triplify.ui.di;

import com.google.inject.AbstractModule;
import com.triplify.application.di.ApplicationModule;
import com.triplify.infrastructure.di.InfrastructureModule;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new InfrastructureModule());
        install(new ApplicationModule());
    }
}

