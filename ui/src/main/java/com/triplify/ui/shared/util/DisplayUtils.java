package com.triplify.ui.shared.util;

import com.triplify.application.shared.localization.LocalizedName;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.domain.model.enums.StatusEnum;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;

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
}
