package com.triplify.application.error;

import com.triplify.domain.error.AppError;


/**
 * Adapter that converts an {@link com.triplify.domain.error.AppError} into a simple record
 * suitable for display in the UI layer.
 *
 * <p>The {@code messageKey} is an i18n key resolved by {@code I18n.t(key)}, while {@code detail}
 * provides optional extra context (e.g. the offending field name) for more specific UI messages.
 *
 * @param messageKey i18n key for the human-readable error message
 * @param detail     optional detail string; may be {@code null}
 */
public record ErrorResponse(String messageKey, String detail) {

    public static ErrorResponse from(AppError error) {
        return new ErrorResponse(error.messageKey(), error.getDetail());
    }
}
