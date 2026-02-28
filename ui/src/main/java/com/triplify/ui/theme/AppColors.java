package com.triplify.ui.theme;

import javafx.scene.paint.Color;

public final class AppColors {

    private AppColors() {}

    public static final Color BG = Color.web("#f0f0f0");

    public static final Color SURFACE = Color.web("#fafafa");

    public static final Color SURFACE_TINT = Color.web("#ecf3f9");

    public static final Color INK = Color.web("#0a090c");

    public static final Color INK_MUTED = Color.web("#999999");

    public static final Color PRIMARY = Color.web("#2f6690");
    public static final Color PRIMARY_HOVER = PRIMARY.deriveColor(0, 1, 0.88, 1);
    public static final Color PRIMARY_PRESSED = PRIMARY.deriveColor(0, 1, 0.78, 1);
    public static final Color PRIMARY_DARK  = Color.web("#1d3f59");
    public static final Color PRIMARY_LIGHT = Color.web("#ecf3f9");

    public static final Color ON_PRIMARY = Color.web("#ecf3f9");
    public static final Color ON_SURFACE = Color.web("#ffffff");

    public static final Color DANGER = Color.web("#c0392b");
    public static final Color DANGER_HOVER = DANGER.deriveColor(0, 1, 0.88, 1);
    public static final Color DANGER_PRESSED = DANGER.deriveColor(0, 1, 0.78, 1);
}
