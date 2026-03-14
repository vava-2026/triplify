package com.triplify.domain.model;

import com.triplify.domain.model.enums.ColorEnum;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.UUID;

@ToString
@Getter
public class Category {
    private final UUID id;
    private final User createdBy;
    private final String name;
    private final String nameSk;
    private final String description;
    private final String descriptionSk;
    private final String emojiUnicode;
    private final ColorEnum color;

    public Category(@NonNull User createdBy, @NonNull String name, @NonNull String nameSk,
                     @NonNull String description, @NonNull String descriptionSk,
                     @NonNull String emojiUnicode, ColorEnum color) {
        if (name.isBlank()) throw new IllegalArgumentException("Name must not be blank.");
        if (nameSk.isBlank()) throw new IllegalArgumentException("Slovak name must not be blank.");
        this.id = UUID.randomUUID();
        this.createdBy = createdBy;
        this.name = name;
        this.nameSk = nameSk;
        this.description = description;
        this.descriptionSk = descriptionSk;
        this.emojiUnicode = emojiUnicode;
        this.color = color != null ? color : ColorEnum.GRAY;
    }
}
