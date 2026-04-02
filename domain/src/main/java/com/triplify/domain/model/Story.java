package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Story {
    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    private final UUID userId;

    private final UUID tripId;

    private final UUID tripRouteId;

    private final UUID tripPlaceId;

    @Setter(AccessLevel.PRIVATE)
    private UUID emotionId;

    @Setter(AccessLevel.PRIVATE)
    private Emotion emotion;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String title;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant storyTime;

    @NonNull
    private final Instant createdAt;

    private final Set<UUID> tagIds;
    private final Set<Tag> tags;

    public Story(@NonNull UUID userId,
                  UUID tripId,
                  UUID tripRouteId,
                  UUID tripPlaceId,
                  UUID emotionId,
                  @NonNull String title,
                  String description,
                  @NonNull Instant storyTime) throws IllegalArgumentException {
        if (tripId == null && tripRouteId == null && tripPlaceId == null) {
            throw new IllegalArgumentException("Story must be linked to at least one of: tripId, tripRouteId, tripPlaceId.");
        }
        if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.tripId = tripId;
        this.tripRouteId = tripRouteId;
        this.tripPlaceId = tripPlaceId;
        this.emotionId = emotionId;
        this.title = title;
        this.description = description;
        this.storyTime = storyTime;
        this.createdAt = Instant.now();
        this.tagIds = new LinkedHashSet<>();
        this.tags = new LinkedHashSet<>();
        log.debug("Story created: id={}, userId={}, tripId={}, tripRouteId={}, tripPlaceId={}", id, userId, tripId, tripRouteId, tripPlaceId);
    }

    public static Story forTrip(@NonNull UUID userId, @NonNull UUID tripId,
                                @NonNull String title, String description,
                                @NonNull Instant storyTime, UUID emotionId) throws IllegalArgumentException {
        return new Story(userId, tripId, null, null, emotionId, title, description, storyTime);
    }

    public static Story forTripRoute(@NonNull UUID userId, @NonNull UUID tripRouteId,
                                     @NonNull String title, String description,
                                     @NonNull Instant storyTime, UUID emotionId) throws IllegalArgumentException {
        return new Story(userId, null, tripRouteId, null, emotionId, title, description, storyTime);
    }

    public static Story forTripPlace(@NonNull UUID userId, @NonNull UUID tripPlaceId,
                                     @NonNull String title, String description,
                                     @NonNull Instant storyTime, UUID emotionId) throws IllegalArgumentException {
        return new Story(userId, null, null, tripPlaceId, emotionId, title, description, storyTime);
    }

    public Set<UUID> getTagIds() {
        return Collections.unmodifiableSet(tagIds);
    }
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void updateTitle(@NonNull String title) throws IllegalArgumentException {
        if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        log.debug("Story [{}] title: {} to {}", id, this.title, title);
        setTitle(title);
    }

    public void updateDescription(String description) {
        log.debug("Story [{}] description updated.", id);
        setDescription(description);
    }

    public void updateStoryTime(@NonNull Instant storyTime) {
        log.debug("Story [{}] storyTime: {} to {}", id, this.storyTime, storyTime);
        setStoryTime(storyTime);
    }

    public void updateEmotion(UUID emotionId) {
        log.debug("Story [{}] emotionId: {} to {}", id, this.emotionId, emotionId);
        setEmotionId(emotionId);
        setEmotion(null);
    }

    public void updateEmotion(@NonNull Emotion emotion) {
        log.debug("Story [{}] emotion linked: {}", id, emotion.getId());
        setEmotionId(emotion.getId());
        setEmotion(emotion);
    }

    public void clearEmotion() {
        log.debug("Story [{}] emotionId cleared.", id);
        setEmotionId(null);
        setEmotion(null);
    }

    public void addTag(@NonNull UUID tagId) {
        log.debug("Trip [{}] tag added: {}", id, tagId);
        tagIds.add(tagId);
    }

    public void addTag(@NonNull Tag tag) {
        log.debug("Trip [{}] tag linked: {}", id, tag.getId());
        tagIds.add(tag.getId());
        tags.removeIf(existing -> existing.getId().equals(tag.getId()));
        tags.add(tag);
    }

    public void removeTag(@NonNull UUID tagId) {
        log.debug("Trip [{}] tag removed: {}", id, tagId);
        tagIds.remove(tagId);
        tags.removeIf(tag -> tag.getId().equals(tagId));
    }
}