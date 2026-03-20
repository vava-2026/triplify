package com.triplify.ui.shared.component.input_item;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.input_item.model.InputVariant;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class PasswordItem extends VBox {

    private TextField textField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button toggleButton;
    private boolean isVisible = false;
    private StackPane fieldPane;
    private FontIcon toggleIcon;

    public PasswordItem(String placeholderKey) {
        this(placeholderKey, InputVariant.OUTLINED);
    }

    public PasswordItem(String placeholderKey, InputVariant variant) {
        errorLabel = new Label();
        errorLabel.getStyleClass().add("input-error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        toggleIcon = new FontIcon("fth-eye-off");
        toggleIcon.getStyleClass().add("input-action-icon");

        passwordField = new PasswordField();
        passwordField.promptTextProperty().bind(Bindings.createStringBinding(() -> I18n.t(placeholderKey), I18n.bundleProperty()));
        passwordField.getStyleClass().addAll("input-item", variant.getStyleClass(), "input-item-with-action");
        passwordField.setPrefWidth(350);
        passwordField.setPrefHeight(45);

        textField = new TextField();
        textField.promptTextProperty().bind(Bindings.createStringBinding(() -> I18n.t(placeholderKey), I18n.bundleProperty()));
        textField.getStyleClass().addAll("input-item", variant.getStyleClass(), "input-item-with-action");
        textField.setPrefWidth(350);
        textField.setPrefHeight(45);
        textField.setVisible(false);

        toggleButton = new Button();
        toggleButton.setGraphic(toggleIcon);
        toggleButton.setFocusTraversable(false);
        toggleButton.getStyleClass().add("input-action-btn");
        toggleButton.setPrefWidth(36);
        toggleButton.setPrefHeight(36);
        toggleButton.setMaxWidth(36);
        toggleButton.setMaxHeight(36);
        toggleButton.setOnAction(e -> togglePasswordVisibility());

        fieldPane = new StackPane();
        fieldPane.setPrefWidth(350);
        fieldPane.getChildren().addAll(passwordField, textField, toggleButton);
        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        toggleButton.translateXProperty().set(-5);

        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(fieldPane, errorLabel);
    }

    private void togglePasswordVisibility() {
        if (isVisible) {
            textField.setVisible(false);
            passwordField.setText(textField.getText());
            passwordField.setVisible(true);
            toggleIcon.setIconLiteral("fth-eye-off");
            isVisible = false;
        } else {
            textField.setText(passwordField.getText());
            textField.setVisible(true);
            passwordField.setVisible(false);
            toggleIcon.setIconLiteral("fth-eye");
            isVisible = true;
        }
    }

    public String getText() {
        return isVisible ? textField.getText() : passwordField.getText();
    }

    public void setText(String text) {
        passwordField.setText(text);
        textField.setText(text);
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        passwordField.getStyleClass().remove("input-item-error");
        passwordField.getStyleClass().add("input-item-error");
        textField.getStyleClass().remove("input-item-error");
        textField.getStyleClass().add("input-item-error");
    }

    public void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        passwordField.getStyleClass().remove("input-item-error");
        textField.getStyleClass().remove("input-item-error");
    }

    public boolean validateRequired() {
        if (getText() == null || getText().trim().isEmpty()) {
            showError(I18n.t("validation.field.required"));
            return false;
        }
        clearError();
        return true;
    }

    public void setPosition(Pos alignment) {
        setAlignment(alignment);
    }

    public void setSpacingBetween(double spacing) {
        setSpacing(spacing);
    }
}