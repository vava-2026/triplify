package com.triplify.application.error;

import com.triplify.application.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.*;

/**
 * A utility bridge between standard Hibernate Validator and our custom {@link Result} wrapper.
 * <p>
 * <b>Key Feature:</b> This mapper intentionally filters out multiple violations for the
 * same field, returning only the most critical one based on {@code ValidationMessage.getPriority()}.
 * This prevents overwhelming the client UI with cascading errors (for example showing both "Email cannot be blank" and "Email format is invalid" simultaneously).
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * Result<CreateUserCommand, FieldError> result = ValidationMapper.validate(requestDTO);
 * }</pre>
 */
public final class ValidationMapper {

    private static final Validator VALIDATOR;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    private ValidationMapper() {}

    /**
     * Evaluates all standard validation annotations (like {@code @NotNull}, {@code @Email}) on the given object.
     * @param object The input object to validate (a Request).
     * @return A {@link Result} containing the safe payload, or a list of {@link FieldError}s.
     */
    public static <T> Result<T, FieldError> validate(T object) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
        if (violations.isEmpty()) {
            return Result.success(object);
        }

        Map<String, FieldError> byField = new LinkedHashMap<>();
        violations.stream()
                .map(ValidationMapper::toFieldViolation)
                .sorted(Comparator
                        .comparing(FieldError::getField)
                        .thenComparingInt(v -> ValidationMessage.getPriority(v.getMessageKey())))
                .forEach(v -> byField.putIfAbsent(v.getField(), v));

        return Result.failure(List.copyOf(byField.values()));
    }

    private static FieldError toFieldViolation(ConstraintViolation<?> cv) {
        String field = cv.getPropertyPath().toString();
        return new FieldError(field, cv.getMessage());
    }
}
