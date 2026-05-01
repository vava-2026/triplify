package com.triplify.ui.pages.stories;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.EditorUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.UUID;

class StoryImageCard extends HBox {

    private static final double PREVIEW_WIDTH = 104;
    private static final double PREVIEW_HEIGHT = 72;

    private final UUID imageId;
    private final String filePath;
    private final TextAreaItem descriptionInput;

    StoryImageCard(File file, Runnable onRemove) {
        this(null, file.getAbsolutePath(),
                new Image(file.toURI().toString(), PREVIEW_WIDTH, PREVIEW_HEIGHT, true, true, true),
                null, onRemove);
    }

    StoryImageCard(ImageResponse image, Runnable onRemove) {
        this(image.id(), null, resolvePreview(image), image.description(), onRemove);
    }

    private StoryImageCard(UUID imageId, String filePath, Image preview, String description, Runnable onRemove) {
        this.imageId = imageId;
        this.filePath = filePath;

        getStyleClass().add("story-image-card");
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Double.MAX_VALUE);

        ImageView imageView = new ImageView(preview);
        imageView.setFitWidth(PREVIEW_WIDTH);
        imageView.setFitHeight(PREVIEW_HEIGHT);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        StackPane previewPane = new StackPane(imageView);
        previewPane.setPrefWidth(PREVIEW_WIDTH);
        previewPane.setMinWidth(PREVIEW_WIDTH);
        previewPane.setMaxWidth(PREVIEW_WIDTH);
        previewPane.setPrefHeight(PREVIEW_HEIGHT);
        previewPane.setMinHeight(PREVIEW_HEIGHT);
        previewPane.setMaxHeight(PREVIEW_HEIGHT);
        previewPane.getStyleClass().add("story-image-preview");
        EditorUtils.installRoundedClip(previewPane, 10);

        descriptionInput = new TextAreaItem("story.add.image.description", FieldVariant.GHOST);
        if (description != null && !description.isBlank()) {
            descriptionInput.setText(description);
        }
        //HBox.setHgrow(descriptionInput, Priority.ALWAYS);

        FontIcon removeIcon = new FontIcon("fth-x");
        removeIcon.getStyleClass().add("input-action-icon");
        Button removeBtn = new Button();
        removeBtn.setGraphic(removeIcon);
        removeBtn.setFocusTraversable(false);
        removeBtn.getStyleClass().add("input-action-btn");
        removeBtn.setOnAction(e -> onRemove.run());

        //rightBox.setAlignment(Pos.CENTER_LEFT);
        //HBox.setHgrow(rightBox, Priority.ALWAYS);

        getChildren().addAll(previewPane, descriptionInput, removeBtn);
    }

    UUID getImageId() { return imageId; }
    String getFilePath() { return filePath; }
    String getDescription() { return descriptionInput.getText().trim(); }
    boolean isDraft() { return imageId == null; }

    private static Image resolvePreview(ImageResponse image) {
        if (image.url() == null) return null;
        try {
            return EditorUtils.resolveCoverImage(image.url().toUri().toString());
        } catch (Exception e) {
            return null;
        }
    }
}
