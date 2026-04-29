package com.triplify.ui;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.ui.routing.AppPage;
import com.triplify.ui.routing.GuardedNavigator;
import com.triplify.ui.routing.PageAccessService;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.header.view.HeaderView;
import com.triplify.ui.shared.menu.view.MenuView;
import com.triplify.ui.shared.menu.view.SidebarIslandView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.FxmlLoadResult;
import com.triplify.ui.shared.util.FxmlLoaderHelper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import rahulstech.jfx.routing.Router;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.layout.RouterStackPane;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    private static Injector injectorRef;

    @Inject private FxmlLoaderHelper fxml;
    @Inject private ToastService toastService;
    @Inject private UserSessionContext userSessionContext;
    @Inject private GuardedNavigator guardedNavigator;
    @Inject private PageAccessService pageAccessService;
    private Router router;
    private boolean initialNavigationHandled;

    public static void launch(Injector injector, String[] args) {
        injectorRef = injector;
        Application.launch(MainApp.class, args);
    }

    @Override
    public void init() {
        injectorRef.injectMembers(this);
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.info("App launched");
        userSessionContext.load();

        // Sidebar island
        FxmlLoadResult<Node, SidebarIslandView> islandResult = fxml.load("/com/triplify/ui/shared/menu/view/SidebarIsland.fxml");
        Node island = islandResult.node();
        SidebarIslandView islandView = islandResult.controller();

        Pane islandPane = new Pane(island);
        islandPane.setPrefWidth(MenuView.SIDEBAR_WIDTH);
        islandPane.setMinWidth(MenuView.SIDEBAR_WIDTH);
        islandPane.setMaxWidth(MenuView.SIDEBAR_WIDTH);

        // Sidebar menu
        FxmlLoadResult<Node, MenuView> menuResult = fxml.load("/com/triplify/ui/shared/menu/view/MenuView.fxml");
        Node menu = menuResult.node();
        MenuView menuView = menuResult.controller();
        menuView.setIslandController(islandView);
        menuView.setNavigationHandler(page -> {
            if (router != null) {
                guardedNavigator.goTo(router, page.getRouteId());
            }
        });

        // Header
        FxmlLoadResult<Node, HeaderView> headerResult = fxml.load("/com/triplify/ui/shared/header/view/HeaderView.fxml");
        Node header = headerResult.node();
        HeaderView headerView = headerResult.controller();
        HBox.setHgrow(header, Priority.ALWAYS);
        headerView.setNavigationHandler(tripId -> {
            if (router != null) {
                RouterArgument args = new RouterArgument();
                args.addArgument("tripId", tripId);
                guardedNavigator.goTo(router, RouteIds.TRIP_DETAILS, args);
            }
        });

        // Router content area
        TriplifyRouterContext routerContext = new TriplifyRouterContext(injectorRef);
        RouterStackPane contentArea = new RouterStackPane();

        contentArea.getStyleClass().add("app-content");
        contentArea.setOpacity(0);
        contentArea.setContext(routerContext);
        contentArea.setRouterConfig("router.xml");
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        Rectangle contentClip = new Rectangle();
        contentClip.widthProperty().bind(contentArea.widthProperty());
        contentClip.heightProperty().bind(contentArea.heightProperty());
        contentArea.setClip(contentClip);

        routerContext.selectedPrimaryPageProperty().addListener((obs, oldPage, newPage) -> {
            if (newPage != menuView.getViewModel().getActivePrimaryPage()) {
                menuView.getViewModel().setActivePrimaryPage(newPage);
            }
        });

        routerContext.currentPageProperty().addListener((obs, oldPage, newPage) -> {
            menuView.getViewModel().setCurrentPage(newPage);
            headerView.setActivePage(newPage);
        });

        BooleanBinding showMenu = routerContext.fullScreenContentProperty().not();
        menu.visibleProperty().bind(showMenu);
        menu.managedProperty().bind(showMenu);
        BooleanBinding showIsland = showMenu;
        islandPane.visibleProperty().bind(showIsland);
        islandPane.managedProperty().bind(showIsland);
        showMenu.addListener((obs, wasVisible, isVisible) -> {
            if (isVisible) {
                menuView.refreshAccountSection();
            }
        });

        BooleanBinding showHeader = menuView.getViewModel()
                .hideHeaderProperty()
                .not()
                .and(routerContext.fullScreenContentProperty().not());
        header.visibleProperty().bind(showHeader);
        header.managedProperty().bind(showHeader);

        // Router navigation
        contentArea.routerProperty().addListener((obs, oldRouter, newRouter) -> {
            router = newRouter;
            log.info("Router initialized: {}", newRouter != null ? "ready" : "null");

            if (router != null && !initialNavigationHandled) {
                initialNavigationHandled = true;
                if (userSessionContext.getCurrent().isPresent()) {
                    AppPage defaultPage = pageAccessService.getDefaultPage(userSessionContext.getCurrent());
                    router.setHomeDestination(defaultPage.getRouteId());
                }
                Platform.runLater(() -> {
                    guardedNavigator.syncContext(router);
                    contentArea.setOpacity(1);
                });
            } else if (router != null) {
                guardedNavigator.syncContext(router);
            }
        });

        HBox topBar = new HBox(islandPane, header);
        topBar.getStyleClass().add("app-top-bar");
        topBar.visibleProperty().bind(showIsland);
        topBar.managedProperty().bind(showIsland);

        // topBar and menu float directly over the full-window content area.
        // topBar is constrained to 70px tall by CSS; menu is constrained to
        // SIDEBAR_WIDTH by its own preferred size — both only pick where they
        // have actual visible content, so the rest of the window stays clickable.
        StackPane.setAlignment(topBar, Pos.TOP_LEFT);
        StackPane.setAlignment(menu, Pos.TOP_LEFT);
        StackPane.setMargin(menu, new Insets(70, 0, 0, 0));
        StackPane root = new StackPane(contentArea, topBar, menu);
        root.getStyleClass().add("app-scene-root");

        // Map page: contentArea fills the full window; topBar and menu float over it.
        // All other pages: constrain contentArea's bounds so page content lands below the
        // topBar and to the right of the menu — replicating the old HBox reflow.
        // Padding cannot be used because RouterStackPane overrides layoutChildren and sizes
        // its children to fill the pane, ignoring StackPane insets. Instead we bind the
        // pref/max size directly and use BOTTOM_RIGHT alignment so the position follows.
        // With BOTTOM_RIGHT: x = root.width - contentArea.width = leftOffset
        //                    y = root.height - contentArea.height = topOffset
        BooleanBinding isMapPage = Bindings.createBooleanBinding(
                () -> {
                    AppPage page = routerContext.getCurrentPage();
                    return page != null && RouteIds.MAP.equals(page.getRouteId());
                },
                routerContext.currentPageProperty());
        var menuCollapsed = menuView.getViewModel().collapsedProperty();
        var leftOffset = Bindings.createDoubleBinding(
                () -> !isMapPage.get() && showMenu.get() && !menuCollapsed.get()
                        ? MenuView.SIDEBAR_WIDTH : 0.0,
                isMapPage, showMenu, menuCollapsed);
        var topOffset = Bindings.createDoubleBinding(
                () -> !isMapPage.get() && showHeader.get() ? 70.0 : 0.0,
                isMapPage, showHeader);
        contentArea.prefWidthProperty().bind(root.widthProperty().subtract(leftOffset));
        contentArea.prefHeightProperty().bind(root.heightProperty().subtract(topOffset));
        contentArea.maxWidthProperty().bind(contentArea.prefWidthProperty());
        contentArea.maxHeightProperty().bind(contentArea.prefHeightProperty());
        StackPane.setAlignment(contentArea, Pos.BOTTOM_RIGHT);

        toastService.attach(root);

        // Scene
        Scene scene = new Scene(root, 1280, 800);

        URL themeUrl = getClass().getResource("/com/triplify/ui/shared/css/theme.css");
        if (themeUrl == null) throw new IllegalStateException("theme.css not found");
        scene.getStylesheets().add(themeUrl.toExternalForm());

        stage.setTitle("Triplify");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        userSessionContext.save();
        if (router != null) router.dispose();
        super.stop();
    }
}
