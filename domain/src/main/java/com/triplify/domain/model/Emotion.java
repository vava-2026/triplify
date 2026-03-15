package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Emotion {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    private final UUID createdById;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String nameSk;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String emojiUnicode;

    @Builder(builderMethodName = "of")
    private Emotion(@NonNull UUID createdById,
                    @NonNull String name,
                    @NonNull String nameSk,
                    @NonNull String emojiUnicode) throws IllegalArgumentException {
        if (name.isBlank()) throw new IllegalArgumentException("Name must not be blank.");
        if (nameSk.isBlank()) throw new IllegalArgumentException("Slovak name must not be blank.");
        if (emojiUnicode.isBlank()) throw new IllegalArgumentException("Emoji unicode must not be blank.");
        this.id = UUID.randomUUID();
        this.createdById = createdById;
        this.name = name;
        this.nameSk = nameSk;
        this.emojiUnicode = emojiUnicode;
        log.debug("Emotion created: id={}, name={}", id, name);
    }

    public void updateName(@NonNull String name) throws IllegalArgumentException {
        if (name.isBlank()) throw new IllegalArgumentException("Name must not be blank.");
        log.debug("Emotion [{}] name: {} to {}", id, this.name, name);
        setName(name);
    }

    public void updateNameSk(@NonNull String nameSk) throws IllegalArgumentException {
        if (nameSk.isBlank()) throw new IllegalArgumentException("Slovak name must not be blank.");
        log.debug("Emotion [{}] nameSk: {} to {}", id, this.nameSk, nameSk);
        setNameSk(nameSk);
    }

    public void updateEmojiUnicode(@NonNull String emojiUnicode) throws IllegalArgumentException {
        if (emojiUnicode.isBlank()) throw new IllegalArgumentException("Emoji unicode must not be blank.");
        log.debug("Emotion [{}] emojiUnicode: {} to {}", id, this.emojiUnicode, emojiUnicode);
        setEmojiUnicode(emojiUnicode);
    }
}
