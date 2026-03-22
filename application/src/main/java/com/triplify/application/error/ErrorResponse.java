package com.triplify.application.error;

import com.triplify.domain.error.AppError;
import com.triplify.domain.error.ErrorCode;

/**
 * The API-facing representation of an error.
 * <p>
 * While {@link AppError} is used internally within the domain and service layers,
 * {@code ErrorResponse} is the DTO sent back to the client (to the UI layer).
 * <p>
 * Keeping this separate from {@code AppError} acts as a boundary, ensuring that
 * internal system details, stack traces, or sensitive domain logic don't accidentally
 * leak to the frontend (UI layer).
 * <p>
 *
 * @param messageKey A translation key intended for the frontend.
 * The client application can use this key to look up the correct localized string (i18n) to display to the user in their preferred language.
 * @param detail A technical or developer-friendly explanation of what went wrong (for example validation fields errors).
 */
public record ErrorResponse(String messageKey, String detail) {

    /**
     * Factory method to safely convert an internal domain error into a client-safe response.
     */
    public static ErrorResponse from(AppError error) {
        return new ErrorResponse(error.messageKey(), error.getDetail());
    }

    /**
     * Factory method to easily create ErrorResponse without explicit mapping from AppError
     */
    public static ErrorResponse of(ErrorCode code) {
        return ErrorResponse.from(AppError.of(code));
    }
}
