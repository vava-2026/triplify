package com.triplify.application.usecase.country.dto;

import com.triplify.application.localization.LocalizedName;
import com.triplify.domain.model.Country;

import java.util.UUID;

public record CountryResponse(
        UUID id,
        UUID createdById,
        String name,
        String nameSk,
        String emojiUnicode,
        boolean isAvailable
) implements LocalizedName {
    public static CountryResponse from(Country country) {
        return new CountryResponse(
                country.getId(),
                country.getCreatedById(),
                country.getName(),
                country.getNameSk(),
                country.getEmojiUnicode(),
                country.isAvailable()
        );
    }
}
