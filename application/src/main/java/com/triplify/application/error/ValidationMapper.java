package com.triplify.application.error;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.*;

/**
 * Utility class that validates objects using the Bean Validation (JSR-380) API and converts
 * {@link ConstraintViolation} instances into application-level {@link FieldError} objects.
 *
 * <p>Usage:
 * <pre>{@code
 * ValidationResult<LoginRequest> result = ValidationMapper.validate(loginRequest);
 * result.onSuccess(req  -> authService.login(req))
 *       .onFailure(errs -> errorPresenter.showValidation(result, fieldMap));
 * }</pre>
 *
 * <p>When multiple violations target the same field, only the highest-priority one (as
 * defined by {@link ValidationMessage#getPriority(String)}) is kept, so the UI shows exactly
 * one error per field.
 *
 * @see ValidationResult
 * @see FieldError
 */
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

        Map<String, FieldError> byField = new HashMap<>();
        violations.stream()
                .map(ValidationMapper::toFieldError)
                .sorted(Comparator
                        .comparing(FieldError::getField)
                        .thenComparingInt(v -> ValidationMessage.getPriority(v.getMessageKey())))
                .forEach(v -> byField.putIfAbsent(v.getField(), v));

        return ValidationResult.invalid(List.copyOf(byField.values()));
    }

    private static FieldError toFieldError(ConstraintViolation<?> cv) {
        String field = cv.getPropertyPath().toString();
        return new FieldError(field, cv.getMessage());
    }
}
