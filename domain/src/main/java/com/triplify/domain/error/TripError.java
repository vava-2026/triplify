package com.triplify.domain.error;

import java.time.LocalDate;

public sealed interface TripError extends DomainError permits
        TripError.NotFound,
        TripError.NotOwner,
        TripError.InvalidDates,
        TripError.InvalidStatusTransition {

    record NotFound(String tripId) implements TripError {
        @Override
        public String code() {
            return "TRIP_NOT_FOUND";
        }

        @Override
        public String message() {
            return "Trip '%s' not found".formatted(tripId);
        }
    }

    record NotOwner(String tripId) implements TripError {
        @Override
        public String code() {
            return "TRIP_NOT_OWNER";
        }

        @Override
        public String message() {
            return "You are not the owner of trip '%s'".formatted(tripId);
        }
    }

    record InvalidDates(LocalDate start, LocalDate end) implements TripError {
        @Override
        public String code() {
            return "TRIP_INVALID_DATES";
        }

        @Override
        public String message() {
            return "Start date %s must be before end date %s".formatted(start, end);
        }
    }

    record InvalidStatusTransition(String from, String to) implements TripError {
        @Override
        public String code() {
            return "TRIP_INVALID_STATUS_TRANSITION";
        }

        @Override
        public String message() {
            return "Cannot transition trip from '%s' to '%s'".formatted(from, to);
        }
    }
}

