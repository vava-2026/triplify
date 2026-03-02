package com.triplify.ui;

import com.triplify.ui.shared.component.entry.model.Entry;
import com.triplify.ui.shared.component.entry.model.EntryVariant;
import com.triplify.ui.shared.component.entry.view.EntryView;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.header.view.HeaderView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        headerView.getViewModel().activeItemProperty().bind(
                menuView.getViewModel().selectedItemProperty());

        // DEMO for the select component - TODO: remove
        SelectView<Integer> selectView = new SelectView<>();
        selectView.update(Select.<Integer>builder()
                .placeholder("Choose a number...")
                .items(Arrays.asList(
                        Entry.builder(1, "Option A").build(),
                        Entry.builder(2, "Option B").variant(EntryVariant.SECONDARY).build(),
                        Entry.builder(3, "Option C").variant(EntryVariant.DANGER).build(),
                        Entry.builder(4, "Option D").variant(EntryVariant.MUTED).build(),
                        Entry.builder(5, "Option C").variant(EntryVariant.DANGER).build()
                ))
                .onSelect(entry -> System.out.println("Selected: " + entry.getValue()))
                .build());

        // Hide header
        header.visibleProperty().bind(menuView.getViewModel().hideHeaderProperty().not());
        header.managedProperty().bind(menuView.getViewModel().hideHeaderProperty().not());

        // DEMO: content - TODO: remove selectView from the braces
        StackPane contentArea = new StackPane(selectView);
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
