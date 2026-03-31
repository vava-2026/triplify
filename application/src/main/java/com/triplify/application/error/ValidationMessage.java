package com.triplify.application.error;

public final class ValidationMessage {

    private ValidationMessage() {
    }

    public static final class Constants {
        public static final String REQUIRED = "validation.field.required";
        public static final String EMAIL_INVALID = "validation.email.invalid";
        public static final String PASSWORD_TOO_SHORT = "validation.password.too.short";
        public static final String USERNAME_TOO_SHORT = "validation.username.too.short";
        public static final String DESCRIPTION_TOO_LONG = "validation.description.too.long";
        public static final String SIGN_UP_INVALID_ROLE = "validation.signUp.invalidRole";
        public static final String SIGN_UP_TERMS_REQUIRED = "validation.signUp.terms.required";

        private Constants() {
        }
    }
}
