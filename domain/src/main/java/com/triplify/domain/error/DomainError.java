package com.triplify.domain.error;

public sealed interface DomainError extends AppError permits
        ValidationError,
        AuthError,
        UserError,
        CountryError,
        CategoryError,
        TagError,
        EmotionError,
        BadgeGroupError,
        BadgeError,
        PlaceError,
        TripError,
        RouteError,
        TripPlaceError,
        TripRouteError,
        StoryError,
        ImageError {
}
