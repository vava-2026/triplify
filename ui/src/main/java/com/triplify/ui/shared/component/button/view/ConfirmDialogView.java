package com.triplify.ui.shared.component.button.view;

import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ConfirmDialogView {

    @FXML private Label messageLabel;
    @FXML private HBox actionsBox;

    private Runnable onConfirmed;

    public void configure(String message, Runnable onConfirmed, FxmlLoaderHelper fxmlLoader) {
        this.onConfirmed = onConfirmed;
        messageLabel.setText(message);

        Button cancelBtn = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.SECONDARY)
                .labelBinding(Localization.textBinding("button.cancel"))
                .onAction(this::close)
                .build();

        Button confirmBtn = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.DANGER)
                .labelBinding(Localization.textBinding("button.confirm"))
                .onAction(this::onConfirm)
                .build();

        actionsBox.getChildren().setAll(cancelBtn, confirmBtn);
    }

    private void onConfirm() {
        close();
        if (onConfirmed != null) onConfirmed.run();
    }

    private void close() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}
