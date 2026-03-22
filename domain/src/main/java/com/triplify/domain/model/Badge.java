package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class Badge {
    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id = UUID.randomUUID();

    @NonNull
    private final UUID createdById;

    @NonNull
    private final UUID groupId;

    @Setter(AccessLevel.PRIVATE)
    private UUID imageId;

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

    private final int level;

    @Setter(AccessLevel.PRIVATE)
    private int requiredValue;

    public void updateName(@NonNull String name) throws IllegalArgumentException {
        if (name.isBlank()) throw new IllegalArgumentException("Name must not be blank.");
        log.debug("Badge [{}] name: {} to {}", id, this.name, name);
        setName(name);
    }

    public void updateNameSk(@NonNull String nameSk) throws IllegalArgumentException {
        if (nameSk.isBlank()) throw new IllegalArgumentException("Slovak name must not be blank.");
        log.debug("Badge [{}] nameSk: {} to {}", id, this.nameSk, nameSk);
        setNameSk(nameSk);
    }

    public void updateDescription(String description) {
        log.debug("Badge [{}] description updated.", id);
        setDescription(description);
    }

    public void updateDescriptionSk(String descriptionSk) {
        log.debug("Badge [{}] descriptionSk updated.", id);
        setDescriptionSk(descriptionSk);
    }

    public void updateRequiredValue(int requiredValue) throws IllegalArgumentException {
        if (requiredValue < 0) throw new IllegalArgumentException("Required value cannot be negative, got: " + requiredValue);
        log.debug("Badge [{}] requiredValue: {} to {}", id, this.requiredValue, requiredValue);
        setRequiredValue(requiredValue);
    }

    public void updateImage(@NonNull UUID imageId) {
        log.debug("Badge [{}] image updated: {}", id, imageId);
        setImageId(imageId);
    }

    public void removeImage() {
        log.debug("Badge [{}] image removed.", id);
        setImageId(null);
    }
}
