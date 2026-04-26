package com.triplify.ui.shared.component.select.entry.view;

import com.triplify.application.shared.ColorTheme;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.util.EmojiUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class EntryView<T> extends HBox {

    private static final URL FXML_URL = EntryView.class.getResource("/com/triplify/ui/shared/component/entry/view/AppEntry.fxml");

    @FXML private ImageView emoji;
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
            emoji.setVisible(false);
            emoji.setManaged(false);
            bindLabel(null);
            icon.setVisible(false);
            icon.setManaged(false);
            applyColorTheme(null);
            return;
        }

        if (entry.hasEmoji()) {
            Image img = EmojiUtil.toImage(entry.getEmoji(), 16);
            boolean show = img != null && !img.isError();
            emoji.setImage(show ? img : null);
            emoji.setVisible(show);
            emoji.setManaged(show);
        } else {
            emoji.setVisible(false);
            emoji.setManaged(false);
        }

        bindLabel(entry.labelProperty());

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

    private void bindLabel(ObservableValue<String> value) {
        label.textProperty().unbind();
        if (value == null) {
            label.setText(null);
            return;
        }
        label.textProperty().bind(value);
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
