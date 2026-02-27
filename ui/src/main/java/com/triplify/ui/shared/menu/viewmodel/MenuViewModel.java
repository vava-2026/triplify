package com.triplify.ui.shared.menu.viewmodel;

import com.triplify.ui.shared.menu.model.MenuItem;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * ViewModel for the sidebar menu component.
 *
 * <p>Holds observable state that the View binds to:
 * <ul>
 *   <li>{@code selectedItem}  – currently active navigation item</li>
 *   <li>{@code collapsed}     – whether the sidebar is in collapsed (icon-only) mode</li>
 * </ul>
 *
 * <p>To wire navigation: listen to {@code selectedItemProperty()} changes in
 * the parent controller and swap the main content accordingly.
 */
public class MenuViewModel {

    private final ObjectProperty<MenuItem> selectedItem =
            new SimpleObjectProperty<>(MenuItem.MAP);

    private final BooleanProperty collapsed =
            new SimpleBooleanProperty(false);

    // ------------------------------------------------------------------ //
    //  Selected item
    // ------------------------------------------------------------------ //

    public ObjectProperty<MenuItem> selectedItemProperty() {
        return selectedItem;
    }

    public MenuItem getSelectedItem() {
        return selectedItem.get();
    }

    public void setSelectedItem(MenuItem item) {
        selectedItem.set(item);
    }

    // ------------------------------------------------------------------ //
    //  Collapsed state
    // ------------------------------------------------------------------ //

    public BooleanProperty collapsedProperty() {
        return collapsed;
    }

    public boolean isCollapsed() {
        return collapsed.get();
    }

    public void toggleCollapsed() {
        collapsed.set(!collapsed.get());
    }
}

