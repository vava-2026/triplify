package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Getter
@ToString(exclude = {"createdById"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class BadgeGroup {
    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

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

    private final UUID createdById;

    public BadgeGroup(@NonNull String name, @NonNull String nameSk, UUID createdById) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.nameSk = nameSk;
        this.createdById = createdById;
    }

    public void updateName(@NonNull String name) throws IllegalArgumentException{
        if (name.isBlank()) throw new IllegalArgumentException("Name must not be blank.");
        log.debug("BadgeGroup [{}] name: {} to {}", id, this.name, name);
        setName(name);
    }

    public void updateNameSk(@NonNull String nameSk) throws IllegalArgumentException{
        if (nameSk.isBlank()) throw new IllegalArgumentException("Slovak name must not be blank.");
        log.debug("BadgeGroup [{}] nameSk: {} to {}", id, this.nameSk, nameSk);
        setNameSk(nameSk);
    }

    public void updateDescription(String description) {
        log.debug("BadgeGroup [{}] description updated.", id);
        setDescription(description);
    }

    public void updateDescriptionSk(String descriptionSk) {
        log.debug("BadgeGroup [{}] descriptionSk updated.", id);
        setDescriptionSk(descriptionSk);
    }
}