package com.triplify.application.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.*;

public final class ValidationMapper {

    private static final Validator VALIDATOR;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    private ValidationMapper() {}

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
