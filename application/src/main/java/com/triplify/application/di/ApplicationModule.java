package com.triplify.application.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.Provider;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.AuthServiceImpl;
import com.triplify.application.usecase.badge.BadgeService;
import com.triplify.application.usecase.badge.BadgeServiceImpl;
import com.triplify.application.usecase.badgegroup.BadgeGroupService;
import com.triplify.application.usecase.badgegroup.BadgeGroupServiceImpl;
import com.triplify.application.usecase.category.CategoryService;
import com.triplify.application.usecase.category.CategoryServiceImpl;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.country.CountryServiceImpl;
import com.triplify.application.usecase.emotion.EmotionService;
import com.triplify.application.usecase.emotion.EmotionServiceImpl;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.PlaceServiceImpl;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.RouteServiceImpl;
import com.triplify.application.usecase.tag.TagService;
import com.triplify.application.usecase.tag.TagServiceImpl;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.TripServiceImpl;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.session.UserSessionContextImpl;
import com.triplify.application.validation.ValidatingProxy;

public class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserSessionContext.class).to(UserSessionContextImpl.class).in(Singleton.class);
        bindValidated(BadgeService.class, BadgeServiceImpl.class);
        bindValidated(BadgeGroupService.class, BadgeGroupServiceImpl.class);
        bindValidated(CategoryService.class, CategoryServiceImpl.class);
        bindValidated(AuthService.class, AuthServiceImpl.class);
        bindValidated(CountryService.class, CountryServiceImpl.class);
        bindValidated(EmotionService.class, EmotionServiceImpl.class);
        bindValidated(PlaceService.class, PlaceServiceImpl.class);
        bindValidated(RouteService.class, RouteServiceImpl.class);
        bindValidated(TagService.class, TagServiceImpl.class);
        bindValidated(TripService.class, TripServiceImpl.class);
    }

    private <T> void bindValidated(Class<T> iface, Class<? extends T> impl) {
        Provider<? extends T> implProvider = getProvider(impl);
        bind(iface).toProvider(() -> ValidatingProxy.wrap(implProvider.get(), iface)).in(Singleton.class);
    }
}
