package com.triplify.ui.routing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.toast.ToastService;
import rahulstech.jfx.routing.Router;
import rahulstech.jfx.routing.element.RouterArgument;

@Singleton
public class GuardedNavigator {

    private final PageAccessService pageAccessService;
    private final UserSessionContext userSessionContext;
    private final ToastService toastService;

    @Inject
    public GuardedNavigator(
            PageAccessService pageAccessService,
            UserSessionContext userSessionContext,
            ToastService toastService
    ) {
        this.pageAccessService = pageAccessService;
        this.userSessionContext = userSessionContext;
        this.toastService = toastService;
    }

    public void openDefault(Router router) {
        AppPage page = pageAccessService.getDefaultPage(userSessionContext.getCurrent());
        goTo(router, page.getRouteId());
    }

    public void goTo(Router router, String routeId) {
        goTo(router, routeId, null);
    }

    public void goTo(Router router, String routeId, RouterArgument data) {
        NavigationGuardResult result = pageAccessService.guard(routeId, userSessionContext.getCurrent());
        updateContext(router, result.targetPage());
        RouterArgument resolvedData = result.targetPage().getRouteId().equals(routeId) ? data : null;

        if (result.toastKey() != null) {
            toastService.error(I18n.t(result.toastKey()));
        }

        if (resolvedData == null
                && router.getCurrentDestination() != null
                && result.targetPage().getRouteId().equals(router.getCurrentDestination().getId())) {
            syncContext(router);
            return;
        }

        if (resolvedData == null) {
            router.moveto(result.targetPage().getRouteId());
            return;
        }
        router.moveto(result.targetPage().getRouteId(), resolvedData);
    }

    public void goBack(Router router) {
        if (router.popBackStack()) {
            syncContext(router);
        }
    }

    public void syncContext(Router router) {
        TriplifyRouterContext context = getContext(router);
        AppPage currentPage = pageAccessService.getPage(router.getCurrentDestination());
        context.setCurrentPage(currentPage);
        context.setSelectedPrimaryPage(currentPage == null ? null : currentPage.getActivePrimaryPage());
    }

    private void updateContext(Router router, AppPage currentPage) {
        TriplifyRouterContext context = getContext(router);
        context.setCurrentPage(currentPage);
        context.setSelectedPrimaryPage(currentPage.getActivePrimaryPage());
    }

    private TriplifyRouterContext getContext(Router router) {
        return (TriplifyRouterContext) router.getContext();
    }
}
