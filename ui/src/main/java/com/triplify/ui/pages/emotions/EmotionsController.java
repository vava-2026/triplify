package com.triplify.ui.pages.emotions;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.emotion.EmotionService;
import com.triplify.application.usecase.emotion.dto.CreateEmotionRequest;
import com.triplify.application.usecase.emotion.dto.DeleteEmotionRequest;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.emotion.dto.UpdateEmotionRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.input_item.EmojiInputItem;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.EmojiUtil;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.*;
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

	@FXML private VBox saveButtonContainer;
	@FXML private VBox clearFormButtonContainer;
	@FXML private VBox deleteButtonContainer;

	@FXML private CardGridPane<EmotionResponse> emotionsGrid;

	@Inject private EmotionService emotionService;
	@Inject private ToastService toast;
	@Inject private ErrorHandler errorHandler;
	@Inject private FxmlLoaderHelper fxmlLoader;

	private InputItem nameInput;
	private InputItem nameSkInput;
	private EmojiInputItem emojiInput;
	private SearchView<String> searchView;

	private final ObjectProperty<EmotionResponse> selectedEmotion = new SimpleObjectProperty<>();
	private final Map<UUID, Region> emotionRowsById = new HashMap<>();
	private final List<EmotionResponse> allEmotions = new ArrayList<>();
	private String activeSearchQuery = "";

	@FXML
	public void initialize() {
		initializeInputs();
		buildButtons();
		configureGrid();
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
					EditorUtils.normalize(nameInput.getText()),
					EditorUtils.normalize(nameSkInput.getText()),
					EditorUtils.normalize(emojiInput.getText())
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
				EditorUtils.normalize(nameInput.getText()),
				EditorUtils.normalize(nameSkInput.getText()),
				EditorUtils.normalize(emojiInput.getText())
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


	private void initializeInputs() {
		nameInput = new InputItem("emotions.input.name", FieldVariant.GHOST);
		nameSkInput = new InputItem("emotions.input.nameSk", FieldVariant.GHOST);
		emojiInput = new EmojiInputItem("emotions.input.emoji", FieldVariant.GHOST);
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
        emotionsGrid.refresh();

        List<EmotionResponse> filtered = filteredEmotions(searchQuery);
        return filtered.stream()
                .map(category -> Entry.builder(category.name(), category.name()).build())
                .toList();
	}

	private void bindLocalizedText() {
		Localization.bindText(formSectionTitleLabel.textProperty(), "emotions.section.form");
		Localization.bindText(listSectionTitleLabel.textProperty(), "emotions.section.list");
		Localization.bindText(nameLabel.textProperty(), "emotions.field.name");
		Localization.bindText(nameSkLabel.textProperty(), "emotions.field.nameSk");
		Localization.bindText(emojiLabel.textProperty(), "emotions.field.emoji");
		emotionsGrid.setEmptyText(I18n.t("emotions.empty"));

		modeBadgeLabel.textProperty().bind(Bindings.createStringBinding(
				() -> selectedEmotion.get() == null
						? I18n.t("emotions.mode.create")
						: I18n.t("emotions.mode.edit"),
				selectedEmotion,
				I18n.bundleProperty()
		));
	}

	private void buildButtons() {
		var saveButton = AppButtonView.builder(fxmlLoader)
				.variant(ButtonVariant.PRIMARY)
				.labelBinding(Bindings.createStringBinding(
						() -> selectedEmotion.get() == null
								? I18n.t("emotions.action.create")
								: I18n.t("emotions.action.update"),
						selectedEmotion,
						I18n.bundleProperty()
				))
				.onAction(this::onSaveEmotion)
				.build();
		saveButton.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(saveButton, Priority.ALWAYS);
		saveButtonContainer.getChildren().setAll(saveButton);

		var clearButton = AppButtonView.builder(fxmlLoader)
				.variant(ButtonVariant.SECONDARY)
				.labelBinding(Localization.textBinding("emotions.action.clear"))
				.onAction(this::onClearForm)
				.build();
		clearFormButtonContainer.getChildren().setAll(clearButton);

		var deleteButton = AppButtonView.builder(fxmlLoader)
				.variant(ButtonVariant.DANGER)
				.labelBinding(Localization.textBinding("emotions.action.delete"))
				.onAction(this::onDeleteEmotion)
				.build();
		deleteButton.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(deleteButton, Priority.ALWAYS);
		deleteButtonContainer.getChildren().setAll(deleteButton);
	}

	private void attachListeners() {
		selectedEmotion.addListener((obs, oldValue, newValue) -> refreshSelectionStyles());
		I18n.languageProperty().addListener((obs, oldValue, newValue) -> {
			emotionsGrid.setEmptyText(I18n.t("emotions.empty"));
			renderFilteredEmotions(activeSearchQuery);
		});
	}

	private void configureGrid() {
		emotionsGrid.setPageSize(PAGE_SIZE);
		emotionsGrid.setMaxColumns(1);
		emotionsGrid.setMinCardWidth(1);
		emotionsGrid.setGap(10);
		emotionsGrid.setPageLoader(this::loadEmotionsPage);
		emotionsGrid.setCardFactory(this::buildEmotionCard);
	}

	private CardGridPane.PageResult<EmotionResponse> loadEmotionsPage(int page, int pageSize) {
        List<EmotionResponse> filtered = filteredEmotions(activeSearchQuery);

        int startIdx = (page - 1) * pageSize;
        int endIdx = Math.min(startIdx + pageSize, filtered.size());

        List<EmotionResponse> pageItems = startIdx < filtered.size()
                ? filtered.subList(startIdx, endIdx)
                : List.of();

        int totalPages = (int) Math.ceil((double) filtered.size() / pageSize);
        return new CardGridPane.PageResult<>(
                pageItems,
                new Pagination(page, pageSize, null, totalPages)
        );
	}

	private void reloadEmotions() {
        var result = emotionService.getAllEmotions();
            result.onSuccess(emotions -> {
            allEmotions.clear();
            allEmotions.addAll(emotions.stream()
                    .sorted(Comparator.comparing(EmotionResponse::name, String.CASE_INSENSITIVE_ORDER))
                    .toList());
            renderFilteredEmotions(activeSearchQuery);
        });
            result.onFailure(error -> {
                allEmotions.clear();
            renderFilteredEmotions(activeSearchQuery);
            errorHandler.handle(error);
        });
	}

	private void renderFilteredEmotions(String searchQuery) {
		activeSearchQuery = searchQuery == null ? "" : searchQuery;
		emotionsGrid.refresh();
	}

	private List<EmotionResponse> filteredEmotions(String searchQuery) {
		String needle = EditorUtils.normalizeNullable(searchQuery);
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

	private ImageView createEmojiView(String emojiUnicode) {
		Image image = EmojiUtil.toImage(emojiUnicode, 20);
		ImageView view = new ImageView(image);
		view.getStyleClass().add("emotions-item-emoji");
		view.setFitWidth(20);
		view.setFitHeight(20);
		view.setPreserveRatio(true);
		view.setSmooth(true);
		return view;
	}

	private Node buildEmotionCard(EmotionResponse emotion) {
		ImageView emoji = createEmojiView(emotion.emojiUnicode());

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
		card.setMaxWidth(Double.MAX_VALUE);
		card.setUserData(emotion.id());
		card.setOnMouseClicked(event -> selectEmotion(emotion));

		emotionRowsById.put(emotion.id(), card);
		return card;
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
}
