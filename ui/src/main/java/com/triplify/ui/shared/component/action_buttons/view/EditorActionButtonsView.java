package com.triplify.ui.shared.component.action_buttons.view;

import com.triplify.ui.shared.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class EditorActionButtonsView extends VBox {

    private static final URL FXML_URL = EditorActionButtonsView.class.getResource(
            "/com/triplify/ui/shared/component/action_buttons/view/EditorActionButtons.fxml"
    );
    private static final URL CSS_URL = EditorActionButtonsView.class.getResource(
            "/com/triplify/ui/shared/component/editor/css/editor_components.css"
    );

    @FXML private Button primaryButton;
    @FXML private Button secondaryButton;

    private final StringProperty primaryTextKey = new SimpleStringProperty();
    private final StringProperty secondaryTextKey = new SimpleStringProperty();

    public EditorActionButtonsView() {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load EditorActionButtons.fxml", e);
        }

        if (CSS_URL != null) {
            getStylesheets().add(CSS_URL.toExternalForm());
        }

        primaryTextKey.addListener((obs, oldKey, newKey) -> bindButtonText(primaryButton, newKey));
        secondaryTextKey.addListener((obs, oldKey, newKey) -> bindButtonText(secondaryButton, newKey));
    }

    private void bindButtonText(Button button, String key) {
        button.textProperty().unbind();
        if (key == null || key.isBlank()) {
            return;
        }
        Localization.bindText(button.textProperty(), key);
    }

    public Button getPrimaryButton() {
        return primaryButton;
    }

    public Button getSecondaryButton() {
        return secondaryButton;
    }

    public String getPrimaryText() {
        return primaryButton.getText();
    }

    public void setPrimaryText(String text) {
        primaryButton.textProperty().unbind();
        primaryButton.setText(text);
    }

    public StringProperty primaryTextProperty() {
        return primaryButton.textProperty();
    }

    public String getSecondaryText() {
        return secondaryButton.getText();
    }

    public void setSecondaryText(String text) {
        secondaryButton.textProperty().unbind();
        secondaryButton.setText(text);
    }

    public StringProperty secondaryTextProperty() {
        return secondaryButton.textProperty();
    }

    public String getPrimaryTextKey() {
        return primaryTextKey.get();
    }

    public void setPrimaryTextKey(String key) {
        primaryTextKey.set(key);
    }

    public StringProperty primaryTextKeyProperty() {
        return primaryTextKey;
    }

    public String getSecondaryTextKey() {
        return secondaryTextKey.get();
    }

    public void setSecondaryTextKey(String key) {
        secondaryTextKey.set(key);
    }

    public StringProperty secondaryTextKeyProperty() {
        return secondaryTextKey;
    }
}
