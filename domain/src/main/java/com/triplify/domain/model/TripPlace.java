package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Getter
@ToString(exclude = {"imageIds"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class TripPlace {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    /** Reference to the owning Trip aggregate by ID only. */
    @NonNull
    private final UUID tripId;

    /** Reference to the Place aggregate by ID only. */
    @NonNull
    private final UUID placeId;

    @Setter(AccessLevel.PRIVATE)
    private Instant visitDate;

    @NonNull
    private final Instant createdAt;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    private final Set<UUID> imageIds;

    public TripPlace(@NonNull UUID tripId, @NonNull UUID placeId) {
        this.id = UUID.randomUUID();
        this.tripId = tripId;
        this.placeId = placeId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.imageIds = new LinkedHashSet<>();
    }

    public Set<UUID> getImageIds() {
        return Collections.unmodifiableSet(imageIds);
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

    public void addImage(@NonNull UUID imageId) {
        log.debug("TripPlace [{}] imageId added: {}", id, imageId);
        imageIds.add(imageId);
        this.updatedAt = Instant.now();
    }

    public void removeImage(@NonNull UUID imageId) {
        log.debug("TripPlace [{}] imageId removed: {}", id, imageId);
        imageIds.remove(imageId);
        this.updatedAt = Instant.now();
    }
}