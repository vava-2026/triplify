package com.triplify.ui.shared.component.input_item;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class InputItem extends VBox {

    private TextField textField;
    private Label errorLabel;
    private Button clearButton;
    private Image cross;
    private ImageView crossView;
    private StackPane fieldPane;

    public InputItem(String text) {

        // Base elements
        textField = new TextField();
        textField.setPromptText(text);

        // Link design from css file
        textField.getStyleClass().add("input-item");

        // Error label
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Cross image
        cross = new Image(getClass().getResourceAsStream("/com/triplify/ui/shared/component/input_item/cross.png"));
        crossView = new ImageView(cross);
        crossView.setFitWidth(15);
        crossView.setFitHeight(15);
        crossView.setPreserveRatio(true);

        // Clear button
        clearButton = new Button();
        clearButton.setGraphic(crossView);
        clearButton.setFocusTraversable(false);
        clearButton.setStyle("-fx-background-color: transparent; -fx-font-size: 20; -fx-text-fill: black;");
        clearButton.setOnAction(e -> {
            textField.clear();
            clearError();
        });

        // Putting button inside of text field
        fieldPane = new StackPane();
        fieldPane.getChildren().addAll(textField, clearButton);
        StackPane.setAlignment(clearButton, Pos.CENTER_RIGHT);
        clearButton.translateXProperty().set(-5); // сдвиг внутрь края

        // CSS
        textField.getStyleClass().add("input-item");

        // Sizes
        textField.setPrefWidth(350);
        textField.setPrefHeight(45);
        clearButton.setPrefWidth(45);
        clearButton.setPrefHeight(45);
        clearButton.setMaxWidth(45);
        clearButton.setMaxHeight(45);
        fieldPane.setMaxWidth(350);
        fieldPane.setPrefWidth(350);

        // VBox
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(fieldPane, errorLabel);

        // Validation of input
        textField.setOnAction(e -> validateRequired());
    }

    // Get text
    public String getText() {
        return textField.getText();
    }

    // Set text
    public void setText(String text) {
        textField.setPromptText(text);
    }

    // Show Error
    public void showError() {
        errorLabel.setVisible(true);
        textField.setStyle("-fx-border-color: red; -fx-text-fill: red;");
    }

    // Hide Error
    public void clearError() {
        errorLabel.setVisible(false);
        textField.setStyle("");
    }

    // Change Position
    public void setPosition(Pos alignment) {
        setAlignment(alignment);
    }

    // Change Spacing
    public void setSpacingBetween(double spacing) {
        setSpacing(spacing);
    }

    // Change css style
    public void setStyleSheet(String cssPath) {
        getStylesheets().clear();
        String css = getClass().getResource(cssPath).toExternalForm();
        getStylesheets().add(css);
    }

    // Validation
    public boolean validateRequired() {
        if (getText() == null || getText().trim().isEmpty() || !getText().trim().endsWith("@gmail.com")) {
            showError();
            return false;
        }
        clearError();
        return true;
    }

}