package com.triplify.ui.shared.menu.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class SidebarIslandView implements Initializable {

    @FXML private HBox sidebarIsland;
    @FXML private FontIcon toggleIcon;

    private Runnable onToggle;

    @Override
    public void initialize(URL location, ResourceBundle resources) { }

    public void setOnToggle(Runnable callback) {
        this.onToggle = callback;
    }

    public void setCollapsed(boolean collapsed, boolean isMap) {
        if (toggleIcon != null) {
            toggleIcon.setIconLiteral(collapsed ? "fth-chevron-right" : "fth-chevron-left");
        }

        if (sidebarIsland != null) {
            if (collapsed && isMap) {
                sidebarIsland.getStyleClass().add("sidebar-island-pill");
                sidebarIsland.setLayoutX(30);
                sidebarIsland.setLayoutY(20);
            } else {
                sidebarIsland.getStyleClass().remove("sidebar-island-pill");
                sidebarIsland.setLayoutX(0);
                sidebarIsland.setLayoutY(0);
            }
        }
    }

    @FXML
    private void onToggleCollapse(MouseEvent event) {
        if (onToggle != null) {
            onToggle.run();
        }
    }
}