package com.triplify.ui.pages.stories.view;

import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.ui.shared.util.DisplayUtils;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.Localization;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Dictionary;
import java.util.ResourceBundle;
import java.util.Set;

public class StoryCardView implements Initializable {

    private static final URL FXML_URL = StoryCardView.class.getResource(
            "/com/triplify/ui/pages/stories/StoryCard.fxml"
    );
    private static final int CATEGORY_EMOJI_SIZE = 18;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneOffset.UTC);

    @FXML private VBox root;
    @FXML private StackPane media;
    @FXML private Label titleLabel;
    @FXML private HBox emotionRow;
    @FXML private ImageView emotionEmojiView;
    @FXML private Label emotionLabel;
    @FXML private Label dateLabel;

    @Setter
    private Runnable onOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        emotionRow.setVisible(false);
        emotionRow.setManaged(false);
        root.setMaxWidth(Double.MAX_VALUE);
        media.setMaxWidth(Double.MAX_VALUE);
        media.minHeightProperty().set(Region.USE_PREF_SIZE);
        media.maxHeightProperty().set(Region.USE_PREF_SIZE);
        EditorUtils.installRoundedClip(media, 12);

        root.setOnMouseClicked(event -> {
            if (onOpen != null) onOpen.run();
        });
    }

    public Node getRoot() {
        return root;
    }

    public void setStory(StoryResponse story) {
        if (story == null) return;

        titleLabel.setText(story.title() == null || story.title().isBlank() ? "—" : story.title());
        dateLabel.setText(story.storyTime() == null ? "" : DATE_FORMAT.format(story.storyTime()));

        if (story.emotion()!=null){
            DisplayUtils.bindEmoji(emotionRow, emotionLabel, emotionEmojiView, story.emotion(), story.emotion().emojiUnicode(), CATEGORY_EMOJI_SIZE);
        }
        applyCover(story.images());
    }

    private void applyCover(Set<ImageResponse> images) {
        String coverUrl = null;
        if (images != null && !images.isEmpty()) {
            ImageResponse first = images.iterator().next();
            if (first.url() != null) {
                try {
                    coverUrl = first.url().toUri().toString();
                } catch (Exception ignored) {}
            }
        }
        if (coverUrl == null) {
            if (!media.getStyleClass().contains("story-cover-default")) {
                media.getStyleClass().add("story-cover-default");
            }
            return;
        }
        media.getStyleClass().remove("story-cover-default");
        EditorUtils.applyCoverBackgroundAsync(media, coverUrl);
    }

    public static StoryCardView create(StoryResponse story, Runnable onOpen) {
        if (FXML_URL == null) throw new IllegalStateException("StoryCard.fxml not found");
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load StoryCard.fxml", e);
        }
        StoryCardView view = loader.getController();
        view.setOnOpen(onOpen);
        view.setStory(story);
        return view;
    }
}
