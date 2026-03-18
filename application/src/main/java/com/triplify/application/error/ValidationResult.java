package com.triplify.application.error;

import com.triplify.application.result.Result;
import com.triplify.domain.error.AppError;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * A specialized version of {@link Result} dedicated only to the input validation part.
 * <p>
 * While {@code Result} handles generic domain failures (like "User not found"), {@code ValidationResult} specifically the exact fields that failed.
 */
public final class ValidationResult<T> {

    private final T value;
    private final List<FieldError> violations;

    private ValidationResult(T value, List<FieldError> violations) {
        this.value = value;
        this.violations = violations;
    }

    public static <T> ValidationResult<T> valid(T value) {
        return new ValidationResult<>(value, List.of());
    }

    public static ValidationResult<Void> valid() {
        return new ValidationResult<>(null, List.of());
    }

    public static <T> ValidationResult<T> invalid(List<FieldError> violations) {
        if (violations == null || violations.isEmpty()) {
            throw new IllegalArgumentException("invalid() requires at least one violation");
        }
        return new ValidationResult<>(null, List.copyOf(violations));
    }

    public boolean isSuccess() {
        return violations.isEmpty();
    }

    public boolean isFailure() {
        return !violations.isEmpty();
    }

    /**
     * Converts the specific field violations into standard domain {@link AppError}s.
     * Useful when you want to convert a failed ValidationResult into a standard Result.
     */
    public List<AppError> getErrors() {
        return violations.stream()
                .map(FieldError::toAppError)
                .toList();
    }

    public List<FieldError> getViolations() {
        return violations;
    }

    public ValidationResult<T> onSuccess(Consumer<T> action) {
        if (isSuccess()) {
            action.accept(value);
        }
        return this;
    }

    public ValidationResult<T> onFailure(Consumer<List<FieldError>> action) {
        if (isFailure()) {
            action.accept(violations);
        }
        return this;
    }
}
