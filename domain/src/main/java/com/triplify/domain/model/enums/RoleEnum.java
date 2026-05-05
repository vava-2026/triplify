package com.triplify.domain.model.enums;

public enum RoleEnum {
    USER("user"),
    PRO_USER("pro-user"),
    CONFIGURATION_MANAGER("configuration manager");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoleEnum fromValue(String value) throws IllegalArgumentException {
        for (RoleEnum role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
