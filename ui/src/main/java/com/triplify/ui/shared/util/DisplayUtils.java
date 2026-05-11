package com.triplify.ui.shared.util;

import com.triplify.application.shared.localization.LocalizedName;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.RouteIds;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.Router;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class DisplayUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter LOCALIZED_DATE_FORMAT = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private DisplayUtils() {}

    public static LocalDate toLocalDate(Instant value) {
        return value == null ? null : value.atZone(ZoneOffset.UTC).toLocalDate();
    }

    public static Instant toInstant(LocalDate value) {
        return value == null ? null : value.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static String formatDateRangeLocalized(LocalDate start, LocalDate end) {
        Locale locale = I18n.getLanguage().getLocale();
        DateTimeFormatter localizedDate = LOCALIZED_DATE_FORMAT.withLocale(locale);

        if (start == null && end == null) return I18n.t("trip.details.dates.tba");
        if (start != null && (end == null || start.equals(end))) {
            return start.format(localizedDate);
        }
        if (start != null) {
            if (start.getYear() == end.getYear() && start.getMonth() == end.getMonth()) {
                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("LLLL", locale);
                String monthString = start.format(monthFormatter);
                String capitalizedMonth = monthString.substring(0, 1).toUpperCase(locale) + monthString.substring(1);
                return String.format("%s %d - %d, %d",
                        capitalizedMonth,
                        start.getDayOfMonth(),
                        end.getDayOfMonth(),
                        start.getYear()
                );
            }
            return start.format(localizedDate) + " - " + end.format(localizedDate);
        }
        return end.format(localizedDate);
    }

    public static String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null && end == null) return "Dates TBA";
        if (start != null && (end == null || start.equals(end))) {
            return start.format(DATE_FORMAT);
        }
        if (start != null) {
            if (start.getYear() == end.getYear() && start.getMonth() == end.getMonth()) {
                return String.format("%s %d - %d, %d",
                        start.getMonth().name().substring(0, 1) + start.getMonth().name().substring(1).toLowerCase(),
                        start.getDayOfMonth(),
                        end.getDayOfMonth(),
                        start.getYear()
                );
            }
            return start.format(DATE_FORMAT) + " - " + end.format(DATE_FORMAT);
        }
        return end.format(DATE_FORMAT);
    }

    public static String deriveCoverUrl(ImageResponse coverImage) {
        if (coverImage == null || coverImage.url() == null) return null;
        return coverImage.url().toUri().toString();
    }

    public static void bindCountry(HBox countryRow, Label label, ImageView emojiView, CountryResponse country, int emojiSize) {
        label.textProperty().unbind();
        Localization.bindLocalizedText(label.textProperty(), country);

        boolean hasCountry = country != null;
        countryRow.setVisible(hasCountry);
        countryRow.setManaged(hasCountry);

        if (!hasCountry) {
            emojiView.setVisible(false);
            emojiView.setManaged(false);
            emojiView.setImage(null);
            return;
        }

        EditorUtils.applyEmojiImage(emojiView, country.emojiUnicode(), emojiSize);
    }

    public static void bindEmoji(HBox row, Label label, ImageView imageView, LocalizedName lName, String emoji, int size) {
        label.textProperty().unbind();
        Localization.bindLocalizedText(label.textProperty(), lName);

        row.setVisible(true);
        row.setManaged(true);

        EditorUtils.applyEmojiImage(imageView, emoji, size);
    }

    public static void applyStatus(Label statusLabel, StatusEnum status) {
        statusLabel.setText(resolveStatusLabel(status));
        statusLabel.getStyleClass().removeIf(style -> style.startsWith("trip-status-"));
        String statusClass = resolveStatusCssClass(status);
        if (statusClass != null) {
            statusLabel.getStyleClass().add(statusClass);
        }
    }

    public static String resolveStatusLabel(StatusEnum status) {
        if (status == null) {
            return I18n.t("trip.status.unknown");
        }

        return switch (status) {
            case VISITED -> I18n.t("trip.status.visited");
            case ONGOING -> I18n.t("trip.status.ongoing");
            case PLANNED -> I18n.t("trip.status.planned");
            case CANCELED -> I18n.t("trip.status.canceled");
        };
    }

    public static String resolveStatusCssClass(StatusEnum status) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case VISITED -> "trip-status-visited";
            case ONGOING -> "trip-status-ongoing";
            case PLANNED -> "trip-status-planned";
            case CANCELED -> "trip-status-rejected";
        };
    }

    public static void bindLocalDate(Instant startDate, Instant endDate, StringProperty dateProperty) {
        dateProperty.unbind();
        dateProperty.bind(Bindings.createStringBinding(
                () -> DisplayUtils.formatDateRangeLocalized(toLocalDate(startDate), toLocalDate(endDate)),
                I18n.languageProperty()
        ));
    }

    public static void renderTripPlaceContext(Router router, VBox contextContainer, TripPlaceResponse tripPlace) {
        contextContainer.getChildren().clear();
        if (tripPlace.tripId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.trip"), () -> openTrip(router,tripPlace.tripId())));
        }
        if (tripPlace.tripRouteId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.route"), () -> openTripRoute(router, tripPlace.tripRouteId())));
        }
    }

    public static void renderTripRouteContext(Router router, VBox contextContainer, TripRouteResponse tripRoute) {
        contextContainer.getChildren().clear();
        if (tripRoute.tripId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.trip"), () -> openTrip(router, tripRoute.tripId())));
        }
    }

    public static void renderStoryContext(Router router, VBox contextContainer, StoryResponse story) {
        contextContainer.getChildren().clear();
        if (story.tripId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.trip"), () -> openTrip(router,story.tripId())));
        }
        if (story.tripRouteId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.route"), () -> openTripRoute(router,story.tripRouteId())));
        }
        if (story.tripPlaceId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.place"), () -> openTripPlace(router,story.tripPlaceId())));
        }
    }

    private static Label buildContextLink(String text, Runnable action) {
        Label label = new Label(text);
        label.getStyleClass().add("story-details-context-link");
        label.setCursor(javafx.scene.Cursor.HAND);
        label.setOnMouseClicked(e -> action.run());
        return label;
    }

    private static void openTrip(Router router,  UUID tripId) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", tripId.toString());
        router.moveto(RouteIds.TRIP_DETAILS, args);
    }

    private static void openRoute(Router router, UUID routeId) {
        RouterArgument args = new RouterArgument();
        args.addArgument("routeId", routeId.toString());
        router.moveto(RouteIds.ROUTE_DETAILS, args);
    }

    private static void openTripRoute(Router router, UUID tripRouteId) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripRouteId", tripRouteId.toString());
        router.moveto(RouteIds.ROUTE_DETAILS, args);
    }

    private static void openTripPlace(Router router, UUID tripPlaceId) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripPlaceId", tripPlaceId.toString());
        router.moveto(RouteIds.PLACE_DETAILS, args);
    }
}
