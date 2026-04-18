package com.triplify.ui.pages.categories;

import com.google.inject.Inject;
import com.triplify.application.model.ColorTheme;
import com.triplify.application.usecase.category.CategoryService;
import com.triplify.application.usecase.category.dto.AddCategoryRequest;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.category.dto.DeleteCategoryRequest;
import com.triplify.application.usecase.category.dto.UpdateCategoryRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.Localization;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class CategoriesController extends SimpleLifecycleAwareController {

    @FXML private Label formSectionTitleLabel;
    @FXML private Label modeBadgeLabel;

    @FXML private Label nameLabel;
    @FXML private Label nameSkLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label descriptionSkLabel;
    @FXML private Label emojiLabel;
    @FXML private Label colorLabel;

    @FXML private VBox nameInputContainer;
    @FXML private VBox nameSkInputContainer;
    @FXML private VBox descriptionInputContainer;
    @FXML private VBox descriptionSkInputContainer;
    @FXML private VBox emojiInputContainer;
    @FXML private VBox colorSelectContainer;
    @FXML private HBox searchContainer;

    @FXML private Button saveButton;
    @FXML private Button clearFormButton;
    @FXML private Button deleteButton;

    @FXML private VBox categoriesListContainer;
    @FXML private Label emptyStateLabel;

    @Inject private CategoryService categoryService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;

    private InputItem nameInput;
    private InputItem nameSkInput;
    private InputItem descriptionInput;
    private InputItem descriptionSkInput;
    private InputItem emojiInput;
    private SearchView<String> searchView;
    private Select<ColorTheme> colorSelectModel;
    private SelectView<ColorTheme> colorSelectView;
    private String activeSearchQuery = "";

    private final ObjectProperty<CategoryResponse> selectedCategory = new SimpleObjectProperty<>();
    private final Map<String, Region> categoryRowsById = new HashMap<>();
    private final List<CategoryResponse> allCategories = new ArrayList<>();

    @FXML
    public void initialize() {
        initializeInputs();
        bindText();
        attachListeners();
        reloadCategories();
    }

    @Override
    public void onLifecycleShow() {
        reloadCategories();
    }

    @FXML
    private void onSaveCategory() {
        clearFieldErrors();

        ColorTheme selectedColor = selectedColor();
        if (selectedColor == null) {
            toast.warning("Please select a color.");
            return;
        }

        boolean creating = selectedCategory.get() == null;
        var handlers = buildFieldHandlers();

        if (creating) {
            AddCategoryRequest request = new AddCategoryRequest(
                    normalize(nameInput.getText()),
                    normalize(nameSkInput.getText()),
                    normalizeNullable(descriptionInput.getText()),
                    normalizeNullable(descriptionSkInput.getText()),
                    normalize(emojiInput.getText()),
                        selectedColor
            );
            var result = categoryService.addCategory(request);
            result.onSuccess(category -> {
                selectedCategory.set(category);
                toast.success("Category created", "New category was saved.");
                reloadCategories();
            });
            result.onFailure(error -> errorHandler.handle(error, handlers));
            return;
        }

        CategoryResponse existing = selectedCategory.get();
        UpdateCategoryRequest request = new UpdateCategoryRequest(
                existing.id(),
                normalize(nameInput.getText()),
                normalize(nameSkInput.getText()),
                normalizeNullable(descriptionInput.getText()),
                normalizeNullable(descriptionSkInput.getText()),
                normalize(emojiInput.getText()),
                selectedColor
        );
        var result = categoryService.updateCategory(request);
        result.onSuccess(category -> {
            selectedCategory.set(category);
            toast.success("Category updated", "Category changes were saved.");
            reloadCategories();
        });
        result.onFailure(error -> errorHandler.handle(error, handlers));
    }

    @FXML
    private void onClearForm() {
        clearSelection();
    }

    @FXML
    private void onDeleteCategory() {
        CategoryResponse current = selectedCategory.get();
        if (current == null) {
            toast.info("Select a category first.");
            return;
        }

        var result = categoryService.deleteCategory(new DeleteCategoryRequest(current.id()));
        result.onSuccess(ignored -> {
            toast.success("Category deleted", "Category was removed.");
            clearSelection();
            reloadCategories();
        });
        result.onFailure(errorHandler::handle);
    }

    private void initializeInputs() {
        nameInput = new InputItem("categories.input.name", FieldVariant.GHOST);
        nameSkInput = new InputItem("categories.input.nameSk", FieldVariant.GHOST);
        descriptionInput = new InputItem("categories.input.description", FieldVariant.GHOST);
        descriptionSkInput = new InputItem("categories.input.descriptionSk", FieldVariant.GHOST);
        emojiInput = new InputItem("categories.input.emoji", FieldVariant.GHOST);

        nameInputContainer.getChildren().setAll(nameInput);
        nameSkInputContainer.getChildren().setAll(nameSkInput);
        descriptionInputContainer.getChildren().setAll(descriptionInput);
        descriptionSkInputContainer.getChildren().setAll(descriptionSkInput);
        emojiInputContainer.getChildren().setAll(emojiInput);

        searchView = new SearchView<>(Search.<String>builder(this::search)
                .placeholderKey("categories.search.placeholder")
                .variant(FieldVariant.OUTLINED)
                .build());
        searchContainer.getChildren().setAll(searchView);

        colorSelectModel = createColorSelectModel();
        colorSelectView = createColorSelectView(colorSelectModel);
        colorSelectContainer.getChildren().setAll(colorSelectView);
    }

    private List<Entry<String>> search(String searchQuery) {
        activeSearchQuery = searchQuery == null ? "" : searchQuery;
        List<CategoryResponse> filtered = filteredCategories(searchQuery);
        renderCategories(filtered);

        return filtered.stream()
                .map(category -> Entry.builder(category.name(), category.name()).build())
                .toList();
    }

    private void bindText() {
        Localization.bindText(formSectionTitleLabel.textProperty(), "nav.categories");

        saveButton.textProperty().bind(Bindings.createStringBinding(
                () -> selectedCategory.get() == null ? I18n.t("countries.action.create") : I18n.t("countries.action.update"),
                selectedCategory,
                I18n.bundleProperty()
        ));

        modeBadgeLabel.textProperty().bind(Bindings.createStringBinding(
                () -> selectedCategory.get() == null ? I18n.t("countries.mode.create") : I18n.t("countries.mode.edit"),
                selectedCategory,
                I18n.bundleProperty()
        ));
    }

    private void attachListeners() {
        selectedCategory.addListener((obs, oldValue, newValue) -> refreshSelectionStyles());
        I18n.languageProperty().addListener((obs, oldValue, newValue) -> renderFilteredCategories(activeSearchQuery));
    }

    private void reloadCategories() {
        var result = categoryService.getAllCategories();
        result.onSuccess(categories -> {
            allCategories.clear();
            allCategories.addAll(categories.stream()
                    .sorted(Comparator.comparing(CategoryResponse::name, String.CASE_INSENSITIVE_ORDER))
                    .toList());
            renderFilteredCategories(activeSearchQuery);
        });
        result.onFailure(error -> {
            allCategories.clear();
            renderFilteredCategories(activeSearchQuery);
            errorHandler.handle(error);
        });
    }

    private void renderFilteredCategories(String searchQuery) {
        activeSearchQuery = searchQuery == null ? "" : searchQuery;
        renderCategories(filteredCategories(searchQuery));
    }

    private List<CategoryResponse> filteredCategories(String searchQuery) {
        String needle = normalizeNullable(searchQuery);
        String search = needle == null ? null : needle.toLowerCase(Locale.ROOT);

        List<CategoryResponse> filtered = new ArrayList<>();
        for (CategoryResponse category : allCategories) {
            if (matchesSearch(category, search)) {
                filtered.add(category);
            }
        }
        return filtered;
    }

    private void renderCategories(List<CategoryResponse> categories) {
        categoriesListContainer.getChildren().clear();
        categoryRowsById.clear();

        for (CategoryResponse category : categories) {
            addCategoryCard(category);
        }

        refreshEmptyState();
        refreshSelectionStyles();
    }

    private boolean matchesSearch(CategoryResponse category, String search) {
        if (search == null) {
            return true;
        }
        return contains(category.name(), search)
                || contains(category.nameSk(), search)
                || contains(category.description(), search)
                || contains(category.descriptionSk(), search)
                || contains(category.emojiUnicode(), search)
                || contains(prettifyColor(category.color()), search);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private void addCategoryCard(CategoryResponse category) {
        Label emoji = new Label(category.emojiUnicode() == null ? "" : category.emojiUnicode());
        emoji.getStyleClass().add("categories-item-emoji");

        Label title = new Label(Localization.localize(category.name(), category.nameSk()));
        title.getStyleClass().add("categories-item-title");

        String details = buildSubtitle(category);
        Label subtitle = new Label(details);
        subtitle.getStyleClass().add("categories-item-subtitle");

        VBox textBox = new VBox(2, title, subtitle);
        textBox.getStyleClass().add("categories-item-text");

        Label colorTag = new Label(prettifyColor(category.color()));
        colorTag.getStyleClass().add("categories-item-color");
        colorTag.getStyleClass().add(resolveColorStyleClass(category.color()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, emoji, textBox, spacer, colorTag);
        header.getStyleClass().add("categories-item-header");

        VBox card = new VBox(header);
        card.getStyleClass().add("categories-item");
        card.setUserData(category.id());
        card.setOnMouseClicked(event -> selectCategory(category));

        categoriesListContainer.getChildren().add(card);
        categoryRowsById.put(category.id(), card);
    }

    private String buildSubtitle(CategoryResponse category) {
        String primary = category.name() == null ? "" : category.name();
        String secondary = category.nameSk() == null ? "" : category.nameSk();

        if (category.description() != null && !category.description().isBlank()) {
            return primary + " / " + secondary + " • " + category.description();
        }
        return primary + " / " + secondary;
    }

    private void refreshSelectionStyles() {
        CategoryResponse current = selectedCategory.get();
        String selectedId = current == null ? null : current.id();

        categoryRowsById.forEach((categoryId, row) -> {
            if (categoryId.equals(selectedId)) {
                if (!row.getStyleClass().contains("categories-item-selected")) {
                    row.getStyleClass().add("categories-item-selected");
                }
            } else {
                row.getStyleClass().remove("categories-item-selected");
            }
        });
    }

    private void refreshEmptyState() {
        boolean isEmpty = categoriesListContainer.getChildren().isEmpty();
        emptyStateLabel.setVisible(isEmpty);
        emptyStateLabel.setManaged(isEmpty);
    }

    private void selectCategory(CategoryResponse category) {
        selectedCategory.set(category);
        nameInput.setText(safe(category.name()));
        nameSkInput.setText(safe(category.nameSk()));
        descriptionInput.setText(safe(category.description()));
        descriptionSkInput.setText(safe(category.descriptionSk()));
        emojiInput.setText(safe(category.emojiUnicode()));
        colorSelectModel.setSelectedItem(findColorEntry(category.color()));
        clearFieldErrors();
    }

    private void clearSelection() {
        selectedCategory.set(null);
        nameInput.setText("");
        nameSkInput.setText("");
        descriptionInput.setText("");
        descriptionSkInput.setText("");
        emojiInput.setText("");
        colorSelectModel.setSelectedItem(null);
        clearFieldErrors();
    }

    private void clearFieldErrors() {
        nameInput.clearError();
        nameSkInput.clearError();
        descriptionInput.clearError();
        descriptionSkInput.clearError();
        emojiInput.clearError();
    }

    private Map<String, Consumer<String>> buildFieldHandlers() {
        return Map.of(
                "name", message -> nameInput.showError(message),
                "nameSk", message -> nameSkInput.showError(message),
                "description", message -> descriptionInput.showError(message),
                "descriptionSk", message -> descriptionSkInput.showError(message),
                "emojiUnicode", message -> emojiInput.showError(message)
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String prettifyColor(ColorTheme colorTheme) {
        if (colorTheme == null) {
            return "Gray";
        }
        return colorTheme.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private String resolveColorStyleClass(ColorTheme colorTheme) {
        return (colorTheme == null ? ColorTheme.GRAY : colorTheme).getStyleClass();
    }

    private Select<ColorTheme> createColorSelectModel() {
        List<Entry<ColorTheme>> entries = new ArrayList<>();
        for (ColorTheme colorTheme : ColorTheme.values()) {
            entries.add(Entry.builder(colorTheme, prettifyColor(colorTheme)).build());
        }

        return Select.<ColorTheme>builder()
                .placeholder("Select color")
                .variant(FieldVariant.GHOST)
                .items(entries)
                .build();
    }

    private SelectView<ColorTheme> createColorSelectView(Select<ColorTheme> model) {
        SelectView<ColorTheme> view = new SelectView<>();
        view.update(model);
        view.setMaxWidth(Double.MAX_VALUE);
        view.getComboBox().setMaxWidth(Double.MAX_VALUE);
        view.getComboBox().getStyleClass().add("categories-color-select");
        return view;
    }

    private Entry<ColorTheme> findColorEntry(ColorTheme colorTheme) {
        if (colorTheme == null) {
            return null;
        }
        for (Entry<ColorTheme> entry : colorSelectModel.getItems()) {
            if (colorTheme.equals(entry.getValue())) {
                return entry;
            }
        }
        return null;
    }

    private ColorTheme selectedColor() {
        Entry<ColorTheme> entry = colorSelectModel.getSelectedItem();
        return entry == null ? null : entry.getValue();
    }
}
