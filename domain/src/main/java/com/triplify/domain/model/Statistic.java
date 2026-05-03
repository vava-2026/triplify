package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import com.triplify.domain.model.enums.StatisticType;

import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Statistic {
    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    private final UUID userId;

    @NonNull
    private final StatisticType type;

    @Setter(AccessLevel.PRIVATE)
    private long amount;

    public Statistic(@NonNull UUID userId, @NonNull StatisticType type) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.type = type;
        this.amount = 0;
    }

    public void incrementAmount() {
        this.amount++;
    }

    public void incrementAmount(long amount) {
        this.amount += amount;
    }

    public boolean isDisplayed() {
        return type.isDisplayed();
    }
}
