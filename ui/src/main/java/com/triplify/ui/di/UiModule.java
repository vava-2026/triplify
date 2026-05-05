package com.triplify.ui.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.toast.ToastServiceImpl;
import com.triplify.ui.shared.util.FxmlLoaderHelper;

public class UiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FxmlLoaderHelper.class).asEagerSingleton();
        bind(ToastService.class).to(ToastServiceImpl.class).in(Singleton.class);
        bind(ErrorHandler.class).in(Singleton.class);
    }
}
