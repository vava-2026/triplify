package com.triplify.application.error;

import com.triplify.domain.error.AppError;
import com.triplify.domain.error.ErrorCode;

/**
 * Represents a single field-level validation error produced during bean validation.
 *
 * <p>Each instance captures the name of the field that failed validation together with
 * an i18n message key describing why it failed (e.g. {@code "validation.field.required"}).
 * Use {@link #toAppError()} to convert to the domain-level {@link AppError} when the
 * error needs to be propagated outside the application layer.
 *
 * <p>Instances are normally created by {@link ValidationMapper#validate(Object)} and
 * collected inside a {@link ValidationResult}.
 *
 * @see ValidationMapper
 * @see ValidationResult
 */
public final class FieldError {

    private final String field;
    private final String messageKey;

    public FieldError(String field, String messageKey) {
        this.field = field;
        this.messageKey = messageKey;
    }

    /** Converts this field error to a domain {@link AppError} with {@link ErrorCode#VALIDATION_FAILED}. */
    public AppError toAppError() {
        return AppError.of(ErrorCode.VALIDATION_FAILED, field + ": " + messageKey);
    }

    /** Returns the name of the field that failed validation. */
    public String getField() {
        return field;
    }

    /** Returns the i18n message key describing the constraint that was violated. */
    public String getMessageKey() {
        return messageKey;
    }
}
