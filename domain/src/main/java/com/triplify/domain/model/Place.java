package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class Place {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id = UUID.randomUUID();

    @NonNull
    private final UUID userId;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private UUID countryId;

    @Setter(AccessLevel.PRIVATE)
    private UUID coverImageId;

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
    private final Instant createdAt = Instant.now();

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt = Instant.now();


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
        setUpdatedAt(Instant.now());
    }

    public void updateCoverImage(@NonNull UUID coverImageId) {
        log.debug("Place [{}] coverImage updated: {}", id, coverImageId);
        setCoverImageId(coverImageId);
        setUpdatedAt(Instant.now());
    }

    public void removeCoverImage() {
        log.debug("Place [{}] coverImage removed.", id);
        setCoverImageId(null);
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