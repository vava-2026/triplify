package com.triplify.ui.routing;

import com.google.inject.Singleton;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.domain.model.enums.RoleEnum;
import rahulstech.jfx.routing.element.Destination;

import java.util.List;
import java.util.Optional;

@Singleton
public class PageAccessService {

    public AppPage getPage(String routeId) {
        return AppPage.fromRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown route id: " + routeId));
    }

    public AppPage getPage(Destination destination) {
        if (destination == null) {
            return null;
        }
        return getPage(destination.getId());
    }

    public AppPage getDefaultPage(Optional<SessionUser> currentUser) {
        if (currentUser.isEmpty()) {
            return AppPage.START;
        }
        return AppPage.defaultForRole(currentUser.get().role());
    }

    public List<AppPage> getPrimaryMenuPages(Optional<SessionUser> currentUser) {
        return currentUser
                .map(SessionUser::role)
                .map(AppPage::primaryMenuPagesFor)
                .orElse(List.of());
    }

    public NavigationGuardResult guard(String routeId, Optional<SessionUser> currentUser) {
        AppPage targetPage = getPage(routeId);

        if (currentUser.isEmpty()) {
            if (!targetPage.requiresAuth()) {
                return new NavigationGuardResult(targetPage, null);
            }
            return new NavigationGuardResult(AppPage.START, "error.user.unauthorized");
        }

        SessionUser user = currentUser.get();
        if (targetPage.isGuestOnly()) {
            return new NavigationGuardResult(getDefaultPage(currentUser), null);
        }
        if (targetPage.requiresAuth() && !targetPage.isAllowedFor(user.role())) {
            return new NavigationGuardResult(getDefaultPage(currentUser), "error.user.forbidden");
        }
        return new NavigationGuardResult(targetPage, null);
    }

    public String getRoleLabelKey(RoleEnum role) {
        return switch (role) {
            case USER, PRO_USER -> "account.role";
            case CONFIGURATION_MANAGER -> "role.configurationManager";
        };
    }
}
