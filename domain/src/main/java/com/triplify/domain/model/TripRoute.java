package com.triplify.domain.model;

import com.triplify.domain.model.enums.StatusEnum;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class TripRoute {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id = UUID.randomUUID();

    @NonNull
    private final UUID tripId;

    @NonNull
    private final UUID routeId;

    @Setter(AccessLevel.PRIVATE)
    private int order;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private StatusEnum status;

    @Setter(AccessLevel.PRIVATE)
    private Instant startedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant endedAt;

    @NonNull
    private final Instant createdAt = Instant.now();

    @NonNull
    private Instant updatedAt = Instant.now();

    private final Set<UUID> imageIds = new LinkedHashSet<>();

    public Set<UUID> getImageIds() {
        return Collections.unmodifiableSet(imageIds);
    }

    public void updateOrder(int order) throws IllegalArgumentException {
        if (order < 0) throw new IllegalArgumentException("Order cannot be negative, got: " + order);
        log.debug("TripRoute [{}] order: {} to {}", id, this.order, order);
        setOrder(order);
        this.updatedAt = Instant.now();
    }

    public void start(@NonNull Instant startedAt) throws IllegalStateException {
        if (this.status != StatusEnum.PLANNED) {
            throw new IllegalStateException("Only planned trip routes can be started, current status: " + this.status);
        }
        log.debug("TripRoute [{}] started at: {}", id, startedAt);
        setStatus(StatusEnum.ONGOING);
        setStartedAt(startedAt);
        this.updatedAt = Instant.now();
    }

    public void complete(@NonNull Instant endedAt) throws IllegalStateException, IllegalArgumentException {
        if (this.status != StatusEnum.ONGOING) {
            throw new IllegalStateException("Only ongoing trip routes can be completed, current status: " + this.status);
        }
        if (startedAt != null && endedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        log.debug("TripRoute [{}] completed at: {}", id, endedAt);
        setStatus(StatusEnum.VISITED);
        setEndedAt(endedAt);
        this.updatedAt = Instant.now();
    }

    public void cancel() throws IllegalStateException {
        if (this.status == StatusEnum.VISITED) {
            throw new IllegalStateException("Completed trip routes cannot be cancelled.");
        }
        log.debug("TripRoute [{}] cancelled, previous status: {}", id, this.status);
        setStatus(StatusEnum.CANCELED);
        this.updatedAt = Instant.now();
    }

    public void addImage(@NonNull UUID imageId) {
        log.debug("TripRoute [{}] imageId added: {}", id, imageId);
        imageIds.add(imageId);
        this.updatedAt = Instant.now();
    }

    public void removeImage(@NonNull UUID imageId) {
        log.debug("TripRoute [{}] imageId removed: {}", id, imageId);
        imageIds.remove(imageId);
        this.updatedAt = Instant.now();
    }
}