package com.triplify.ui.shared.component.license.view;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.upload_panel.view.ImageUploadPanelView;
import com.triplify.ui.shared.util.Localization;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.List;

public final class LicenseModalView {

    private static final URL FXML_URL = LicenseModalView.class.getResource("/com/triplify/ui/shared/component/license/view/LicenseModal.fxml");
    private static final URL THEME_URL = LicenseModalView.class.getResource("/com/triplify/ui/shared/css/theme.css");
    private static final URL CSS_URL = LicenseModalView.class.getResource("/com/triplify/ui/shared/component/license/css/license_modal.css");
    private static final String UPLOAD_ICON_LITERAL = "fth-upload";
    private static final String SUCCESS_ICON_LITERAL = "fth-check-circle";

    private final FxmlLoaderHelper fxmlLoader;
    private final Stage stage;
    private final StackPane root;

    @FXML private ImageUploadPanelView uploadPanel;
    @FXML private Button closeButton;
    @FXML private StackPane primaryButtonContainer;
    @FXML private StackPane secondaryButtonContainer;

    private final List<String> allowedPatterns = List.of("*.json");

    private boolean ownerInitialized;

    public LicenseModalView(FxmlLoaderHelper fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
        this.stage = new Stage(StageStyle.TRANSPARENT);
        this.root = loadView();

        this.stage.initModality(Modality.APPLICATION_MODAL);
        configureScene();
    }

    public void show(Window owner) {
        if (owner == null) {
            return;
        }

        if (!ownerInitialized) {
            stage.initOwner(owner);
            ownerInitialized = true;
        }

        double width = owner.getWidth() > 0 ? owner.getWidth() : 1280;
        double height = owner.getHeight() > 0 ? owner.getHeight() : 800;
        root.setPrefSize(width, height);
        resetUploadState();
        stage.setX(owner.getX());
        stage.setY(owner.getY());
        stage.setWidth(width);
        stage.setHeight(height);
        stage.showAndWait();
    }

    public void hide() {
        stage.hide();
    }

    @FXML
    private void initialize() {
        uploadPanel.setPanelSize(270, 270);
        Localization.bindText(uploadPanel.sectionTitleProperty(), "account.license.modal.section");
        uploadPanel.setSectionIconLiteral("fth-file-text");
        uploadPanel.setSectionIconSize(24);
        Localization.bindText(uploadPanel.uploadTitleProperty(), "account.license.modal.upload.title");
        uploadPanel.setUploadIconLiteral(UPLOAD_ICON_LITERAL);
        uploadPanel.setUploadIconSize(48);
        uploadPanel.setUploadSubtitle("JSON");
        uploadPanel.showSelectedImageText(false);
        uploadPanel.setPlaceholderVisible(true);
        uploadPanel.setPreviewVisible(false);

        closeButton.setFocusTraversable(false);
        closeButton.setOnAction(event -> hide());

        uploadPanel.setOnUploadClicked(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            chooseLicenseFile();
        });

        uploadPanel.setOnUploadDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                File file = event.getDragboard().getFiles().getFirst();
                if (isAccepted(file)) {
                    event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
                }
            }
            event.consume();
        });

        uploadPanel.setOnUploadDragExited(DragEvent::consume);
        uploadPanel.setOnUploadDragDropped(event -> {
            if (event.getDragboard().hasFiles()) {
                File file = event.getDragboard().getFiles().getFirst();
                if (isAccepted(file)) {
                    handleSelectedFile(file);
                    event.setDropCompleted(true);
                    event.consume();
                    return;
                }
            }
            event.setDropCompleted(false);
            event.consume();
        });

        Button applyButton = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.PRIMARY)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("account.license.modal.apply"), I18n.bundleProperty()))
                .onAction(this::hide)
                .build();
        applyButton.setFocusTraversable(false);
        primaryButtonContainer.getChildren().setAll(applyButton);

        Button validateButton = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.SECONDARY)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("account.license.modal.validate"), I18n.bundleProperty()))
                .onAction(this::hide)
                .build();
        validateButton.setFocusTraversable(false);
        secondaryButtonContainer.getChildren().setAll(validateButton);
    }

    private StackPane loadView() {
        if (FXML_URL == null) {
            throw new IllegalStateException("FXML not found: /com/triplify/ui/shared/component/license/view/LicenseModal.fxml");
        }

        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setController(this);
        try {
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load LicenseModal.fxml", e);
        }
    }

    private void configureScene() {
        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        if (THEME_URL != null) {
            scene.getStylesheets().add(THEME_URL.toExternalForm());
        }
        if (CSS_URL != null) {
            scene.getStylesheets().add(CSS_URL.toExternalForm());
        }
        stage.setScene(scene);
        stage.setResizable(false);
    }

    private void chooseLicenseFile() {
        Window owner = stage.getOwner();
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.t("account.license.modal.file.dialog.title"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                I18n.t("account.license.modal.file.dialog.filter"),
                allowedPatterns.toArray(String[]::new)
        ));

        File file = chooser.showOpenDialog(owner);
        if (file != null) {
            handleSelectedFile(file);
        }
    }

    private void handleSelectedFile(File file) {
        uploadPanel.setSelectedImageText(file.getName());
        uploadPanel.showSelectedImageText(true);
        uploadPanel.setUploadIconLiteral(SUCCESS_ICON_LITERAL);
    }

    private void resetUploadState() {
        uploadPanel.setSelectedImageText("");
        uploadPanel.showSelectedImageText(false);
        uploadPanel.setUploadIconLiteral(UPLOAD_ICON_LITERAL);
    }


    private boolean isAccepted(File file) {
        String lowerName = file.getName().toLowerCase();
        return lowerName.endsWith(".json");
    }
}



