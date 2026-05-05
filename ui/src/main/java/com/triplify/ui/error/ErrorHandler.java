package com.triplify.ui.error;

import com.google.inject.Inject;
import com.triplify.application.shared.error.ApplicationError;
import com.triplify.domain.error.AppError;
import com.triplify.domain.error.CountryError;
import com.triplify.domain.error.FieldViolation;
import com.triplify.domain.error.ValidationError;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.toast.ToastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    private final ToastService toast;

    @Inject
    public ErrorHandler(ToastService toast) {
        this.toast = toast;
    }

    public void handle(AppError error) {
        handle(error, Collections.emptyMap());
    }

    public void handle(AppError error, Map<String, Consumer<String>> fieldHandlers) {
        switch (error) {
            case ValidationError validationError -> handleValidationError(validationError, fieldHandlers);
            case CountryError.AlreadyBanned ignored -> toast.warning(I18n.t("error.country.already.banned"));
            case CountryError.NotBanned ignored -> toast.warning(I18n.t("error.country.not.banned"));

            case ApplicationError.Unexpected unexpected -> {
                log.error("Unexpected application error: {}", unexpected.message());
                toast.error(I18n.t(unexpected.code()));
            }
            case ApplicationError.StorageFailure storageFailure -> {
                log.error("{}", storageFailure.message(), storageFailure.cause());
                toast.error(I18n.t(storageFailure.code()));
            }
            case ApplicationError.FileFailure fileFailure -> {
                log.error("{}", fileFailure.message(), fileFailure.cause());
                toast.error(I18n.t(fileFailure.code()));
            }
            default -> toast.error(I18n.t(error.code()));
        }
    }

    private void handleValidationError(ValidationError validationError, Map<String, Consumer<String>> fieldHandlers) {
        Set<String> handledFields = new HashSet<>();
        validationError.violations().forEach(violation -> {
            if (handledFields.add(violation.field())) {
                handleValidationViolation(violation, fieldHandlers);
            }
        });
    }

    private void handleValidationViolation(FieldViolation violation, Map<String, Consumer<String>> fieldHandlers) {
        String messageKey = violation.messageKey();
        String localizedMessage = (messageKey == null || messageKey.isBlank())
                ? I18n.t("error.validation.failed")
                : I18n.t(messageKey);

        Consumer<String> handler = fieldHandlers.get(violation.field());
        if (handler != null) {
            handler.accept(localizedMessage);
            return;
        }

        toast.error(localizedMessage);
    }
}
