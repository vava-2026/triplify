package com.triplify.domain.error;

public sealed interface TripPlaceError extends DomainError permits TripPlaceError.NotFound, TripPlaceError.InvalidStatusTransition {

    record NotFound(String tripPlaceId) implements TripPlaceError {
        @Override
        public String code() {
            return "error.trip.place.not.found";
        }

        @Override
        public String message() {
            return "Trip place '%s' not found".formatted(tripPlaceId);
        }
    }

    record InvalidStatusTransition(String from, String to) implements TripPlaceError {
        @Override
        public String code() {
            return "error.trip.place.invalid.status.transition";
        }

        @Override
        public String message() {
            return "Cannot transition trip place from '%s' to '%s'".formatted(from, to);
        }
    }
}

