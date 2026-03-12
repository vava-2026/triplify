package com.triplify.ui.shared.component.select.view;

import com.triplify.ui.shared.component.entry.model.Entry;
import com.triplify.ui.shared.component.entry.view.EntryView;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;

public class SelectButtonCell<T> extends ListCell<Entry<T>> {

    private final EntryView<T> entryView = new EntryView<>();
    private final String placeholder;

    public SelectButtonCell(String placeholder) {
        this.placeholder = placeholder;
        setPadding(javafx.geometry.Insets.EMPTY);
        entryView.setMaxWidth(Double.MAX_VALUE);
        entryView.setMaxHeight(Double.MAX_VALUE);
        setPadding(new Insets(5, 10, 5, 10));
    }

    @Override
    protected void updateItem(Entry<T> entry, boolean empty) {
        super.updateItem(entry, empty);
        if (empty || entry == null) {
            setGraphic(null);
            setText(placeholder);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        } else {
            setText(null);
            entryView.update(entry);
            setGraphic(entryView);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}