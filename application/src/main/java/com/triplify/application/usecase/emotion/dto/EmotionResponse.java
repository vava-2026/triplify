package com.triplify.application.usecase.emotion.dto;

public record EmotionResponse(
        String id,
        String createdById,
        String name,
        String nameSk,
        String emojiUnicode
) {
}
