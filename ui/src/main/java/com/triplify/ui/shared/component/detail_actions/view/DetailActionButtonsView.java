package com.triplify.ui.shared.component.detail_actions.view;

import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class DetailActionButtonsView extends VBox {

    private static final URL FXML_URL = DetailActionButtonsView.class.getResource(
            "/com/triplify/ui/shared/component/detail_actions/view/DetailActionButtons.fxml"
    );
    private static final URL CSS_URL = DetailActionButtonsView.class.getResource(
            "/com/triplify/ui/shared/component/detail_actions/css/detail_actions.css"
    );

    @FXML private StackPane primaryContainer;
    @FXML private StackPane secondaryContainer;

    public DetailActionButtonsView() {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DetailActionButtons.fxml", e);
        }

        if (CSS_URL != null) {
            getStylesheets().add(CSS_URL.toExternalForm());
        }
    }

    public void configurePrimary(FxmlLoaderHelper fxmlLoader, ObservableValue<String> label, String icon, Runnable onAction) {
        Button btn = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.PRIMARY)
                .labelBinding(label)
                .icon(icon)
                .onAction(onAction)
                .build();
        btn.setMaxWidth(Double.MAX_VALUE);
        primaryContainer.getChildren().setAll(btn);
    }

    public void configureDelete(FxmlLoaderHelper fxmlLoader, ObservableValue<String> label, String icon,
                                ObservableValue<String> confirmMessage, Runnable onAction) {
        Button btn = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.DANGER_OUTLINE)
                .labelBinding(label)
                .icon(icon)
                .requireConfirm(confirmMessage)
                .onAction(onAction)
                .build();
        btn.setMaxWidth(Double.MAX_VALUE);
        secondaryContainer.getChildren().setAll(btn);
    }
}
