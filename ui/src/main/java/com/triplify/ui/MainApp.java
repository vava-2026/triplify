package com.triplify.ui;


import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.header.view.HeaderView;
import com.triplify.ui.shared.menu.model.MenuItem;
import com.triplify.ui.shared.menu.view.MenuView;
import com.triplify.ui.shared.menu.view.SidebarIslandView;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXMLLoader;
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
import java.util.List;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    private Router router;

    @Override
    public void start(Stage stage) throws Exception {
        log.info("App launched");

        // Sidebar island
        URL islandFxmlUrl = getClass().getResource("/com/triplify/ui/shared/menu/view/SidebarIsland.fxml");
        if (islandFxmlUrl == null) throw new IllegalStateException("SidebarIsland.fxml not found");
        FXMLLoader islandLoader = new FXMLLoader(islandFxmlUrl);
        Node island = islandLoader.load();
        SidebarIslandView islandView = islandLoader.getController();

        Pane islandPane = new Pane(island);
        islandPane.setPrefWidth(MenuView.SIDEBAR_WIDTH);
        islandPane.setMinWidth(MenuView.SIDEBAR_WIDTH);
        islandPane.setMaxWidth(MenuView.SIDEBAR_WIDTH);

        // Sidebar menu
        URL menuFxmlUrl = getClass().getResource("/com/triplify/ui/shared/menu/view/MenuView.fxml");
        if (menuFxmlUrl == null) throw new IllegalStateException("MenuView.fxml not found");
        FXMLLoader menuLoader = new FXMLLoader(menuFxmlUrl);
        Node menu = menuLoader.load();
        MenuView menuView = menuLoader.getController();
        menuView.setIslandController(islandView);

        // Header
        URL headerFxmlUrl = getClass().getResource("/com/triplify/ui/shared/header/view/HeaderView.fxml");
        if (headerFxmlUrl == null) throw new IllegalStateException("HeaderView.fxml not found");
        FXMLLoader headerLoader = new FXMLLoader(headerFxmlUrl);
        Node header = headerLoader.load();
        HeaderView headerView = headerLoader.getController();
        HBox.setHgrow(header, Priority.ALWAYS);
        headerView.getViewModel().activeItemProperty().bind(menuView.getViewModel().selectedItemProperty());

        // Map layer
        URL mapFxmlUrl = getClass().getResource("/com/triplify/ui/pages/map/MapView.fxml");
        if (mapFxmlUrl == null) throw new IllegalStateException("MapView.fxml not found");
        FXMLLoader mapLoader = new FXMLLoader(mapFxmlUrl);
        Node mapView = mapLoader.load();

        // Router content area
        TriplifyRouterContext routerContext = new TriplifyRouterContext();
        RouterStackPane contentArea = new RouterStackPane();

        contentArea.getStyleClass().add("app-content");
        contentArea.setContext(routerContext);
        contentArea.setRouterConfig("router.xml");
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        Rectangle contentClip = new Rectangle();
        contentClip.widthProperty().bind(contentArea.widthProperty());
        contentClip.heightProperty().bind(contentArea.heightProperty());
        contentArea.setClip(contentClip);

        //isMap binding
        BooleanBinding isMap = Bindings.createBooleanBinding(
                () -> menuView.getViewModel().getSelectedItem() == MenuItem.MAP,
                menuView.getViewModel().selectedItemProperty());

        mapView.visibleProperty().bind(isMap);
        mapView.managedProperty().bind(isMap);

        contentArea.visibleProperty().bind(isMap.not());
        contentArea.managedProperty().bind(isMap.not());

        BooleanBinding showMenu = routerContext.fullScreenContentProperty().not();
        menu.visibleProperty().bind(showMenu);
        menu.managedProperty().bind(showMenu);
        islandPane.visibleProperty().bind(showMenu);
        islandPane.managedProperty().bind(showMenu);

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
        });

        menuView.getViewModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (router != null && newItem != null) {
                log.info("Navigate to route: {}", newItem.getRouteId());
                router.moveto(newItem.getRouteId());
            }
        });

        HBox topBar = new HBox(islandPane, header);
        topBar.getStyleClass().add("app-top-bar");

        HBox bottomRow = new HBox(menu, contentArea);
        bottomRow.getStyleClass().add("app-bottom-row");
        VBox.setVgrow(bottomRow, Priority.ALWAYS);

        VBox normalLayout = new VBox(topBar, bottomRow);
        normalLayout.getStyleClass().add("app-root");

        // Root
        StackPane root = new StackPane(mapView, normalLayout);
        root.getStyleClass().add("app-scene-root");

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
        if (router != null) router.dispose();
        super.stop();
    }
}
