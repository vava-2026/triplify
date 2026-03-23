package com.triplify.domain.error;

public sealed interface TripRouteError extends DomainError permits TripRouteError.NotFound, TripRouteError.InvalidStatusTransition {

    record NotFound(String tripRouteId) implements TripRouteError {
        @Override
        public String code() {
            return "TRIP_ROUTE_NOT_FOUND";
        }

        @Override
        public String message() {
            return "Trip route '%s' not found".formatted(tripRouteId);
        }
    }

    record InvalidStatusTransition(String from, String to) implements TripRouteError {
        @Override
        public String code() {
            return "TRIP_ROUTE_INVALID_STATUS_TRANSITION";
        }

        @Override
        public String message() {
            return "Cannot transition trip route from '%s' to '%s'".formatted(from, to);
        }
    }
}

