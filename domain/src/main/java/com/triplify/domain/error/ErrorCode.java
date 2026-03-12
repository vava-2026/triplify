package com.triplify.domain.error;

/**
 * Enumeration of all error codes recognised by the application.
 *
 * <p>Each constant maps a logical error scenario to an i18n message key. The message key is
 * consumed by the UI layer (via {@code I18n.t(key)}) to render a localised error message.
 *
 * <p>Extend this enum whenever a new category of error needs to be surfaced to the user.
 *
 * @see AppError
 */
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
