package com.triplify.domain.model;

import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Story {
    private final UUID id;
    private final User user;
    private final Trip trip;
    private final TripRoute tripRoute;
    private final TripPlace tripPlace;
    private Emotion emotion;
    private String title;
    private String description;
    private Instant storyTime;
    private final Instant createdAt;
    private final Set<Tag> tags = new HashSet<>();
    private final Set<Image> images = new LinkedHashSet<>();

    private Story(@NonNull User user, Trip trip, TripRoute tripRoute, TripPlace tripPlace,
                  Emotion emotion, String title, String description, Instant storyTime) {
        if (tripRoute == null && tripPlace == null) {
            throw new IllegalArgumentException("Story must be linked to at least one of: trip, tripRoute, tripPlace.");
        }
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        if (storyTime == null) throw new IllegalArgumentException("Story time must not be null.");
        this.user = user;
        this.trip = trip;
        this.tripRoute = tripRoute;
        this.tripPlace = tripPlace;
        this.emotion = emotion;
        this.title = title;
        this.description = description;
        this.storyTime = storyTime;
        this.createdAt = Instant.now();
    }

    public static Story forTrip(User user, Trip trip, String title,
                                String description, Instant storyTime, Emotion emotion) {
        return new Story(user, trip, null, null, emotion, title, description, storyTime);
    }

    public static Story forTripRoute(User user, TripRoute tripRoute, String title,
                                     String description, Instant storyTime, Emotion emotion) {
        return new Story(user, null, tripRoute, null, emotion, title, description, storyTime);
    }

    public static Story forTripPlace(User user, TripPlace tripPlace, String title,
                                     String description, Instant storyTime, Emotion emotion) {
        return new Story(user, null, null, tripPlace, emotion, title, description, storyTime);
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public Set<Image> getImages() {
        return Collections.unmodifiableSet(images);
    }

    public void updateContent(String title, String description, Instant storyTime, Emotion emotion) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        if (storyTime == null) throw new IllegalArgumentException("Story time must not be null.");
        this.title = title;
        this.description = description;
        this.storyTime = storyTime;
        this.emotion = emotion;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    public void addImage(Image image) {
        this.images.add(image);
    }

    public void removeImage(Image image) {
        this.images.remove(image);
    }
}
