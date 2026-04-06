package com.triplify.ui;

import com.google.inject.Inject;
import com.triplify.application.usecase.session.UserSessionContext;
import com.google.inject.Injector;
import com.triplify.ui.routing.GuardedNavigator;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.header.view.HeaderView;
import com.triplify.ui.shared.menu.view.MenuView;
import com.triplify.ui.shared.menu.view.SidebarIslandView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.FxmlLoadResult;
import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXMLLoader;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.Router;
import rahulstech.jfx.routing.layout.RouterStackPane;
import java.net.URL;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    private static Injector injectorRef;

    @Inject private FxmlLoaderHelper fxml;
    @Inject private ToastService toastService;
    @Inject private UserSessionContext userSessionContext;
    @Inject private GuardedNavigator guardedNavigator;
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
                    guardedNavigator.openDefault(router);
                } else {
                    guardedNavigator.syncContext(router);
                }
                contentArea.setOpacity(1);
            } else if (router != null) {
                guardedNavigator.syncContext(router);
            }
        });

        HBox topBar = new HBox(islandPane, header);
        topBar.getStyleClass().add("app-top-bar");
        topBar.visibleProperty().bind(showIsland);
        topBar.managedProperty().bind(showIsland);

        HBox bottomRow = new HBox(menu, contentArea);
        bottomRow.getStyleClass().add("app-bottom-row");
        VBox.setVgrow(bottomRow, Priority.ALWAYS);

        VBox normalLayout = new VBox(topBar, bottomRow);
        normalLayout.getStyleClass().add("app-root");

        // Root
        StackPane root = new StackPane(normalLayout);
        root.getStyleClass().add("app-scene-root");

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
