package com.triplify.ui.shared.component.entry.view;

import com.triplify.application.model.ColorTheme;
import com.triplify.ui.shared.component.entry.model.Entry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class EntryView<T> extends HBox {

    private static final URL FXML_URL = EntryView.class.getResource("/com/triplify/ui/shared/component/entry/view/AppEntry.fxml");

    @FXML private Label label;
    @FXML private FontIcon icon;

    private ColorTheme lastColorTheme = null;

    public EntryView() {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AppEntry.fxml", e);
        }
    }

    public void update(Entry<T> entry) {
        if (entry == null) {
            label.setText(null);
            icon.setVisible(false);
            icon.setManaged(false);
            applyColorTheme(null);
            return;
        }

        label.setText(entry.getLabel());

        if (entry.hasIcon()) {
            icon.setIconLiteral(entry.getIconLiteral());
            icon.setVisible(true);
            icon.setManaged(true);
        } else {
            icon.setVisible(false);
            icon.setManaged(false);
        }

        applyColorTheme(entry.hasColorTheme() ? entry.getColorTheme() : null);
    }

    private void applyColorTheme(ColorTheme theme) {
        if (lastColorTheme == theme) return;
        if (lastColorTheme != null) {
            getStyleClass().remove(lastColorTheme.getStyleClass());
        }
        if (theme != null) {
            getStyleClass().add(theme.getStyleClass());
        }
        lastColorTheme = theme;
    }
}
