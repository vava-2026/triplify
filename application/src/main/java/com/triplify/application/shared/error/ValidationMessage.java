package com.triplify.application.shared.error;

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
        public static final String USERNAME_TOO_LONG = "validation.username.too.long";
        public static final String TITLE_TOO_LONG = "validation.title.too.long";
        public static final String NAME_TOO_LONG = "validation.name.too.long";
        public static final String DESCRIPTION_TOO_LONG = "validation.description.too.long";
        public static final String SIGN_UP_INVALID_ROLE = "validation.signUp.invalidRole";
        public static final String SIGN_UP_TERMS_REQUIRED = "validation.signUp.terms.required";
        public static final String AT_LEAST_ONE_COUNTRY_REQUIRED = "validation.atLeastOneCountryRequired";

        private Constants() {
        }
    }
}
