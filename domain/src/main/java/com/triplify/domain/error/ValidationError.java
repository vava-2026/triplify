package com.triplify.domain.error;

import java.util.List;

public record ValidationError(List<FieldViolation> violations) implements DomainError {

    @Override
    public String code() {
        return "VALIDATION_FAILED";
    }

    @Override
    public String message() {
        return "Validation failed";
    }

    public List<FieldViolation> forField(String field) {
        return violations.stream().filter(v -> v.field().equals(field)).toList();
    }

    public boolean hasField(String field) {
        return violations.stream().anyMatch(v -> v.field().equals(field));
    }
}
