package com.triplify.ui.shared.menu.view;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.menu.model.MenuItem;
import com.triplify.ui.shared.menu.model.NavItem;
import com.triplify.ui.shared.menu.viewmodel.MenuViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MenuView implements Initializable {

    public static final double SIDEBAR_WIDTH = 260;
    private static final double SIDEBAR_COLLAPSED_WIDTH = 0;

    @FXML private StackPane sidebarRoot;
    @FXML private VBox mainPageInner;
    @FXML private VBox navContainer;

    @FXML private Label accountRole;

    private final MenuViewModel viewModel = new MenuViewModel();
    private final List<NavButtonView> navButtons = new ArrayList<>();

    private SidebarIslandView islandController;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarRoot.setMaxHeight(Double.MAX_VALUE);
        mainPageInner.setMaxHeight(Double.MAX_VALUE);

        for (NavItem navItem : NavItem.values()) {
            NavButtonView btn = NavButtonView.create(navItem);
            btn.setOnSelect(() -> viewModel.setSelectedItem(navItem.getMenuItem()));
            navContainer.getChildren().add(btn.getButton());
            navButtons.add(btn);
        }

        accountRole.textProperty().bind(
                Bindings.createStringBinding(() -> I18n.t("account.role"), I18n.bundleProperty()));

        viewModel.selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> refreshActiveState(newVal));
        refreshActiveState(viewModel.getSelectedItem());

        viewModel.collapsedProperty().addListener(
                (obs, oldVal, newVal) -> applyCollapsedState(newVal));
        applyCollapsedState(viewModel.isCollapsed());
    }

    public void setIslandController(SidebarIslandView island) {
        this.islandController = island;
        island.setOnToggle(viewModel::toggleCollapsed);
        applyCollapsedState(viewModel.isCollapsed());
    }

    public MenuViewModel getViewModel() { return viewModel; }


    @FXML
    private void onAccountClicked(MouseEvent event) {
        viewModel.setSelectedItem(MenuItem.ACCOUNT);
    }

    private void refreshActiveState(MenuItem active) {
        navButtons.forEach(btn ->
                btn.setActive(btn.getNavItem().getMenuItem() == active));
    }

    private void applyCollapsedState(boolean collapsed) {
        mainPageInner.setVisible(!collapsed);
        mainPageInner.setManaged(!collapsed);

        double width = collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_WIDTH;
        sidebarRoot.setMinWidth(width);
        sidebarRoot.setPrefWidth(width);
        sidebarRoot.setMaxWidth(width);

        if (islandController != null) {
            islandController.setCollapsed(collapsed, viewModel.isHideHeader());
        }
    }
}
