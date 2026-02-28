package com.triplify.ui;

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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage stage) throws Exception {
        log.info("App launched");

        // Sidebar menu
        URL menuFxmlUrl = getClass().getResource("/com/triplify/ui/shared/menu/view/MenuView.fxml");
        if (menuFxmlUrl == null) {
            throw new IllegalStateException("MenuView.fxml not found on classpath");
        }
        FXMLLoader menuLoader = new FXMLLoader(menuFxmlUrl);
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

        // Bind header title to menu selection
        headerView.getViewModel().pageTitleProperty().bind(
                menuView.getViewModel().selectedItemProperty()
                        .map(item -> item.getLabel())
        );

        // Hide header on Map state
        javafx.beans.binding.BooleanBinding showHeader =
                menuView.getViewModel().selectedItemProperty()
                        .isNotEqualTo(MenuItem.MAP);
        header.visibleProperty().bind(showHeader);
        header.managedProperty().bind(showHeader);

        // Main content area
        StackPane contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f0f0f0;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Right column: header + content
        VBox rightColumn = new VBox(header, contentArea);
        rightColumn.setStyle("-fx-background-color: #f0f0f0;");
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        // Root
        HBox root = new HBox(menu, rightColumn);
        root.setStyle("-fx-background-color: #f0f0f0;");

        Scene scene = new Scene(root, 1280, 800);
        scene.setFill(Color.web("#f0f0f0"));

        stage.setTitle("Triplify");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
