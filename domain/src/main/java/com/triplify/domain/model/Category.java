package com.triplify.domain.model;

import com.triplify.domain.model.enums.ColorEnum;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;


@Slf4j
@Getter
@ToString(exclude = {"createdById"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Category {
    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    private final UUID createdById;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String nameSk;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @Setter(AccessLevel.PRIVATE)
    private String descriptionSk;

    @Setter(AccessLevel.PRIVATE)
    private String emojiUnicode;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private ColorEnum color;

    public Category(@NonNull UUID createdById, @NonNull String name, @NonNull String nameSk, @NonNull ColorEnum color) {
        this.id = UUID.randomUUID();
        this.createdById = createdById;
        this.name = name;
        this.nameSk = nameSk;
        this.color = color;
    }

    public void updateName(@NonNull String name) throws IllegalArgumentException {
        if (name.isBlank()) throw new IllegalArgumentException("Name must not be blank.");
        log.debug("Category [{}] name: {} to {}", id, this.name, name);
        setName(name);
    }

    public void updateNameSk(@NonNull String nameSk) throws IllegalArgumentException {
        if (nameSk.isBlank()) throw new IllegalArgumentException("Slovak name must not be blank.");
        log.debug("Category [{}] nameSk: {} to {}", id, this.nameSk, nameSk);
        setNameSk(nameSk);
    }

    public void updateDescription(String description) {
        log.debug("Category [{}] description updated.", id);
        setDescription(description);
    }

    public void updateDescriptionSk(String descriptionSk) {
        log.debug("Category [{}] descriptionSk updated.", id);
        setDescriptionSk(descriptionSk);
    }

    public void updateEmojiUnicode(String emojiUnicode) {
        log.debug("Category [{}] emojiUnicode: {} to {}", id, this.emojiUnicode, emojiUnicode);
        setEmojiUnicode(emojiUnicode);
    }

    public void updateColor(@NonNull ColorEnum color) {
        log.debug("Category [{}] color: {} to {}", id, this.color, color);
        setColor(color);
    }
}
