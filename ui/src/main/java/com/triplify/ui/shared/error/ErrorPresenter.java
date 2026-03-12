package com.triplify.ui.shared.error;

import com.triplify.application.error.FieldError;
import com.triplify.application.error.ValidationResult;
import com.triplify.ui.i18n.I18n;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.MissingResourceException;

/**
 * Utility class that applies validation error styles to JavaFX form fields.
 *
 * <p>{@code ErrorPresenter} is the bridge between the application-layer validation result and
 * the JavaFX UI. Its primary responsibility is to mark {@link TextField} controls with the
 * {@value #ERROR_STYLE_CLASS} CSS class when a validation error is associated with them, and
 * to clear those marks when the user corrects the input.
 *
 * <p>Typical usage inside a JavaFX controller:
 * <pre>{@code
 * Map<String, TextField> fieldMap = Map.of(
 *     "email",    emailField,
 *     "password", passwordField
 * );
 * ErrorPresenter.showValidation(validationResult, fieldMap);
 * }</pre>
 *
 * <p>The {@link #resolveKey(String)} helper translates an i18n key to a localised string,
 * falling back to the raw key if the bundle entry is missing.
 */
public final class ErrorPresenter {

    private static final Logger log = LoggerFactory.getLogger(ErrorPresenter.class);

    public static final String ERROR_STYLE_CLASS = "input-error";

    private ErrorPresenter() {}

    public static <T> void showValidation(ValidationResult<T> result, Map<String, TextField> fieldMap) {
        fieldMap.values().forEach(ErrorPresenter::clearField);

        if (!result.isFailure()) return;

        for (FieldError v : result.getViolations()) {
            TextField field = fieldMap.get(v.getField());
            if (field != null) {
                markFieldError(field);
            } else {
                log.warn("No TextField mapped for violated field '{}'", v.getField());
            }
        }
    }

    public static String resolveKey(String key) {
        try {
            return I18n.t(key);
        } catch (MissingResourceException e) {
            log.warn("Missing i18n key: '{}'", key);
            return key;
        }
    }

    private static void markFieldError(TextField field) {
        if (!field.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            field.getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }

    private static void clearField(TextField field) {
        field.getStyleClass().remove(ERROR_STYLE_CLASS);
    }
}
