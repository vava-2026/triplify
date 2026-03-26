package com.triplify.application.error;

public final class ValidationMessage {

    private ValidationMessage() {
    }

    public static final class Constants {
        public static final String REQUIRED = "validation.field.required";
        public static final String NUMBER_MUST_BE_NON_NEGATIVE = "validation.number.must.be.non.negative";
        public static final String LATITUDE_OUT_OF_RANGE = "validation.latitude.out.of.range";
        public static final String LONGITUDE_OUT_OF_RANGE = "validation.longitude.out.of.range";
        public static final String EMAIL_INVALID = "validation.email.invalid";
        public static final String PASSWORD_TOO_SHORT = "validation.password.too.short";
        public static final String USERNAME_TOO_SHORT = "validation.username.too.short";
        public static final String DESCRIPTION_TOO_LONG = "validation.description.too.long";

        private Constants() {
        }
    }
}
