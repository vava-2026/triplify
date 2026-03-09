package com.triplify.ui.shared.component.input_item;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class PasswordItem extends VBox {

    private TextField textField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button toggleButton;
    private boolean isVisible = false;
    private HBox fieldBox;

    private ImageView eyeView;
    private Image eye;
    private Image eye_off;

    public PasswordItem(String placeholder) {
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        eye = new Image(getClass().getResourceAsStream("/com/triplify/ui/shared/component/input_item/eye.png"));
        eye_off = new Image(getClass().getResourceAsStream("/com/triplify/ui/shared/component/input_item/eye_off.png"));

        eyeView = new ImageView(eye);
        eyeView.setFitWidth(20);
        eyeView.setFitHeight(20);
        eyeView.setPreserveRatio(true);

        // Password fields
        passwordField = new PasswordField();
        passwordField.setPromptText(placeholder);
        passwordField.getStyleClass().add("input-item");
        passwordField.setMaxWidth(200);
        passwordField.setPrefHeight(28);

        textField = new TextField();
        textField.setPromptText(placeholder);
        textField.getStyleClass().add("input-item");
        textField.setVisible(false);
        textField.setMaxWidth(200); // ограничиваем ширину
        textField.setPrefHeight(28);

        // Создаём ImageView
        eyeView.setFitWidth(20);  // ширина иконки
        eyeView.setFitHeight(20); // высота иконки
        eyeView.setPreserveRatio(true);


        // Switch button
        toggleButton = new Button();
        toggleButton.setGraphic(eyeView);
        toggleButton.setFocusTraversable(false);
        toggleButton.setStyle("""
            -fx-background-color: #f5f5f5;
            -fx-border-color: #d0d0d0;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            -fx-cursor: hand;
        """);
        toggleButton.setOnAction(e -> togglePasswordVisibility());
        toggleButton.setPrefHeight(28);


        // HBox для поля + кнопка
        fieldBox = new HBox(5, passwordField, toggleButton);
        fieldBox.setAlignment(Pos.CENTER);

        // VBox
        setSpacing(5);
        setAlignment(Pos.CENTER);
        setMaxWidth(200);
        getChildren().addAll(fieldBox, errorLabel);
    }

    private void togglePasswordVisibility() {
        if (isVisible) {
            textField.setVisible(false);
            passwordField.setText(textField.getText());
            passwordField.setVisible(true);
            fieldBox.getChildren().set(0, passwordField);

            // Меняем иконку
            eyeView.setImage(eye_off);

            isVisible = false;
        } else {
            textField.setText(passwordField.getText());
            textField.setVisible(true);
            passwordField.setVisible(false);
            fieldBox.getChildren().set(0, textField);

            eyeView.setImage(eye);

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
        passwordField.setStyle("-fx-border-color: red;");
        textField.setStyle("-fx-border-color: red;");
    }

    public void clearError() {
        errorLabel.setVisible(false);
        passwordField.setStyle("");
        textField.setStyle("");
    }

    public boolean validateRequired() {
        if (getText() == null || getText().trim().isEmpty()) {
            showError("This field is required");
            return false;
        }
        clearError();
        return true;
    }

    // Change Position
    public void setPosition(Pos alignment) {
        setAlignment(alignment);
        fieldBox.setAlignment(alignment);
    }

    // Change Spacing
    public void setSpacingBetween(double spacing) {
        setSpacing(spacing);
        fieldBox.setSpacing(spacing);
    }

    // Change css style
    public void setStyleSheet(String cssPath) {
        getStylesheets().clear();
        String css = getClass().getResource(cssPath).toExternalForm();
        getStylesheets().add(css);
    }

}