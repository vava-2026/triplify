package com.triplify.ui.shared.menu.view;

import com.triplify.ui.shared.menu.model.MenuItem;
import com.triplify.ui.shared.menu.viewmodel.MenuViewModel;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MenuView implements Initializable {

    // ---- FXML injections -------------------------------------------------

    @FXML private StackPane sidebarRoot;
    @FXML private VBox mainPageInner;
    @FXML private Pane collapsedIsland;
    @FXML private FontIcon toggleIcon;

    @FXML private Button navMap;
    @FXML private Button navMyTrips;
    @FXML private Button navCalendar;
    @FXML private Button navSettings;

    @FXML private HBox accountIsland;

    // ---- ViewModel -------------------------------------------------------

    private final MenuViewModel viewModel = new MenuViewModel();
    private final Map<MenuItem, Button> navButtons = new EnumMap<>(MenuItem.class);

    private final Map<Region, Timeline> hoverTimelines = new IdentityHashMap<>();
    private final Map<Region, DoubleProperty> borderProgress = new IdentityHashMap<>();

    private static final Color BORDER_COLOR = Color.web("#2f6690");
    private static final Duration ANIM_DURATION = Duration.millis(180);

    // ---- Initializable ---------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainPageInner.setMaxHeight(Double.MAX_VALUE);

        navButtons.put(MenuItem.MAP,      navMap);
        navButtons.put(MenuItem.MY_TRIPS, navMyTrips);
        navButtons.put(MenuItem.CALENDAR, navCalendar);
        navButtons.put(MenuItem.SETTINGS, navSettings);

        navButtons.values().forEach(this::installHoverAnimation);
        installHoverAnimation(accountIsland);

        viewModel.selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> refreshActiveState(newVal));
        refreshActiveState(viewModel.getSelectedItem());

        viewModel.collapsedProperty().addListener(
                (obs, oldVal, newVal) -> applyCollapsedState(newVal));
        applyCollapsedState(viewModel.isCollapsed());
    }

    // ---- FXML handlers ---------------------------------------------------

    @FXML private void onToggleCollapse(MouseEvent event) { viewModel.toggleCollapsed(); }
    @FXML private void onNavMap()      { viewModel.setSelectedItem(MenuItem.MAP); }
    @FXML private void onNavMyTrips()  { viewModel.setSelectedItem(MenuItem.MY_TRIPS); }
    @FXML private void onNavCalendar() { viewModel.setSelectedItem(MenuItem.CALENDAR); }
    @FXML private void onNavSettings() { viewModel.setSelectedItem(MenuItem.SETTINGS); }
    @FXML private void onAccountClicked(MouseEvent event) { viewModel.onAccountClicked(); }

    // ---- Helpers ---------------------------------------------------------

    private void refreshActiveState(MenuItem active) {
        navButtons.forEach((item, btn) -> {
            boolean isActive = (item == active);
            btn.getStyleClass().remove("nav-btn-active");
            if (isActive) {
                btn.getStyleClass().add("nav-btn-active");
                applyBorderStyle(btn, Color.TRANSPARENT);
            } else {
                double p = borderProgress.containsKey(btn) ? borderProgress.get(btn).get() : 0.0;
                applyBorderStyle(btn, interpolateColor(p));
            }
        });
    }

    private void applyCollapsedState(boolean collapsed) {
        mainPageInner.setVisible(!collapsed);
        mainPageInner.setManaged(!collapsed);
        collapsedIsland.setVisible(collapsed);
        collapsedIsland.setManaged(collapsed);

        sidebarRoot.setPrefWidth(260);
        sidebarRoot.setMaxWidth(260);

        if (toggleIcon != null) {
            toggleIcon.setIconLiteral(collapsed ? "fth-chevron-right" : "fth-chevron-left");
        }
    }

    // ---- Hover border animation ------------------------------------------

    private void installHoverAnimation(Region node) {
        DoubleProperty progress = new SimpleDoubleProperty(0.0);
        borderProgress.put(node, progress);

        progress.addListener((obs, oldV, newV) -> {
            // For buttons: skip when active (active style owns the border)
            if (node instanceof Button btn && btn.getStyleClass().contains("nav-btn-active")) return;
            applyBorderStyle(node, interpolateColor(newV.doubleValue()));
        });

        node.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> animateBorder(node, progress, 1.0));
        node.addEventHandler(MouseEvent.MOUSE_EXITED,  e -> animateBorder(node, progress, 0.0));
    }

    private void animateBorder(Region node, DoubleProperty progress, double target) {
        Timeline existing = hoverTimelines.get(node);
        if (existing != null) existing.stop();

        double remaining = Math.abs(target - progress.get());
        Duration dur = ANIM_DURATION.multiply(remaining);

        Timeline tl = new Timeline(new KeyFrame(dur,
                new KeyValue(progress, target, Interpolator.EASE_BOTH)));
        hoverTimelines.put(node, tl);
        tl.play();
    }

    private static Color interpolateColor(double t) {
        return new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), BORDER_COLOR.getBlue(), t);
    }

    private static void applyBorderStyle(Region node, Color color) {
        String rgba = String.format("rgba(%d,%d,%d,%.3f)",
                (int) Math.round(color.getRed()   * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue()  * 255),
                color.getOpacity());
        node.setStyle("-fx-border-color: " + rgba + ";");
    }
}
