package com.triplify.ui.shared.header.viewmodel;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.menu.model.MenuItem;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HeaderViewModel {

    private final ObjectProperty<MenuItem> activeItem =
            new SimpleObjectProperty<>(MenuItem.MY_TRIPS);

    private final StringBinding pageTitle = Bindings.createStringBinding(
            () -> activeItem.get() != null ? activeItem.get().getLabel() : "",
            activeItem, I18n.bundleProperty());

    private final StringProperty searchText = new SimpleStringProperty("");

    public ObjectProperty<MenuItem> activeItemProperty() { return activeItem; }

    public StringBinding pageTitleBinding() { return pageTitle; }
    public StringProperty searchTextProperty() { return searchText; }
}
