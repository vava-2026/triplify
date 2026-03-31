package com.triplify.ui.shared.component.tag_picker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public class TagPickerItem extends VBox {

    private static final URL FXML_URL = TagPickerItem.class.getResource(
            "/com/triplify/ui/shared/component/tag_picker/view/AppTagPicker.fxml"
    );
    private static final String THEME_STYLESHEET = "/com/triplify/ui/shared/css/theme.css";
    private static final String COMPONENT_STYLESHEET = "/com/triplify/ui/shared/component/tag_picker/css/tag_picker.css";
    private static final double POPUP_OFFSET_Y = 6.0;
    private static final double MIN_POPUP_WIDTH = 316.0;
    private static final int COLOR_VARIANTS = 5;

    @FXML private HBox shell;
    @FXML private TextField inputField;
    @FXML private Button clearInputButton;
    @FXML private Button toggleButton;
    @FXML private FlowPane selectedFlow;

    private final Popup popup = new Popup();
    private final VBox popupRoot = new VBox(8);
    private final VBox popupList = new VBox(4);
    private final ScrollPane popupScroll = new ScrollPane();

    private final List<String> availableTags = new ArrayList<>();
    private final LinkedHashSet<String> selectedTags = new LinkedHashSet<>();
    private Consumer<Set<String>> onSelectionChanged;

    public TagPickerItem() {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AppTagPicker.fxml", e);
        }

        getStyleClass().add("app-tag-picker");
        setMaxWidth(Double.MAX_VALUE);
        setSpacing(8);
        shell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        configureShellButtons();
        configureFieldBehavior();
        configurePopup();
        updateSelectedView();
    }

    private void configureShellButtons() {
        FontIcon clearIcon = new FontIcon("fth-x");
        clearIcon.getStyleClass().add("app-tag-picker-clear-icon");
        clearIcon.setIconSize(14);
        clearInputButton.setGraphic(clearIcon);
        clearInputButton.setFocusTraversable(false);
        clearInputButton.setOnAction(event -> {
            inputField.clear();
            if (popup.isShowing()) {
                renderPopupList();
            }
        });

        FontIcon toggleIcon = new FontIcon("fth-chevron-down");
        toggleIcon.getStyleClass().add("app-tag-picker-toggle-icon");
        toggleIcon.setIconSize(14);
        toggleButton.setGraphic(toggleIcon);
        toggleButton.setFocusTraversable(false);
        toggleButton.setOnAction(event -> togglePopup());
    }

    private void configureFieldBehavior() {
        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasText = newVal != null && !newVal.isBlank();
            clearInputButton.setVisible(hasText);
            clearInputButton.setManaged(hasText);
            if (!popup.isShowing()) {
                showPopup();
            } else {
                renderPopupList();
            }
        });
        inputField.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (focused) {
                showPopup();
            }
        });
        inputField.setOnAction(event -> addCurrentInputAsTag());
    }

    private void configurePopup() {
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
        popup.setConsumeAutoHidingEvents(false);
        popup.setOnShown(event -> updateOpenState(true));
        popup.setOnHidden(event -> updateOpenState(false));

        popupRoot.getStyleClass().add("app-tag-picker-popup");
        popupList.getStyleClass().add("app-tag-picker-popup-list");

        popupScroll.getStyleClass().add("app-tag-picker-popup-scroll");
        popupScroll.setFitToWidth(true);
        popupScroll.setPrefViewportHeight(148);
        popupScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        popupScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        popupScroll.setContent(popupList);

        popupRoot.getChildren().add(popupScroll);
        popup.getContent().setAll(popupRoot);

        popupRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                installPopupStylesheets(newScene);
            }
        });
    }

    private void installPopupStylesheets(Scene scene) {
        addStylesheetIfMissing(scene.getStylesheets(), THEME_STYLESHEET);
        addStylesheetIfMissing(scene.getStylesheets(), COMPONENT_STYLESHEET);
    }

    private void addStylesheetIfMissing(List<String> stylesheets, String resourcePath) {
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return;
        }

        String externalForm = resource.toExternalForm();
        if (!stylesheets.contains(externalForm)) {
            stylesheets.add(externalForm);
        }
    }

    private void togglePopup() {
        if (popup.isShowing()) {
            popup.hide();
            return;
        }
        showPopup();
    }

    private void showPopup() {
        Bounds bounds = shell.localToScreen(shell.getBoundsInLocal());
        Window window = getScene() == null ? null : getScene().getWindow();
        if (bounds == null || window == null) {
            return;
        }

        renderPopupList();
        double popupWidth = Math.max(bounds.getWidth() + 28.0, MIN_POPUP_WIDTH);
        double popupX = bounds.getMinX() - ((popupWidth - bounds.getWidth()) / 2.0);
        popupRoot.setPrefWidth(popupWidth);
        popupRoot.setMinWidth(popupWidth);
        popupRoot.setMaxWidth(popupWidth);
        if (!popup.isShowing()) {
            popup.show(window, popupX, bounds.getMaxY() + POPUP_OFFSET_Y);
        } else {
            popup.setX(popupX);
            popup.setY(bounds.getMaxY() + POPUP_OFFSET_Y);
        }
    }

    private void updateOpenState(boolean open) {
        if (open) {
            if (!shell.getStyleClass().contains("app-tag-picker-shell-open")) {
                shell.getStyleClass().add("app-tag-picker-shell-open");
            }
            return;
        }

        shell.getStyleClass().remove("app-tag-picker-shell-open");
    }

    private void renderPopupList() {
        popupList.getChildren().clear();
        String filter = normalizedTag(inputField.getText());

        if (filter != null && !filter.isBlank() && findMatchingTag(filter) == null) {
            popupList.getChildren().add(createSuggestionRow(filter, true));
        }

        List<String> matchingTags = availableTags.stream()
                .filter(tag -> matchesFilter(tag, filter))
                .toList();

        if (matchingTags.isEmpty() && (filter == null || filter.isBlank())) {
            popupList.getChildren().add(createEmptyRow("No tags yet"));
            return;
        }

        if (matchingTags.isEmpty()) {
            popupList.getChildren().add(createEmptyRow("No matching tags"));
            return;
        }

        for (String tag : matchingTags) {
            popupList.getChildren().add(createSuggestionRow(tag, false));
        }
    }

    private Region createEmptyRow(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("app-tag-picker-empty");

        HBox row = new HBox(label);
        row.getStyleClass().add("app-tag-picker-row-empty");
        return row;
    }

    private Button createSuggestionRow(String tag, boolean isNewTag) {
        Button row = new Button();
        row.setFocusTraversable(false);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getStyleClass().add("app-tag-picker-row");
        if (selectedTags.contains(tag) && !isNewTag) {
            row.getStyleClass().add("app-tag-picker-row-selected");
        }

        Label title = new Label(isNewTag ? tag + " (New Tag)" : tag);
        title.getStyleClass().addAll(
                "app-tag-picker-row-chip",
                colorClass(tag),
                isNewTag ? "app-tag-picker-row-chip-new" : "app-tag-picker-row-chip-existing"
        );
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox content = new HBox(8, title, spacer);
        content.setFillHeight(true);

        if (!isNewTag && selectedTags.contains(tag)) {
            FontIcon check = new FontIcon("fth-check");
            check.getStyleClass().add("app-tag-picker-row-check");
            check.setIconSize(12);
            content.getChildren().add(check);
        }

        row.setGraphic(content);
        row.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        row.setOnAction(event -> {
            if (isNewTag) {
                addTag(tag);
            } else {
                toggleSelectedTag(tag);
            }
        });
        return row;
    }

    private boolean matchesFilter(String tag, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return tag.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private void toggleSelectedTag(String tag) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag);
        } else {
            selectedTags.add(tag);
        }
        updateSelectedView();
        renderPopupList();
        notifySelectionChanged();
    }

    private void addCurrentInputAsTag() {
        String typedTag = normalizedTag(inputField.getText());
        if (typedTag == null || typedTag.isBlank()) {
            return;
        }
        addTag(typedTag);
    }

    private void addTag(String tag) {
        String existingTag = findMatchingTag(tag);
        String targetTag = existingTag == null ? tag : existingTag;
        if (existingTag == null) {
            availableTags.add(targetTag);
        }
        selectedTags.add(targetTag);
        inputField.clear();
        updateSelectedView();
        renderPopupList();
        notifySelectionChanged();
    }

    private void updateSelectedView() {
        selectedFlow.getChildren().clear();
        boolean hasTags = !selectedTags.isEmpty();
        selectedFlow.setVisible(hasTags);
        selectedFlow.setManaged(hasTags);

        for (String tag : selectedTags) {
            selectedFlow.getChildren().add(createSelectedChip(tag));
        }
    }

    private Button createSelectedChip(String tag) {
        Button chip = new Button(tag + " ×");
        chip.setFocusTraversable(false);
        chip.getStyleClass().addAll("app-tag-picker-chip", colorClass(tag), "app-tag-picker-chip-selected");
        chip.setOnAction(event -> {
            selectedTags.remove(tag);
            updateSelectedView();
            if (popup.isShowing()) {
                renderPopupList();
            }
            notifySelectionChanged();
        });
        return chip;
    }

    private String colorClass(String tag) {
        int index = Math.floorMod(tag.hashCode(), COLOR_VARIANTS);
        return "app-tag-picker-chip-color-" + index;
    }

    private String findMatchingTag(String candidate) {
        for (String tag : availableTags) {
            if (tag.equalsIgnoreCase(candidate)) {
                return tag;
            }
        }
        return null;
    }

    private String normalizedTag(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            return "";
        }
        return normalized;
    }

    private void notifySelectionChanged() {
        if (onSelectionChanged != null) {
            onSelectionChanged.accept(Set.copyOf(selectedTags));
        }
    }

    public void setPlaceholderText(String text) {
        inputField.setPromptText(text);
    }

    public void setPopupTitle(String text) {
        // Compatibility no-op: popup is row-based now.
    }

    public void setAvailableTags(Collection<String> tags) {
        availableTags.clear();
        if (tags != null) {
            availableTags.addAll(tags);
        }
        if (popup.isShowing()) {
            renderPopupList();
        }
    }

    public void setSelectedTags(Collection<String> tags) {
        selectedTags.clear();
        if (tags != null) {
            selectedTags.addAll(tags);
            for (String tag : tags) {
                if (findMatchingTag(tag) == null) {
                    availableTags.add(tag);
                }
            }
        }
        updateSelectedView();
        if (popup.isShowing()) {
            renderPopupList();
        }
    }

    public Set<String> getSelectedTags() {
        return new LinkedHashSet<>(selectedTags);
    }

    public void setOnSelectionChanged(Consumer<Set<String>> onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
    }
}
