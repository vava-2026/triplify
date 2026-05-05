package com.triplify.ui.shared.component.select.view;

import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.entry.view.EntryCell;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.model.FieldVariant;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;

public class SelectView<T> extends HBox {

    private static final String ERROR_STYLE_CLASS = "app-select-error";

    private static final URL FXML_URL = SelectView.class.getResource("/com/triplify/ui/shared/component/select/view/AppSelect.fxml");

    @FXML private ComboBox<Entry<T>> comboBox;

    private Select<T> model;
    private FieldVariant lastVariant = null;
    private AppComponentSize lastSize = null;

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
        applySize(select.getSize());

        select.variantProperty().addListener((obs, oldVal, newVal) -> applyVariant(newVal));
        select.sizeProperty().addListener((obs, oldVal, newVal) -> applySize(newVal));

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

    private static String toSizeClass(AppComponentSize size) {
        return switch (size) {
            case SMALL -> "app-select-size-small";
            case MIDDLE -> "app-select-size-middle";
            case BIG -> "app-select-size-big";
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

    private void applySize(AppComponentSize size) {
        AppComponentSize effective = size == null ? AppComponentSize.MIDDLE : size;
        if (lastSize == effective) return;
        if (lastSize != null) {
            comboBox.getStyleClass().remove(toSizeClass(lastSize));
        }
        comboBox.getStyleClass().add(toSizeClass(effective));
        lastSize = effective;
    }

    @FXML
    private void onSelectionChanged() {
        Entry<T> value = comboBox.getValue();
        if (value != null && model != null) {
            clearError();
            model.setSelectedItem(value);
            model.triggerSelect(value);
        }
    }

    public void showError(String message) {
        if (!comboBox.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            comboBox.getStyleClass().add(ERROR_STYLE_CLASS);
        }
        if (message != null && !message.isBlank()) {
            comboBox.setTooltip(new Tooltip(message));
        }
    }

    public void clearError() {
        comboBox.getStyleClass().remove(ERROR_STYLE_CLASS);
        comboBox.setTooltip(null);
    }

    public ComboBox<Entry<T>> getComboBox() {
        return comboBox;
    }
}
