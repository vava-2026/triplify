package com.triplify.ui.shared.component.add_card.view;

import com.triplify.ui.shared.util.Localization;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;

public class AddCardView extends StackPane {

    private static final URL CSS_URL = AddCardView.class.getResource(
            "/com/triplify/ui/shared/component/add_card/css/add_card.css"
    );

    private boolean enabled;

    public AddCardView(String titleKey, String subtitleKey, Runnable onAction) {
        this(titleKey, subtitleKey, onAction, true, null);
    }

    public AddCardView(String titleKey, String subtitleKey, Runnable onAction, boolean enabled, Runnable onBlockedAction) {
        this.enabled = enabled;
        getStyleClass().add("add-card");
        setMaxWidth(Double.MAX_VALUE);
        if (!enabled) {
            getStyleClass().add("add-card-disabled");
        }
        setOnMouseClicked(e -> {
            if (this.enabled) {
                onAction.run();
                return;
            }
            if (onBlockedAction != null) {
                onBlockedAction.run();
            }
        });

        FontIcon icon = new FontIcon("fth-plus");
        icon.getStyleClass().add("add-card-icon");

        Label title = new Label();
        title.getStyleClass().add("add-card-title");
        Localization.bindText(title.textProperty(), titleKey);

        Label subtitle = new Label();
        subtitle.getStyleClass().add("add-card-subtitle");
        Localization.bindText(subtitle.textProperty(), subtitleKey);

        VBox content = new VBox(6, icon, title, subtitle);
        content.setAlignment(Pos.CENTER);
        getChildren().add(content);

        if (CSS_URL != null) {
            getStylesheets().add(CSS_URL.toExternalForm());
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            getStyleClass().remove("add-card-disabled");
        } else if (!getStyleClass().contains("add-card-disabled")) {
            getStyleClass().add("add-card-disabled");
        }
    }
}
