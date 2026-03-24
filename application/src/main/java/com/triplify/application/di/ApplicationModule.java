package com.triplify.application.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.session.UserSessionContextImpl;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.AuthServiceImpl;
import com.triplify.application.usecase.category.CategoryService;
import com.triplify.application.usecase.category.CategoryServiceImpl;

public class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CategoryService.class).to(CategoryServiceImpl.class).in(Singleton.class);
        bind(AuthService.class).to(AuthServiceImpl.class).in(Singleton.class);
        bind(UserSessionContext.class).to(UserSessionContextImpl.class).in(Singleton.class);
    }
}
