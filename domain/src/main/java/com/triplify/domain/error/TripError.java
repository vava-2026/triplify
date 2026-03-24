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
            return "error.trip.not.found";
        }

        @Override
        public String message() {
            return "Trip '%s' not found".formatted(tripId);
        }
    }

    record NotOwner(String tripId) implements TripError {
        @Override
        public String code() {
            return "error.trip.not.owner";
        }

        @Override
        public String message() {
            return "You are not the owner of trip '%s'".formatted(tripId);
        }
    }

    record InvalidDates(LocalDate start, LocalDate end) implements TripError {
        @Override
        public String code() {
            return "error.trip.invalid.dates";
        }

        @Override
        public String message() {
            return "Start date %s must be before end date %s".formatted(start, end);
        }
    }

    record InvalidStatusTransition(String from, String to) implements TripError {
        @Override
        public String code() {
            return "error.trip.invalid.status.transition";
        }

        @Override
        public String message() {
            return "Cannot transition trip from '%s' to '%s'".formatted(from, to);
        }
    }
}

