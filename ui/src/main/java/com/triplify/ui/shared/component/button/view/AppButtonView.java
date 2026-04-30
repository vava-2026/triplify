package com.triplify.ui.shared.component.button.view;

import com.google.inject.Inject;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.viewmodel.AppButtonViewModel;
import com.triplify.ui.shared.model.AppComponentSize;
import javafx.beans.value.ObservableValue;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.FxmlLoadResult;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class AppButtonView implements Initializable {

    private static final URL FXML_URL = AppButtonView.class.getResource("/com/triplify/ui/shared/component/button/view/AppButton.fxml");
    private static final URL CSS_URL = AppButtonView.class.getResource("/com/triplify/ui/shared/component/button/css/button.css");
    private static final URL DIALOG_FXML_URL = AppButtonView.class.getResource("/com/triplify/ui/shared/component/button/view/ConfirmDialog.fxml");

    @Getter
    @FXML private Button button;

    @Getter
    private AppButtonViewModel viewModel;

    @Inject private FxmlLoaderHelper fxmlLoader;

    @Override
    public void initialize(URL location, ResourceBundle resources) { }

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
        private ObservableValue<String> labelBinding = null;
        private ButtonVariant variant = ButtonVariant.PRIMARY;
        private AppComponentSize size = AppComponentSize.MIDDLE;
        private String icon = null;
        private boolean disabled = false;
        private boolean requireConfirm = false;
        private String confirmMessage = "Are you sure?";
        private Runnable onAction = null;

        private Builder(FxmlLoaderHelper fxmlLoader) { this.fxmlLoader = fxmlLoader; }

        public Builder label(String v) { this.label = v; return this; }
        public Builder labelBinding(ObservableValue<String> binding) { this.labelBinding = binding; return this; }
        public Builder variant(ButtonVariant v) { this.variant = v; return this; }
        public Builder size(AppComponentSize v) { this.size = v == null ? AppComponentSize.MIDDLE : v; return this; }
        public Builder icon(String iconLiteral) { this.icon = iconLiteral; return this; }
        public Builder disabled(boolean v) { this.disabled = v; return this; }
        public Builder requireConfirm(String msg) { this.requireConfirm = true; this.confirmMessage = msg; return this; }
        public Builder onAction(Runnable r) { this.onAction = r; return this; }

        public Button build() {
            if (FXML_URL == null) throw new IllegalStateException("AppButton.fxml not found");
            FxmlLoadResult<?, AppButtonView> result = fxmlLoader.load(FXML_URL);
            AppButtonView view = result.controller();
            view.configure(label, labelBinding, variant, size, icon, disabled, requireConfirm, confirmMessage, onAction);
            return view.getButton();
        }
    }

    private void configure(String label, ObservableValue<String> labelBinding, ButtonVariant variant, AppComponentSize size, String iconLiteral,
                           boolean disabled, boolean requireConfirm, String confirmMessage,
                           Runnable onAction) {
        viewModel = new AppButtonViewModel();
        viewModel.setLabel(label);
        viewModel.setVariant(variant);
        viewModel.setSize(size);
        viewModel.setIcon(iconLiteral);
        viewModel.setDisabled(disabled);
        viewModel.setRequireConfirm(requireConfirm);
        viewModel.setConfirmMessage(confirmMessage);
        viewModel.setOnAction(onAction);

        if (CSS_URL != null) {
            button.getStylesheets().add(CSS_URL.toExternalForm());
        }

        button.getStyleClass().add(variant.getStyleClass());
        button.getStyleClass().add(toSizeClass(size));

        if (labelBinding != null) {
            button.textProperty().bind(labelBinding);
        } else {
            button.textProperty().bind(viewModel.labelProperty());
        }
        button.disableProperty().bind(viewModel.disabledProperty());

        rebuildGraphic(iconLiteral, false);

        viewModel.loadingProperty().addListener((obs, oldV, newV) -> {
            button.setDisable(newV || viewModel.isDisabled());
            rebuildGraphic(viewModel.getIcon(), newV);
        });
    }

    private String toSizeClass(AppComponentSize size) {
        AppComponentSize effective = size == null ? AppComponentSize.MIDDLE : size;
        return switch (effective) {
            case SMALL -> "app-btn-size-small";
            case MIDDLE -> "app-btn-size-middle";
            case BIG -> "app-btn-size-big";
        };
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
        FxmlLoadResult<StackPane, ConfirmDialogView> result = fxmlLoader.load(DIALOG_FXML_URL);
        StackPane content = result.node();
        ConfirmDialogView dialogView = result.controller();
        dialogView.configure(viewModel.getConfirmMessage(), viewModel::execute);

        Scene scene = new Scene(content);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        if (CSS_URL != null) scene.getStylesheets().add(CSS_URL.toExternalForm());
        if (button.getScene() != null) {
            scene.getStylesheets().addAll(button.getScene().getStylesheets());
        }

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (button.getScene() != null && button.getScene().getWindow() != null) {
            javafx.stage.Window owner = button.getScene().getWindow();
            dialog.initOwner(owner);
            double width = owner.getWidth() > 0 ? owner.getWidth() : 1280;
            double height = owner.getHeight() > 0 ? owner.getHeight() : 800;
            content.setPrefSize(width, height);
            dialog.setX(owner.getX());
            dialog.setY(owner.getY());
            dialog.setWidth(width);
            dialog.setHeight(height);
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

