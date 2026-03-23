package com.triplify.ui.error;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.domain.error.AppError;
import com.triplify.domain.error.AuthError;
import com.triplify.domain.error.BadgeError;
import com.triplify.domain.error.BadgeGroupError;
import com.triplify.domain.error.CategoryError;
import com.triplify.domain.error.CountryError;
import com.triplify.domain.error.DomainError;
import com.triplify.domain.error.EmotionError;
import com.triplify.domain.error.FieldViolation;
import com.triplify.domain.error.ImageError;
import com.triplify.domain.error.PlaceError;
import com.triplify.domain.error.RouteError;
import com.triplify.domain.error.StoryError;
import com.triplify.domain.error.TagError;
import com.triplify.domain.error.TripError;
import com.triplify.domain.error.TripPlaceError;
import com.triplify.domain.error.TripRouteError;
import com.triplify.domain.error.UserError;
import com.triplify.domain.error.ValidationError;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.toast.ToastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
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
            case ValidationError validationError -> validationError.violations().forEach(v -> handleValidationViolation(v, fieldHandlers));

            case AuthError.InvalidCredentials ignored -> toast.error(I18n.t("error.auth.invalid.credentials"));
            case AuthError.SessionExpired ignored -> toast.error(I18n.t("error.auth.session.expired"));

            case UserError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case UserError.AlreadyExists ignored -> toast.error(I18n.t("error.already.exists"));
            case UserError.Unauthorized ignored -> toast.error(I18n.t("error.unauthorized"));
            case UserError.Forbidden ignored -> toast.error(I18n.t("error.forbidden"));
            case UserError.InvalidCurrentPassword ignored -> toast.error(I18n.t("error.user.invalid.current.password"));

            case CountryError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case CountryError.AlreadyExists ignored -> toast.error(I18n.t("error.already.exists"));
            case CountryError.AlreadyBanned ignored -> toast.warning(I18n.t("error.country.already.banned"));
            case CountryError.NotBanned ignored -> toast.warning(I18n.t("error.country.not.banned"));
            case CountryError.NotOwner ignored -> toast.error(I18n.t("error.not.owner"));

            case CategoryError.NotFound ignored -> toast.error(I18n.t("error.category.not.found"));
            case CategoryError.AlreadyExists ignored -> toast.error(I18n.t("error.category.already.exists"));

            case TagError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case TagError.AlreadyExists ignored -> toast.error(I18n.t("error.already.exists"));

            case EmotionError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case EmotionError.AlreadyExists ignored -> toast.error(I18n.t("error.already.exists"));

            case BadgeGroupError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case BadgeError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case BadgeError.AlreadyExists ignored -> toast.error(I18n.t("error.already.exists"));

            case PlaceError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case PlaceError.NotOwner ignored -> toast.error(I18n.t("error.not.owner"));
            case PlaceError.InvalidCoordinates ignored -> toast.error(I18n.t("error.place.invalid.coordinates"));

            case TripError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case TripError.NotOwner ignored -> toast.error(I18n.t("error.not.owner"));
            case TripError.InvalidDates ignored -> toast.error(I18n.t("error.trip.invalid.dates"));
            case TripError.InvalidStatusTransition ignored -> toast.error(I18n.t("error.invalid.status.transition"));

            case RouteError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case RouteError.NotOwner ignored -> toast.error(I18n.t("error.not.owner"));
            case RouteError.PlaceNotInRoute ignored -> toast.error(I18n.t("error.route.place.not.in.route"));
            case RouteError.InvalidStatusTransition ignored -> toast.error(I18n.t("error.invalid.status.transition"));
            case RouteError.TooFewPlaces ignored -> toast.error(I18n.t("error.route.too.few.places"));

            case TripPlaceError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case TripPlaceError.InvalidStatusTransition ignored -> toast.error(I18n.t("error.invalid.status.transition"));

            case TripRouteError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case TripRouteError.InvalidStatusTransition ignored -> toast.error(I18n.t("error.invalid.status.transition"));

            case StoryError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case StoryError.NotOwner ignored -> toast.error(I18n.t("error.not.owner"));
            case StoryError.PremiumRequired ignored -> toast.error(I18n.t("error.story.premium.required"));
            case StoryError.InvalidStatusTransition ignored -> toast.error(I18n.t("error.invalid.status.transition"));

            case ImageError.NotFound ignored -> toast.error(I18n.t("error.not.found"));
            case ImageError.InvalidFormat ignored -> toast.error(I18n.t("error.image.invalid.format"));
            case ImageError.TooLarge ignored -> toast.error(I18n.t("error.image.too.large"));

            case ApplicationError.Unexpected unexpected -> {
                log.error("Unexpected application error: {}", unexpected.message());
                toast.error(I18n.t("error.unexpected"));
            }
            case ApplicationError.StorageFailure storageFailure -> {
                log.error("{}", storageFailure.message(), storageFailure.cause());
                toast.error(I18n.t("error.storage"));
            }
            case ApplicationError.FileFailure fileFailure -> {
                log.error("{}", fileFailure.message(), fileFailure.cause());
                toast.error(I18n.t("error.file"));
            }
            case DomainError domainError -> {
                log.warn("Unhandled domain error mapping: {} - {}", domainError.code(), domainError.message());
                toast.error(I18n.t("error.domain"));
            }
            default -> {
                log.error("Unhandled app error mapping: {} - {}", error.code(), error.message());
                toast.error(I18n.t("error.unexpected"));
            }
        }
    }

    private void handleValidationViolation(FieldViolation violation, Map<String, Consumer<String>> fieldHandlers) {
        Consumer<String> handler = fieldHandlers.get(violation.field());
        if (handler != null) {
            handler.accept(I18n.t(violation.messageKey()));
            return;
        }

        toast.error(I18n.t("error.validation.failed"));
    }
}
