package com.triplify.domain.model;

import com.triplify.domain.model.enums.ColorEnum;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class Tag {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id = UUID.randomUUID();

    @NonNull
    private final UUID userId;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private ColorEnum color;

    public void updateName(@NonNull String name) {
        if (name.isBlank()) throw new IllegalArgumentException("Tag name must not be blank.");
        log.debug("Tag [{}] name: {} to {}", id, this.name, name);
        setName(name);
    }

    public void updateColor(ColorEnum color) {
        log.debug("Tag [{}] color: {} to {}", id, this.color, color);
        setColor(color != null ? color : ColorEnum.TEAL);
    }
}