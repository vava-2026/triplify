package com.triplify.ui.pages.images;

import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public final class ImageViewModalView {

    private static final URL FXML_URL = ImageViewModalView.class.getResource(
            "/com/triplify/ui/pages/images/ImageViewModal.fxml"
    );
    private static final URL THEME_URL = ImageViewModalView.class.getResource(
            "/com/triplify/ui/shared/css/theme.css"
    );
    private static final URL CSS_URL = ImageViewModalView.class.getResource(
            "/com/triplify/ui/pages/images/css/images.css"
    );

    private final ImageService imageService;
    private final ErrorHandler errorHandler;
    private final Stage stage;
    private final StackPane root;

    private FxmlLoaderHelper fxmlLoader;

    @FXML private StackPane root_fxml;
    @FXML private ImageView imageView;
    @FXML private Label descriptionLabel;
    @FXML private Button closeButton;
    @FXML private HBox deleteButtonContainer;
    //@FXML private Button deleteButton;

    private ImageResponse currentImage;
    private Consumer<ImageResponse> onDeleted;
    private boolean ownerInitialized;

    public ImageViewModalView(ImageService imageService, ErrorHandler errorHandler, FxmlLoaderHelper fxmlLoader) {
        this.imageService = imageService;
        this.errorHandler = errorHandler;
        this.fxmlLoader = fxmlLoader;
        this.stage = new Stage(StageStyle.TRANSPARENT);
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.root = loadView();
        configureScene();
    }

    public void show(Window owner, ImageResponse image, Consumer<ImageResponse> onDeleted) {
        if (owner == null || image == null) return;
        this.currentImage = image;
        this.onDeleted = onDeleted;

        if (!ownerInitialized) {
            stage.initOwner(owner);
            ownerInitialized = true;
        }

        double width = owner.getWidth();
        double height = owner.getHeight();
        root.setPrefSize(width, height);
        stage.setX(owner.getX());
        stage.setY(owner.getY());
        stage.setWidth(width);
        stage.setHeight(height);

        applyImage(image);
        stage.showAndWait();
    }

    public void hide() {
        stage.hide();
    }

    @FXML
    private void initialize() {
        closeButton.setOnAction(e -> hide());

        Button deleteButton = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.DANGER_OUTLINE)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("images.delete"), I18n.bundleProperty()))
                .icon("fth-trash-2")
                .requireConfirm(I18n.t("images.delete.confirm.message"))
                .onAction(this::performDelete)
                .build();

        deleteButtonContainer.getChildren().setAll(deleteButton);
    }

    private void applyImage(ImageResponse image) {
        if (image.url() != null) {
            Image img = EditorUtils.resolveCoverImage(image.url().toUri().toString());
            imageView.setImage(img);
        } else {
            imageView.setImage(null);
        }

        String desc = image.description();
        descriptionLabel.setText(desc != null ? desc : "");
        descriptionLabel.setVisible(desc != null && !desc.isBlank());
        descriptionLabel.setManaged(desc != null && !desc.isBlank());
    }

    private void performDelete() {
        var res = imageService.deleteImage(new DeleteImageRequest(currentImage.id()));
        if (res.isFailure()) {
            errorHandler.handle(res.getError());
            return;
        }
        if (onDeleted != null) onDeleted.accept(currentImage);
        hide();
    }

    private StackPane loadView() {
        if (FXML_URL == null) throw new IllegalStateException("ImageViewModal.fxml not found");
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setController(this);
        try {
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ImageViewModal.fxml", e);
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
