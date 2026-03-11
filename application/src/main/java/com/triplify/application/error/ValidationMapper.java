package com.triplify.application.error;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

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

        Map<String, FieldViolation> byField = new HashMap<>();
        violations.stream()
                .map(ValidationMapper::toFieldViolation)
                .sorted(Comparator
                        .comparing(FieldViolation::getField)
                        .thenComparingInt(v -> ValidationMessage.getPriority(v.getMessageKey())))
                .forEach(v -> byField.putIfAbsent(v.getField(), v));

        return ValidationResult.invalid(List.copyOf(byField.values()));
    }

    private static FieldViolation toFieldViolation(ConstraintViolation<?> cv) {
        String field = cv.getPropertyPath().toString();
        return new FieldViolation(field, cv.getMessage());
    }
}
