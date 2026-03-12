package com.triplify.domain.error;

/**
 * Immutable domain-level representation of an application error.
 *
 * <p>{@code AppError} is the single error type propagated through the domain and application
 * layers. Every error is identified by an {@link ErrorCode}, which carries an i18n message key
 * used by the UI layer to display a localised message. An optional {@code detail} string can
 * carry extra context (e.g. the failing field name) for debugging or more specific UI messages.
 *
 * <p>Instances are created through the static factory methods {@link #of(ErrorCode)} and
 * {@link #of(ErrorCode, String)}.
 *
 * @see ErrorCode
 */
public final class AppError {

    private final ErrorCode code;
    private final String detail;

    private AppError(ErrorCode code, String detail) {
        this.code = code;
        this.detail = detail;
    }

    public static AppError of(ErrorCode code) {
        return new AppError(code, null);
    }

    public static AppError of(ErrorCode code, String detail) {
        return new AppError(code, detail);
    }

    public String messageKey() {
        return code.messageKey();
    }

    public String getDetail() {
        return detail;
    }
}
