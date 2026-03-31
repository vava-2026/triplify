package com.triplify.application.usecase.country.dto;

import com.triplify.domain.model.Country;

public record CountryResponse(
        String id,
        String createdById,
        String name,
        String nameSk,
        String emojiUnicode,
        boolean isAvailable
) {
    public static CountryResponse from(Country country) {
        return new CountryResponse(
                country.getId().toString(),
                country.getCreatedById().toString(),
                country.getName(),
                country.getNameSk(),
                country.getEmojiUnicode(),
                country.isAvailable()
        );
    }
}
