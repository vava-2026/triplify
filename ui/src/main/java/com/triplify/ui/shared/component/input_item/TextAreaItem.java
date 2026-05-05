package com.triplify.ui.shared.component.input_item;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.Localization;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class TextAreaItem extends VBox {

    private static final double FIELD_HEIGHT = 120;
    private static final double ACTION_BTN_SIZE = 36;

    private TextArea textArea;
    private Label errorLabel;
    private Button clearButton;
    private FontIcon clearIcon;
    private StackPane fieldPane;

    public TextAreaItem(String placeholderKey) {
        this(placeholderKey, FieldVariant.OUTLINED);
    }

    public TextAreaItem(String placeholderKey, FieldVariant variant) {
        textArea = new TextArea();
        textArea.promptTextProperty().bind(Localization.textBinding(placeholderKey));
        textArea.getStyleClass().addAll("input-item", "textarea-item", toStyleClass(variant));
        textArea.setWrapText(true);

        textArea.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin == null) return;
            ScrollPane sp = (ScrollPane) textArea.lookup(".scroll-pane");
            if (sp != null) {
                sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }
        });

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
            textArea.clear();
            clearError();
        });

        clearButton.setVisible(false);
        clearButton.setManaged(false);
        textArea.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasText = newVal != null && !newVal.isEmpty();
            clearButton.setVisible(hasText);
            clearButton.setManaged(hasText);
            if (hasText) {
                textArea.getStyleClass().add("textarea-item-with-action");
            } else {
                textArea.getStyleClass().remove("textarea-item-with-action");
            }
        });

        fieldPane = new StackPane();
        fieldPane.getChildren().addAll(textArea, clearButton);
        StackPane.setAlignment(clearButton, Pos.TOP_RIGHT);
        clearButton.translateXProperty().set(-5);
        clearButton.translateYProperty().set(5);

        textArea.setPrefHeight(FIELD_HEIGHT);
        textArea.setMaxWidth(Double.MAX_VALUE);
        clearButton.setPrefWidth(ACTION_BTN_SIZE);
        clearButton.setPrefHeight(ACTION_BTN_SIZE);
        clearButton.setMaxWidth(ACTION_BTN_SIZE);
        clearButton.setMaxHeight(ACTION_BTN_SIZE);
        fieldPane.setMaxWidth(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);

        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(fieldPane, errorLabel);
    }

    private static String toStyleClass(FieldVariant variant) {
        return switch (variant) {
            case OUTLINED -> "input-item--outlined";
            case FILLED -> "input-item--filled";
            case GHOST -> "input-item--ghost";
        };
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        textArea.getStyleClass().remove("input-item-error");
        textArea.getStyleClass().add("input-item-error");
    }

    public void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        textArea.getStyleClass().remove("input-item-error");
    }

    public boolean validateRequired() {
        if (getText() == null || getText().trim().isEmpty()) {
            showError(I18n.t("validation.field.required"));
            return false;
        }
        clearError();
        return true;
    }

    public void setRows(int rows) {
        textArea.setPrefRowCount(rows);
    }

    public void setPosition(Pos alignment) {
        setAlignment(alignment);
    }

    public void setSpacingBetween(double spacing) {
        setSpacing(spacing);
    }
}
