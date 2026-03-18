package com.triplify.domain.error;

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
