package com.triplify.domain.error;

public sealed interface CountryError extends DomainError permits
        CountryError.NotFound,
        CountryError.AlreadyExists,
        CountryError.AlreadyBanned,
        CountryError.NotBanned,
        CountryError.NotOwner {

    record NotFound(String countryId) implements CountryError {
        @Override
        public String code() {
            return "COUNTRY_NOT_FOUND";
        }

        @Override
        public String message() {
            return "Country '%s' not found".formatted(countryId);
        }
    }

    record AlreadyExists(String name) implements CountryError {
        @Override
        public String code() {
            return "COUNTRY_ALREADY_EXISTS";
        }

        @Override
        public String message() {
            return "Country '%s' already exists".formatted(name);
        }
    }

    record AlreadyBanned(String countryId) implements CountryError {
        @Override
        public String code() {
            return "COUNTRY_ALREADY_BANNED";
        }

        @Override
        public String message() {
            return "Country '%s' is already banned".formatted(countryId);
        }
    }

    record NotBanned(String countryId) implements CountryError {
        @Override
        public String code() {
            return "COUNTRY_NOT_BANNED";
        }

        @Override
        public String message() {
            return "Country '%s' is not banned".formatted(countryId);
        }
    }

    record NotOwner(String countryId) implements CountryError {
        @Override
        public String code() {
            return "COUNTRY_NOT_OWNER";
        }

        @Override
        public String message() {
            return "You are not the owner of country '%s'".formatted(countryId);
        }
    }
}

