package com.triplify.application.result;

import com.triplify.application.error.ErrorResponse;
import com.triplify.domain.error.AppError;
import com.triplify.domain.error.ErrorCode;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Result<T> {

    private final T value;
    private final List<AppError> errors;

    private Result(T value, List<AppError> errors) {
        this.value = value;
        this.errors = errors;
    }

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

    public T getValue() {
        if (isFailure()) {
            throw new NoSuchElementException("Result is a failure: " + errors);
        }
        return value;
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    public List<AppError> getErrors() {
        return errors;
    }

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

    public Result<T> onFailureResponse(Consumer<List<ErrorResponse>> action) {
        if (isFailure()) action.accept(getErrorResponses());
        return this;
    }

    public Result<T> onSuccess(Consumer<T> action) {
        if (isSuccess()) {
            action.accept(value);
        }
        return this;
    }

    public Result<T> onFailure(Consumer<List<AppError>> action) {
        if (isFailure()) {
            action.accept(errors);
        }
        return this;
    }

    public <U> Result<U> map(Function<T, U> mapper) {
        if (isSuccess()) {
            return Result.success(mapper.apply(value));
        }
        return Result.failure(errors);
    }

    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        if (isSuccess()) {
            return mapper.apply(value);
        }
        return Result.failure(errors);
    }
}
