package com.triplify.application.error;

import com.triplify.domain.error.AppError;
import com.triplify.domain.error.ErrorCode;

public final class FieldViolation {

    private final String field;
    private final String messageKey;

    public FieldViolation(String field, String messageKey) {
        this.field = field;
        this.messageKey = messageKey;
    }

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
