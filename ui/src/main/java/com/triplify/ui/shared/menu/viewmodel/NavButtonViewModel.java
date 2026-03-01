package com.triplify.ui.shared.menu.viewmodel;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.menu.model.NavItem;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class NavButtonViewModel {

    private final NavItem navItem;
    private final BooleanProperty active = new SimpleBooleanProperty(false);
    private final StringBinding label;

    public NavButtonViewModel(NavItem navItem) {
        this.navItem = navItem;
        this.label = Bindings.createStringBinding(
                () -> I18n.t(this.navItem.getI18nKey()),
                I18n.bundleProperty());
    }

    public NavItem getNavItem() { return navItem; }
    public void setActive(boolean v) { active.set(v); }
    public StringBinding labelBinding() { return label; }
}