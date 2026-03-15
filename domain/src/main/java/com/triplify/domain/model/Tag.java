package com.triplify.domain.model;

import com.triplify.domain.model.enums.ColorEnum;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tag {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    private final UUID userId;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private ColorEnum color;

    @Builder(builderMethodName = "of")
    private Tag(@NonNull UUID userId, @NonNull String name, ColorEnum color) {
        if (name.isBlank()) throw new IllegalArgumentException("Tag name must not be blank.");
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.color = color != null ? color : ColorEnum.GRAY;
        log.debug("Tag created: id={}, name={}, color={}", id, name, this.color);
    }

    public void updateName(@NonNull String name) {
        if (name.isBlank()) throw new IllegalArgumentException("Tag name must not be blank.");
        log.debug("Tag [{}] name: {} to {}", id, this.name, name);
        setName(name);
    }

    public void updateColor(ColorEnum color) {
        log.debug("Tag [{}] color: {} to {}", id, this.color, color);
        setColor(color != null ? color : ColorEnum.GRAY);
    }
}