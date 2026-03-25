package com.triplify.application.usecase.country.dto;

public record CountryResponse(
        String id,
        String createdById,
        String name,
        String nameSk,
        String emojiUnicode,
        boolean isAvailable
) {
}
