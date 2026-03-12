package com.triplify.application.error;

/**
 * Catalogue of well-known validation message keys and their display priority.
 *
 * <p>Each constant pairs an i18n message key (e.g. {@code "validation.field.required"}) with
 * a numeric priority used by {@link ValidationMapper} to select the most important error when
 * multiple constraints are violated on the same field. Lower numeric priority means higher
 * importance — {@code REQUIRED} (0) is always shown first.
 *
 * <p>The inner {@link Constants} class exposes the raw {@code String} keys so that
 * Bean Validation {@code @NotBlank}, {@code @Email}, etc. annotations can reference them
 * without creating a compile-time dependency on this enum.
 */
public enum ValidationMessage {

    REQUIRED(Constants.REQUIRED, 0),
    EMAIL_INVALID(Constants.EMAIL_INVALID, 1),
    PASSWORD_TO_SHORT(Constants.PASSWORD_TO_SHORT, 2),
    USERNAME_TO_SHORT(Constants.USERNAME_TO_SHORT, 3);

    public static final class Constants {
        public static final String REQUIRED = "validation.field.required";
        public static final String EMAIL_INVALID = "validation.email.invalid";
        public static final String PASSWORD_TO_SHORT = "validation.password.too.short";
        public static final String USERNAME_TO_SHORT = "validation.username.too.short";
    }

    private final String messageKey;
    private final int priority;

    ValidationMessage(String messageKey, int priority) {
        this.messageKey = messageKey;
        this.priority = priority;
    }

    public String messageKey() {
        return messageKey;
    }

    public static int getPriority(String key) {
        for (ValidationMessage vm : values()) {
            if (vm.messageKey.equals(key)) {
                return vm.priority;
            }
        }
        return Integer.MAX_VALUE;
    }
}
