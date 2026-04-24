package com.triplify.ui.shared.component.detail_actions.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class DetailActionButtonsView extends VBox {

    private static final URL FXML_URL = DetailActionButtonsView.class.getResource(
            "/com/triplify/ui/shared/component/detail_actions/view/DetailActionButtons.fxml"
    );
    private static final URL CSS_URL = DetailActionButtonsView.class.getResource(
            "/com/triplify/ui/shared/component/detail_actions/css/detail_actions.css"
    );

    @FXML private Button primaryButton;
    @FXML private Button secondaryButton;

    public DetailActionButtonsView() {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DetailActionButtons.fxml", e);
        }

        if (CSS_URL != null) {
            getStylesheets().add(CSS_URL.toExternalForm());
        }
    }

    public Button getPrimaryButton() {
        return primaryButton;
    }

    public Button getSecondaryButton() {
        return secondaryButton;
    }
}
