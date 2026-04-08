package com.triplify.ui.shared.header.viewmodel;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.AppPage;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HeaderViewModel {

    private final ObjectProperty<AppPage> activePage =
            new SimpleObjectProperty<>(null);

    private final StringBinding pageTitle = Bindings.createStringBinding(
            () -> activePage.get() != null ? I18n.t(activePage.get().getLabelKey()) : "",
            activePage, I18n.bundleProperty());

    private final StringProperty searchText = new SimpleStringProperty("");

    public ObjectProperty<AppPage> activePageProperty() { return activePage; }

    public StringBinding pageTitleBinding() { return pageTitle; }
    public StringProperty searchTextProperty() { return searchText; }
}
