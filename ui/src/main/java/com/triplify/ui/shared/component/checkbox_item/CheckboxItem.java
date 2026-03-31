package com.triplify.ui.shared.component.checkbox_item;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class CheckboxItem extends HBox {

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private static final double BOX_SIZE = 38;

    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StringProperty text = new SimpleStringProperty("");

    private final StackPane box;
    private final FontIcon checkIcon;
    private final VBox labelContainer;

    public CheckboxItem(String text) {
        this(text, false);
    }

    public CheckboxItem(String text, boolean selected) {
        this.text.set(text);

        getStyleClass().add("checkbox-item");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);

        box = new StackPane();
        box.getStyleClass().add("checkbox-item-box");
        box.setMinSize(BOX_SIZE, BOX_SIZE);
        box.setPrefSize(BOX_SIZE, BOX_SIZE);
        box.setMaxSize(BOX_SIZE, BOX_SIZE);

        checkIcon = new FontIcon("fth-check");
        checkIcon.getStyleClass().add("checkbox-item-icon");
        box.getChildren().add(checkIcon);

        labelContainer = new VBox();
        labelContainer.getStyleClass().add("checkbox-item-label-container");
        rebuildLabels(this.text.get());
        this.text.addListener((obs, oldValue, newValue) -> rebuildLabels(newValue));

        getChildren().addAll(box, labelContainer);

        this.selected.addListener((obs, oldValue, newValue) ->
                pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, newValue));

        setOnMouseClicked(event -> {
            if (!isDisabled()) {
                setSelected(!isSelected());
            }
        });

        setSelected(selected);
    }

    private void rebuildLabels(String value) {
        labelContainer.getChildren().clear();
        for (String line : value.split("\\n", -1)) {
            Label lineLabel = new Label(line);
            lineLabel.getStyleClass().add("checkbox-item-label");
            labelContainer.getChildren().add(lineLabel);
        }
    }

    public final BooleanProperty selectedProperty() {
        return selected;
    }

    public final boolean isSelected() {
        return selected.get();
    }

    public final void setSelected(boolean value) {
        selected.set(value);
    }

    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String value) {
        text.set(value);
    }
}

