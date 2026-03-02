import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class PasswordItem extends VBox {

    private TextField textField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button toggleButton;
    private boolean isVisible = false;
    private HBox fieldBox;

    public PasswordItem(String placeholder) {
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Password fields
        passwordField = new PasswordField();
        passwordField.setPromptText(placeholder);
        passwordField.getStyleClass().add("input-item");

        textField = new TextField();
        textField.setPromptText(placeholder);
        textField.getStyleClass().add("input-item");
        textField.setVisible(false);

        // Switch button
        toggleButton = new Button("👁");
        toggleButton.getStyleClass().add("toggle-button");
        toggleButton.setFocusTraversable(false);
        toggleButton.setOnAction(e -> togglePasswordVisibility());

        // HBox для поля + кнопка
        fieldBox = new HBox(5, passwordField, toggleButton);
        fieldBox.setAlignment(Pos.CENTER_LEFT);

        // VBox
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(fieldBox, errorLabel);
    }

    private void togglePasswordVisibility() {
        if (isVisible) {
            textField.setVisible(false);
            passwordField.setText(textField.getText());
            passwordField.setVisible(true);
            fieldBox.getChildren().set(0, passwordField);
            isVisible = false;
        } else {
            textField.setText(passwordField.getText());
            textField.setVisible(true);
            passwordField.setVisible(false);
            fieldBox.getChildren().set(0, textField);
            isVisible = true;
        }
    }

    public String getText() {
        return isVisible ? passwordField.getText() : textField.getText();
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
}