package com.triplify.application.error;

import com.triplify.domain.error.AppError;


public record ErrorResponse(String messageKey, String detail) {

    public static ErrorResponse from(AppError error) {
        return new ErrorResponse(error.messageKey(), error.getDetail());
    }
}
