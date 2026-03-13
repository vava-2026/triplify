package com.triplify.domain.model;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@Getter
public class Country {
    private final UUID id;
    private final User createdBy;
    private String name;
    private String nameSk;
    private String emojiUnicode;
    private boolean available;

    public Country(@NonNull User createdBy, @NonNull String name, @NonNull String nameSk, @NonNull String emojiUnicode) {
        if (name.isBlank()) throw new IllegalArgumentException("Name must not be blank.");
        if (nameSk.isBlank()) throw new IllegalArgumentException("Slovak name must not be blank.");
        if (emojiUnicode.isBlank()) throw new IllegalArgumentException("Emoji unicode must not be blank.");
        this.id = UUID.randomUUID();
        this.createdBy = createdBy;
        this.name = name;
        this.nameSk = nameSk;
        this.emojiUnicode = emojiUnicode;
        this.available = true;
    }

    public void update(@NonNull String name, @NonNull String nameSk, @NonNull String emojiUnicode) {
        this.name = name;
        this.nameSk = nameSk;
        this.emojiUnicode = emojiUnicode;
    }

    public void enable() {
        this.available = true;
    }

    public void disable() {
        this.available = false;
    }
}