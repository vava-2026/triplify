package com.triplify.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
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

        URL fxmlUrl = getClass().getResource(
                "/com/triplify/ui/shared/menu/view/MenuView.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("MenuView.fxml not found on classpath");
        }

        Node menu = FXMLLoader.load(fxmlUrl);

        StackPane contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #e8f0f7;");
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        HBox root = new HBox(menu, contentArea);
        root.setStyle("-fx-background-color: #e8f0f7;");

        Scene scene = new Scene(root, 1024, 800);
        scene.setFill(Color.web("#e8f0f7"));

        stage.setTitle("Triplify");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
