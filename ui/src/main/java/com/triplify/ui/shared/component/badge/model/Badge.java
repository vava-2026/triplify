package com.triplify.ui.shared.component.badge.model;

import lombok.Getter;

@Getter
public class Badge {
    private String name;
    private String description;
    private String image;
    private BadgeGroup group;
    private int level;
    private int requiredValue;
    private int currentValue;
    private boolean isUnlocked;

    public Badge(String name, String description, String image, BadgeGroup group,
                 int level, int requiredValue, int currentValue, boolean isUnlocked) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.group = group;
        this.level = level;
        this.requiredValue = requiredValue;
        this.currentValue = currentValue;
        this.isUnlocked = isUnlocked;
    }
}
