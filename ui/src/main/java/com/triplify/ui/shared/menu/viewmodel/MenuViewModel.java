package com.triplify.ui.shared.menu.viewmodel;

import com.triplify.ui.shared.menu.model.MenuItem;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MenuViewModel {

    private final ObjectProperty<MenuItem> selectedItem =
            new SimpleObjectProperty<>(MenuItem.MAP);
    private final BooleanProperty collapsed =
            new SimpleBooleanProperty(false);

    private final BooleanBinding hideHeader =
            Bindings.createBooleanBinding(
                    () -> selectedItem.get().isHideHeader(),
                    selectedItem);

    public ObjectProperty<MenuItem> selectedItemProperty() { return selectedItem; }
    public MenuItem getSelectedItem() { return selectedItem.get(); }
    public void setSelectedItem(MenuItem item) { selectedItem.set(item); }

    public BooleanProperty collapsedProperty() { return collapsed; }
    public boolean isCollapsed() { return collapsed.get(); }
    public void toggleCollapsed() { collapsed.set(!collapsed.get()); }

    public BooleanBinding hideHeaderProperty() { return hideHeader; }
    public boolean isHideHeader() { return hideHeader.get(); }
}
