package com.triplify.ui.shared.component.entry.view;

import com.triplify.ui.shared.component.entry.model.EntryVariant;
import com.triplify.ui.shared.component.entry.model.Entry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

/// Basically a label with an icon, used to represent an entry in a list or menu.
/// Supports different variants for styling (e.g. primary, secondary, danger).
///
public class EntryView<T> extends HBox {

    @FXML private Label label;
    @FXML private FontIcon icon;

    private EntryVariant lastVariant = null;

    public EntryView() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/triplify/ui/shared/component/entry/view/AppEntry.fxml")
        );
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
            applyVariant(null);
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

        applyVariant(entry.hasVariant() ? entry.getVariant() : null);
    }

    private void applyVariant(EntryVariant variant) {
        if (lastVariant == variant) return;
        if (lastVariant != null) {
            getStyleClass().remove(lastVariant.getStyleClass());
        }
        if (variant != null) {
            getStyleClass().add(variant.getStyleClass());
        }
        lastVariant = variant;
    }
}
