package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoutePlace {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    private final UUID routeId;

    @NonNull
    private final UUID placeId;

    @Setter(AccessLevel.PRIVATE)
    private int order;

    @NonNull
    private final Instant createdAt;

    @NonNull
    private Instant updatedAt;

    @Builder(builderMethodName = "of")
    private RoutePlace(@NonNull UUID routeId,
                       @NonNull UUID placeId,
                       int order) throws IllegalArgumentException {
        if (order < 0) throw new IllegalArgumentException("Order cannot be negative, got: " + order);
        this.id = UUID.randomUUID();
        this.routeId = routeId;
        this.placeId = placeId;
        this.order = order;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        log.debug("RoutePlace created: id={}, routeId={}, placeId={}, order={}", id, routeId, placeId, order);
    }

    public void updateOrder(int order) throws IllegalArgumentException {
        if (order < 0) throw new IllegalArgumentException("Order cannot be negative, got: " + order);
        log.debug("RoutePlace [{}] order: {} to {}", id, this.order, order);
        setOrder(order);
        this.updatedAt = Instant.now();
    }
}