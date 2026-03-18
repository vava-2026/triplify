package com.triplify.application.error;

import com.triplify.domain.error.AppError;
import com.triplify.domain.error.ErrorCode;

/**
 * A targeted error representing a validation failure on a specific object property.
 * <p>
 * This acts as an intermediate DTO. It holds the precise field that failed (for example "email")
 * and the specific reason (for example "validation.email.invalid").
 */
public final class FieldError {

    private final String field;
    private final String messageKey;

    public FieldError(String field, String messageKey) {
        this.field = field;
        this.messageKey = messageKey;
    }

    /**
     * Bridges this field-specific validation error into a broader domain {@link AppError}.
     * <p>
     * This is useful when you need to merge validation failures into a standard business logic flow that expects generic AppErrors.
     */
    public AppError toAppError() {
        return AppError.of(ErrorCode.VALIDATION_FAILED, field + ": " + messageKey);
    }

    public String getField() {
        return field;
    }
    public String getMessageKey() {
        return messageKey;
    }
}
