package com.triplify.ui.shared.component.select.model;

import com.triplify.ui.shared.component.entry.model.Entry;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class Select<T> {

    private final StringProperty placeholder = new SimpleStringProperty("Select...");
    private final BooleanProperty disabled = new SimpleBooleanProperty(false);
    private final ObjectProperty<Entry<T>> selectedItem = new SimpleObjectProperty<>(null);
    private final ListProperty<Entry<T>> items = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<SelectVariant> variant = new SimpleObjectProperty<>(SelectVariant.FILLED);

    private java.util.function.Consumer<Entry<T>> onSelect = null;

    public StringProperty placeholderProperty() {
        return placeholder;
    }

    public String getPlaceholder() {
        return placeholder.get();
    }

    public void setPlaceholder(String v) {
        placeholder.set(v);
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public void setDisabled(boolean v) {
        disabled.set(v);
    }

    public ObjectProperty<Entry<T>> selectedItemProperty() {
        return selectedItem;
    }

    public Entry<T> getSelectedItem() {
        return selectedItem.get();
    }

    public void setSelectedItem(Entry<T> v) {
        selectedItem.set(v);
    }

    public ListProperty<Entry<T>> itemsProperty() {
        return items;
    }

    public ObservableList<Entry<T>> getItems() {
        return items.get();
    }

    public void setItems(ObservableList<Entry<T>> v) {
        items.set(v);
    }

    public ObjectProperty<SelectVariant> variantProperty() {
        return variant;
    }

    public SelectVariant getVariant() {
        return variant.get();
    }

    public void setVariant(SelectVariant v) {
        variant.set(v);
    }

    public void setOnSelect(java.util.function.Consumer<Entry<T>> r) {
        this.onSelect = r;
    }

    public void triggerSelect(Entry<T> value) {
        if (onSelect != null) onSelect.accept(value);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {
        private String placeholder = "Select...";
        private boolean disabled = false;
        private Entry<T> selectedItem = null;
        private ObservableList<Entry<T>> items = FXCollections.observableArrayList();
        private SelectVariant variant = SelectVariant.FILLED;
        private java.util.function.Consumer<Entry<T>> onSelect = null;

        private Builder() {}

        public Builder<T> placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder<T> disabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public Builder<T> selectedItem(Entry<T> selectedItem) {
            this.selectedItem = selectedItem;
            return this;
        }

        public Builder<T> items(ObservableList<Entry<T>> items) {
            this.items = items;
            return this;
        }

        public Builder<T> items(List<Entry<T>> items) {
            this.items = FXCollections.observableArrayList(items);
            return this;
        }

        public Builder<T> variant(SelectVariant variant) {
            this.variant = variant;
            return this;
        }

        public Builder<T> onSelect(java.util.function.Consumer<Entry<T>> onSelect) {
            this.onSelect = onSelect;
            return this;
        }

        public Select<T> build() {
            Select<T> select = new Select<>();
            select.setPlaceholder(placeholder);
            select.setDisabled(disabled);
            select.setSelectedItem(selectedItem);
            select.setItems(items);
            select.setVariant(variant);
            select.setOnSelect(onSelect);
            return select;
        }
    }
}
