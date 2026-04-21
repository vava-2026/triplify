package com.triplify.application.usecase.story.dto;

import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.domain.model.Emotion;
import com.triplify.domain.model.Image;
import com.triplify.domain.model.Story;
import com.triplify.domain.pagination.Page;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record StoryResponse(
        UUID id,
        UUID userId,
        UUID tripId,
        UUID tripRouteId,
        UUID tripPlaceId,
        EmotionResponse emotion,
        String title,
        String description,
        Instant storyTime,
        Instant createdAt,
        Set<TagResponse> tags,
        Set<ImageResponse> images
) {
    public static StoryResponse from(Story story, Page<Image> imagesPage) {
        EmotionResponse emotionResponse = null;
        Emotion emotion = story.getEmotion();
        if (emotion != null) {
            emotionResponse = new EmotionResponse(
                    emotion.getId().toString(),
                    emotion.getCreatedById().toString(),
                    emotion.getName(),
                    emotion.getNameSk(),
                    emotion.getEmojiUnicode()
            );
        }

        Set<TagResponse> tagResponses = story.getTags().stream()
                .map(TagResponse::from)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<ImageResponse> imageResponses = imagesPage.items().stream()
                .map(ImageResponse::from)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new StoryResponse(
                story.getId().toString(),
                story.getUserId().toString(),
                uuidStr(story.getTripId()),
                uuidStr(story.getTripRouteId()),
                uuidStr(story.getTripPlaceId()),
                emotionResponse,
                story.getTitle(),
                story.getDescription(),
                story.getStoryTime(),
                story.getCreatedAt(),
                tagResponses,
                imageResponses
        );
    }

    private static String uuidStr(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }
}
