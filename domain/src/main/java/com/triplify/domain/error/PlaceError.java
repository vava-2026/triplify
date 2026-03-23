package com.triplify.domain.error;

public sealed interface PlaceError extends DomainError permits PlaceError.NotFound, PlaceError.NotOwner, PlaceError.InvalidCoordinates {

    record NotFound(String placeId) implements PlaceError {
        @Override
        public String code() {
            return "error.place.not.found";
        }

        @Override
        public String message() {
            return "Place '%s' not found".formatted(placeId);
        }
    }

    record NotOwner(String placeId) implements PlaceError {
        @Override
        public String code() {
            return "error.place.not.owner";
        }

        @Override
        public String message() {
            return "You are not the owner of place '%s'".formatted(placeId);
        }
    }

    record InvalidCoordinates(double latitude, double longitude) implements PlaceError {
        @Override
        public String code() {
            return "error.place.invalid.coordinates";
        }

        @Override
        public String message() {
            return "Coordinates (%s, %s) are invalid".formatted(latitude, longitude);
        }
    }
}

