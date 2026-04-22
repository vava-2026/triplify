package com.triplify.application.usecase.emotion.dto;

import com.triplify.application.localization.LocalizedName;
import com.triplify.domain.model.Emotion;

import java.util.UUID;

public record EmotionResponse(
        UUID id,
        UUID createdById,
        String name,
        String nameSk,
        String emojiUnicode
) implements LocalizedName {
    public static EmotionResponse fromDomain(Emotion emotion) {
        return new EmotionResponse(
                emotion.getId(),
                emotion.getCreatedById(),
                emotion.getName(),
                emotion.getNameSk(),
                emotion.getEmojiUnicode()
        );
    }
}
