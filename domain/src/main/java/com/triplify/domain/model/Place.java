package com.triplify.domain.model;


import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@ToString
@Getter
public class Place {
    private final UUID id;
    private final User user;
    private Country country;
    private Image coverImage;
    private String title;
    private String description;
    private double latitude;
    private double longitude;
    private final Instant createdAt;
    private Instant updatedAt;

    public Place(@NonNull User user, @NonNull Country country, @NonNull String title, @NonNull String description, double latitude, double longitude) {
        if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        validateLocation(latitude, longitude);

        this.id = UUID.randomUUID();
        this.user = user;
        this.country = country;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateTitle(@NonNull String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public void updateDescription(@NonNull String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void updateLocation(double latitude, double longitude) throws IllegalArgumentException {
        validateLocation(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = Instant.now();
    }

    public void updateCountry(@NonNull Country country) {
        this.country = country;
        this.updatedAt = Instant.now();
    }

    public void updateCoverImage(@NonNull Image coverImage) {
        this.coverImage = coverImage;
        this.updatedAt = Instant.now();
    }

    public void removeCoverImage() {
        this.coverImage = null;
        this.updatedAt = Instant.now();
    }

    private void validateLocation(double latitude, double longitude) throws IllegalArgumentException{
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90, got: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180, got: " + longitude);
        }
    }
}