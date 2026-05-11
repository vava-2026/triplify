package com.triplify.ui.shared.component.input_item;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.Localization;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class InputItem extends VBox {

    private static final double FIELD_HEIGHT = 45;
    private static final double ACTION_BTN_SIZE = 36;

    private final TextField textField;
    private final Label errorLabel;
    private final Button clearButton;
    private final FontIcon clearIcon;
    private final StackPane fieldPane;

    public InputItem(String placeholderKey) {
        this(placeholderKey, FieldVariant.OUTLINED);
    }

    public InputItem(String placeholderKey, FieldVariant variant) {
        textField = new TextField();
        textField.promptTextProperty().bind(Localization.textBinding(placeholderKey));
        textField.getStyleClass().addAll("input-item", toStyleClass(variant));

        errorLabel = new Label();
        errorLabel.getStyleClass().add("input-error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        clearIcon = new FontIcon("fth-x");
        clearIcon.getStyleClass().add("input-action-icon");

        clearButton = new Button();
        clearButton.setGraphic(clearIcon);
        clearButton.setFocusTraversable(false);
        clearButton.getStyleClass().add("input-action-btn");
        clearButton.setOnAction(e -> {
            textField.clear();
            clearError();
        });

        clearButton.setVisible(false);
        clearButton.setManaged(false);
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasText = newVal != null && !newVal.isEmpty();
            clearButton.setVisible(hasText);
            clearButton.setManaged(hasText);
            if (hasText) {
                textField.getStyleClass().add("input-item-with-action");
            } else {
                textField.getStyleClass().remove("input-item-with-action");
            }
        });

        fieldPane = new StackPane();
        fieldPane.getChildren().addAll(textField, clearButton);
        StackPane.setAlignment(clearButton, Pos.CENTER_RIGHT);
        clearButton.translateXProperty().set(-5);

        textField.setPrefHeight(FIELD_HEIGHT);
        textField.setMaxWidth(Double.MAX_VALUE);
        clearButton.setPrefWidth(ACTION_BTN_SIZE);
        clearButton.setPrefHeight(ACTION_BTN_SIZE);
        clearButton.setMaxWidth(ACTION_BTN_SIZE);
        clearButton.setMaxHeight(ACTION_BTN_SIZE);
        fieldPane.setMaxWidth(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(fieldPane, errorLabel);

        textField.setOnAction(e -> validateRequired());
    }

    private static String toStyleClass(FieldVariant variant) {
        return switch (variant) {
            case OUTLINED -> "input-item--outlined";
            case FILLED -> "input-item--filled";
            case GHOST -> "input-item--ghost";
        };
    }

    public StringProperty textProperty() {
        return textField.textProperty();
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        textField.getStyleClass().remove("input-item-error");
        textField.getStyleClass().add("input-item-error");
    }

    public void showErrorHighlightOnly() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        textField.getStyleClass().remove("input-item-error");
        textField.getStyleClass().add("input-item-error");
    }

    public void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
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
