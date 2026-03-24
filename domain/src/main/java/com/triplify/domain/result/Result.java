package com.triplify.domain.result;

import com.triplify.domain.error.AppError;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Result<T> permits Result.Success, Result.Failure {
    record Success<T>(T value) implements Result<T> {
    }

    record Failure<T>(AppError error) implements Result<T> {
    }

    static <T> Result<T> ok(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> ok() {
        return new Success<>(null);
    }

    static <T> Result<T> fail(AppError error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T>;
    }

    default T getValue() {
        if (this instanceof Success<T> s) {
            return s.value();
        }
        throw new NoSuchElementException("Result is a failure");
    }

    default AppError getError() {
        if (this instanceof Failure<T> f) {
            return f.error();
        }
        throw new NoSuchElementException("Result is a success");
    }

    default <U> Result<U> map(Function<T, U> fn) {
        return switch (this) {
            case Success<T> s -> Result.ok(fn.apply(s.value()));
            case Failure<T> f -> Result.fail(f.error());
        };
    }

    default <U> Result<U> flatMap(Function<T, Result<U>> fn) {
        return switch (this) {
            case Success<T> s -> fn.apply(s.value());
            case Failure<T> f -> Result.fail(f.error());
        };
    }

    default void onSuccess(Consumer<T> fn) {
        if (this instanceof Success<T> s) {
            fn.accept(s.value());
        }
    }

    default void onFailure(Consumer<AppError> fn) {
        if (this instanceof Failure<T> f) {
            fn.accept(f.error());
        }
    }

    default <U> U fold(Function<T, U> onSuccess, Function<AppError, U> onFailure) {
        return switch (this) {
            case Success<T> s -> onSuccess.apply(s.value());
            case Failure<T> f -> onFailure.apply(f.error());
        };
    }
}

