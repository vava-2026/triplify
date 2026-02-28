package com.triplify.ui.shared.header.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HeaderViewModel {

    private final StringProperty pageTitle = new SimpleStringProperty("My Trips");
    private final StringProperty searchText = new SimpleStringProperty("");
    private final StringProperty language   = new SimpleStringProperty("EN");

    //  Page title
    public StringProperty pageTitleProperty() { return pageTitle; }
    public String getPageTitle() { return pageTitle.get(); }

    //  Search
    public StringProperty searchTextProperty() { return searchText; }

    //  Language
    public StringProperty languageProperty() { return language; }

    //  Actions
    public void onLanguageClicked() {
        language.set(language.get().equals("EN") ? "UA" : "EN");
    }
}

