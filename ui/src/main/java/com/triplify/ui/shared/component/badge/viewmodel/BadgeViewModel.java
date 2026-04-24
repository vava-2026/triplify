package com.triplify.ui.shared.component.badge.viewmodel;

import com.triplify.application.shared.localization.LocalizedDescription;
import com.triplify.application.shared.localization.LocalizedName;
import com.triplify.application.usecase.badgegroup.dto.BadgeGroupType;
import lombok.Getter;

@Getter
public class BadgeViewModel implements LocalizedName, LocalizedDescription {
    private final String name;
    private final String nameSk;
    private final String description;
    private final String descriptionSk;
    private final String image;
    private final BadgeGroupType group;
    private final int level;
    private final int requiredValue;
    private final int currentValue;
    private final boolean isUnlocked;

    public BadgeViewModel(String name,
                          String nameSk,
                          String description,
                          String descriptionSk,
                          String image,
                          BadgeGroupType group,
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

