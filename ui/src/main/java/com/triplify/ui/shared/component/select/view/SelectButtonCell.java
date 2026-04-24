package com.triplify.ui.shared.component.select.view;

import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.entry.view.EntryView;
import com.triplify.ui.shared.util.EmojiUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class SelectButtonCell<T> extends ListCell<Entry<T>> {

    private final EntryView<T> entryView = new EntryView<>();
    private final String placeholder;
    private final ImageView emojiView;
    private final Label placeholderLabel;
    private final HBox wrapper;

    public SelectButtonCell(String placeholder, String emoji) {
        this.placeholder = placeholder;
        setPadding(new Insets(5, 10, 5, 10));
        entryView.setMaxWidth(Double.MAX_VALUE);
        entryView.setMaxHeight(Double.MAX_VALUE);

        if (emoji != null) {
            emojiView = new ImageView(EmojiUtil.toImage(emoji, 18));
            emojiView.setFitWidth(18);
            emojiView.setFitHeight(18);
            emojiView.setPreserveRatio(true);
            placeholderLabel = new Label(placeholder);
            placeholderLabel.getStyleClass().add("select-placeholder");
            wrapper = new HBox(6, emojiView, placeholderLabel);
            wrapper.setAlignment(Pos.CENTER_LEFT);
        } else {
            emojiView = null;
            placeholderLabel = null;
            wrapper = null;
        }
    }

    @Override
    protected void updateItem(Entry<T> entry, boolean empty) {
        super.updateItem(entry, empty);
        getStyleClass().remove("select-placeholder");
        if (empty || entry == null) {
            entryView.update(null);
            if (emojiView != null) {
                wrapper.getChildren().setAll(emojiView, placeholderLabel);
                setGraphic(wrapper);
                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setGraphic(null);
                setText(placeholder);
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        } else {
            entryView.update(entry);
            if (emojiView != null) {
                wrapper.getChildren().setAll(emojiView, entryView);
                setGraphic(wrapper);
            } else {
                setGraphic(entryView);
            }
            setText(null);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
