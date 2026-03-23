package com.triplify.domain.error;

public sealed interface RouteError extends DomainError permits
        RouteError.NotFound,
        RouteError.NotOwner,
        RouteError.PlaceNotInRoute,
        RouteError.InvalidStatusTransition,
        RouteError.TooFewPlaces {

    record NotFound(String routeId) implements RouteError {
        @Override
        public String code() {
            return "error.route.not.found";
        }

        @Override
        public String message() {
            return "Route '%s' not found".formatted(routeId);
        }
    }

    record NotOwner(String routeId) implements RouteError {
        @Override
        public String code() {
            return "error.route.not.owner";
        }

        @Override
        public String message() {
            return "You are not the owner of route '%s'".formatted(routeId);
        }
    }

    record PlaceNotInRoute(String placeId, String routeId) implements RouteError {
        @Override
        public String code() {
            return "error.route.place.not.in.route";
        }

        @Override
        public String message() {
            return "Place '%s' is not part of route '%s'".formatted(placeId, routeId);
        }
    }

    record InvalidStatusTransition(String from, String to) implements RouteError {
        @Override
        public String code() {
            return "error.route.invalid.status.transition";
        }

        @Override
        public String message() {
            return "Cannot transition route from '%s' to '%s'".formatted(from, to);
        }
    }

    record TooFewPlaces(int placeCount) implements RouteError {
        @Override
        public String code() {
            return "error.route.too.few.places";
        }

        @Override
        public String message() {
            return "Route must contain at least 2 places, got %s".formatted(placeCount);
        }
    }
}

