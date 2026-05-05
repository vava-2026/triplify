package com.triplify.domain.result;

import com.triplify.domain.error.AppError;
import lombok.Getter;

@Getter
public final class FailureException extends RuntimeException {
    private final AppError error;

    public FailureException(AppError error) {
        super(error == null ? "Result failed" : error.message());
        this.error = error;
    }
}
