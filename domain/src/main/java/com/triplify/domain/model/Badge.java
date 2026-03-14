package com.triplify.domain.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Slf4j
@Getter
@ToString(exclude = {"createdBy", "group", "image"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Badge {

    @NonNull
    private final UUID id;

    @NonNull
    private final User createdBy;

    @NonNull
    private final BadgeGroup group;

    @Setter(AccessLevel.PRIVATE)
    private Image image;

    @NonNull
    private String name;

    @NonNull
    private String nameSk;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @Setter(AccessLevel.PRIVATE)
    private String descriptionSk;

    /**
     * Difficulty tier — must be >= 1. Immutable after creation.
     */
    private final int level;

    /**
     * Progress threshold a user must reach to unlock this badge.
     */
    private int requiredValue;

    @Builder(builderMethodName = "of")
    private Badge(@NonNull User createdBy,
                  @NonNull BadgeGroup group,
                  @NonNull String name,
                  @NonNull String nameSk,
                  String description,
                  String descriptionSk,
                  @Builder.ObtainVia(method = "defaultLevel") int level,
                  int requiredValue,
                  Image image) {
        if (level < 1) throw new IllegalArgumentException("Badge level must be >= 1, got: " + level);
        if (requiredValue < 0) throw new IllegalArgumentException("Required value cannot be negative, got: " + requiredValue);
        this.id = UUID.randomUUID();
        this.createdBy = createdBy;
        this.group = group;
        this.name = name;
        this.nameSk = nameSk;
        this.description = description;
        this.descriptionSk = descriptionSk;
        this.level = level;
        this.requiredValue = requiredValue;
        this.image = image;
        log.debug("Badge created: name={}, level={}, requiredValue={}", name, level, requiredValue);
    }

    @SuppressWarnings("unused")
    private static int defaultLevel() {
        return 1;
    }

    public void updateDetails(@NonNull String name,
                              @NonNull String nameSk,
                              String description,
                              String descriptionSk,
                              int requiredValue) {
        if (requiredValue < 0) throw new IllegalArgumentException("Required value cannot be negative.");
        log.debug("Updating Badge [{}]: name {} -> {}, requiredValue {} -> {}", getId(), this.name, name, this.requiredValue, requiredValue);
        this.name = name;
        this.nameSk = nameSk;
        setDescription(description);
        setDescriptionSk(descriptionSk);
        this.requiredValue = requiredValue;
    }

    public void updateImage(Image image) {
        log.debug("Updating image for Badge [{}]", getId());
        setImage(image);
    }

    public void removeImage() {
        log.debug("Removing image from Badge [{}]", getId());
        setImage(null);
    }
}