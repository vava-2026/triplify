package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Getter
@ToString(exclude = {"createdById"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class Country {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id = UUID.randomUUID();

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

    @Setter(AccessLevel.PRIVATE)
    private boolean isAvailable = true;

    public void updateName(@NonNull String name) throws IllegalArgumentException{
        if (name.isBlank()) throw new IllegalArgumentException("Name must not be blank.");
        log.debug("Country [{}] name: {} to {}", id, this.name, name);
        setName(name);
    }

    public void updateNameSk(@NonNull String nameSk) throws IllegalArgumentException {
        if (nameSk.isBlank()) throw new IllegalArgumentException("Slovak name must not be blank.");
        log.debug("Country [{}] nameSk: {} to {}", id, this.nameSk, nameSk);
        setNameSk(nameSk);
    }

    public void updateEmojiUnicode(@NonNull String emojiUnicode) throws IllegalArgumentException {
        if (emojiUnicode.isBlank()) throw new IllegalArgumentException("Emoji unicode must not be blank.");
        log.debug("Country [{}] emojiUnicode: {} to {}", id, this.emojiUnicode, emojiUnicode);
        setEmojiUnicode(emojiUnicode);
    }

    public void enable() {
        log.debug("Country [{}] enabled.", id);
        setAvailable(true);
    }

    public void disable() {
        log.debug("Country [{}] disabled.", id);
        setAvailable(false);
    }
}