package com.triplify.ui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.triplify.ui.di.AppModule;
import com.triplify.ui.shared.header.view.HeaderView;
import com.triplify.ui.shared.menu.model.MenuItem;
import com.triplify.ui.shared.menu.view.MenuView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    private Injector injector;

    @Override
    public void init() {
        injector = Guice.createInjector(new AppModule());
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.info("App launched");

        // Sidebar menu
        URL menuFxmlUrl = getClass().getResource("/com/triplify/ui/shared/menu/view/MenuView.fxml");
        if (menuFxmlUrl == null) {
            throw new IllegalStateException("MenuView.fxml not found on classpath");
        }
        FXMLLoader menuLoader = new FXMLLoader(menuFxmlUrl);
        menuLoader.setControllerFactory(clazz -> injector.getInstance(clazz));
        Node menu = menuLoader.load();
        MenuView menuView = menuLoader.getController();

        // Header bar
        URL headerFxmlUrl = getClass().getResource("/com/triplify/ui/shared/header/view/HeaderView.fxml");
        if (headerFxmlUrl == null) {
            throw new IllegalStateException("HeaderView.fxml not found on classpath");
        }
        FXMLLoader headerLoader = new FXMLLoader(headerFxmlUrl);
        Node header = headerLoader.load();
        HeaderView headerView = headerLoader.getController();
        HBox.setHgrow(header, Priority.ALWAYS);

        headerView.getViewModel().activeItemProperty().bind(
                menuView.getViewModel().selectedItemProperty());

        // Hide header
        header.visibleProperty().bind(menuView.getViewModel().hideHeaderProperty().not());
        header.managedProperty().bind(menuView.getViewModel().hideHeaderProperty().not());

        // Main content area
        StackPane contentArea = new StackPane();
        contentArea.getStyleClass().add("app-content");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Right column: header + content
        VBox rightColumn = new VBox(header, contentArea);
        rightColumn.getStyleClass().add("app-right-column");
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        // Root
        HBox root = new HBox(menu, rightColumn);
        root.getStyleClass().add("app-root");

        Scene scene = new Scene(root, 1280, 800);

        // Load the global theme
        URL themeUrl = getClass().getResource("/com/triplify/ui/shared/css/theme.css");
        if (themeUrl == null) {
            throw new IllegalStateException("theme.css not found on classpath");
        }
        scene.getStylesheets().add(themeUrl.toExternalForm());

        stage.setTitle("Triplify");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
