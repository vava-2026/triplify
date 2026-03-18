package com.triplify.application.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.*;

/**
 * A utility bridge between standard Hibernate Validator and our custom {@link ValidationResult} wrapper.
 * <p>
 * <b>Key Feature:</b> This mapper intentionally filters out multiple violations for the
 * same field, returning only the most critical one based on {@code ValidationMessage.getPriority()}.
 * This prevents overwhelming the client UI with cascading errors (for example showing both "Email cannot be blank" and "Email format is invalid" simultaneously).
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * ValidationResult<CreateUserCommand> result = ValidationMapper.validate(requestDTO);
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
     * @return A {@link ValidationResult} containing the safe payload, or a list of field errors.
     */
    public static <T> ValidationResult<T> validate(T object) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
        if (violations.isEmpty()) {
            return ValidationResult.valid(object);
        }

        Map<String, FieldError> byField = new LinkedHashMap<>();
        violations.stream()
                .map(ValidationMapper::toFieldViolation)
                .sorted(Comparator
                        .comparing(FieldError::getField)
                        .thenComparingInt(v -> ValidationMessage.getPriority(v.getMessageKey())))
                .forEach(v -> byField.putIfAbsent(v.getField(), v));

        return ValidationResult.invalid(List.copyOf(byField.values()));
    }

    private static FieldError toFieldViolation(ConstraintViolation<?> cv) {
        String field = cv.getPropertyPath().toString();
        return new FieldError(field, cv.getMessage());
    }
}
