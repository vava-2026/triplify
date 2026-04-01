package com.triplify.infrastructure.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.triplify.domain.repository.CategoryRepository;
import com.triplify.domain.repository.ImageRepository;
import com.triplify.domain.repository.UserRepository;
import com.triplify.domain.service.ImageStorageService;
import com.triplify.domain.service.PasswordEncoder;
import com.triplify.infrastructure.repository.CategoryRepositoryImpl;
import com.triplify.infrastructure.repository.ImageRepositoryImpl;
import com.triplify.infrastructure.repository.UserRepositoryImpl;
import com.triplify.infrastructure.security.BCryptPasswordEncoder;
import com.triplify.infrastructure.storage.DiscImageSaver;

public class InfrastructureModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CategoryRepository.class).to(CategoryRepositoryImpl.class).in(Singleton.class);
        bind(ImageRepository.class).to(ImageRepositoryImpl.class).in(Singleton.class);
        bind(UserRepository.class).to(UserRepositoryImpl.class).in(Singleton.class);
        bind(PasswordEncoder.class).to(BCryptPasswordEncoder.class).in(Singleton.class);
        bind(ImageStorageService.class).to(DiscImageSaver.class).in(Singleton.class);
    }
}

