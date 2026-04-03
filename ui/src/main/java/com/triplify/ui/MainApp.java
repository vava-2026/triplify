package com.triplify.ui;

import com.google.inject.Inject;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.dto.LogInRequest;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.country.dto.*;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.AddPlaceRequest;
import com.triplify.application.usecase.place.dto.DeletePlaceRequest;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.UpdatePlaceRequest;
import com.triplify.domain.filter.CountryFilter;
import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.shared.toast.ToastService;
import com.google.inject.Injector;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.PasswordItem;
import com.triplify.ui.shared.header.view.HeaderView;
import com.triplify.ui.shared.menu.model.MenuItem;
import com.triplify.ui.shared.menu.view.MenuView;
import com.triplify.ui.shared.menu.view.SidebarIslandView;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.FxmlLoadResult;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
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
import java.util.List;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    private static Injector injectorRef;

    // TODO: remove only for testing
    @Inject private AuthService authService;
    @Inject private CountryService countryService;
    @Inject private PlaceService placeService;

    @Inject private FxmlLoaderHelper fxml;
    @Inject private ToastService toastService;
    private Router router;

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

        // Header
        FxmlLoadResult<Node, HeaderView> headerResult = fxml.load("/com/triplify/ui/shared/header/view/HeaderView.fxml");
        Node header = headerResult.node();
        HeaderView headerView = headerResult.controller();
        HBox.setHgrow(header, Priority.ALWAYS);
        headerView.getViewModel().activeItemProperty().bind(menuView.getViewModel().selectedItemProperty());

        // Map layer
        FxmlLoadResult<Node, ?> mapResult = fxml.load("/com/triplify/ui/pages/map/MapView.fxml");
        Node mapView = mapResult.node();

        // Router content area
        TriplifyRouterContext routerContext = new TriplifyRouterContext(injectorRef);
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

        routerContext.selectedMenuItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem != menuView.getViewModel().getSelectedItem()) {
                menuView.getViewModel().setSelectedItem(newItem);
            }
        });

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
        });

        menuView.getViewModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (router != null && newItem != null) {
                log.info("Navigate to route: {}", newItem.getRouteId());
                router.moveto(newItem.getRouteId());
            }
        });

        HBox topBar = new HBox(islandPane, header);
        topBar.getStyleClass().add("app-top-bar");
        topBar.visibleProperty().bind(showMenu);
        topBar.managedProperty().bind(showMenu);

        HBox bottomRow = new HBox(menu, contentArea);
        bottomRow.getStyleClass().add("app-bottom-row");
        VBox.setVgrow(bottomRow, Priority.ALWAYS);

        VBox normalLayout = new VBox(topBar, bottomRow);
        normalLayout.getStyleClass().add("app-root");

        // Root
        StackPane root = new StackPane(mapView, normalLayout);
        root.getStyleClass().add("app-scene-root");

        toastService.attach(root);

        // Scene
        Scene scene = new Scene(root, 1280, 800);

        URL themeUrl = getClass().getResource("/com/triplify/ui/shared/css/theme.css");
        if (themeUrl == null) throw new IllegalStateException("theme.css not found");
        scene.getStylesheets().add(themeUrl.toExternalForm());

        // TODO: remove only for testing

        var authRes = authService.login(new LogInRequest("admin@triplify.com", "password"));
        if (authRes.isFailure()) {
            log.error("Auth Error");
            return;
        }

        // fetch countries
        PageRequest austriaPageRequest = new PageRequest(0, 10);
        CountryFilter austriaCountryFilter = new CountryFilter("Austria", null, false);
        var austria = countryService.getCountries(new GetCountriesRequest(austriaPageRequest, austriaCountryFilter)).getValue().items().get(0);

        PageRequest argentinaPageRequest = new PageRequest(0, 10);
        CountryFilter argentinaCountryFilter = new CountryFilter("Argentina", null, false);
        var argentina = countryService.getCountries(new GetCountriesRequest(argentinaPageRequest, argentinaCountryFilter)).getValue().items().get(0);

        PageRequest columbiaPageRequest = new PageRequest(0, 10);
        CountryFilter columbiaCountryFilter = new CountryFilter("Colombia", null, false);
        var Columbia = countryService.getCountries(new GetCountriesRequest(columbiaPageRequest, columbiaCountryFilter)).getValue().items().get(0);

        // add places
        var wien = placeService.addPlace(new AddPlaceRequest(austria.id(), null, "Wien", "Wien description", 48.2082, 16.3738)).getValue();
        var linz = placeService.addPlace(new AddPlaceRequest(austria.id(), null, "Linz", "Linz description", 48.2082, 16.3738)).getValue();
        var bueno = placeService.addPlace(new AddPlaceRequest(austria.id(), null, "Bueno", "Bueno description", 7.8, 56.93)).getValue();
        var alex = placeService.addPlace(new AddPlaceRequest(austria.id(), null, "Alex", "Alex description", 88.20, 36.903)).getValue();
        var wienUpdated = placeService.updatePlace(new UpdatePlaceRequest(wien.id(), austria.id(), null, "Wien Updated", "Wien description updated", 48.2082, 16.3738)).getValue();
        assert(wienUpdated.equals(wien.id()));
        placeService.deletePlace(new DeletePlaceRequest(wien.id()));

        // find places
        PageRequest placePageRequest = new PageRequest(0, 10);
        PlaceFilter placeFilter = new PlaceFilter("linz", null);
        var placeResult = placeService.getPlaces(new GetPlacesRequest(placePageRequest, placeFilter)).getValue();
        assert(!placeResult.items().isEmpty());

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
