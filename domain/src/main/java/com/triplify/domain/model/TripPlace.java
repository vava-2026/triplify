package com.triplify.domain.model;

import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.model.enums.TripPlaceSourceType;
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

    @NonNull
    private final UUID tripId;

    @NonNull
    private final UUID placeId;

    @Setter(AccessLevel.PRIVATE)
    private Place place;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private TripPlaceSourceType sourceType;

    @Setter(AccessLevel.PRIVATE)
    private UUID tripRouteId;

    @Setter(AccessLevel.PRIVATE)
    private UUID routePlaceId;

    @Setter(AccessLevel.PRIVATE)
    private Instant visitDate;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private StatusEnum status;

    @NonNull
    private final Instant createdAt;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    public TripPlace(@NonNull UUID tripId, @NonNull UUID placeId) {
        this.id = UUID.randomUUID();
        this.tripId = tripId;
        this.placeId = placeId;
        this.sourceType = TripPlaceSourceType.MANUAL;
        this.status = StatusEnum.PLANNED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public TripPlace(@NonNull UUID tripId, @NonNull UUID placeId, @NonNull UUID tripRouteId, @NonNull UUID routePlaceId) {
        this.id = UUID.randomUUID();
        this.tripId = tripId;
        this.placeId = placeId;
        this.sourceType = TripPlaceSourceType.ROUTE;
        this.tripRouteId = tripRouteId;
        this.routePlaceId = routePlaceId;
        this.status = StatusEnum.PLANNED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateStatus(@NonNull StatusEnum status) {
        log.debug("TripPlace [{}] status: {} to {}", id, this.status, status);
        setStatus(status);
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

    public boolean isManual() {
        return sourceType == TripPlaceSourceType.MANUAL;
    }

    public boolean isRouteDerived() {
        return sourceType == TripPlaceSourceType.ROUTE;
    }
}
