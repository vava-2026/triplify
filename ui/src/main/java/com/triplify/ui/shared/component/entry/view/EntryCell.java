package com.triplify.ui.shared.component.entry.view;

import com.triplify.ui.shared.component.entry.model.Entry;
import javafx.scene.control.ListCell;
import javafx.scene.control.ContentDisplay;

///  Convenient wrapper around AppEntryView, allowing it to be used with AppSelect component
///
public class EntryCell<T> extends ListCell<Entry<T>> {

    private final EntryView<T> entryView = new EntryView<>();

    public EntryCell() {
        setText(null);
        setPadding(javafx.geometry.Insets.EMPTY);
        entryView.setMaxWidth(Double.MAX_VALUE);
        entryView.setMaxHeight(Double.MAX_VALUE);
    }

    @Override
    protected void updateItem(Entry<T> entry, boolean empty) {
        super.updateItem(entry, empty);

        setText(null);

        if (empty || entry == null) {
            setGraphic(null);
        }
        else {
            entryView.update(entry);
            setGraphic(entryView);
        }
    }
}
