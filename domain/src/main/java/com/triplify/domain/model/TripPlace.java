package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class TripPlace {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    /** Reference to the owning Trip aggregate by ID only. */
    @NonNull
    private final UUID tripId;

    /** Related Place reference by stable ID, with optional linked object. */
    @NonNull
    private final UUID placeId;

    @Setter(AccessLevel.PRIVATE)
    private Place place;

    @Setter(AccessLevel.PRIVATE)
    private Instant visitDate;

    @NonNull
    private final Instant createdAt;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    public TripPlace(@NonNull UUID tripId, @NonNull UUID placeId) {
        this.id = UUID.randomUUID();
        this.tripId = tripId;
        this.placeId = placeId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void scheduleVisit(Instant visitDate) {
        log.debug("TripPlace [{}] visitDate: {} to {}", id, this.visitDate, visitDate);
        setVisitDate(visitDate);
        this.updatedAt = Instant.now();
    }

    public void clearVisitDate() {
        log.debug("TripPlace [{}] visitDate cleared.", id);
        setVisitDate(null);
        this.updatedAt = Instant.now();
    }

    public void linkPlace(@NonNull Place place) {
        if (!place.getId().equals(placeId)) {
            throw new IllegalArgumentException("place id mismatch. Expected: " + placeId + ", got: " + place.getId());
        }
        setPlace(place);
    }
}