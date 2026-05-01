package com.triplify.ui.shared.component.input_item;

import com.triplify.ui.shared.model.FieldVariant;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class NumericInputItem extends VBox {

    private static final double FIELD_HEIGHT = 45;
    private static final double BTN_WIDTH = 28;

    private final TextField textField;
    private final Label errorLabel;
    private final int min;
    private final int max;

    public NumericInputItem(int min, int max, int initialValue) {
        this(min, max, initialValue, FieldVariant.OUTLINED);
    }

    public NumericInputItem(int min, int max, int initialValue, FieldVariant variant) {
        this.min = min;
        this.max = max;

        textField = new TextField();
        textField.getStyleClass().addAll("input-item", toStyleClass(variant), "input-item-with-action");
        textField.setPrefHeight(FIELD_HEIGHT);
        textField.setMaxWidth(Double.MAX_VALUE);

        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String filtered = newVal.replaceAll("[^0-9]", "");
            if (!filtered.equals(newVal)) {
                textField.setText(filtered);
            }
        });

        textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) setValue(getValue());
        });

        FontIcon upIcon = new FontIcon("fth-chevron-up");
        upIcon.getStyleClass().add("input-action-icon");
        upIcon.setIconSize(12);
        Button upButton = new Button();
        upButton.setGraphic(upIcon);
        upButton.setFocusTraversable(false);
        upButton.getStyleClass().add("input-action-btn");
        upButton.setPrefWidth(BTN_WIDTH);
        upButton.setPrefHeight(FIELD_HEIGHT / 2);
        upButton.setMaxWidth(BTN_WIDTH);
        upButton.setMaxHeight(FIELD_HEIGHT / 2);
        upButton.setOnAction(e -> setValue(getValue() + 1));

        FontIcon downIcon = new FontIcon("fth-chevron-down");
        downIcon.getStyleClass().add("input-action-icon");
        downIcon.setIconSize(12);
        Button downButton = new Button();
        downButton.setGraphic(downIcon);
        downButton.setFocusTraversable(false);
        downButton.getStyleClass().add("input-action-btn");
        downButton.setPrefWidth(BTN_WIDTH);
        downButton.setPrefHeight(FIELD_HEIGHT / 2);
        downButton.setMaxWidth(BTN_WIDTH);
        downButton.setMaxHeight(FIELD_HEIGHT / 2);
        downButton.setOnAction(e -> setValue(getValue() - 1));

        VBox buttons = new VBox(upButton, downButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPrefWidth(BTN_WIDTH);
        buttons.setMaxWidth(BTN_WIDTH);
        buttons.setTranslateX(-2);

        StackPane fieldPane = new StackPane(textField, buttons);
        StackPane.setAlignment(buttons, Pos.CENTER_RIGHT);
        fieldPane.setMaxWidth(Double.MAX_VALUE);

        errorLabel = new Label();
        errorLabel.getStyleClass().add("input-error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        setMaxWidth(Double.MAX_VALUE);
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(fieldPane, errorLabel);

        setValue(initialValue);
    }

    private static String toStyleClass(FieldVariant variant) {
        return switch (variant) {
            case OUTLINED -> "input-item--outlined";
            case FILLED -> "input-item--filled";
            case GHOST -> "input-item--ghost";
        };
    }

    public int getValue() {
        String text = textField.getText();
        if (text == null || text.isBlank()) return min;
        try {
            return Math.min(max, Math.max(min, Integer.parseInt(text)));
        } catch (NumberFormatException e) {
            return min;
        }
    }

    public void setValue(int value) {
        int clamped = Math.min(max, Math.max(min, value));
        textField.setText(String.valueOf(clamped));
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        textField.getStyleClass().remove("input-item-error");
        textField.getStyleClass().add("input-item-error");
    }

    public void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        textField.getStyleClass().remove("input-item-error");
    }
}
