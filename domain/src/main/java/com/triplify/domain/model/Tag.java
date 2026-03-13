package com.triplify.domain.model;

import com.triplify.domain.model.enums.ColorEnum;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@Getter
public class Tag {
    private final UUID id;
    private final User user;
    private String name;
    private ColorEnum color;

    public Tag(@NonNull User user, @NonNull String name, ColorEnum color) {
        if (name.isBlank()) throw new IllegalArgumentException("Tag name must not be blank.");
        this.id = UUID.randomUUID();
        this.user = user;
        this.name = name;
        this.color = color != null ? color : ColorEnum.GRAY;
    }

    public void update(@NonNull String name, ColorEnum color) {
        this.name = name;
        this.color = color != null ? color : ColorEnum.GRAY;
    }
}
