package com.triplify.domain.error;

public record FieldViolation(String field, Object invalidValue, String constraint, String messageKey) {
}
