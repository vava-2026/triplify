package com.triplify.ui.shared.menu.view;

import com.triplify.ui.shared.menu.model.NavItem;
import com.triplify.ui.shared.menu.viewmodel.NavButtonViewModel;
import com.triplify.ui.theme.AppColors;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NavButtonView implements Initializable {

    private static final Color COLOR_PRIMARY  = AppColors.PRIMARY;
    private static final Duration ANIM_DURATION  = Duration.millis(180);

    @FXML private Button button;
    @FXML private FontIcon icon;
    @FXML private Label label;

    private NavButtonViewModel viewModel;
    private Runnable onSelect;

    private final DoubleProperty borderProgress = new SimpleDoubleProperty(0.0);
    private Timeline hoverTimeline;

    public static NavButtonView create(NavItem navItem) {
        URL fxml = NavButtonView.class.getResource(
                "/com/triplify/ui/shared/menu/view/NavButton.fxml");
        if (fxml == null) throw new IllegalStateException("NavButton.fxml not found");

        FXMLLoader loader = new FXMLLoader(fxml);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load NavButton.fxml", e);
        }
        NavButtonView view = loader.getController();
        view.configure(navItem);
        return view;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        borderProgress.addListener((obs, oldV, newV) -> {
            if (button.getStyleClass().contains("nav-btn-active")) return;
            applyBorder(interpolate(newV.doubleValue()));
        });
        button.setOnMouseEntered(e -> animateBorder(1.0));
        button.setOnMouseExited(e -> animateBorder(0.0));
    }

    public Button getButton() { return button; }

    public void setOnSelect(Runnable callback) { this.onSelect = callback; }

    public void setActive(boolean active) {
        viewModel.setActive(active);
        if (active) {
            button.getStyleClass().add("nav-btn-active");
            applyBorder(Color.TRANSPARENT);
        } else {
            button.getStyleClass().remove("nav-btn-active");
            applyBorder(interpolate(borderProgress.get()));
        }
    }

    public NavItem getNavItem() { return viewModel.getNavItem(); }

    private void configure(NavItem navItem) {
        viewModel = new NavButtonViewModel(navItem);
        icon.setIconLiteral(navItem.getIcon());
        icon.setIconSize(18);
        label.textProperty().bind(viewModel.labelBinding());
    }

    @FXML
    private void onClicked() {
        if (onSelect != null) onSelect.run();
    }

    private void animateBorder(double target) {
        if (hoverTimeline != null) hoverTimeline.stop();
        double remaining = Math.abs(target - borderProgress.get());
        hoverTimeline = new Timeline(new KeyFrame(
                ANIM_DURATION.multiply(remaining),
                new KeyValue(borderProgress, target, Interpolator.EASE_BOTH)));
        hoverTimeline.play();
    }

    private void applyBorder(Color color) {
        String rgba = String.format("rgba(%d,%d,%d,%.3f)",
                (int) Math.round(color.getRed()   * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue()  * 255),
                color.getOpacity());
        button.setStyle("-fx-border-color: " + rgba + ";");
    }

    private static Color interpolate(double t) {
        return new Color(
                COLOR_PRIMARY.getRed(),
                COLOR_PRIMARY.getGreen(),
                COLOR_PRIMARY.getBlue(), t);
    }
}

