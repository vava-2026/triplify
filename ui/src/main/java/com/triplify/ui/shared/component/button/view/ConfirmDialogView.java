package com.triplify.ui.shared.component.button.view;

import com.triplify.ui.i18n.I18n;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ConfirmDialogView implements Initializable {

    @FXML private Label  messageLabel;
    @FXML private Button cancelBtn;
    @FXML private Button confirmBtn;

    private Runnable onConfirmed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cancelBtn.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> I18n.t("button.cancel"), I18n.bundleProperty()));
        confirmBtn.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> I18n.t("button.confirm"), I18n.bundleProperty()));
    }

    public void configure(String message, Runnable onConfirmed) {
        this.onConfirmed = onConfirmed;
        messageLabel.setText(message);
    }

    @FXML
    private void onCancel() {
        close();
    }

    @FXML
    private void onConfirm() {
        close();
        if (onConfirmed != null) onConfirmed.run();
    }

    private void close() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}

