package com.triplify.ui.shared.component.select.view;

import com.triplify.ui.shared.component.entry.model.Entry;
import com.triplify.ui.shared.component.entry.view.EntryCell;
import com.triplify.ui.shared.component.select.model.Select;
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
        comboBox.setButtonCell(new EntryCell<>());

        comboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Entry<T> e) { return e == null ? "" : e.getLabel(); }
            @Override public Entry<T> fromString(String s) { return null; }
        });
    }

    public void update(Select<T> select) {
        this.model = select;

        comboBox.setItems(select.getItems());
        comboBox.disableProperty().bind(select.disabledProperty());
        comboBox.promptTextProperty().bind(select.placeholderProperty());

        if (select.getSelectedItem() != null) {
            comboBox.setValue(select.getSelectedItem());
        }
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
