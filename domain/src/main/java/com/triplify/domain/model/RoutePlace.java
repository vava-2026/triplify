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
public class RoutePlace {
    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id = UUID.randomUUID();

    @NonNull
    private final UUID routeId;

    @NonNull
    private final UUID placeId;

    @Setter(AccessLevel.PRIVATE)
    private int order;

    @NonNull
    private final Instant createdAt = Instant.now();

    @NonNull
    private Instant updatedAt =  Instant.now();

    public void updateOrder(int order) throws IllegalArgumentException {
        if (order < 0) throw new IllegalArgumentException("Order cannot be negative, got: " + order);
        log.debug("RoutePlace [{}] order: {} to {}", id, this.order, order);
        setOrder(order);
        this.updatedAt = Instant.now();
    }
}