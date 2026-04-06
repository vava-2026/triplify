package com.triplify.ui.shared.menu.viewmodel;

import com.triplify.ui.routing.AppPage;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MenuViewModel {

    private final ObjectProperty<AppPage> activePrimaryPage =
            new SimpleObjectProperty<>(null);
    private final ObjectProperty<AppPage> currentPage =
            new SimpleObjectProperty<>(null);
    private final BooleanProperty collapsed =
            new SimpleBooleanProperty(false);

    private final BooleanBinding hideHeader =
            Bindings.createBooleanBinding(
                    () -> currentPage.get() != null && currentPage.get().isHideHeader(),
                    currentPage);

    public ObjectProperty<AppPage> activePrimaryPageProperty() { return activePrimaryPage; }
    public AppPage getActivePrimaryPage() { return activePrimaryPage.get(); }
    public void setActivePrimaryPage(AppPage item) { activePrimaryPage.set(item); }

    public ObjectProperty<AppPage> currentPageProperty() { return currentPage; }
    public AppPage getCurrentPage() { return currentPage.get(); }
    public void setCurrentPage(AppPage page) { currentPage.set(page); }

    public BooleanProperty collapsedProperty() { return collapsed; }
    public boolean isCollapsed() { return collapsed.get(); }
    public void toggleCollapsed() { collapsed.set(!collapsed.get()); }

    public BooleanBinding hideHeaderProperty() { return hideHeader; }
    public boolean isHideHeader() { return hideHeader.get(); }
}
