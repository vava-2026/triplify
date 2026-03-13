package com.triplify.application.result;

import com.triplify.application.error.ErrorResponse;
import com.triplify.domain.error.AppError;
import com.triplify.domain.error.ErrorCode;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A functional wrapper for operations that can either succeed or fail.
 * <p>
 * This is designed to replace throwing exceptions for expected business rule violations.
 * By returning a {@code Result}, you force the caller to explicitly handle the failure case,
 * leading to safer and more predictable code. For all the services, wrap result into Result (return Result<T\>)
 * <p>
 * <b>Quick Example:</b>
 * <pre>{@code
 * Result<User> result = userService.findById(id);
 * result.onSuccess(responseDTO -> log.info("Found user!"))
 * .onFailure(errors -> log.warn("Failed to find user"));
 * }</pre>
 *
 * @param <T> the type of the wrapped value if the operation is successful
 */
public final class Result<T> {

    private final T value;
    private final List<AppError> errors;

    private Result(T value, List<AppError> errors) {
        this.value = value;
        this.errors = errors;
    }

    /**
     * Creates a success result with no payload. Useful for operations that only perform side effects (like saving to a database) but don't need to return data
     */
    public static <T> Result<T> success(T value) {
        return new Result<>(value, List.of());
    }

    public static Result<Void> success() {
        return new Result<>(null, List.of());
    }

    public static <T> Result<T> failure(AppError error) {
        return new Result<>(null, List.of(error));
    }

    public static <T> Result<T> failure(ErrorCode code) {
        return failure(AppError.of(code));
    }

    public static <T> Result<T> failure(ErrorCode code, String detail) {
        return failure(AppError.of(code, detail));
    }

    /**
     * Creates a failure with multiple errors. Great for form validations where multiple fields might be invalid at the same time
     */
    public static <T> Result<T> failure(List<AppError> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("Failure result requires at least one error");
        }
        return new Result<>(null, List.copyOf(errors));
    }

    public boolean isSuccess() {
        return errors.isEmpty();
    }

    public boolean isFailure() {
        return !errors.isEmpty();
    }

    /**
     * Safely wraps the value in an Optional. If this is a failure result. It simply returns {@code Optional.empty()}
     */
    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    public List<AppError> getErrors() {
        return errors;
    }

    /**
     * Grabs the first error. Throws an exception if called on a success result. Useful when you only care about the primary reason an operation failed
     */
    public AppError getFirstError() {
        if (isSuccess()) {
            throw new NoSuchElementException("Result is a success");
        }
        return errors.getFirst();
    }

    public List<ErrorResponse> getErrorResponses() {
        return errors.stream().map(ErrorResponse::from).toList();
    }

    public ErrorResponse getFirstErrorResponse() {
        if (isSuccess()) throw new NoSuchElementException("Result is a success");
        return ErrorResponse.from(errors.getFirst());
    }

    /**
     * Executes the given action only if this result is a failure. Use in UI layer
     */
    public Result<T> onFailureResponse(Consumer<List<ErrorResponse>> action) {
        if (isFailure()) action.accept(getErrorResponses());
        return this;
    }

    /**
     * Executes the given action only if this result is a success. Ideal for side effects like logging, sending metrics, or triggering events
     */
    public Result<T> onSuccess(Consumer<T> action) {
        if (isSuccess()) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Executes the given action only if this result is a failure. Ideal for side effects like logging the exact {@link AppError}s.
     * Do not use in UI layer, use {@link #onFailureResponse(Consumer)} instead to avoid coupling the UI to the domain error model
     */
    public Result<T> onFailure(Consumer<List<AppError>> action) {
        if (isFailure()) {
            action.accept(errors);
        }
        return this;
    }

    /**
     * Transforms the successful value of this result into a new value.
     * If this is a failure, the mapping is ignored and the errors are passed along.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * Result<User> result = ...;
     * Result<String> nameResult = result.map(User::getName);
     * }</pre>
     */
    public <U> Result<U> map(Function<T, U> mapper) {
        if (isSuccess()) {
            return Result.success(mapper.apply(value));
        }
        return Result.failure(errors);
    }

    /**
     * Chains another operation that also returns a {@code Result}.
     * If this result is already a failure, it short-circuits and skips the next operation.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * // Fetch a user, and if successful, fetch their settings
     * Result<Settings> settings = userRepository.findById(id)
     * .flatMap(user -> settingsRepository.findByUserId(user.getId()));
     * }</pre>
     */
    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        if (isSuccess()) {
            return mapper.apply(value);
        }
        return Result.failure(errors);
    }
}
