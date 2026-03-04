package com.triplify.application.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.triplify.application.category.usecase.CategoryService;
import com.triplify.application.category.usecase.CategoryServiceImpl;

public class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CategoryService.class).to(CategoryServiceImpl.class).in(Singleton.class);
    }
}

