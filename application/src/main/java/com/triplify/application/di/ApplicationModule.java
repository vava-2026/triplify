package com.triplify.application.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.Provider;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.AuthServiceImpl;
import com.triplify.application.usecase.category.CategoryService;
import com.triplify.application.usecase.category.CategoryServiceImpl;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.PlaceServiceImpl;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.session.UserSessionContextImpl;
import com.triplify.application.validation.ValidatingProxy;

public class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserSessionContext.class).to(UserSessionContextImpl.class).in(Singleton.class);
        bindValidated(CategoryService.class, CategoryServiceImpl.class);
        bindValidated(AuthService.class, AuthServiceImpl.class);
        bindValidated(PlaceService.class, PlaceServiceImpl.class);
    }

    private <T> void bindValidated(Class<T> iface, Class<? extends T> impl) {
        Provider<? extends T> implProvider = getProvider(impl);
        bind(iface).toProvider(() -> ValidatingProxy.wrap(implProvider.get(), iface)).in(Singleton.class);
    }
}
