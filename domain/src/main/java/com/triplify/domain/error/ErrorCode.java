package com.triplify.domain.error;

public enum ErrorCode {

    AUTH_INVALID_CREDENTIALS("error.auth.invalid.credentials"),
    AUTH_USERNAME_TAKEN("error.auth.username.taken"),
    AUTH_EMAIL_TAKEN("error.auth.email.taken"),
    VALIDATION_FAILED("error.validation.failed");

    private final String messageKey;

    ErrorCode(String messageKey) {
        this.messageKey = messageKey;
    }

    public String messageKey() {
        return messageKey;
    }
}
