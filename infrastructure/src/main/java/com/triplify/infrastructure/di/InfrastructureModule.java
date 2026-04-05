package com.triplify.infrastructure.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.triplify.domain.repository.*;
import com.triplify.domain.service.ImageStorageService;
import com.triplify.domain.service.PasswordEncoder;
import com.triplify.infrastructure.repository.*;
import com.triplify.infrastructure.security.BCryptPasswordEncoder;
import com.triplify.infrastructure.storage.DiscImageStorage;

public class InfrastructureModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CategoryRepository.class).to(CategoryRepositoryImpl.class).in(Singleton.class);
        bind(UserRepository.class).to(UserRepositoryImpl.class).in(Singleton.class);
        bind(ImageRepository.class).to(ImageRepositoryImpl.class).in(Singleton.class);
        bind(CountryRepository.class).to(CountryRepositoryImpl.class).in(Singleton.class);
        bind(PasswordEncoder.class).to(BCryptPasswordEncoder.class).in(Singleton.class);
        bind(ImageStorageService.class).to(DiscImageStorage.class).in(Singleton.class);
        bind(PlaceRepository.class).to(PlaceRepositoryImpl.class).in(Singleton.class);
        bind(TagRepository.class).to(TagRepositoryImpl.class).in(Singleton.class);
        bind(RouteRepository.class).to(RouteRepositoryImpl.class).in(Singleton.class);
        bind(RoutePlaceRepository.class).to(RoutePlaceRepositoryImpl.class).in(Singleton.class);
        bind(TripRepository.class).to(TripRepositoryImpl.class).in(Singleton.class);
        bind(TripRouteRepository.class).to(TripRouteRepositoryImpl.class).in(Singleton.class);
        bind(TripPlaceRepository.class).to(TripPlaceRepositoryImpl.class).in(Singleton.class);
    }
}

