package com.triplify.ui.shared.menu.viewmodel;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.AppPage;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class NavButtonViewModel {

    private final AppPage page;
    private final BooleanProperty active = new SimpleBooleanProperty(false);
    private final StringBinding label;

    public NavButtonViewModel(AppPage page) {
        this.page = page;
        this.label = Bindings.createStringBinding(
                () -> I18n.t(this.page.getLabelKey()),
                I18n.bundleProperty());
    }

    public AppPage getPage() { return page; }
    public void setActive(boolean v) { active.set(v); }
    public StringBinding labelBinding() { return label; }
}
