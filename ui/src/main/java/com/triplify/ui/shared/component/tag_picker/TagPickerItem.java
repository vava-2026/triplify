package com.triplify.ui.shared.component.tag_picker;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.kordamp.ikonli.javafx.FontIcon;

import com.triplify.application.shared.ColorTheme;
import com.triplify.application.usecase.tag.TagService;
import com.triplify.application.usecase.tag.dto.CreateTagRequest;
import com.triplify.application.usecase.tag.dto.DeleteTagRequest;
import com.triplify.application.usecase.tag.dto.GetTagsRequest;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.Localization;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

public class TagPickerItem extends VBox {

    private static final URL FXML_URL = TagPickerItem.class.getResource(
            "/com/triplify/ui/shared/component/tag_picker/view/AppTagPicker.fxml"
    );
    private static final String THEME_STYLESHEET = "/com/triplify/ui/shared/css/theme.css";
    private static final String COMPONENT_STYLESHEET = "/com/triplify/ui/shared/component/tag_picker/css/tag_picker.css";
    private static final double POPUP_OFFSET_Y = 3.0;
    private static final int COLOR_VARIANTS = 20;
    private static final String DEFAULT_POPUP_TITLE = "All tags";

    @FXML private HBox shell;
    @FXML private TextField inputField;
    @FXML private Button clearInputButton;
    @FXML private Button toggleButton;
    @FXML private FlowPane selectedFlow;

    private final Popup popup = new Popup();
    private final StackPane popupContainer = new StackPane();
    private final VBox popupRoot = new VBox(10);
    private final Label popupTitleLabel = new Label(DEFAULT_POPUP_TITLE);
    private final VBox popupContent = new VBox(10);
    private final FlowPane popupChipFlow = new FlowPane();
    private final ScrollPane popupScroll = new ScrollPane();
    private final InvalidationListener popupAnchorListener = observable -> updatePopupPosition();
    private final ChangeListener<Window> sceneWindowListener = (obs, oldWindow, newWindow) -> trackWindow(newWindow);

    private final List<TagItem> availableTags = new ArrayList<>();
    private final LinkedHashSet<UUID> selectedTagIds = new LinkedHashSet<>();
    private final Map<UUID, String> selectedTagNamesById = new LinkedHashMap<>();
    private final StringProperty placeholderKey = new SimpleStringProperty();
    private final StringProperty popupTitleKey = new SimpleStringProperty();
    private Consumer<Set<UUID>> onSelectionChanged;
    private Consumer<String> onTagOperationFailed;
    private String popupTitle = DEFAULT_POPUP_TITLE;
    private Window trackedWindow;
    private boolean allowCustomTags = true;
    private TagService tagService;
    private FieldVariant lastVariant = null;

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
        configurePopupAnchorTracking();
        configureLocalizationBindings();
        updateSelectedView();
    }

    private void configureLocalizationBindings() {
        placeholderKey.addListener((obs, oldKey, newKey) -> bindPromptText(newKey));
        popupTitleKey.addListener((obs, oldKey, newKey) -> bindPopupTitle(newKey));
    }

    private void bindPromptText(String key) {
        inputField.promptTextProperty().unbind();
        if (key == null || key.isBlank()) {
            return;
        }
        Localization.bindText(inputField.promptTextProperty(), key);
    }

    private void bindPopupTitle(String key) {
        popupTitleLabel.textProperty().unbind();
        if (key == null || key.isBlank()) {
            popupTitle = DEFAULT_POPUP_TITLE;
            popupTitleLabel.setText(popupTitle);
            return;
        }
        popupTitle = I18n.t(key);
        Localization.bindText(popupTitleLabel.textProperty(), key);
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
            loadAvailableTags(normalizedTag(newVal));
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
        popup.setAutoFix(false);
        popup.setHideOnEscape(true);
        popup.setConsumeAutoHidingEvents(false);
        popup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
        popup.setOnShown(event -> updateOpenState(true));
        popup.setOnHidden(event -> updateOpenState(false));

        popupContainer.setPadding(Insets.EMPTY);
        popupContainer.getChildren().setAll(popupRoot);

        popupRoot.getStyleClass().add("app-tag-picker-popup");

        popupTitleLabel.getStyleClass().add("app-tag-picker-popup-title");

        popupContent.getStyleClass().add("app-tag-picker-popup-content");
        popupChipFlow.getStyleClass().add("app-tag-picker-popup-chip-flow");
        popupChipFlow.setHgap(10);
        popupChipFlow.setVgap(12);

        popupScroll.getStyleClass().add("app-tag-picker-popup-scroll");
        popupScroll.setFitToWidth(true);
        popupScroll.setPrefViewportHeight(190);
        popupScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        popupScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        popupScroll.setContent(popupContent);

        popupRoot.getChildren().addAll(popupTitleLabel, popupScroll);
        popup.getContent().setAll(popupContainer);

        popupRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                installPopupStylesheets(newScene);
            }
        });
    }

    private void configurePopupAnchorTracking() {
        shell.localToSceneTransformProperty().addListener(popupAnchorListener);
        shell.boundsInLocalProperty().addListener(popupAnchorListener);
        widthProperty().addListener(popupAnchorListener);

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.windowProperty().removeListener(sceneWindowListener);
            }
            trackWindow(newScene == null ? null : newScene.getWindow());
            if (newScene != null) {
                newScene.windowProperty().addListener(sceneWindowListener);
            }
            updatePopupPosition();
        });
    }

    private void trackWindow(Window window) {
        if (trackedWindow == window) {
            return;
        }
        if (trackedWindow != null) {
            trackedWindow.xProperty().removeListener(popupAnchorListener);
            trackedWindow.yProperty().removeListener(popupAnchorListener);
            trackedWindow.widthProperty().removeListener(popupAnchorListener);
            trackedWindow.heightProperty().removeListener(popupAnchorListener);
        }

        trackedWindow = window;
        if (trackedWindow != null) {
            trackedWindow.xProperty().addListener(popupAnchorListener);
            trackedWindow.yProperty().addListener(popupAnchorListener);
            trackedWindow.widthProperty().addListener(popupAnchorListener);
            trackedWindow.heightProperty().addListener(popupAnchorListener);
        }
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
        Window window = getScene() == null ? null : getScene().getWindow();
        if (window == null) {
            return;
        }

        loadAvailableTags(normalizedTag(inputField.getText()));

        Bounds bounds = shell.localToScreen(shell.getBoundsInLocal());
        if (bounds == null) {
            return;
        }

        double popupWidth = bounds.getWidth();
        updatePopupWidth(popupWidth);
        renderPopupList();

        if (!popup.isShowing()) {
            popup.show(window, calculatePopupX(bounds, popupWidth), calculatePopupY(bounds));
        } else {
            updatePopupPosition();
        }
    }

    private void updatePopupPosition() {
        if (!popup.isShowing()) {
            return;
        }

        Bounds bounds = shell.localToScreen(shell.getBoundsInLocal());
        if (bounds == null) {
            return;
        }

        double popupWidth = bounds.getWidth();
        updatePopupWidth(popupWidth);
        popup.setAnchorX(calculatePopupX(bounds, popupWidth));
        popup.setAnchorY(calculatePopupY(bounds));
    }

    private double calculatePopupX(Bounds shellBounds, double popupWidth) {
        return shellBounds.getMinX();
    }

    private double calculatePopupY(Bounds shellBounds) {
        return shellBounds.getMaxY() + POPUP_OFFSET_Y;
    }

    private void updatePopupWidth(double popupWidth) {
        popupContainer.setPrefWidth(popupWidth);
        popupContainer.setMinWidth(popupWidth);
        popupContainer.setMaxWidth(popupWidth);
        popupRoot.setPrefWidth(popupWidth);
        popupRoot.setMinWidth(popupWidth);
        popupRoot.setMaxWidth(popupWidth);
        popupChipFlow.setPrefWrapLength(Math.max(popupWidth - 28.0, 120.0));
    }

    private void updateOpenState(boolean open) {
        if (open) {
            if (!shell.getStyleClass().contains("app-tag-picker-shell-open")) {
                shell.getStyleClass().add("app-tag-picker-shell-open");
            }
            if (!toggleButton.getStyleClass().contains("app-tag-picker-toggle-open")) {
                toggleButton.getStyleClass().add("app-tag-picker-toggle-open");
            }
            return;
        }

        shell.getStyleClass().remove("app-tag-picker-shell-open");
        toggleButton.getStyleClass().remove("app-tag-picker-toggle-open");
    }

    private void renderPopupList() {
        if (!popupTitleLabel.textProperty().isBound()) {
            popupTitleLabel.setText(popupTitle);
        }
        popupChipFlow.getChildren().clear();
        popupContent.getChildren().clear();

        String filter = normalizedTag(inputField.getText());
        TagItem matchingTag = findMatchingTag(filter);
        if (allowCustomTags && filter != null && !filter.isBlank() && matchingTag == null) {
            popupChipFlow.getChildren().add(createSuggestionChip(filter, true));
        }

        availableTags.stream()
                .filter(tag -> matchesFilter(tag.name(), filter))
                .forEach(tag -> popupChipFlow.getChildren().add(createSuggestionChip(tag, false)));

        if (popupChipFlow.getChildren().isEmpty()) {
            popupContent.getChildren().add(createEmptyRow(
                    filter == null || filter.isBlank() ? "No tags yet" : "No matching tags"
            ));
            return;
        }

        popupContent.getChildren().add(popupChipFlow);
    }

    private Region createEmptyRow(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("app-tag-picker-empty");

        HBox row = new HBox(label);
        row.getStyleClass().add("app-tag-picker-empty-row");
        return row;
    }

    private Button createSuggestionChip(TagItem tag, boolean isNewTag) {
        Button chip = new Button(isNewTag ? tag.name() + " +" : tag.name());
        chip.setFocusTraversable(false);
        chip.getStyleClass().addAll("app-tag-picker-option", colorClass(tag.name()));

        if (isNewTag) {
            chip.getStyleClass().add("app-tag-picker-option-new");
        }
        if (!isNewTag && selectedTagIds.contains(tag.id())) {
            chip.getStyleClass().add("app-tag-picker-option-selected");
        }

        chip.setOnAction(event -> {
            if (isNewTag) {
                addTag(tag.name());
                return;
            }
            toggleSelectedTag(tag);
        });
        return chip;
    }

    private Button createSuggestionChip(String tagName, boolean isNewTag) {
        return createSuggestionChip(new TagItem(null, tagName), isNewTag);
    }

    private boolean matchesFilter(String tag, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return tag.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private void toggleSelectedTag(TagItem tag) {
        if (tag.id() == null) {
            return;
        }

        if (selectedTagIds.contains(tag.id())) {
            deleteTag(tag);
        } else {
            selectedTagIds.add(tag.id());
            selectedTagNamesById.put(tag.id(), tag.name());
            updateSelectedView();
            renderPopupList();
            notifySelectionChanged();
        }
    }

    private void addCurrentInputAsTag() {
        String typedTag = normalizedTag(inputField.getText());
        if (typedTag == null || typedTag.isBlank()) {
            return;
        }
        if (!allowCustomTags && findMatchingTag(typedTag) == null) {
            return;
        }
        addTag(typedTag);
    }

    private void addTag(String tag) {
        TagItem existingTag = findMatchingTag(tag);
        if (existingTag != null && existingTag.id() != null) {
            selectedTagIds.add(existingTag.id());
            selectedTagNamesById.put(existingTag.id(), existingTag.name());
            inputField.clear();
            updateSelectedView();
            renderPopupList();
            notifySelectionChanged();
            return;
        }

        if (tagService == null) {
            return;
        }

        var result = tagService.createTag(new CreateTagRequest(tag, ColorTheme.forLabel(tag)));
        if (result.isFailure()) {
            notifyTagFailure(result.getError().message());
            loadAvailableTags(normalizedTag(inputField.getText()));
            return;
        }

        TagResponse created = result.getValue();
        if (created == null || created.id() == null || created.name() == null || created.name().isBlank()) {
            return;
        }

        TagItem createdTag = new TagItem(created.id(), created.name().trim());
        availableTags.removeIf(item -> item.id() != null && item.id().equals(createdTag.id()));
        availableTags.add(createdTag);
        selectedTagIds.add(createdTag.id());
        selectedTagNamesById.put(createdTag.id(), createdTag.name());
        inputField.clear();
        updateSelectedView();
        renderPopupList();
        notifySelectionChanged();
    }

    private void deleteTag(TagItem tag) {
        if (tag.id() == null) {
            return;
        }

        selectedTagIds.remove(tag.id());
        selectedTagNamesById.remove(tag.id());
        updateSelectedView();
        renderPopupList();
        notifySelectionChanged();
    }

    private void updateSelectedView() {
        selectedFlow.getChildren().clear();
        boolean hasTags = !selectedTagIds.isEmpty();
        selectedFlow.setVisible(hasTags);
        selectedFlow.setManaged(hasTags);

        for (UUID tagId : selectedTagIds) {
            String tagName = selectedTagNamesById.get(tagId);
            if (tagName == null || tagName.isBlank()) {
                TagItem availableTag = findTagById(tagId);
                tagName = availableTag == null ? null : availableTag.name();
            }
            if (tagName == null || tagName.isBlank()) {
                continue;
            }
            selectedFlow.getChildren().add(createSelectedChip(new TagItem(tagId, tagName)));
        }
    }

    private Button createSelectedChip(TagItem tag) {
        Button chip = new Button(tag.name() + "  x");
        chip.setFocusTraversable(false);
        chip.getStyleClass().addAll("app-tag-picker-chip", colorClass(tag.name()), "app-tag-picker-chip-selected");
        chip.setOnAction(event -> {
            deleteTag(tag);
        });
        return chip;
    }

    private String colorClass(String tag) {
        int index = Math.floorMod(tag.hashCode(), COLOR_VARIANTS);
        return "app-tag-picker-chip-color-" + index;
    }

    private TagItem findMatchingTag(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }

        for (TagItem tag : availableTags) {
            if (tag.name().equalsIgnoreCase(candidate)) {
                return tag;
            }
        }
        return null;
    }

    private TagItem findTagById(UUID id) {
        if (id == null) {
            return null;
        }
        for (TagItem tag : availableTags) {
            if (id.equals(tag.id())) {
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
            onSelectionChanged.accept(Set.copyOf(selectedTagIds));
        }
    }

    private void notifyTagFailure(String message) {
        if (onTagOperationFailed != null && message != null && !message.isBlank()) {
            onTagOperationFailed.accept(message);
        }
    }

    private void loadAvailableTags(String filter) {
        if (tagService == null) {
            return;
        }

        GetTagsRequest request = new GetTagsRequest(
                PageRequest.defaultRequest(),
                filter == null || filter.isBlank() ? null : new GetTagsRequest.Filter(filter)
        );
        var result = tagService.getTags(request);
        if (result.isFailure()) {
            notifyTagFailure(result.getError().message());
            return;
        }

        availableTags.clear();
        result.getValue().stream()
                .filter(tag -> tag != null && tag.id() != null && tag.name() != null && !tag.name().isBlank())
                .forEach(tag -> {
                    TagItem mapped = new TagItem(tag.id(), tag.name().trim());
                    if (findTagById(mapped.id()) == null) {
                        availableTags.add(mapped);
                    }
                    if (selectedTagIds.contains(mapped.id())) {
                        selectedTagNamesById.put(mapped.id(), mapped.name());
                    }
                });

        if (popup.isShowing()) {
            renderPopupList();
        }
        updateSelectedView();
    }

    public void configureTagService(TagService tagService, Consumer<String> onTagOperationFailed) {
        this.tagService = tagService;
        this.onTagOperationFailed = onTagOperationFailed;
        loadAvailableTags(null);
    }

    public void reloadTags() {
        loadAvailableTags(normalizedTag(inputField.getText()));
    }

    public void setSelectedTagIds(Collection<UUID> tagIds) {
        selectedTagIds.clear();
        selectedTagNamesById.clear();
        if (tagIds != null) {
            tagIds.stream()
                    .filter(id -> id != null)
                    .forEach(id -> {
                        selectedTagIds.add(id);
                        TagItem availableTag = findTagById(id);
                        if (availableTag != null) {
                            selectedTagNamesById.put(id, availableTag.name());
                        }
                    });
        }
        updateSelectedView();
        if (popup.isShowing()) {
            renderPopupList();
        }
        notifySelectionChanged();
    }

    public void setPlaceholderText(String text) {
        inputField.promptTextProperty().unbind();
        inputField.setPromptText(text);
    }

    public void setPopupTitle(String text) {
        popupTitleLabel.textProperty().unbind();
        popupTitle = text == null || text.isBlank() ? DEFAULT_POPUP_TITLE : text;
        popupTitleLabel.setText(popupTitle);
    }

    public String getPlaceholderKey() {
        return placeholderKey.get();
    }

    public void setPlaceholderKey(String key) {
        placeholderKey.set(key);
    }

    public StringProperty placeholderKeyProperty() {
        return placeholderKey;
    }

    public String getPopupTitleKey() {
        return popupTitleKey.get();
    }

    public void setPopupTitleKey(String key) {
        popupTitleKey.set(key);
    }

    public StringProperty popupTitleKeyProperty() {
        return popupTitleKey;
    }

    public Set<UUID> getSelectedTagIds() {
        return new LinkedHashSet<>(selectedTagIds);
    }

    public void setAllowCustomTags(boolean allowCustomTags) {
        this.allowCustomTags = allowCustomTags;
        if (popup.isShowing()) {
            renderPopupList();
        }
    }

    public void setOnSelectionChanged(Consumer<Set<UUID>> onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
    }

    public void setVariant(FieldVariant variant) {
        applyVariant(variant);
    }

    public FieldVariant getVariant() {
        return lastVariant;
    }

    private void applyVariant(FieldVariant variant) {
        if (lastVariant == variant) return;
        if (lastVariant != null) {
            getStyleClass().remove(toVariantStyleClass(lastVariant));
        }
        if (variant != null) {
            getStyleClass().add(toVariantStyleClass(variant));
        }
        lastVariant = variant;
    }

    private static String toVariantStyleClass(FieldVariant variant) {
        return switch (variant) {
            case OUTLINED -> "app-tag-picker-variant-outlined";
            case FILLED -> "app-tag-picker-variant-filled";
            case GHOST -> "app-tag-picker-variant-ghost";
        };
    }

    private record TagItem(UUID id, String name) {
    }
}
