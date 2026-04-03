package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Place {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    private final UUID userId;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private UUID countryId;

    @Setter(AccessLevel.PRIVATE)
    private Country country;

    @Setter(AccessLevel.PRIVATE)
    private UUID coverImageId;

    @Setter(AccessLevel.PRIVATE)
    private Image coverImage;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String title;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @Setter(AccessLevel.PRIVATE)
    private double latitude;

    @Setter(AccessLevel.PRIVATE)
    private double longitude;

    @NonNull
    private final Instant createdAt;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    public Place(@NonNull UUID userId, @NonNull UUID countryId, @NonNull String title) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.countryId = countryId;
        this.title = title;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Place(@NonNull UUID userId, @NonNull UUID countryId, UUID coverImageId, @NonNull String title, String description, double latitude, double longitude) {
        this(userId, countryId, title);
        this.coverImageId = coverImageId;
        this.description = description;
        validateLocation(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Place(@NonNull UUID id, @NonNull UUID userId, @NonNull UUID countryId, UUID coverImageId, @NonNull String title, String description, double latitude, double longitude)
    {
        this.id = id;
        this.userId = userId;
        this.countryId = countryId;
        this.coverImageId = coverImageId;
        this.title = title;
        this.description = description;
        validateLocation(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateTitle(@NonNull String title) throws IllegalArgumentException {
        if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        log.debug("Place [{}] title: {} to {}", id, this.title, title);
        setTitle(title);
        setUpdatedAt(Instant.now());
    }

    public void updateDescription(String description) {
        log.debug("Place [{}] description updated.", id);
        setDescription(description);
        setUpdatedAt(Instant.now());
    }

    public void updateLocation(double latitude, double longitude) throws IllegalArgumentException {
        validateLocation(latitude, longitude);
        log.debug("Place [{}] location: ({}, {}) to ({}, {})", id, this.latitude, this.longitude, latitude, longitude);
        setLatitude(latitude);
        setLongitude(longitude);
        setUpdatedAt(Instant.now());
    }

    public void updateCountry(@NonNull UUID countryId) {
        log.debug("Place [{}] country: {} to {}", id, this.countryId, countryId);
        setCountryId(countryId);
        setCountry(null);
        setUpdatedAt(Instant.now());
    }

    public void updateCountry(@NonNull Country country) {
        if (!country.getId().equals(countryId)) {
            log.debug("Place [{}] country changed via object: {} to {}", id, this.countryId, country.getId());
            setCountryId(country.getId());
        }
        setCountry(country);
        setUpdatedAt(Instant.now());
    }

    public void updateCoverImage(@NonNull UUID coverImageId) {
        log.debug("Place [{}] coverImage updated: {}", id, coverImageId);
        setCoverImageId(coverImageId);
        setCoverImage(null);
        setUpdatedAt(Instant.now());
    }

    public void updateCoverImage(@NonNull Image coverImage) {
        log.debug("Place [{}] coverImage linked: {}", id, coverImage.getId());
        setCoverImageId(coverImage.getId());
        setCoverImage(coverImage);
        setUpdatedAt(Instant.now());
    }

    public void removeCoverImage() {
        log.debug("Place [{}] coverImage removed.", id);
        setCoverImageId(null);
        setCoverImage(null);
        setUpdatedAt(Instant.now());
    }

    private void validateLocation(double latitude, double longitude) throws IllegalArgumentException {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90, got: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180, got: " + longitude);
        }
    }
}