package com.triplify.ui.shared.menu.view;

import com.triplify.ui.routing.AppPage;
import com.triplify.ui.shared.menu.viewmodel.NavButtonViewModel;
import com.triplify.ui.theme.AppColors;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class NavButtonView implements Initializable {

    private static final URL FXML_URL = NavButtonView.class.getResource("/com/triplify/ui/shared/menu/view/NavButton.fxml");
    private static final Color COLOR_PRIMARY = AppColors.PRIMARY;
    private static final Duration ANIM_DURATION = Duration.millis(180);

    @FXML private Button button;
    @FXML private FontIcon icon;
    @FXML private Label label;

    private NavButtonViewModel viewModel;
    private Runnable onSelect;

    private final DoubleProperty borderProgress = new SimpleDoubleProperty(0.0);
    private Timeline hoverTimeline;

    public NavButtonView withPage(AppPage page) {
        configure(page);
        return this;
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

    public AppPage getPage() { return viewModel.getPage(); }

    private void configure(AppPage page) {
        viewModel = new NavButtonViewModel(page);
        icon.setIconLiteral(page.getIcon());
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
