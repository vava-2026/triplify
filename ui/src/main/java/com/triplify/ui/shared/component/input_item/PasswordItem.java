package com.triplify.ui.shared.component.input_item;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.Localization;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class PasswordItem extends VBox {

    private static final double FIELD_HEIGHT = 45;
    private static final double ACTION_BTN_SIZE = 36;

    private TextField textField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button toggleButton;
    private boolean isVisible = false;
    private StackPane fieldPane;
    private FontIcon toggleIcon;

    public PasswordItem(String placeholderKey) {
        this(placeholderKey, FieldVariant.OUTLINED);
    }

    public PasswordItem(String placeholderKey, FieldVariant variant) {
        errorLabel = new Label();
        errorLabel.getStyleClass().add("input-error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        toggleIcon = new FontIcon("fth-eye-off");
        toggleIcon.getStyleClass().add("input-action-icon");

        passwordField = new PasswordField();
        passwordField.promptTextProperty().bind(Localization.textBinding(placeholderKey));
        passwordField.getStyleClass().addAll("input-item", toStyleClass(variant), "input-item-with-action");
        passwordField.setPrefHeight(FIELD_HEIGHT);
        passwordField.setMaxWidth(Double.MAX_VALUE);

        textField = new TextField();
        textField.promptTextProperty().bind(Localization.textBinding(placeholderKey));
        textField.getStyleClass().addAll("input-item", toStyleClass(variant), "input-item-with-action");
        textField.setPrefHeight(FIELD_HEIGHT);
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setVisible(false);

        toggleButton = new Button();
        toggleButton.setGraphic(toggleIcon);
        toggleButton.setFocusTraversable(false);
        toggleButton.getStyleClass().add("input-action-btn");
        toggleButton.setPrefWidth(ACTION_BTN_SIZE);
        toggleButton.setPrefHeight(ACTION_BTN_SIZE);
        toggleButton.setMaxWidth(ACTION_BTN_SIZE);
        toggleButton.setMaxHeight(ACTION_BTN_SIZE);
        toggleButton.setOnAction(e -> togglePasswordVisibility());

        fieldPane = new StackPane();
        fieldPane.setMaxWidth(Double.MAX_VALUE);
        fieldPane.getChildren().addAll(passwordField, textField, toggleButton);
        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        toggleButton.translateXProperty().set(-5);

        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Double.MAX_VALUE);
        getChildren().addAll(fieldPane, errorLabel);
    }

    private static String toStyleClass(FieldVariant variant) {
        return switch (variant) {
            case OUTLINED -> "input-item--outlined";
            case FILLED -> "input-item--filled";
            case GHOST -> "input-item--ghost";
        };
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

    public void reset() {
        setText("");
        clearError();
        textField.setVisible(false);
        passwordField.setVisible(true);
        toggleIcon.setIconLiteral("fth-eye-off");
        isVisible = false;
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