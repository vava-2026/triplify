package com.triplify.ui.routing;

public record NavigationGuardResult(
        AppPage targetPage,
        String toastKey
) {}
