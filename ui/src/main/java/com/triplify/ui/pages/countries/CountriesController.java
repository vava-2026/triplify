package com.triplify.ui.pages.countries;

import com.google.inject.Inject;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.country.dto.AddCountryRequest;
import com.triplify.application.usecase.country.dto.BanCountryRequest;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.country.dto.DeleteCountryRequest;
import com.triplify.application.usecase.country.dto.GetCountriesRequest;
import com.triplify.application.usecase.country.dto.UnbanCountryRequest;
import com.triplify.application.usecase.country.dto.UpdateCountryRequest;
import com.triplify.domain.filter.CountryFilter;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.EmojiUtil;
import com.triplify.ui.shared.util.Localization;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.*;
import java.util.function.Consumer;

public class CountriesController extends SimpleLifecycleAwareController {

	private static final int PAGE_SIZE = 8;

    @FXML private Label formSectionTitleLabel;
    // @FXML private Label listSectionTitleLabel;
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
	@FXML private Button toggleAvailabilityButton;
	@FXML private Button deleteButton;
    @FXML private Button loadMoreButton;

	@FXML private VBox countriesListContainer;
	@FXML private Label emptyStateLabel;

	@Inject private CountryService countryService;
	@Inject private ToastService toast;
	@Inject private ErrorHandler errorHandler;

	private InputItem nameInput;
	private InputItem nameSkInput;
	private InputItem emojiInput;
    private SearchView<String> searchView;

	private final ObjectProperty<CountryResponse> selectedCountry = new SimpleObjectProperty<>();
	private final Map<UUID, Region> countryRowsById = new HashMap<>();

	private int nextPage;
	private boolean hasNextPage;
	private boolean loading;

	@FXML
	public void initialize() {
		initializeInputs();
		bindLocalizedText();
		// bindState();
		attachListeners();
		reloadCountries();
	}

	@Override
	public void onLifecycleShow() {
		reloadCountries();
	}

	private List<Entry<String>> search(String searchQuery) {
        var request = new GetCountriesRequest(
                new PageRequest(0, PAGE_SIZE),
                new CountryFilter(normalizeNullable(searchQuery), CountryFilter.CountryBanFilter.ALL, false)
        );

        var result = countryService.getCountries(request);
        if (result.isSuccess())
        {
            var page = result.getValue();

            // TODO: this is just like reload countries
            countriesListContainer.getChildren().clear();
            countryRowsById.clear();
            nextPage = 0;
            hasNextPage = true;
            loadMoreButton.setVisible(false);
            loadMoreButton.setManaged(false);
            
            loadNextPage(searchQuery);

            return page.items().stream()
                    .map(country -> Entry.builder(country.name(), country.name()).build())
                    .toList();
        }
        else {
            errorHandler.handle(result.getError());
            return Collections.emptyList();
        }
	}

	@FXML
	private void onSaveCountry() {
		clearFieldErrors();

		boolean creating = selectedCountry.get() == null;
		var fieldHandlers = buildFieldHandlers();

		if (creating) {
			AddCountryRequest request = new AddCountryRequest(
					normalize(nameInput.getText()),
					normalize(nameSkInput.getText()),
					normalize(emojiInput.getText())
			);
			var result = countryService.addCountry(request);
			result.onSuccess(country -> {
				selectedCountry.set(country);
				toast.success(
						I18n.t("countries.toast.created.title"),
						I18n.t("countries.toast.created.body")
				);
				reloadCountries();
			});
			result.onFailure(error -> errorHandler.handle(error, fieldHandlers));
			return;
		}

		CountryResponse existing = selectedCountry.get();
		UpdateCountryRequest request = new UpdateCountryRequest(
				existing.id(),
				normalize(nameInput.getText()),
				normalize(nameSkInput.getText()),
				normalize(emojiInput.getText())
		);
		var result = countryService.updateCountry(request);
		result.onSuccess(country -> {
			selectedCountry.set(country);
			toast.success(
					I18n.t("countries.toast.updated.title"),
					I18n.t("countries.toast.updated.body")
			);
			reloadCountries();
		});
		result.onFailure(error -> errorHandler.handle(error, fieldHandlers));
	}

	@FXML
	private void onClearForm() {
		clearSelection();
	}

	@FXML
	private void onToggleAvailability() {
		CountryResponse current = selectedCountry.get();
		if (current == null) {
			toast.info(I18n.t("countries.toast.selectFirst"));
			return;
		}

		if (current.isAvailable()) {
			var result = countryService.banCountry(new BanCountryRequest(current.id()));
			result.onSuccess(country -> {
				selectedCountry.set(country);
				toast.success(I18n.t("countries.toast.banned.title"), I18n.t("countries.toast.banned.body"));
				reloadCountries();
			});
			result.onFailure(errorHandler::handle);
			return;
		}

		var result = countryService.unbanCountry(new UnbanCountryRequest(current.id()));
		result.onSuccess(country -> {
			selectedCountry.set(country);
			toast.success(I18n.t("countries.toast.unbanned.title"), I18n.t("countries.toast.unbanned.body"));
			reloadCountries();
		});
		result.onFailure(errorHandler::handle);
	}

	@FXML
	private void onDeleteCountry() {
		CountryResponse current = selectedCountry.get();
		if (current == null) {
			toast.info(I18n.t("countries.toast.selectFirst"));
			return;
		}

		var result = countryService.deleteCountry(new DeleteCountryRequest(current.id()));
		result.onSuccess(ignored -> {
			toast.success(I18n.t("countries.toast.deleted.title"), I18n.t("countries.toast.deleted.body"));
			clearSelection();
			reloadCountries();
		});
		result.onFailure(errorHandler::handle);
	}

	 @FXML
	 private void onLoadMore() {
		loadNextPage(null);
	}

	private void initializeInputs() {
		nameInput = new InputItem("countries.input.name", FieldVariant.GHOST);
		nameSkInput = new InputItem("countries.input.nameSk", FieldVariant.GHOST);
		emojiInput = new InputItem("countries.input.emoji", FieldVariant.GHOST);
        searchView = new SearchView<String>(Search.builder(this::search).placeholderKey("countries.search.placeholder").variant(FieldVariant.OUTLINED).build());

		 nameInputContainer.getChildren().setAll(nameInput);
		 nameSkInputContainer.getChildren().setAll(nameSkInput);
		 emojiInputContainer.getChildren().setAll(emojiInput);
         searchContainer.getChildren().setAll(searchView);
	 }

	private void bindLocalizedText() {

		Localization.bindText(formSectionTitleLabel.textProperty(), "countries.section.form");
		// Localization.bindText(listSectionTitleLabel.textProperty(), "countries.section.list");
		Localization.bindText(nameLabel.textProperty(), "countries.field.name");
		Localization.bindText(nameSkLabel.textProperty(), "countries.field.nameSk");
		Localization.bindText(emojiLabel.textProperty(), "countries.field.emoji");
		Localization.bindText(clearFormButton.textProperty(), "countries.action.clear");
		Localization.bindText(deleteButton.textProperty(), "countries.action.delete");
		Localization.bindText(loadMoreButton.textProperty(), "countries.action.loadMore");
//		Localization.bindText(emptyStateLabel.textProperty(), "countries.empty");
//
		saveButton.textProperty().bind(Bindings.createStringBinding(
				() -> selectedCountry.get() == null
						? I18n.t("countries.action.create")
						: I18n.t("countries.action.update"),
				selectedCountry,
				I18n.bundleProperty()
		));

		modeBadgeLabel.textProperty().bind(Bindings.createStringBinding(
				() -> selectedCountry.get() == null
						? I18n.t("countries.mode.create")
						: I18n.t("countries.mode.edit"),
				selectedCountry,
				I18n.bundleProperty()
		));

		toggleAvailabilityButton.textProperty().bind(Bindings.createStringBinding(
				() -> {
					CountryResponse current = selectedCountry.get();
					if (current == null || current.isAvailable()) {
						return I18n.t("countries.action.ban");
					}
					return I18n.t("countries.action.unban");
				},
				selectedCountry,
				I18n.bundleProperty()
		));
	}

//	private void bindState() {
//		toggleAvailabilityButton.disableProperty().bind(selectedCountry.isNull());
//		deleteButton.disableProperty().bind(selectedCountry.isNull());
//	}

	private void attachListeners() {
		selectedCountry.addListener((obs, oldValue, newValue) -> refreshSelectionStyles());
		I18n.languageProperty().addListener((obs, oldValue, newValue) -> reloadCountries());
	}

	private void reloadCountries() {
		countriesListContainer.getChildren().clear();
		countryRowsById.clear();
		nextPage = 0;
		hasNextPage = true;
		loadMoreButton.setVisible(false);
		loadMoreButton.setManaged(false);
		loadNextPage(null);
	}

	private void loadNextPage(String searchQuery) {
		if (loading || !hasNextPage) {
			return;
		}

		loading = true;
		loadMoreButton.setDisable(true);

		var request = new GetCountriesRequest(
				new PageRequest(nextPage, PAGE_SIZE),
				new CountryFilter(normalizeNullable(searchQuery), CountryFilter.CountryBanFilter.ALL, false)
		);

		var result = countryService.getCountries(request);
		result.onSuccess(page -> {
			page.items().forEach(this::addCountryCard);
			nextPage = page.page() + 1;
			hasNextPage = page.hasNext();
			refreshEmptyState();
			refreshLoadMoreVisibility();
			refreshSelectionStyles();
		});
		result.onFailure(error -> {
			hasNextPage = false;
			refreshEmptyState();
			refreshLoadMoreVisibility();
			errorHandler.handle(error);
		});

		loading = false;
		loadMoreButton.setDisable(false);
	}

	private void addCountryCard(CountryResponse country) {
		ImageView emoji = createEmojiView(country.emojiUnicode());

		Label title = new Label(Localization.localize(country.name(), country.nameSk()));
		title.getStyleClass().add("countries-item-title");

		Label subtitle = new Label(country.name() + " / " + country.nameSk());
		subtitle.getStyleClass().add("countries-item-subtitle");

		VBox textBox = new VBox(2, title, subtitle);
		textBox.getStyleClass().add("countries-item-text");

		Label status = new Label(country.isAvailable()
				? I18n.t("countries.status.active")
				: I18n.t("countries.status.banned"));
		status.getStyleClass().addAll(
				"countries-item-status",
				country.isAvailable() ? "countries-item-status-active" : "countries-item-status-banned"
		);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox header = new HBox(10, emoji, textBox, spacer, status);
		header.getStyleClass().add("countries-item-header");

		VBox card = new VBox(header);
		card.getStyleClass().add("countries-item");
		card.setUserData(country.id());
		card.setOnMouseClicked(event -> selectCountry(country));

		countriesListContainer.getChildren().add(card);
		countryRowsById.put(country.id(), card);
	}

	private ImageView createEmojiView(String emojiUnicode) {
		Image image = EmojiUtil.toImage(emojiUnicode, 20);
		ImageView emoji = new ImageView(image);
		emoji.getStyleClass().add("countries-item-emoji");
		emoji.setFitWidth(20);
		emoji.setFitHeight(20);
		emoji.setPreserveRatio(true);
		emoji.setSmooth(true);
		return emoji;
	}

	private void refreshSelectionStyles() {
		CountryResponse current = selectedCountry.get();
		UUID selectedId = current == null ? null : current.id();

		countryRowsById.forEach((countryId, row) -> {
			if (countryId.equals(selectedId)) {
				if (!row.getStyleClass().contains("countries-item-selected")) {
					row.getStyleClass().add("countries-item-selected");
				}
			} else {
				row.getStyleClass().remove("countries-item-selected");
			}
		});
	}

	private void refreshEmptyState() {
		boolean isEmpty = countriesListContainer.getChildren().isEmpty();
		emptyStateLabel.setVisible(isEmpty);
		emptyStateLabel.setManaged(isEmpty);
	}

	private void refreshLoadMoreVisibility() {
		loadMoreButton.setVisible(hasNextPage);
		loadMoreButton.setManaged(hasNextPage);
	}

	private void selectCountry(CountryResponse country) {
		selectedCountry.set(country);
		nameInput.setText(country.name());
		nameSkInput.setText(country.nameSk());
		emojiInput.setText(country.emojiUnicode());
		clearFieldErrors();
	}

	private void clearSelection() {
		selectedCountry.set(null);
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
