package com.triplify.ui.shared.component.button.view;

import com.google.inject.Inject;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.viewmodel.AppButtonViewModel;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.FxmlLoadResult;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class AppButtonView implements Initializable {

    private static final URL FXML_URL = AppButtonView.class.getResource("/com/triplify/ui/shared/component/button/view/AppButton.fxml");
    private static final URL CSS_URL = AppButtonView.class.getResource("/com/triplify/ui/shared/component/button/css/button.css");
    private static final URL DIALOG_FXML_URL = AppButtonView.class.getResource("/com/triplify/ui/shared/component/button/view/ConfirmDialog.fxml");

    @FXML private Button button;

    private AppButtonViewModel viewModel;

    @Inject private FxmlLoaderHelper fxmlLoader;

    @Override
    public void initialize(URL location, ResourceBundle resources) { }

    public Button getButton() { return button; }

    public AppButtonViewModel getViewModel() { return viewModel; }

    @FXML
    private void onClicked() {
        if (viewModel.isDisabled() || viewModel.isLoading()) return;
        if (viewModel.isRequireConfirm()) {
            showConfirmDialog();
        } else {
            viewModel.execute();
        }
    }

    public static Builder builder(FxmlLoaderHelper fxmlLoader) { return new Builder(fxmlLoader); }

    public static final class Builder {
        private final FxmlLoaderHelper fxmlLoader;
        private String label = "";
        private ButtonVariant variant = ButtonVariant.PRIMARY;
        private String icon = null;
        private boolean disabled = false;
        private boolean requireConfirm = false;
        private String confirmMessage = "Are you sure?";
        private Runnable onAction = null;

        private Builder(FxmlLoaderHelper fxmlLoader) { this.fxmlLoader = fxmlLoader; }

        public Builder label(String v) { this.label = v; return this; }
        public Builder variant(ButtonVariant v) { this.variant = v; return this; }
        public Builder icon(String iconLiteral) { this.icon = iconLiteral; return this; }
        public Builder disabled(boolean v) { this.disabled = v; return this; }
        public Builder requireConfirm(String msg) { this.requireConfirm = true; this.confirmMessage = msg; return this; }
        public Builder onAction(Runnable r) { this.onAction = r; return this; }

        public Button build() {
            if (FXML_URL == null) throw new IllegalStateException("AppButton.fxml not found");
            FxmlLoadResult<?, AppButtonView> result = fxmlLoader.load(FXML_URL);
            AppButtonView view = result.controller();
            view.configure(label, variant, icon, disabled, requireConfirm, confirmMessage, onAction);
            return view.getButton();
        }
    }

    private void configure(String label, ButtonVariant variant, String iconLiteral,
                           boolean disabled, boolean requireConfirm, String confirmMessage,
                           Runnable onAction) {
        viewModel = new AppButtonViewModel();
        viewModel.setLabel(label);
        viewModel.setVariant(variant);
        viewModel.setIcon(iconLiteral);
        viewModel.setDisabled(disabled);
        viewModel.setRequireConfirm(requireConfirm);
        viewModel.setConfirmMessage(confirmMessage);
        viewModel.setOnAction(onAction);

        if (CSS_URL != null) {
            button.getStylesheets().add(CSS_URL.toExternalForm());
        }

        button.getStyleClass().add(variant.getStyleClass());

        button.textProperty().bind(viewModel.labelProperty());

        button.disableProperty().bind(viewModel.disabledProperty());

        rebuildGraphic(iconLiteral, false);

        viewModel.loadingProperty().addListener((obs, oldV, newV) -> {
            button.setDisable(newV || viewModel.isDisabled());
            rebuildGraphic(viewModel.getIcon(), newV);
        });
    }

    private void rebuildGraphic(String iconLiteral, boolean loading) {
        if (loading) {
            ProgressIndicator spinner = new ProgressIndicator();
            spinner.getStyleClass().add("app-btn-spinner");
            spinner.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            button.setGraphic(spinner);
        } else if (iconLiteral != null && !iconLiteral.isBlank()) {
            FontIcon icon = new FontIcon(iconLiteral);
            icon.getStyleClass().add("app-btn-icon");
            icon.setIconSize(18);
            button.setGraphic(icon);
        } else {
            button.setGraphic(null);
        }
    }

    private void showConfirmDialog() {
        if (DIALOG_FXML_URL == null) return;
        FxmlLoadResult<VBox, ConfirmDialogView> result = fxmlLoader.load(DIALOG_FXML_URL);
        VBox content = result.node();
        ConfirmDialogView dialogView = result.controller();
        dialogView.configure(viewModel.getConfirmMessage(), viewModel::execute);

        Scene scene = new Scene(content);
        if (CSS_URL != null) scene.getStylesheets().add(CSS_URL.toExternalForm());
        if (button.getScene() != null) {
            scene.getStylesheets().addAll(button.getScene().getStylesheets());
        }

        Stage dialog = new Stage(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (button.getScene() != null) {
            dialog.initOwner(button.getScene().getWindow());
        }
        dialog.setResizable(false);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

