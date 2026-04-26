package com.triplify.ui.pages.badges;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.badge.BadgeService;
import com.triplify.application.usecase.badge.dto.AddBadgeRequest;
import com.triplify.application.usecase.badge.dto.BadgeResponse;
import com.triplify.application.usecase.badge.dto.DeleteBadgeRequest;
import com.triplify.application.usecase.badge.dto.GetBadgesRequest;
import com.triplify.application.usecase.badge.dto.UpdateBadgeRequest;
import com.triplify.application.usecase.badgegroup.dto.BadgeGroupResponse;
import com.triplify.application.usecase.badgegroup.dto.BadgeGroupType;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class BadgesController extends SimpleLifecycleAwareController {

	private static final int PAGE_SIZE = 8;

    @FXML private Label formSectionTitleLabel;
    @FXML private Label modeBadgeLabel;

    @FXML private Label nameLabel;
    @FXML private Label nameSkLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label descriptionSkLabel;
    @FXML private Label groupLabel;
    @FXML private Label levelLabel;
    @FXML private Label requiredValueLabel;
    @FXML private Label imagePathLabel;

    @FXML private VBox nameInputContainer;
    @FXML private VBox nameSkInputContainer;
    @FXML private VBox descriptionInputContainer;
    @FXML private VBox descriptionSkInputContainer;
    @FXML private VBox groupSelectContainer;
    @FXML private VBox levelInputContainer;
    @FXML private VBox requiredValueInputContainer;
    @FXML private VBox imagePathInputContainer;
    @FXML private HBox searchContainer;

      @FXML private VBox saveButtonContainer;
      @FXML private VBox clearFormButtonContainer;
      @FXML private VBox deleteButtonContainer;

	@FXML private CardGridPane<BadgeResponse> badgesGrid;

    @Inject private BadgeService badgeService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private InputItem nameInput;
    private InputItem nameSkInput;
    private InputItem descriptionInput;
    private InputItem descriptionSkInput;
    private InputItem levelInput;
    private InputItem requiredValueInput;
    private InputItem imagePathInput;
    private SearchView<UUID> searchView;
    private Select<UUID> groupSelectModel;
    private SelectView<UUID> groupSelectView;

    private final ObjectProperty<BadgeResponse> selectedBadge = new SimpleObjectProperty<>();
    private final Map<UUID, Region> badgeRowsById = new HashMap<>();
    private final List<BadgeResponse> allBadges = new ArrayList<>();
    private final Map<String, BadgeGroupResponse> groupsById = new HashMap<>();

    private String activeSearchQuery = "";

	@FXML
	public void initialize() {
		initializeInputs();
        buildButtons();
		configureGrid();
		bindText();
		attachListeners();
		initializeBadgeGroups();
	}

    @Override
    public void onLifecycleShow() {
        reloadBadges();
    }

    @FXML
    private void onSaveBadge() {
        clearFieldErrors();

        Integer level = parseInteger(levelInput.getText(), levelInput, "badges.validation.levelNonNegative");
        Integer requiredValue = parseInteger(requiredValueInput.getText(), requiredValueInput, "badges.validation.requiredNonNegative");
        if (level == null || requiredValue == null) {
            return;
        }

        BadgeResponse existing = selectedBadge.get();
        boolean creating = existing == null;

        UUID groupId = selectedGroupId();
        if (creating && groupId == null) {
            toast.warning(I18n.t("badges.validation.selectGroup"));
            return;
        }

        Path imagePath = parseImagePath(imagePathInput.getText());
        var handlers = buildFieldHandlers();

        if (creating) {
            AddBadgeRequest request = new AddBadgeRequest(
                    groupId,
                    imagePath,
                    normalize(nameInput.getText()),
                    normalize(nameSkInput.getText()),
                    normalizeNullable(descriptionInput.getText()),
                    normalizeNullable(descriptionSkInput.getText()),
                    level,
                    requiredValue
            );

            var result = badgeService.addBadge(request);
            result.onSuccess(badge -> {
                selectedBadge.set(badge);
                toast.success(I18n.t("badges.toast.created.title"), I18n.t("badges.toast.created.body"));
                reloadBadges();
            });
            result.onFailure(error -> errorHandler.handle(error, handlers));
            return;
        }

        UpdateBadgeRequest request = new UpdateBadgeRequest(
                existing.id(),
                imagePath,
                normalize(nameInput.getText()),
                normalize(nameSkInput.getText()),
                normalizeNullable(descriptionInput.getText()),
                normalizeNullable(descriptionSkInput.getText()),
                level,
                requiredValue
        );

        var result = badgeService.updateBadge(request);
        result.onSuccess(badge -> {
            selectedBadge.set(badge);
            toast.success(I18n.t("badges.toast.updated.title"), I18n.t("badges.toast.updated.body"));
            reloadBadges();
        });
        result.onFailure(error -> errorHandler.handle(error, handlers));
    }

    @FXML
    private void onClearForm() {
        clearSelection();
    }

    @FXML
    private void onDeleteBadge() {
        BadgeResponse current = selectedBadge.get();
        if (current == null) {
            toast.info(I18n.t("badges.toast.selectFirst"));
            return;
        }

        var result = badgeService.deleteBadge(new DeleteBadgeRequest(current.id()));
        result.onSuccess(ignored -> {
            toast.success(I18n.t("badges.toast.deleted.title"), I18n.t("badges.toast.deleted.body"));
            clearSelection();
            reloadBadges();
        });
        result.onFailure(errorHandler::handle);
    }

    private void initializeInputs() {
        nameInput = new InputItem("badges.input.name", FieldVariant.GHOST);
        nameSkInput = new InputItem("badges.input.nameSk", FieldVariant.GHOST);
        descriptionInput = new InputItem("badges.input.description", FieldVariant.GHOST);
        descriptionSkInput = new InputItem("badges.input.descriptionSk", FieldVariant.GHOST);
        levelInput = new InputItem("badges.input.level", FieldVariant.GHOST);
        requiredValueInput = new InputItem("badges.input.requiredValue", FieldVariant.GHOST);
        imagePathInput = new InputItem("badges.input.imagePath", FieldVariant.GHOST);

        nameInputContainer.getChildren().setAll(nameInput);
        nameSkInputContainer.getChildren().setAll(nameSkInput);
        descriptionInputContainer.getChildren().setAll(descriptionInput);
        descriptionSkInputContainer.getChildren().setAll(descriptionSkInput);
        levelInputContainer.getChildren().setAll(levelInput);
        requiredValueInputContainer.getChildren().setAll(requiredValueInput);
        imagePathInputContainer.getChildren().setAll(imagePathInput);

        searchView = new SearchView<>(Search.<UUID>builder(this::search)
                .placeholderKey("badges.search.placeholder")
                .variant(FieldVariant.OUTLINED)
                .maxResults(12)
                .build());
        if (searchContainer != null) {
            searchContainer.getChildren().setAll(searchView);
        }

        groupSelectModel = createGroupSelectModel(List.of());
        groupSelectView = createGroupSelectView(groupSelectModel);
        groupSelectContainer.getChildren().setAll(groupSelectView);
    }

	private void bindText() {
		Localization.bindText(formSectionTitleLabel.textProperty(), "nav.badges");
		Localization.bindText(nameLabel.textProperty(), "badges.field.name");
		Localization.bindText(nameSkLabel.textProperty(), "badges.field.nameSk");
		Localization.bindText(descriptionLabel.textProperty(), "badges.field.description");
		Localization.bindText(descriptionSkLabel.textProperty(), "badges.field.descriptionSk");
		Localization.bindText(groupLabel.textProperty(), "badges.field.group");
		Localization.bindText(levelLabel.textProperty(), "badges.field.level");
		Localization.bindText(requiredValueLabel.textProperty(), "badges.field.requiredValue");
		Localization.bindText(imagePathLabel.textProperty(), "badges.field.imagePath");
		badgesGrid.setEmptyText(I18n.t("badges.empty"));

		modeBadgeLabel.textProperty().bind(Bindings.createStringBinding(
				() -> selectedBadge.get() == null ? I18n.t("badges.mode.create") : I18n.t("badges.mode.edit"),
				selectedBadge,
				I18n.bundleProperty()
		));
	}

  private void buildButtons() {
    var saveButton = AppButtonView.builder(fxmlLoader)
        .variant(ButtonVariant.PRIMARY)
        .labelBinding(Bindings.createStringBinding(
            () -> selectedBadge.get() == null ? I18n.t("badges.action.create") : I18n.t("badges.action.update"),
            selectedBadge,
            I18n.bundleProperty()
        ))
        .onAction(this::onSaveBadge)
        .build();
    saveButton.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(saveButton, Priority.ALWAYS);
    saveButtonContainer.getChildren().setAll(saveButton);

    var clearButton = AppButtonView.builder(fxmlLoader)
        .variant(ButtonVariant.SECONDARY)
        .labelBinding(Localization.textBinding("badges.action.clear"))
        .onAction(this::onClearForm)
        .build();
    clearFormButtonContainer.getChildren().setAll(clearButton);

    var deleteButton = AppButtonView.builder(fxmlLoader)
        .variant(ButtonVariant.DANGER)
        .labelBinding(Localization.textBinding("badges.action.delete"))
        .onAction(this::onDeleteBadge)
        .build();
    deleteButton.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(deleteButton, Priority.ALWAYS);
    deleteButtonContainer.getChildren().setAll(deleteButton);
  }

	private void attachListeners() {
		selectedBadge.addListener((obs, oldValue, newValue) -> {
			refreshSelectionStyles();
			// updateGroupSelectState();
		});
		I18n.languageProperty().addListener((obs, oldValue, newValue) -> {
			badgesGrid.setEmptyText(I18n.t("badges.empty"));
			refreshGroupSelectEntries();
			renderFilteredBadges(activeSearchQuery);
		});
	}

	private void configureGrid() {
		badgesGrid.setPageSize(PAGE_SIZE);
		badgesGrid.setMaxColumns(1);
		badgesGrid.setMinCardWidth(1);
		badgesGrid.setGap(10);
		badgesGrid.setPageLoader(this::loadBadgesPage);
		badgesGrid.setCardFactory(this::buildBadgeCard);
	}

	private CardGridPane.PageResult<BadgeResponse> loadBadgesPage(int page, int pageSize) {
		List<BadgeResponse> filtered = filteredBadges(activeSearchQuery);

		int startIdx = (page - 1) * pageSize;
		int endIdx = Math.min(startIdx + pageSize, filtered.size());

		List<BadgeResponse> pageItems = startIdx < filtered.size()
				? filtered.subList(startIdx, endIdx)
				: List.of();

		int totalPages = (int) Math.ceil((double) filtered.size() / pageSize);
		return new CardGridPane.PageResult<>(
				pageItems,
				new Pagination(page, pageSize, null, totalPages)
		);
	}

    private void initializeBadgeGroups() {
        groupsById.clear();
        for (BadgeGroupType groupType : BadgeGroupType.values()) {
            String label = prettifyGroupName(groupType);
            groupsById.put(groupType.id(), new BadgeGroupResponse(
                    UUID.fromString(groupType.id()),
                    label,
                    label,
                    null,
                    null,
                    null
            ));
        }
        refreshGroupSelectEntries();
    }

    private void reloadBadges() {
        var result = badgeService.getBadges(new GetBadgesRequest(null));
        result.onSuccess(badges -> {
            allBadges.clear();
            allBadges.addAll(badges.stream()
                    .sorted(Comparator.comparing((BadgeResponse badge) -> badge.group().name())
                            .thenComparingInt(BadgeResponse::level)
                            .thenComparing(BadgeResponse::name, String.CASE_INSENSITIVE_ORDER))
                    .toList());
            renderFilteredBadges(activeSearchQuery);
        });
        result.onFailure(error -> {
            allBadges.clear();
            renderFilteredBadges(activeSearchQuery);
            errorHandler.handle(error);
        });
    }

    private void refreshGroupSelectEntries() {
        List<Entry<UUID>> entries = groupsById.values().stream()
                .sorted(Comparator.comparing(group -> Localization.localize(group.name(), group.nameSk()), String.CASE_INSENSITIVE_ORDER))
                .map(group -> Entry.builder(group.id(), Localization.localize(group.name(), group.nameSk())).build())
                .toList();

        Entry<UUID> selectedGroup = groupSelectModel == null ? null : groupSelectModel.getSelectedItem();
        UUID selectedGroupId = selectedGroup == null ? null : selectedGroup.getValue();

        groupSelectModel = createGroupSelectModel(entries);
        if (selectedGroupId != null) {
            groupSelectModel.setSelectedItem(findGroupEntry(selectedGroupId));
        }

        if (groupSelectView == null) {
            groupSelectView = createGroupSelectView(groupSelectModel);
            groupSelectContainer.getChildren().setAll(groupSelectView);
        } else {
            groupSelectView.update(groupSelectModel);
            groupSelectView.getComboBox().setMaxWidth(Double.MAX_VALUE);
            if (!groupSelectView.getComboBox().getStyleClass().contains("badges-group-select")) {
                groupSelectView.getComboBox().getStyleClass().add("badges-group-select");
            }
        }

        // updateGroupSelectState();
    }

    private List<Entry<UUID>> search(String searchQuery) {
        activeSearchQuery = searchQuery == null ? "" : searchQuery;
        badgesGrid.refresh();

        List<BadgeResponse> filtered = filteredBadges(searchQuery);
        return filtered.stream()
                .map(badge -> Entry.builder(badge.id(), badge.name()).build())
                .toList();
    }

    private void renderFilteredBadges(String searchQuery) {
        activeSearchQuery = searchQuery == null ? "" : searchQuery;
        badgesGrid.refresh();
    }

    private List<BadgeResponse> filteredBadges(String searchQuery) {
        String needle = normalizeNullable(searchQuery);
        String search = needle == null ? null : needle.toLowerCase(Locale.ROOT);

        List<BadgeResponse> filtered = new ArrayList<>();
        for (BadgeResponse badge : allBadges) {
            if (matchesSearch(badge, search)) {
                filtered.add(badge);
            }
        }
        return filtered;
    }

    private boolean matchesSearch(BadgeResponse badge, String search) {
        if (search == null) {
            return true;
        }

        return contains(badge.name(), search)
                || contains(badge.nameSk(), search)
                || contains(badge.description(), search)
                || contains(badge.descriptionSk(), search)
                || contains(groupName(badge.group().id()), search)
                || contains(Integer.toString(badge.level()), search)
                || contains(Integer.toString(badge.requiredValue()), search);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private Node buildBadgeCard(BadgeResponse badge) {
        ImageView preview = new ImageView();
        String imageUrl = resolveImageUrl(badge.image());
        if (imageUrl != null) {
            preview.setImage(new Image(imageUrl, false));
        }
        preview.setFitWidth(44);
        preview.setFitHeight(44);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);
        preview.setMouseTransparent(true);

        Label title = new Label(Localization.localize(badge.name(), badge.nameSk()));
        title.getStyleClass().add("badges-item-title");

        Label subtitle = new Label(buildSubtitle(badge));
        subtitle.getStyleClass().add("badges-item-subtitle");

        VBox textBox = new VBox(6, title, subtitle);
        textBox.getStyleClass().add("badges-item-text");
        VBox.setVgrow(textBox, Priority.ALWAYS);

        Label groupTag = new Label(groupName(badge.group().id()));
        groupTag.getStyleClass().add("badges-item-group");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, preview, textBox, spacer, groupTag);
        row.getStyleClass().add("badges-item-header");

        VBox card = new VBox(row);
        card.getStyleClass().add("badges-item");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setUserData(badge.id());
        card.setOnMouseClicked(event -> selectBadge(badge));

        badgeRowsById.put(badge.id(), card);
        return card;
    }

    private String buildSubtitle(BadgeResponse badge) {
        String description = Localization.localize(badge.description(), badge.descriptionSk());
        String levelPart = I18n.t("badges.item.level") + " " + badge.level();
        String requiredPart = I18n.t("badges.item.required") + " " + badge.requiredValue();

        if (description != null && !description.isBlank()) {
            return levelPart + " • " + requiredPart + " • " + description;
        }
        return levelPart + " • " + requiredPart;
    }

    private String resolveImageUrl(ImageResponse image) {
        if (image == null || image.url() == null) {
            return null;
        }

        String rawPath = image.url().toString().replace("\\", "/");
        String fileName = rawPath.substring(rawPath.lastIndexOf('/') + 1);

        URL classpathUrl = getClass().getResource("/com/triplify/ui/shared/component/badge/images/" + fileName);
        if (classpathUrl != null) {
            return classpathUrl.toExternalForm();
        }

        Path path = image.url();
        if (path.isAbsolute()) {
            return path.toUri().toString();
        }

        return rawPath;
    }

    private void selectBadge(BadgeResponse badge) {
        selectedBadge.set(badge);
        nameInput.setText(safe(badge.name()));
        nameSkInput.setText(safe(badge.nameSk()));
        descriptionInput.setText(safe(badge.description()));
        descriptionSkInput.setText(safe(badge.descriptionSk()));
        levelInput.setText(Integer.toString(badge.level()));
        requiredValueInput.setText(Integer.toString(badge.requiredValue()));
        imagePathInput.setText("");
        groupSelectModel.setSelectedItem(findGroupEntry(UUID.fromString(badge.group().id())));
        clearFieldErrors();
    }

    private void clearSelection() {
        selectedBadge.set(null);
        nameInput.setText("");
        nameSkInput.setText("");
        descriptionInput.setText("");
        descriptionSkInput.setText("");
        levelInput.setText("");
        requiredValueInput.setText("");
        imagePathInput.setText("");
        groupSelectModel.setSelectedItem(null);
        clearFieldErrors();
    }

    private void refreshSelectionStyles() {
        BadgeResponse current = selectedBadge.get();
        UUID selectedId = current == null ? null : current.id();

        badgeRowsById.forEach((badgeId, row) -> {
            if (badgeId.equals(selectedId)) {
                if (!row.getStyleClass().contains("badges-item-selected")) {
                    row.getStyleClass().add("badges-item-selected");
                }
            } else {
                row.getStyleClass().remove("badges-item-selected");
            }
        });
    }


//    private void updateGroupSelectState() {
//        if (groupSelectView == null) {
//            return;
//        }
//        groupSelectView.getComboBox().setDisable(selectedBadge.get() != null);
//    }

    private Entry<UUID> findGroupEntry(UUID groupId) {
        if (groupId == null || groupSelectModel == null) {
            return null;
        }
        for (Entry<UUID> entry : groupSelectModel.getItems()) {
            if (groupId.equals(entry.getValue())) {
                return entry;
            }
        }
        return null;
    }

    private UUID selectedGroupId() {
        Entry<UUID> selected = groupSelectModel.getSelectedItem();
        return selected == null ? null : selected.getValue();
    }

    private String groupName(String groupId) {
        BadgeGroupResponse group = groupsById.get(groupId);
        if (group == null) {
            return groupId;
        }
        return Localization.localize(group.name(), group.nameSk());
    }

    private String prettifyGroupName(BadgeGroupType groupType) {
        return groupType.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private Select<UUID> createGroupSelectModel(List<Entry<UUID>> entries) {
        return Select.<UUID>builder()
                .placeholder(I18n.t("badges.select.group.placeholder"))
                .variant(FieldVariant.GHOST)
                .items(entries)
                .build();
    }

    private SelectView<UUID> createGroupSelectView(Select<UUID> model) {
        SelectView<UUID> view = new SelectView<>();
        view.update(model);
        view.setMaxWidth(Double.MAX_VALUE);
        view.getComboBox().setMaxWidth(Double.MAX_VALUE);
        view.getComboBox().getStyleClass().add("badges-group-select");
        return view;
    }

    private Integer parseInteger(String raw, InputItem input, String errorKey) {
        String normalized = normalize(raw);
        if (normalized.isBlank()) {
            input.showError(I18n.t("badges.validation.numberRequired"));
            return null;
        }

        try {
            int value = Integer.parseInt(normalized);
            if (value < 0) {
                input.showError(I18n.t(errorKey));
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            input.showError(I18n.t("badges.validation.invalidNumber"));
            return null;
        }
    }

    private Path parseImagePath(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return null;
        }
        return Path.of(normalized);
    }

    private Map<String, Consumer<String>> buildFieldHandlers() {
        return Map.of(
                "name", message -> nameInput.showError(message),
                "nameSk", message -> nameSkInput.showError(message),
                "description", message -> descriptionInput.showError(message),
                "descriptionSk", message -> descriptionSkInput.showError(message),
                "level", message -> levelInput.showError(message),
                "requiredValue", message -> requiredValueInput.showError(message)
        );
    }

    private void clearFieldErrors() {
        nameInput.clearError();
        nameSkInput.clearError();
        descriptionInput.clearError();
        descriptionSkInput.clearError();
        levelInput.clearError();
        requiredValueInput.clearError();
        imagePathInput.clearError();
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
}
