package com.triplify.ui.shared.util;

import com.triplify.application.shared.localization.LocalizedName;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.RouteIds;
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
import java.util.Set;
import java.util.UUID;

public final class DisplayUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private DisplayUtils() {}

    public static LocalDate toLocalDate(Instant value) {
        return value == null ? null : value.atZone(ZoneOffset.UTC).toLocalDate();
    }

    public static Instant toInstant(LocalDate value) {
        return value == null ? null : value.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null && end == null) return "Dates TBA";
        if (start != null && (end == null || start.equals(end))) {
            return start.format(DATE_FORMAT);
        }
        if (start != null && end != null) {
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
        return start == null ? end.format(DATE_FORMAT) : start.format(DATE_FORMAT);
    }

    public static String deriveCountryLabel(Set<CountryResponse> countries) {
        if (countries == null || countries.isEmpty()) return "";
        if (countries.size() == 1) return countries.iterator().next().name();
        return countries.iterator().next().name() + " +" + (countries.size() - 1);
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

    private static String resolveStatusLabel(StatusEnum status) {
        if (status == null) {
            return I18n.t("trip.status.unknown");
        }

        return switch (status) {
            case VISITED -> I18n.t("trip.status.visited");
            case ONGOING -> I18n.t("trip.status.ongoing");
            case PLANNED, CANCELED -> I18n.t("trip.status.planned");
        };
    }

    private static String resolveStatusCssClass(StatusEnum status) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case VISITED -> "trip-status-visited";
            case ONGOING -> "trip-status-ongoing";
            case PLANNED, CANCELED -> "trip-status-planned";
        };
    }

    public static void renderTripPlaceContext(Router router, VBox contextContainer, TripPlaceResponse tripPlace) {
        contextContainer.getChildren().clear();
        if (tripPlace.tripId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.trip"), () -> openTrip(router,tripPlace.tripId())));
        }
        if (tripPlace.tripRouteId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.route"), () -> openRoute(tripPlace.tripRouteId())));
        }
    }

    public static void renderStoryContext(Router router, VBox contextContainer, StoryResponse story) {
        contextContainer.getChildren().clear();
        if (story.tripId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.trip"), () -> openTrip(router,story.tripId())));
        }
        if (story.tripRouteId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.route"), () -> openRoute(story.tripRouteId())));
        }
        if (story.tripPlaceId() != null) {
            contextContainer.getChildren().add(buildContextLink(I18n.t("details.context.place"), () -> openPlace(story.tripPlaceId())));
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

    private static void openRoute(UUID tripRouteId) {
        // tripRouteId is not the same as routeId — no dedicated route details navigation
    }

    private static void openPlace(UUID tripPlaceId) {
        // tripPlaceId is not the same as placeId — no dedicated navigation without a service lookup
    }
}
