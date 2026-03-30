package com.triplify.application.util;

import com.triplify.application.error.ApplicationError;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * Eliminates repetitive try/catch blocks in service methods.
 * Catches infrastructure exceptions and converts them to {@code Result.fail(ApplicationError.Unexpected)}.
 */
public final class SafeCall {

    private SafeCall() {}

    /**
     * Executes the given supplier, catching any exception and converting it to {@code Result.fail(ApplicationError.Unexpected)}.
     *
     * @param action the operation that may throw (typically repository calls)
     * @param operation short description for logging/error messages (e.g. "create country")
     * @param log logger to record the failure
     */
    public static <T> Result<T> execute(Supplier<Result<T>> action, String operation, Logger log) {
        try {
            return action.get();
        } catch (Exception e) {
            log.error("Failed to {}", operation, e);
            return Result.fail(new ApplicationError.Unexpected(operation, e));
        }
    }
}
