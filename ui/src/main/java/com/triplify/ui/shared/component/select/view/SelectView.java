package com.triplify.ui.shared.component.select.view;

import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.entry.view.EntryCell;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.model.FieldVariant;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;

public class SelectView<T> extends HBox {

    private static final URL FXML_URL = SelectView.class.getResource("/com/triplify/ui/shared/component/select/view/AppSelect.fxml");

    @FXML private ComboBox<Entry<T>> comboBox;

    private Select<T> model;
    private FieldVariant lastVariant = null;

    public SelectView() {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AppSelect.fxml", e);
        }

        comboBox.setCellFactory(lv -> new EntryCell<>());

        comboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Entry<T> e) { return e == null ? "" : e.getLabel(); }
            @Override public Entry<T> fromString(String s) { return null; }
        });
    }

    public void update(Select<T> select) {
        this.model = select;

        comboBox.setButtonCell(new SelectButtonCell<>(select.getPlaceholder(), select.getEmoji()));
        comboBox.setItems(select.getItems());
        comboBox.disableProperty().bind(select.disabledProperty());
        comboBox.promptTextProperty().bind(select.placeholderProperty());

        if (select.getSelectedItem() != null) {
            comboBox.setValue(select.getSelectedItem());
        }

        applyVariant(select.getVariant());

        select.variantProperty().addListener((obs, oldVal, newVal) -> applyVariant(newVal));

        comboBox.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (!isShowing && comboBox.getScene() != null) {
                comboBox.getScene().getRoot().requestFocus();
            }
        });
    }

    private static String toStyleClass(FieldVariant variant) {
        return switch (variant) {
            case OUTLINED -> "app-select-variant-outlined";
            case FILLED -> "app-select-variant-filled";
            case GHOST -> "app-select-variant-ghost";
        };
    }

    private void applyVariant(FieldVariant variant) {
        if (lastVariant == variant) return;
        if (lastVariant != null) {
            comboBox.getStyleClass().remove(toStyleClass(lastVariant));
        }
        if (variant != null) {
            comboBox.getStyleClass().add(toStyleClass(variant));
        }
        lastVariant = variant;
    }

    @FXML
    private void onSelectionChanged() {
        Entry<T> value = comboBox.getValue();
        if (value != null && model != null) {
            model.setSelectedItem(value);
            model.triggerSelect(value);
        }
    }

    public ComboBox<Entry<T>> getComboBox() {
        return comboBox;
    }
}
