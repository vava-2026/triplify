package com.triplify.ui.shared.menu.viewmodel;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.menu.model.MenuItem;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MenuViewModel {

    private final ObjectProperty<MenuItem> selectedItem = new SimpleObjectProperty<>(MenuItem.MAP);
    private final BooleanProperty collapsed = new SimpleBooleanProperty(false);

    public ObjectProperty<MenuItem> selectedItemProperty() { return selectedItem; }
    public MenuItem getSelectedItem() { return selectedItem.get(); }
    public void setSelectedItem(MenuItem item) { selectedItem.set(item); }

    public BooleanProperty collapsedProperty() { return collapsed; }
    public boolean isCollapsed() { return collapsed.get(); }
    public void toggleCollapsed() { collapsed.set(!collapsed.get()); }

    public StringBinding labelFor(MenuItem item) {
        return Bindings.createStringBinding(
                item::getLabel,
                I18n.bundleProperty());
    }

    public void onAccountClicked() {
        // TODO: navigate to profile page
    }
}
