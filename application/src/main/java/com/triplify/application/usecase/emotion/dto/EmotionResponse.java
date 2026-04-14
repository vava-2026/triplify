package com.triplify.application.usecase.emotion.dto;

import com.triplify.application.localization.LocalizedName;

public record EmotionResponse(
        String id,
        String createdById,
        String name,
        String nameSk,
        String emojiUnicode
) implements LocalizedName {
}
