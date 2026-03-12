package com.triplify.ui.shared.toast;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class ToastView {

    private static final Duration SLIDE_IN_DURATION  = Duration.millis(280);
    private static final Duration VISIBLE_DURATION = Duration.millis(3500);
    private static final Duration FADE_OUT_DURATION = Duration.millis(300);

    @FXML private HBox toastRoot;
    @FXML private FontIcon toastIcon;
    @FXML private Label titleLabel;
    @FXML private Label messageLabel;

    private Runnable onDone;
    private SequentialTransition activeTransition;

    public void show(ToastType type, String title, String message) {
        if (activeTransition != null) {
            activeTransition.stop();
            activeTransition = null;
        }
        toastRoot.getStyleClass().removeAll(
                ToastType.ERROR.getStyleClass(),
                ToastType.SUCCESS.getStyleClass(),
                ToastType.INFO.getStyleClass(),
                ToastType.WARNING.getStyleClass());
        toastRoot.setOpacity(1.0);
        toastRoot.setTranslateX(0);

        toastRoot.getStyleClass().add(type.getStyleClass());
        toastIcon.setIconLiteral(type.getIconLiteral());
        titleLabel.setText(title);
        messageLabel.setText(message);

        TranslateTransition slideIn = new TranslateTransition(SLIDE_IN_DURATION, toastRoot);
        slideIn.setFromX(420);
        slideIn.setToX(0);

        PauseTransition pause = new PauseTransition(VISIBLE_DURATION);

        FadeTransition fadeOut = new FadeTransition(FADE_OUT_DURATION, toastRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        activeTransition = new SequentialTransition(slideIn, pause, fadeOut);
        activeTransition.setOnFinished(e -> finishOnce());
        activeTransition.play();
    }

    @FXML
    private void onClose() {
        if (activeTransition != null) {
            activeTransition.stop();
            activeTransition = null;
        }
        finishOnce();
    }

    public void setOnDone(Runnable onDone) {
        this.onDone = onDone;
    }

    private void finishOnce() {
        Runnable callback = onDone;
        onDone = null;
        if (callback != null) {
            callback.run();
        }
    }
}
