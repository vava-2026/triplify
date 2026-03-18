package com.triplify.application.error;

public enum ValidationMessage {

    REQUIRED(Constants.REQUIRED, 0),
    EMAIL_INVALID(Constants.EMAIL_INVALID, 1),
    PASSWORD_TOO_SHORT(Constants.PASSWORD_TOO_SHORT, 2),
    USERNAME_TOO_SHORT(Constants.USERNAME_TOO_SHORT, 3);

    public static final class Constants {
        public static final String REQUIRED = "validation.field.required";
        public static final String EMAIL_INVALID = "validation.email.invalid";
        public static final String PASSWORD_TOO_SHORT = "validation.password.too.short";
        public static final String USERNAME_TOO_SHORT = "validation.username.too.short";
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
