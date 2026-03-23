package com.triplify.domain.error;

public sealed interface TripPlaceError extends DomainError permits TripPlaceError.NotFound, TripPlaceError.InvalidStatusTransition {

    record NotFound(String tripPlaceId) implements TripPlaceError {
        @Override
        public String code() {
            return "TRIP_PLACE_NOT_FOUND";
        }

        @Override
        public String message() {
            return "Trip place '%s' not found".formatted(tripPlaceId);
        }
    }

    record InvalidStatusTransition(String from, String to) implements TripPlaceError {
        @Override
        public String code() {
            return "TRIP_PLACE_INVALID_STATUS_TRANSITION";
        }

        @Override
        public String message() {
            return "Cannot transition trip place from '%s' to '%s'".formatted(from, to);
        }
    }
}

