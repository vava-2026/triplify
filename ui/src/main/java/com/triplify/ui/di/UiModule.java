package com.triplify.ui.di;

import com.google.inject.AbstractModule;
import com.triplify.ui.shared.util.FxmlLoaderHelper;

public class UiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FxmlLoaderHelper.class).asEagerSingleton();
    }
}

