package com.triplify.ui.shared.component.input_item;

import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.EmojiUtil;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

public class EmojiInputItem extends HBox {

    private static final int PREVIEW_SIZE = 28;
    private static final double BOX_SIZE = 45;

    private final InputItem textInput;
    private final ImageView previewImage;

    public EmojiInputItem(String placeholderKey, FieldVariant variant) {
        previewImage = new ImageView();
        previewImage.setFitWidth(PREVIEW_SIZE);
        previewImage.setFitHeight(PREVIEW_SIZE);
        previewImage.setPreserveRatio(true);
        previewImage.setSmooth(true);

        StackPane previewBox = new StackPane(previewImage);
        previewBox.getStyleClass().add("emoji-preview-box");
        previewBox.setPrefWidth(BOX_SIZE);
        previewBox.setPrefHeight(BOX_SIZE);
        previewBox.setMinWidth(BOX_SIZE);
        previewBox.setMaxWidth(BOX_SIZE);
        previewBox.setAlignment(Pos.CENTER);

        textInput = new InputItem(placeholderKey, variant);
        HBox.setHgrow(textInput, Priority.ALWAYS);
        textInput.setMaxWidth(Double.MAX_VALUE);

        textInput.textProperty().addListener((obs, oldVal, newVal) -> refreshPreview(newVal));

        setSpacing(8);
        setAlignment(Pos.TOP_LEFT);
        setMaxWidth(Double.MAX_VALUE);
        getChildren().addAll(previewBox, textInput);
    }

    private void refreshPreview(String text) {
        if (text == null || text.isBlank()) {
            previewImage.setImage(null);
            return;
        }
        Image image = EmojiUtil.toImage(text.trim(), PREVIEW_SIZE);
        previewImage.setImage(image);
    }

    public String getText() {
        return textInput.getText();
    }

    public void setText(String text) {
        textInput.setText(text);
        refreshPreview(text);
    }

    public void showError(String message) {
        textInput.showError(message);
    }

    public void clearError() {
        textInput.clearError();
    }

    public boolean validateRequired() {
        return textInput.validateRequired();
    }
}
