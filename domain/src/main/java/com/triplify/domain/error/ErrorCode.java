package com.triplify.domain.error;

public enum ErrorCode {

    AUTH_INVALID_CREDENTIALS("error.auth.invalid.credentials"),
    VALIDATION_FAILED("error.validation.failed");

    private final String messageKey;

    ErrorCode(String messageKey) {
        this.messageKey = messageKey;
    }

    public String messageKey() {
        return messageKey;
    }
}
