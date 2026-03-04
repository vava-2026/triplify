package com.triplify.infrastructure.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.triplify.domain.repository.CategoryRepository;
import com.triplify.infrastructure.repository.CategoryRepositoryImpl;

public class InfrastructureModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CategoryRepository.class).to(CategoryRepositoryImpl.class).in(Singleton.class);
    }
}

