package com.triplify.ui.pages.emotions;

import com.google.inject.Inject;
import com.triplify.application.usecase.emotion.EmotionService;
import com.triplify.application.usecase.emotion.dto.CreateEmotionRequest;
import com.triplify.application.usecase.emotion.dto.DeleteEmotionRequest;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.emotion.dto.GetAllEmotionsRequest;
import com.triplify.application.usecase.emotion.dto.UpdateEmotionRequest;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
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
import java.util.UUID;
import java.util.function.Consumer;

public class EmotionsController extends SimpleLifecycleAwareController {

	private static final int PAGE_SIZE = 16;

	@FXML private Label formSectionTitleLabel;
	@FXML private Label listSectionTitleLabel;
	@FXML private Label modeBadgeLabel;

	@FXML private Label nameLabel;
	@FXML private Label nameSkLabel;
	@FXML private Label emojiLabel;

	@FXML private VBox nameInputContainer;
	@FXML private VBox nameSkInputContainer;
	@FXML private VBox emojiInputContainer;
	@FXML private HBox searchContainer;

	@FXML private Button saveButton;
	@FXML private Button clearFormButton;
	@FXML private Button deleteButton;
	@FXML private Button loadMoreButton;

	@FXML private VBox emotionsListContainer;
	@FXML private Label emptyStateLabel;

	@Inject private EmotionService emotionService;
	@Inject private ToastService toast;
	@Inject private ErrorHandler errorHandler;

	private InputItem nameInput;
	private InputItem nameSkInput;
	private InputItem emojiInput;
	private SearchView<String> searchView;

	private final ObjectProperty<EmotionResponse> selectedEmotion = new SimpleObjectProperty<>();
	private final Map<UUID, Region> emotionRowsById = new HashMap<>();
	private final List<EmotionResponse> allEmotions = new ArrayList<>();

	private int nextPage;
	private boolean hasNextPage;
	private boolean loading;
	private String activeSearchQuery = "";

	@FXML
	public void initialize() {
		initializeInputs();
		bindLocalizedText();
		attachListeners();
		reloadEmotions();
	}

	@Override
	public void onLifecycleShow() {
		reloadEmotions();
	}

	@FXML
	private void onSaveEmotion() {
		clearFieldErrors();

		boolean creating = selectedEmotion.get() == null;
		var fieldHandlers = buildFieldHandlers();

		if (creating) {
			CreateEmotionRequest request = new CreateEmotionRequest(
					normalize(nameInput.getText()),
					normalize(nameSkInput.getText()),
					normalize(emojiInput.getText())
			);
			var result = emotionService.createEmotion(request);
			result.onSuccess(emotion -> {
				selectedEmotion.set(emotion);
				toast.success(
						I18n.t("emotions.toast.created.title"),
						I18n.t("emotions.toast.created.body")
				);
				reloadEmotions();
			});
			result.onFailure(error -> errorHandler.handle(error, fieldHandlers));
			return;
		}

		EmotionResponse existing = selectedEmotion.get();
		UpdateEmotionRequest request = new UpdateEmotionRequest(
				existing.id(),
				normalize(nameInput.getText()),
				normalize(nameSkInput.getText()),
				normalize(emojiInput.getText())
		);
		var result = emotionService.updateEmotion(request);
		result.onSuccess(emotion -> {
			selectedEmotion.set(emotion);
			toast.success(
					I18n.t("emotions.toast.updated.title"),
					I18n.t("emotions.toast.updated.body")
			);
			reloadEmotions();
		});
		result.onFailure(error -> errorHandler.handle(error, fieldHandlers));
	}

	@FXML
	private void onClearForm() {
		clearSelection();
	}

	@FXML
	private void onDeleteEmotion() {
		EmotionResponse current = selectedEmotion.get();
		if (current == null) {
			toast.info(I18n.t("emotions.toast.selectFirst"));
			return;
		}

		var result = emotionService.deleteEmotion(new DeleteEmotionRequest(current.id()));
		result.onSuccess(ignored -> {
			toast.success(I18n.t("emotions.toast.deleted.title"), I18n.t("emotions.toast.deleted.body"));
			clearSelection();
			reloadEmotions();
		});
		result.onFailure(errorHandler::handle);
	}

	@FXML
	private void onLoadMore() {
		loadNextPage();
	}

	private void initializeInputs() {
		nameInput = new InputItem("emotions.input.name", FieldVariant.GHOST);
		nameSkInput = new InputItem("emotions.input.nameSk", FieldVariant.GHOST);
		emojiInput = new InputItem("emotions.input.emoji", FieldVariant.GHOST);
		searchView = new SearchView<>(Search.<String>builder(this::search)
				.placeholderKey("emotions.search.placeholder")
				.variant(FieldVariant.OUTLINED)
				.build());

		nameInputContainer.getChildren().setAll(nameInput);
		nameSkInputContainer.getChildren().setAll(nameSkInput);
		emojiInputContainer.getChildren().setAll(emojiInput);
		searchContainer.getChildren().setAll(searchView);
	}

	private List<Entry<String>> search(String searchQuery) {
		activeSearchQuery = searchQuery == null ? "" : searchQuery;
		List<EmotionResponse> filtered = filteredEmotions(searchQuery);
		renderEmotions(filtered);

		return filtered.stream()
				.map(emotion -> Entry.builder(emotion.name(), emotion.name()).build())
				.toList();
	}

	private void bindLocalizedText() {
		Localization.bindText(formSectionTitleLabel.textProperty(), "emotions.section.form");
		Localization.bindText(listSectionTitleLabel.textProperty(), "emotions.section.list");
		Localization.bindText(nameLabel.textProperty(), "emotions.field.name");
		Localization.bindText(nameSkLabel.textProperty(), "emotions.field.nameSk");
		Localization.bindText(emojiLabel.textProperty(), "emotions.field.emoji");
		Localization.bindText(clearFormButton.textProperty(), "emotions.action.clear");
		Localization.bindText(deleteButton.textProperty(), "emotions.action.delete");
		Localization.bindText(loadMoreButton.textProperty(), "emotions.action.loadMore");
		Localization.bindText(emptyStateLabel.textProperty(), "emotions.empty");

		saveButton.textProperty().bind(Bindings.createStringBinding(
				() -> selectedEmotion.get() == null
						? I18n.t("emotions.action.create")
						: I18n.t("emotions.action.update"),
				selectedEmotion,
				I18n.bundleProperty()
		));

		modeBadgeLabel.textProperty().bind(Bindings.createStringBinding(
				() -> selectedEmotion.get() == null
						? I18n.t("emotions.mode.create")
						: I18n.t("emotions.mode.edit"),
				selectedEmotion,
				I18n.bundleProperty()
		));
	}

	private void attachListeners() {
		selectedEmotion.addListener((obs, oldValue, newValue) -> refreshSelectionStyles());
		I18n.languageProperty().addListener((obs, oldValue, newValue) -> renderFilteredEmotions(activeSearchQuery));
	}

	private void reloadEmotions() {
		allEmotions.clear();
		emotionsListContainer.getChildren().clear();
		emotionRowsById.clear();
		nextPage = 0;
		hasNextPage = true;
		loadMoreButton.setVisible(false);
		loadMoreButton.setManaged(false);
		loadNextPage();
	}

	private void loadNextPage() {
		if (loading || !hasNextPage) {
			return;
		}

		loading = true;
		loadMoreButton.setDisable(true);

		var result = emotionService.getAllEmotions(new GetAllEmotionsRequest(new PageRequest(nextPage, PAGE_SIZE)));
		result.onSuccess(page -> {
			allEmotions.addAll(page.items());
			allEmotions.sort(Comparator.comparing(EmotionResponse::name, String.CASE_INSENSITIVE_ORDER));
			nextPage = page.page() + 1;
			hasNextPage = page.hasNext();
			renderFilteredEmotions(activeSearchQuery);
			refreshLoadMoreVisibility();
		});
		result.onFailure(error -> {
			hasNextPage = false;
			refreshLoadMoreVisibility();
			errorHandler.handle(error);
		});

		loading = false;
		loadMoreButton.setDisable(false);
	}

	private void renderFilteredEmotions(String searchQuery) {
		activeSearchQuery = searchQuery == null ? "" : searchQuery;
		renderEmotions(filteredEmotions(searchQuery));
	}

	private List<EmotionResponse> filteredEmotions(String searchQuery) {
		String needle = normalizeNullable(searchQuery);
		String search = needle == null ? null : needle.toLowerCase(Locale.ROOT);

		List<EmotionResponse> filtered = new ArrayList<>();
		for (EmotionResponse emotion : allEmotions) {
			if (matchesSearch(emotion, search)) {
				filtered.add(emotion);
			}
		}
		return filtered;
	}

	private boolean matchesSearch(EmotionResponse emotion, String search) {
		if (search == null) {
			return true;
		}
		return contains(emotion.name(), search)
				|| contains(emotion.nameSk(), search)
				|| contains(emotion.emojiUnicode(), search);
	}

	private boolean contains(String value, String needle) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
	}

	private void renderEmotions(List<EmotionResponse> emotions) {
		emotionsListContainer.getChildren().clear();
		emotionRowsById.clear();

		for (EmotionResponse emotion : emotions) {
			addEmotionCard(emotion);
		}

		refreshEmptyState();
		refreshSelectionStyles();
	}

	private void addEmotionCard(EmotionResponse emotion) {
		Label emoji = new Label(emotion.emojiUnicode());
		emoji.getStyleClass().add("emotions-item-emoji");

		Label title = new Label(Localization.localize(emotion.name(), emotion.nameSk()));
		title.getStyleClass().add("emotions-item-title");

		Label subtitle = new Label(emotion.name() + " / " + emotion.nameSk());
		subtitle.getStyleClass().add("emotions-item-subtitle");

		VBox textBox = new VBox(2, title, subtitle);
		textBox.getStyleClass().add("emotions-item-text");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox header = new HBox(10, emoji, textBox, spacer);
		header.getStyleClass().add("emotions-item-header");

		VBox card = new VBox(header);
		card.getStyleClass().add("emotions-item");
		card.setUserData(emotion.id());
		card.setOnMouseClicked(event -> selectEmotion(emotion));

		emotionsListContainer.getChildren().add(card);
		emotionRowsById.put(emotion.id(), card);
	}

	private void refreshSelectionStyles() {
		EmotionResponse current = selectedEmotion.get();
		UUID selectedId = current == null ? null : current.id();

		emotionRowsById.forEach((emotionId, row) -> {
			if (emotionId.equals(selectedId)) {
				if (!row.getStyleClass().contains("emotions-item-selected")) {
					row.getStyleClass().add("emotions-item-selected");
				}
			} else {
				row.getStyleClass().remove("emotions-item-selected");
			}
		});
	}

	private void refreshEmptyState() {
		boolean isEmpty = emotionsListContainer.getChildren().isEmpty();
		emptyStateLabel.setVisible(isEmpty);
		emptyStateLabel.setManaged(isEmpty);
	}

	private void refreshLoadMoreVisibility() {
		loadMoreButton.setVisible(hasNextPage);
		loadMoreButton.setManaged(hasNextPage);
	}

	private void selectEmotion(EmotionResponse emotion) {
		selectedEmotion.set(emotion);
		nameInput.setText(emotion.name());
		nameSkInput.setText(emotion.nameSk());
		emojiInput.setText(emotion.emojiUnicode());
		clearFieldErrors();
	}

	private void clearSelection() {
		selectedEmotion.set(null);
		nameInput.setText("");
		nameSkInput.setText("");
		emojiInput.setText("");
		clearFieldErrors();
	}

	private void clearFieldErrors() {
		nameInput.clearError();
		nameSkInput.clearError();
		emojiInput.clearError();
	}

	private Map<String, Consumer<String>> buildFieldHandlers() {
		return Map.of(
				"name", message -> nameInput.showError(message),
				"nameSk", message -> nameSkInput.showError(message),
				"emojiUnicode", message -> emojiInput.showError(message)
		);
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
