package com.triplify.ui.shared.component.license.view;

import com.triplify.application.license.LicenseManager;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.user.UserService;
import com.triplify.application.usecase.user.dto.InstallLicenseRequest;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.result.Result;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.upload_panel.view.ImageUploadPanelView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class LicenseModalView {

    private static final URL FXML_URL = LicenseModalView.class.getResource("/com/triplify/ui/shared/component/license/view/LicenseModal.fxml");
    private static final URL THEME_URL = LicenseModalView.class.getResource("/com/triplify/ui/shared/css/theme.css");
    private static final URL CSS_URL = LicenseModalView.class.getResource("/com/triplify/ui/shared/component/license/css/license_modal.css");
    private static final String UPLOAD_ICON_LITERAL = "fth-upload";
    private static final String SUCCESS_ICON_LITERAL = "fth-check-circle";

    private final FxmlLoaderHelper fxmlLoader;
    private final LicenseManager licenseManager;
    private final UserService userService;
    private final UserSessionContext userSessionContext;
    private final ToastService toast;
    private final ErrorHandler errorHandler;
    private final Runnable onLicenseApplied;
    private final Stage stage;
    private final StackPane root;

    @FXML private ImageUploadPanelView uploadPanel;
    @FXML private Button closeButton;
    @FXML private StackPane primaryButtonContainer;
    @FXML private StackPane secondaryButtonContainer;

    private AppButtonView applyButtonView;
    private AppButtonView validateButtonView;
    private boolean fileSelected;
    private File selectedFile;

    private final List<String> allowedPatterns = List.of("*.json");

    private boolean ownerInitialized;

    public LicenseModalView(
            FxmlLoaderHelper fxmlLoader,
            LicenseManager licenseManager,
            UserService userService,
            UserSessionContext userSessionContext,
            ToastService toast,
            ErrorHandler errorHandler,
            Runnable onLicenseApplied) {
        this.fxmlLoader = fxmlLoader;
        this.licenseManager = licenseManager;
        this.userService = userService;
        this.userSessionContext = userSessionContext;
        this.toast = toast;
        this.errorHandler = errorHandler;
        this.onLicenseApplied = onLicenseApplied;
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

        double width = owner.getWidth();
        double height = owner.getHeight();
        root.setPrefSize(owner.getWidth(),  owner.getHeight());
        resetUploadState();
        loadStoredLicenseSummary();
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
        uploadPanel.showSelectedMeta(false);

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

        AppButtonView applyView = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.PRIMARY)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("account.license.modal.apply"), I18n.bundleProperty()))
                .onAction(this::onApply)
                .buildView();
        Button applyButton = applyView.getButton();
        applyButton.setFocusTraversable(false);
        primaryButtonContainer.getChildren().setAll(applyButton);
        this.applyButtonView = applyView;

        AppButtonView validateView = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.SECONDARY)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("account.license.modal.validate"), I18n.bundleProperty()))
                .onAction(this::onValidate)
                .buildView();
        Button validateButton = validateView.getButton();
        validateButton.setFocusTraversable(false);
        secondaryButtonContainer.getChildren().setAll(validateButton);
        this.validateButtonView = validateView;

        updateActionButtons();
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
        fileSelected = true;
        selectedFile = file;
        hideMetaDetails();
        updateActionButtons();
    }

    private void resetUploadState() {
        uploadPanel.setSelectedImageText("");
        uploadPanel.showSelectedImageText(false);
        uploadPanel.setUploadIconLiteral(UPLOAD_ICON_LITERAL);
        fileSelected = false;
        selectedFile = null;
        hideMetaDetails();
        updateActionButtons();
    }

    private void onValidate() {
        if (!fileSelected || selectedFile == null) {
            return;
        }
        LicenseDetails details = readLicenseDetails(selectedFile);
        applyMetaDetails(details);
    }

    private void onApply() {
        if (!fileSelected || selectedFile == null) {
            return;
        }

        LicenseDetails details = readLicenseDetails(selectedFile);
        applyMetaDetails(details);
        if (details.status != Status.VALID) {
            String messageKey = details.status == Status.EXPIRED
                    ? "account.license.modal.toast.expired"
                    : "account.license.modal.toast.invalid";
            toast.warning(I18n.t(messageKey));
            return;
        }

        Result<Void> result = userService.installLicense(new InstallLicenseRequest(selectedFile.toPath()));
        result.onSuccess(ignored -> {
            toast.success(I18n.t("account.license.modal.toast.success"));
            if (onLicenseApplied != null) {
                onLicenseApplied.run();
            }
            hide();
        });
        result.onFailure(errorHandler::handle);
    }

    private LicenseDetails readLicenseDetails(File file) {
        try {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            LicenseManager.LicenseResult result = licenseManager.validateLicensePayload(json);
            if (!result.valid || result.expiresAt == null) {
                return LicenseDetails.invalid();
            }

            LocalDate expiryDate = result.expiresAt.atZone(ZoneId.systemDefault()).toLocalDate();
            if (Instant.now().isAfter(result.expiresAt)) {
                return new LicenseDetails(Status.EXPIRED, formatDate(expiryDate));
            }
            return new LicenseDetails(Status.VALID, formatDate(expiryDate));
        } catch (Exception e) {
            return LicenseDetails.invalid();
        }
    }

    private void loadStoredLicenseSummary() {
        if (!userSessionContext.isLoggedIn()) {
            return;
        }

        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        if (user.role() != RoleEnum.PRO_USER) {
            return;
        }

        licenseManager.readLicenseSummary(user.userId().toString())
                .ifPresent(summary -> {
                    uploadPanel.setSelectedImageText(summary.licenseId + ".json");
                    uploadPanel.showSelectedImageText(true);
                    uploadPanel.setUploadIconLiteral(SUCCESS_ICON_LITERAL);
                    LocalDate expiryDate = summary.expiresAt.atZone(ZoneId.systemDefault()).toLocalDate();
                    Status status = summary.isExpired() ? Status.EXPIRED : Status.VALID;
                    applyMetaDetails(new LicenseDetails(status, formatDate(expiryDate)));
                });
    }

    private String formatDate(LocalDate date) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    private void applyMetaDetails(LicenseDetails details) {
        String statusLabel = I18n.t("account.license.modal.details.status") + " " + details.statusLabel();
        uploadPanel.setSelectedStatusText(statusLabel);

        if (details.status == Status.INVALID) {
            uploadPanel.setSelectedExpiryText("");
            uploadPanel.showSelectedExpiry(false);
        } else {
            String expiryLabel = I18n.t("account.license.modal.details.expires") + " " + details.expiresAt();
            uploadPanel.setSelectedExpiryText(expiryLabel);
            uploadPanel.showSelectedExpiry(true);
        }

        uploadPanel.showSelectedMeta(true);
    }

    private void hideMetaDetails() {
        uploadPanel.setSelectedStatusText("");
        uploadPanel.setSelectedExpiryText("");
        uploadPanel.showSelectedExpiry(false);
        uploadPanel.showSelectedMeta(false);
    }

    private record LicenseDetails(Status status, String expiresAt) {
        static LicenseDetails invalid() {
            return new LicenseDetails(Status.INVALID, "?");
        }

        String statusLabel() {
            return switch (status) {
                case VALID -> I18n.t("account.license.modal.status.valid");
                case EXPIRED -> I18n.t("account.license.modal.status.expired");
                case INVALID -> I18n.t("account.license.modal.status.invalid");
            };
        }
    }

    private enum Status {
        VALID,
        EXPIRED,
        INVALID
    }

    private void updateActionButtons() {
        boolean disableButtons = !fileSelected;
        if (applyButtonView != null && applyButtonView.getViewModel() != null) {
            applyButtonView.getViewModel().setDisabled(disableButtons);
        }
        if (validateButtonView != null && validateButtonView.getViewModel() != null) {
            validateButtonView.getViewModel().setDisabled(disableButtons);
        }
    }

    private boolean isAccepted(File file) {
        String lowerName = file.getName().toLowerCase();
        return lowerName.endsWith(".json");
    }
}
