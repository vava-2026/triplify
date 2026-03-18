package com.triplify.ui.shared.toast;

public enum ToastType {

    ERROR("Error", "fth-alert-circle", "toast-error"),
    SUCCESS("Success", "fth-check-circle", "toast-success"),
    INFO("Info", "fth-info", "toast-info"),
    WARNING("Warning", "fth-alert-triangle", "toast-warning");

    private final String defaultTitle;
    private final String iconLiteral;
    private final String styleClass;

    ToastType(String defaultTitle, String iconLiteral, String styleClass) {
        this.defaultTitle = defaultTitle;
        this.iconLiteral = iconLiteral;
        this.styleClass = styleClass;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public String getIconLiteral() {
        return iconLiteral;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
