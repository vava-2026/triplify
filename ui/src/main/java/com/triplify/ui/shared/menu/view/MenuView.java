package com.triplify.ui.shared.menu.view;

import com.triplify.ui.shared.menu.model.MenuItem;
import com.triplify.ui.shared.menu.viewmodel.MenuViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * FXML controller (View) for the sidebar menu.
 *
 * <p>All state lives in {@link MenuViewModel}; the view only reads from it and
 * delegates user actions back to it.  To hook up navigation, listen to
 * {@code getViewModel().selectedItemProperty()} in the parent controller.
 */
public class MenuView implements Initializable {

    // ---- FXML injections -------------------------------------------------

    @FXML private StackPane sidebarRoot;
    @FXML private VBox      mainPageInner;
    @FXML private Pane      collapsedIsland;
    @FXML private StackPane toggleBox;
    @FXML private FontIcon  toggleIcon;

    @FXML private Button navMap;
    @FXML private Button navMyTrips;
    @FXML private Button navCalendar;
    @FXML private Button navSettings;

    // ---- ViewModel -------------------------------------------------------

    private final MenuViewModel viewModel = new MenuViewModel();
    private final Map<MenuItem, Button> navButtons = new EnumMap<>(MenuItem.class);

    // ---- Initializable ---------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Stretch VBox to fill the full height of the parent StackPane
        mainPageInner.setMaxHeight(Double.MAX_VALUE);

        navButtons.put(MenuItem.MAP,      navMap);
        navButtons.put(MenuItem.MY_TRIPS, navMyTrips);
        navButtons.put(MenuItem.CALENDAR, navCalendar);
        navButtons.put(MenuItem.SETTINGS, navSettings);

        viewModel.selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> refreshActiveState(newVal));
        refreshActiveState(viewModel.getSelectedItem());

        viewModel.collapsedProperty().addListener(
                (obs, oldVal, newVal) -> applyCollapsedState(newVal));
        applyCollapsedState(viewModel.isCollapsed());
    }

    // ---- FXML handlers ---------------------------------------------------

    @FXML private void onToggleCollapse(MouseEvent event) { viewModel.toggleCollapsed(); }
    @FXML private void onNavMap()      { viewModel.setSelectedItem(MenuItem.MAP);      }
    @FXML private void onNavMyTrips()  { viewModel.setSelectedItem(MenuItem.MY_TRIPS); }
    @FXML private void onNavCalendar() { viewModel.setSelectedItem(MenuItem.CALENDAR); }
    @FXML private void onNavSettings() { viewModel.setSelectedItem(MenuItem.SETTINGS); }

    // ---- Helpers ---------------------------------------------------------

    private void refreshActiveState(MenuItem active) {
        navButtons.forEach((item, btn) -> {
            btn.getStyleClass().remove("nav-btn-active");
            if (item == active) {
                btn.getStyleClass().add("nav-btn-active");
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

        // Flip the toggle arrow direction
        if (toggleIcon != null) {
            toggleIcon.setIconLiteral(collapsed ? "fth-chevron-right" : "fth-chevron-left");
        }
    }
}
