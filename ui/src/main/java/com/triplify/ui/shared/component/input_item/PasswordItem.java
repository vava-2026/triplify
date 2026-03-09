package com.triplify.ui.shared.component.input_item;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class PasswordItem extends VBox {

    private TextField textField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button toggleButton;
    private boolean isVisible = false;
    private StackPane fieldPane;
    private ImageView eyeView;
    private Image eye;
    private Image eye_off;

    public PasswordItem(String placeholder) {
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        eye = new Image(getClass().getResourceAsStream("/com/triplify/ui/shared/component/input_item/eye.png"));
        eye_off = new Image(getClass().getResourceAsStream("/com/triplify/ui/shared/component/input_item/eye_off.png"));

        eyeView = new ImageView(eye_off);
        eyeView.setFitWidth(25);
        eyeView.setFitHeight(25);
        eyeView.setPreserveRatio(true);

        // Password fields
        passwordField = new PasswordField();
        passwordField.setPromptText(placeholder);
        passwordField.getStyleClass().add("input-item");
        System.out.println("Classes: " + passwordField.getStyleClass()); // добавь это
        passwordField.setPrefWidth(350);
        passwordField.setPrefHeight(45);

        textField = new TextField();
        textField.setPromptText(placeholder);
        textField.getStyleClass().add("input-item");
        textField.setPrefWidth(350);
        textField.setPrefHeight(45);
        textField.setVisible(false);

        // Toggle button
        toggleButton = new Button();
        toggleButton.setGraphic(eyeView);
        toggleButton.setFocusTraversable(false);
        toggleButton.setStyle("-fx-background-color: transparent;");
        toggleButton.setPrefWidth(45);
        toggleButton.setPrefHeight(45);
        toggleButton.setMaxWidth(45);
        toggleButton.setMaxHeight(45);
        toggleButton.setOnAction(e -> togglePasswordVisibility());

        // StackPane — кнопка внутри поля
        fieldPane = new StackPane();
        fieldPane.setPrefWidth(350);
        fieldPane.setMaxWidth(350);
        fieldPane.getChildren().addAll(passwordField, textField, toggleButton);
        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        toggleButton.translateXProperty().set(-5);

        // VBox
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(fieldPane, errorLabel);
    }

    private void togglePasswordVisibility() {
        if (isVisible) {
            textField.setVisible(false);
            passwordField.setText(textField.getText());
            passwordField.setVisible(true);
            eyeView.setImage(eye_off);
            isVisible = false;
        } else {
            textField.setText(passwordField.getText());
            textField.setVisible(true);
            passwordField.setVisible(false);
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

    public void setPosition(Pos alignment) {
        setAlignment(alignment);
    }

    public void setSpacingBetween(double spacing) {
        setSpacing(spacing);
    }

    public void setStyleSheet(String cssPath) {
        getStylesheets().clear();
        String css = getClass().getResource(cssPath).toExternalForm();
        getStylesheets().add(css);
    }
}