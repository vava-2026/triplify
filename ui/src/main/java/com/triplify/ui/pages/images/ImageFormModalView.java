package com.triplify.ui.pages.images;

import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.domain.model.enums.ImageOwnerType;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.image.dto.LinkImageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.upload_panel.view.ImageUploadPanelView;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

public final class ImageFormModalView {

    private static final Logger log = LoggerFactory.getLogger(ImageFormModalView.class);
    private static final URL FXML_URL = ImageFormModalView.class.getResource(
            "/com/triplify/ui/pages/images/ImageFormModal.fxml"
    );
    private static final URL THEME_URL = ImageFormModalView.class.getResource(
            "/com/triplify/ui/shared/css/theme.css"
    );
    private static final URL CSS_URL = ImageFormModalView.class.getResource(
            "/com/triplify/ui/pages/images/css/images.css"
    );

    private final FxmlLoaderHelper fxmlLoader;
    private final ImageService imageService;
    private final ErrorHandler errorHandler;
    private final Stage stage;
    private final StackPane root;

    @FXML private StackPane cancelButtonContainer;
    @FXML private StackPane saveButtonContainer;
    @FXML private Button closeButton;
    @FXML private ImageUploadPanelView uploadPanel;
    @FXML private VBox descriptionContainer;

    @Inject
    private ToastService toast;

    private TextAreaItem descriptionInput;
    private UUID ownerId;
    private ImageOwnerType ownerType;
    private UUID tripId;
    private Consumer<ImageResponse> onSaved;
    private boolean ownerInitialized;

    private StackPane uploadArea;
    private ImageView coverPreview;
    private VBox uploadPlaceholder;
    private Label selectedImageLabel;

    private String imagePath;

    public ImageFormModalView(FxmlLoaderHelper fxmlLoader, ImageService imageService, ErrorHandler errorHandler) {
        this.fxmlLoader = fxmlLoader;
        this.imageService = imageService;
        this.errorHandler = errorHandler;
        this.stage = new Stage(StageStyle.TRANSPARENT);
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.root = loadView();
        configureScene();
    }

    public void show(Window owner, UUID ownerId, ImageOwnerType ownerType, UUID tripId, Consumer<ImageResponse> onSaved) {
        if (owner == null) return;
        this.ownerId = ownerId;
        this.ownerType = ownerType;
        this.tripId = tripId;
        this.onSaved = onSaved;

        if (!ownerInitialized) {
            stage.initOwner(owner);
            ownerInitialized = true;
        }

        double w = owner.getWidth() > 0 ? owner.getWidth() : 1280;
        double h = owner.getHeight() > 0 ? owner.getHeight() : 800;
        root.setPrefSize(w, h);
        stage.setX(owner.getX());
        stage.setY(owner.getY());
        stage.setWidth(w);
        stage.setHeight(h);
        resetState();
        stage.showAndWait();
    }

    public void hide() {
        stage.hide();
    }

    @FXML
    private void initialize() {
        uploadArea = uploadPanel.getUploadArea();
        coverPreview = uploadPanel.getCoverPreview();
        uploadPlaceholder = uploadPanel.getUploadPlaceholder();
        selectedImageLabel = uploadPanel.getSelectedImageLabel();

        EditorUtils.initializeCoverPreview(coverPreview, uploadArea);
        bindUploadPanelHandlers();

        Localization.bindText(uploadPanel.sectionTitleProperty(), "images.form.upload.section");
        Localization.bindText(uploadPanel.uploadTitleProperty(), "images.form.upload.title");
        Localization.bindText(uploadPanel.uploadSubtitleProperty(), "place.add.upload.subtitle");


        descriptionInput = new TextAreaItem("images.form.description.placeholder", FieldVariant.OUTLINED);
        descriptionContainer.getChildren().setAll(descriptionInput);

        closeButton.setOnAction(e -> hide());

        Button cancelBtn = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.SECONDARY)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("common.cancel"), I18n.bundleProperty()))
                .onAction(this::hide)
                .build();
        cancelButtonContainer.getChildren().setAll(cancelBtn);

        Button saveBtn = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.PRIMARY)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("common.save"), I18n.bundleProperty()))
                .icon("fth-save")
                .onAction(this::onSave)
                .build();
        saveButtonContainer.getChildren().setAll(saveBtn);

        EditorUtils.installRoundedClip(uploadArea, 16);
    }

    private void onSave() {
        if (imagePath == null) return;

        var imagePath = Path.of(this.imagePath);
        String description = descriptionInput.getText();

        var res = imageService.addImage(new AddImageRequest(imagePath, description));

        if (res.isFailure()){
            errorHandler.handle(res.getError());
            return;
        }
        var image = res.getValue();
        imageService.linkImage(new LinkImageRequest(image.id(), ownerId, ownerType, tripId))
                .onFailure(err -> log.warn("Failed to link image: {}", err.message()));

        if (onSaved != null) onSaved.accept(image);
        hide();
    }
    private void bindUploadPanelHandlers() {
        uploadPanel.setOnUploadClicked(event -> onChooseCoverImage());
        uploadPanel.setOnImageFileSelected(this::handleCoverImage);
        uploadPanel.setOnUnsupportedImageFile(file -> toast.warning(I18n.t("place.add.toast.image.unsupported")));
        uploadPanel.installDefaultImageDragAndDrop();
    }

    private void onChooseCoverImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.t("place.add.dialog.cover.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.t("place.add.dialog.cover.filter"), "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(uploadArea.getScene() == null ? null : uploadArea.getScene().getWindow());
        if (file != null) {
            handleCoverImage(file);
        }
    }

    private void handleCoverImage(File file) {
        if (!EditorUtils.isSupportedImageFile(file)) {
            toast.warning(I18n.t("place.add.toast.image.unsupported"));
            return;
        }

        imagePath = file.getAbsolutePath();
        selectedImageLabel.setText(file.getName());
        selectedImageLabel.setVisible(true);
        selectedImageLabel.setManaged(true);

        Image image = new Image(file.toURI().toString(), true);
        image.errorProperty().addListener((obs, oldVal, isError) -> {
            if (Boolean.TRUE.equals(isError)) {
                coverPreview.setImage(null);
                coverPreview.setViewport(null);
                coverPreview.setVisible(false);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(true);
                uploadPlaceholder.setManaged(true);
                log.warn("Failed to load cover image preview", image.getException());
                toast.warning(I18n.t("place.add.toast.image.previewUnavailable"));
            }
        });
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0 && !image.isError()) {
                EditorUtils.setCoverPreviewImage(coverPreview, uploadArea, image);
                coverPreview.setVisible(true);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(false);
                uploadPlaceholder.setManaged(false);
            }
        });
    }

    private void resetState() {
        if (descriptionInput != null) descriptionInput.setText("");
        uploadPanel.setPreviewVisible(false);
        uploadPanel.setPlaceholderVisible(true);
        uploadPanel.showSelectedImageText(false);
        uploadPanel.getCoverPreview().setImage(null);
    }

    private StackPane loadView() {
        if (FXML_URL == null) throw new IllegalStateException("ImageFormModal.fxml not found");
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setController(this);
        try {
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ImageFormModal.fxml", e);
        }
    }

    private void configureScene() {
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        if (THEME_URL != null) scene.getStylesheets().add(THEME_URL.toExternalForm());
        if (CSS_URL != null) scene.getStylesheets().add(CSS_URL.toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
    }
}
