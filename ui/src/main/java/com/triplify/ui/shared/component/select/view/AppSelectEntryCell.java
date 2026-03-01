package com.triplify.ui.shared.component.select.view;

import com.triplify.ui.shared.component.select.model.SelectEntry;
import com.triplify.ui.shared.component.select.model.SelectVariant;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Reusable ListCell for {@link SelectEntry} items.
 * Can be used by both Select and Search components.
 */
public class AppSelectEntryCell<T> extends ListCell<SelectEntry<T>> {

    private static final String BASE_CLASS  = "app-select-entry-cell";
    private static final String ICON_CLASS  = "app-select-entry-icon";
    private static final String LABEL_CLASS = "app-select-entry-label";

    /** Variant applied when the entry itself carries no variant. */
    private final SelectVariant componentVariant;

    public AppSelectEntryCell(SelectVariant componentVariant) {
        this.componentVariant = componentVariant;
        getStyleClass().add(BASE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(SelectEntry<T> entry, boolean empty) {
        super.updateItem(entry, empty);

        // Remove any previously applied variant style classes
        getStyleClass().removeIf(c -> c.endsWith("-cell") && !c.equals(BASE_CLASS));

        if (empty || entry == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        // Entry-level variant wins over component-level variant
        SelectVariant effective = entry.hasVariant() ? entry.getVariant() : componentVariant;
        if (effective != null) {
            getStyleClass().add(effective.getStyleClass() + "-cell");
        }

        Label textLabel = new Label(entry.getLabel());
        textLabel.getStyleClass().add(LABEL_CLASS);

        if (entry.hasIcon()) {
            FontIcon icon = new FontIcon(entry.getIconLiteral());
            icon.getStyleClass().add(ICON_CLASS);
            HBox box = new HBox(6, icon, textLabel);
            box.setAlignment(Pos.CENTER_LEFT);
            setGraphic(box);
        } else {
            setGraphic(textLabel);
        }

        setText(null);
    }
}
