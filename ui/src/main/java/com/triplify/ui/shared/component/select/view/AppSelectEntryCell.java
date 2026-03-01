package com.triplify.ui.shared.component.select.view;

import com.triplify.ui.shared.component.select.model.SelectEntry;
import com.triplify.ui.shared.component.select.model.SelectVariant;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class AppSelectEntryCell<T> extends ListCell<SelectEntry<T>> {

    private static final String BASE_CLASS  = "app-select-entry-cell";
    private static final String ICON_CLASS  = "app-select-entry-icon";
    private static final String LABEL_CLASS = "app-select-entry-label";

    private final SelectVariant componentVariant;

    private final Label    textLabel = new Label();
    private final FontIcon icon      = new FontIcon();
    private final HBox     contentBox;

    private SelectVariant lastVariant = null;

    public AppSelectEntryCell(SelectVariant componentVariant) {
        this.componentVariant = componentVariant;
        getStyleClass().add(BASE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
        setText(null);

        textLabel.getStyleClass().add(LABEL_CLASS);
        icon.getStyleClass().add(ICON_CLASS);

        contentBox = new HBox(6, icon, textLabel);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        setGraphic(contentBox);
    }

    @Override
    protected void updateItem(SelectEntry<T> entry, boolean empty) {
        super.updateItem(entry, empty);
        setText(null);

        if (empty || entry == null) {
            setGraphic(null);
            removeVariantStyle();
            lastVariant = null;
            return;
        }

        SelectVariant effective = entry.hasVariant() ? entry.getVariant() : componentVariant;
        if (effective != lastVariant) {
            removeVariantStyle();
            if (effective != null) {
                getStyleClass().add(effective.getStyleClass() + "-cell");
            }
            lastVariant = effective;
        }

        textLabel.setText(entry.getLabel());

        if (entry.hasIcon()) {
            icon.setIconLiteral(entry.getIconLiteral());
            icon.setVisible(true);
            icon.setManaged(true);
        } else {
            icon.setVisible(false);
            icon.setManaged(false);
        }

        setGraphic(contentBox);
    }

    private void removeVariantStyle() {
        if (lastVariant != null) {
            getStyleClass().remove(lastVariant.getStyleClass() + "-cell");
        }
    }
}
