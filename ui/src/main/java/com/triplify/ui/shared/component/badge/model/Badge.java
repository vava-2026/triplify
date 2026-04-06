package com.triplify.ui.shared.component.badge.model;

import com.triplify.application.localization.LocalizedDescription;
import com.triplify.application.localization.LocalizedName;
import lombok.Getter;

@Getter
public class Badge implements LocalizedName, LocalizedDescription {
    private final String name;
    private final String nameSk;
    private final String description;
    private final String descriptionSk;
    private final String image;
    private final BadgeGroup group;
    private final int level;
    private final int requiredValue;
    private final int currentValue;
    private final boolean isUnlocked;

    public Badge(String name,
                 String nameSk,
                 String description,
                 String descriptionSk,
                 String image,
                 BadgeGroup group,
                 int level,
                 int requiredValue,
                 int currentValue,
                 boolean isUnlocked) {
        this.name = name;
        this.nameSk = nameSk;
        this.description = description;
        this.descriptionSk = descriptionSk;
        this.image = image;
        this.group = group;
        this.level = level;
        this.requiredValue = requiredValue;
        this.currentValue = currentValue;
        this.isUnlocked = isUnlocked;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String nameSk() {
        return nameSk;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String descriptionSk() {
        return descriptionSk;
    }
}
