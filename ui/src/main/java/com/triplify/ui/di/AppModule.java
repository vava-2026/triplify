package com.triplify.ui.di;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;
import an.awesome.pipelinr.Pipelinr;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.triplify.application.category.query.GetAllCategoriesQueryHandler;
import com.triplify.domain.repository.CategoryRepository;
import com.triplify.infrastructure.repository.CategoryRepositoryImpl;

import java.util.Set;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CategoryRepository.class).to(CategoryRepositoryImpl.class);

        @SuppressWarnings("rawtypes")
        Multibinder<Command.Handler> handlerBinder = Multibinder.newSetBinder(binder(), Command.Handler.class);

        handlerBinder.addBinding().to(GetAllCategoriesQueryHandler.class);
    }

    @Provides
    @Singleton
    @SuppressWarnings("rawtypes")
    public Pipeline providePipeline(Set<Command.Handler> handlers) {
        return new Pipelinr().with(handlers::stream);
    }
}