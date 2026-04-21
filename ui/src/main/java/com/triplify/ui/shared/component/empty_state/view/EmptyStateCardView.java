package com.triplify.ui.shared.component.empty_state.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class EmptyStateCardView extends StackPane {

    private static final URL FXML_URL = EmptyStateCardView.class.getResource(
            "/com/triplify/ui/shared/component/empty_state/view/EmptyStateCard.fxml"
    );
    private static final URL CSS_URL = EmptyStateCardView.class.getResource(
            "/com/triplify/ui/shared/component/empty_state/css/empty_state.css"
    );

    @FXML private Label textLabel;

    public EmptyStateCardView() {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load EmptyStateCard.fxml", e);
        }

        if (CSS_URL != null) {
            getStylesheets().add(CSS_URL.toExternalForm());
        }
    }

    public void setText(String text) {
        textLabel.setText(text);
    }
}
