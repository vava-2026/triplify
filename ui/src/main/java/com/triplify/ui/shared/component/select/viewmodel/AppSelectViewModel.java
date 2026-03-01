package com.triplify.ui.shared.component.select.viewmodel;

import com.triplify.ui.shared.component.select.model.SelectEntry;
import com.triplify.ui.shared.component.select.model.SelectVariant;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppSelectViewModel<T> {

    private final StringProperty placeholder = new SimpleStringProperty("Select...");
    private final BooleanProperty disabled = new SimpleBooleanProperty(false);
    private final ObjectProperty<SelectEntry<T>> selectedItem = new SimpleObjectProperty<>(null);
    private final ListProperty<SelectEntry<T>> items =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<SelectVariant> variant =
            new SimpleObjectProperty<>(SelectVariant.PRIMARY);

    private java.util.function.Consumer<SelectEntry<T>> onSelect = null;

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

    public ObjectProperty<SelectEntry<T>> selectedItemProperty() {
        return selectedItem;
    }

    public SelectEntry<T> getSelectedItem() {
        return selectedItem.get();
    }

    public void setSelectedItem(SelectEntry<T> v) {
        selectedItem.set(v);
    }

    public ListProperty<SelectEntry<T>> itemsProperty() {
        return items;
    }

    public ObservableList<SelectEntry<T>> getItems() {
        return items.get();
    }

    public void setItems(ObservableList<SelectEntry<T>> v) {
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

    public void setOnSelect(java.util.function.Consumer<SelectEntry<T>> r) {
        this.onSelect = r;
    }

    public void triggerSelect(SelectEntry<T> value) {
        if (onSelect != null) onSelect.accept(value);
    }
}
