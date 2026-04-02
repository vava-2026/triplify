package com.triplify.domain.model;

import com.triplify.domain.model.enums.StatusEnum;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class TripRoute {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    private final UUID tripId;

    @NonNull
    private final UUID routeId;

    @Setter(AccessLevel.PRIVATE)
    private Route route;

    @Setter(AccessLevel.PRIVATE)
    private int order;

    @NonNull
    @Setter(AccessLevel.PUBLIC)
    private StatusEnum status;

    @Setter(AccessLevel.PRIVATE)
    private Instant startedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant endedAt;

    @NonNull
    private final Instant createdAt;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    public TripRoute(@NonNull UUID tripId, @NonNull UUID routeId, int order) {
        this.id = UUID.randomUUID();
        this.tripId = tripId;
        this.routeId = routeId;
        this.order = order;
        this.status = StatusEnum.PLANNED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateOrder(int order) throws IllegalArgumentException {
        if (order < 0) throw new IllegalArgumentException("Order cannot be negative, got: " + order);
        log.debug("TripRoute [{}] order: {} to {}", id, this.order, order);
        setOrder(order);
        this.updatedAt = Instant.now();
    }

    public void linkRoute(@NonNull Route route) {
        if (!route.getId().equals(routeId)) {
            throw new IllegalArgumentException("route id mismatch. Expected: " + routeId + ", got: " + route.getId());
        }
        setRoute(route);
    }
}