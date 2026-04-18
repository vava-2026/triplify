package com.triplify.ui.pages.trips;

import com.google.inject.Inject;
import com.triplify.application.usecase.category.CategoryService;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.response.TripStatus;
import com.triplify.application.usecase.tag.TagService;
import com.triplify.application.usecase.tag.dto.CreateTagRequest;
import com.triplify.application.usecase.tag.dto.GetTagsRequest;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.AddTripRequest;
import com.triplify.application.usecase.trip.dto.GetTripByIdRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.trip.dto.UpdateTripRequest;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.GetRoutesRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.tripplace.TripPlaceService;
import com.triplify.application.usecase.tripplace.dto.AddTripPlaceRequest;
import com.triplify.application.usecase.tripplace.dto.DeleteTripPlaceRequest;
import com.triplify.application.usecase.tripplace.dto.GetTripPlacesRequest;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.application.usecase.triproute.TripRouteService;
import com.triplify.application.usecase.triproute.dto.AddTripRouteRequest;
import com.triplify.application.usecase.triproute.dto.DeleteTripRouteRequest;
import com.triplify.application.usecase.triproute.dto.GetTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.error.AppError;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.result.Result;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.action_buttons.view.EditorActionButtonsView;
import com.triplify.ui.shared.component.categories.model.Categories;
import com.triplify.ui.shared.component.countries.model.Countries;
import com.triplify.ui.shared.component.countries.view.CountriesView;
import com.triplify.ui.shared.component.date_picker.DatePickerItem;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.media_card.view.EditorMediaCardView;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.model.SearchDisplayMode;
import com.triplify.ui.shared.component.search.model.SearchSize;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.tag_picker.TagPickerItem;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.upload_panel.view.ImageUploadPanelView;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.storage.EditorDraftStorage;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.Localization;
import com.triplify.application.model.ColorTheme;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AddTripController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(AddTripController.class);

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";

    @FXML private VBox contentContainer;
    @FXML private FlowPane contentFlow;

    @FXML private SectionHeaderView generalSectionHeader;
    @FXML private Label tripTitleLabel;
    @FXML private Label countriesLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label descriptionLabel;
    @FXML private SectionHeaderView routesSectionHeader;
    @FXML private SectionHeaderView placesSectionHeader;
    @FXML private SectionHeaderView metaSectionHeader;
    @FXML private Label categoryLabel;
    @FXML private Label tagsLabel;

    @FXML private VBox titleInputContainer;
    @FXML private FlowPane countriesFlow;
    @FXML private VBox countrySelectContainer;
    @FXML private VBox startDateContainer;
    @FXML private VBox endDateContainer;
    @FXML private VBox descriptionInputContainer;

    @FXML private ImageUploadPanelView imageUploadPanel;

    @FXML private VBox categorySelectContainer;
    @FXML private TagPickerItem tagPickerInput;
    @FXML private FlowPane routeListContainer;
    @FXML private FlowPane placesFlow;
    @FXML private VBox routePickerContainer;
    @FXML private VBox routeSearchContainer;
    @FXML private Button routeCreateButton;
    @FXML private VBox placePickerContainer;
    @FXML private VBox placeSearchContainer;
    @FXML private Button placeCreateButton;

    @FXML private Button addCountryButton;
    @FXML private Button addRouteButton;
    @FXML private Button addPlaceButton;
    @FXML private EditorActionButtonsView actionButtonsView;

    @Inject private ToastService toast;
    @Inject private TripService tripService;
    @Inject private CategoryService categoryService;
    @Inject private TagService tagService;
    @Inject private RouteService routeService;
    @Inject private PlaceService placeService;
    @Inject private TripRouteService tripRouteService;
    @Inject private TripPlaceService tripPlaceService;
    @Inject private CountryService countryService;

    private final Set<String> selectedCountryIds = new LinkedHashSet<>();
    private final Map<String, String> selectedCountryLabelsById = new java.util.LinkedHashMap<>();
    private final Set<String> selectedTagLabels = new LinkedHashSet<>();
    private final Set<String> selectedTagIds = new LinkedHashSet<>();
    private final List<RouteItem> routeItems = new ArrayList<>();
    private final List<PlaceItem> placeItems = new ArrayList<>();

    private InputItem titleInput;
    private TextAreaItem descriptionInput;
    private String tripId;
    private boolean createMode;
    private TripStatus tripStatus;
    private String coverImagePath;
    private String currentTripDisplayName = "New Trip";
    private DatePickerItem startDateInput;
    private DatePickerItem endDateInput;
    private CountriesView countrySelectView;
    private Categories categoriesComponent;
    private Entry<String> pendingCountryEntry;
    private Select<String> categorySelectModel;
    private StackPane uploadArea;
    private ImageView coverPreview;
    private VBox uploadPlaceholder;
    private Label selectedImageLabel;
    private SearchView<RouteItem> routeSearchView;
    private SearchView<PlaceItem> placeSearchView;
    private List<RouteItem> availableRouteCandidates = List.of();
    private List<PlaceItem> availablePlaceCandidates = List.of();
    private List<CategoryResponse> availableCategories = List.of();
    private List<TagOption> availableTags = List.of();
    private boolean coverImageDirty;

    @FXML
    public void initialize() {
        titleInput = new InputItem("input.placeholder.tripTitle", FieldVariant.GHOST);
        descriptionInput = new TextAreaItem("input.placeholder.tripDescription", FieldVariant.GHOST);
        startDateInput = new DatePickerItem("dd/MM/yyyy", FieldVariant.GHOST);
        endDateInput = new DatePickerItem("dd/MM/yyyy", FieldVariant.GHOST);
        titleInputContainer.getChildren().add(titleInput);
        descriptionInputContainer.getChildren().add(descriptionInput);
        startDateContainer.getChildren().add(startDateInput);
        endDateContainer.getChildren().add(endDateInput);
        uploadArea = imageUploadPanel.getUploadArea();
        coverPreview = imageUploadPanel.getCoverPreview();
        uploadPlaceholder = imageUploadPanel.getUploadPlaceholder();
        selectedImageLabel = imageUploadPanel.getSelectedImageLabel();

        contentFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        initializeCoverPreview();
        bindUploadPanelHandlers();

        configureButtonIcon(addCountryButton, "fth-plus");
        configureButtonIcon(addRouteButton, "fth-plus");
        configureButtonIcon(addPlaceButton, "fth-plus");
        configureButtonIcon(actionButtonsView.getPrimaryButton(), "fth-save");
        configureButtonIcon(actionButtonsView.getSecondaryButton(), "fth-trash-2");
        actionButtonsView.getPrimaryButton().setOnAction(event -> onSave());
        actionButtonsView.getSecondaryButton().setOnAction(event -> onDiscard());

        installRoundedClip(uploadArea, 16);
        bindLocalizedText();
        configureTagPicker();
        initializeCountrySelector();
        categoriesComponent = Categories.builder(categoryService).build();
        loadAvailableCategories();
        loadAvailableTags();
        refreshLocalizedUi();
        initializeActionPickers();
        I18n.bundleProperty().addListener((obs, oldBundle, newBundle) -> refreshLocalizedUi());
        renderCountryChips();
        renderRoutes();
        renderPlaces();
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        tripId = readTripId(data);
        createMode = tripId == null || tripId.isBlank() || "0".equals(tripId);
        tripStatus = data == null ? null : data.getValue("tripStatus");

        String tripName = data == null ? null : data.getValue("tripName");
        String tripCountry = data == null ? null : data.getValue("tripCountry");
        String tripCategory = data == null ? null : data.getValue("tripCategory");
        String tripCoverUrl = data == null ? null : data.getValue("tripCoverUrl");
        String tripTags = data == null ? null : data.getValue("tripTags");
        String tripDates = data == null ? null : data.getValue("tripDates");
        String tripStartDate = data == null ? null : data.getValue("tripStartDate");
        String tripEndDate = data == null ? null : data.getValue("tripEndDate");

        EditorDraftStorage.TripDraft draft = EditorDraftStorage.consumeTripDraft();
        if (matchesTripDraft(draft)) {
            applyTripDraft(draft);
        } else if (createMode) {
            populateHeader(tripName, tripDates);
            populateForm(tripName, tripCountry, tripCategory, tripTags, tripStartDate, tripEndDate);
            routeItems.clear();
            placeItems.clear();
            renderRoutes();
            renderPlaces();
            coverImageDirty = false;
            showCoverImage(tripCoverUrl, tripName);
        } else {
            loadExistingTrip(
                    tripName,
                    tripCountry,
                    tripCategory,
                    tripTags,
                    tripStartDate,
                    tripEndDate,
                    tripCoverUrl,
                    tripDates
            );
        }

        consumeReturnedEditorResults();
        log.info("Trip editor opened: id={}, name={}, createMode={}", tripId, tripName, createMode);
    }

    @Override
    public void onLifecycleShow() {
        updateFullScreenMode(false);
        EditorDraftStorage.clearTripDraft();
        consumeReturnedEditorResults();
    }

    @Override
    public void onLifecycleHide() {
        updateFullScreenMode(false);
    }

    @Override
    public void onLifecycleDestroy() {
        updateFullScreenMode(false);
    }

    @FXML
    private void onDiscard() {
        EditorDraftStorage.clearTripDraft();
        getRouter().popBackStack();
    }

    @FXML
    private void onAddCountry() {
        if (pendingCountryEntry == null) {
            toast.info(I18n.t("trip.add.toast.country.required"));
            return;
        }

        String countryId = pendingCountryEntry.getValue();
        String countryLabel = pendingCountryEntry.getLabel();
        if (countryId != null && !countryId.isBlank() && countryLabel != null && !countryLabel.isBlank()) {
            selectedCountryIds.add(countryId);
            selectedCountryLabelsById.putIfAbsent(countryId, countryLabel);
            renderCountryChips();
            if (countrySelectView != null) {
                countrySelectView.clearSearch();
            }
        }
        pendingCountryEntry = null;
    }

    @FXML
    private void onAddRoute() {
        boolean nextState = !routePickerContainer.isVisible();
        setRoutePickerVisible(nextState);
        if (nextState) {
            setPlacePickerVisible(false);
        }
    }

    @FXML
    private void onAddPlace() {
        boolean nextState = !placePickerContainer.isVisible();
        setPlacePickerVisible(nextState);
        if (nextState) {
            setRoutePickerVisible(false);
        }
    }

    @FXML
    private void onCloseRoutePicker() {
        setRoutePickerVisible(false);
    }

    @FXML
    private void onClosePlacePicker() {
        setPlacePickerVisible(false);
    }

    @FXML
    private void onCreateRoute() {
        setRoutePickerVisible(false);
        EditorDraftStorage.saveTripDraft(captureTripDraft());
        openCreateRoute();
    }

    @FXML
    private void onCreatePlace() {
        setPlacePickerVisible(false);
        EditorDraftStorage.saveTripDraft(captureTripDraft());
        openCreatePlace();
    }

    @FXML
    private void onSave() {
        if (!titleInput.validateRequired()) {
            return;
        }
        if (selectedCountryIds.isEmpty()) {
            toast.warning(I18n.t("trip.add.toast.countries.required"));
            return;
        }
        if (selectedValue(categorySelectModel) == null || selectedValue(categorySelectModel).isBlank()) {
            toast.warning(I18n.t("trip.add.toast.category.required"));
            return;
        }

        String categoryId = resolveSelectedCategoryId();
        if (categoryId == null || categoryId.isBlank()) {
            toast.warning(I18n.t("trip.add.toast.category.required"));
            return;
        }

        String tripTitle = titleInput.getText().trim();
        StatusEnum status = mapTripStatus(tripStatus);
        Instant startedAt = toInstant(startDateInput.getValue());
        Instant endedAt = toInstant(endDateInput.getValue());
        Result<Set<String>> tagIdsResult = ensureSelectedTagsPersisted();
        if (tagIdsResult.isFailure()) {
            toast.error(I18n.t("trip.add.toast.title.saved"), tagIdsResult.getError().message());
            return;
        }

        Set<Path> images = coverImagePath == null || coverImagePath.isBlank()
                ? Set.of()
                : Set.of(Path.of(coverImagePath));

        var result = createMode
                ? tripService.addTrip(new AddTripRequest(
                        categoryId,
                        tripTitle,
                        normalizeNullable(descriptionInput.getText()),
                        status,
                        startedAt,
                        endedAt,
                        tagIdsResult.getValue(),
                        images,
                        new LinkedHashSet<>(selectedCountryIds)
                ))
                : tripService.updateTrip(new UpdateTripRequest(
                        tripId,
                        categoryId,
                        tripTitle,
                        normalizeNullable(descriptionInput.getText()),
                        status,
                        startedAt,
                        endedAt,
                        tagIdsResult.getValue(),
                        coverImageDirty ? images : null,
                        new LinkedHashSet<>(selectedCountryIds)
                ));

        result.onSuccess(savedTrip -> {
            var relationsResult = syncTripRelations(savedTrip.id());
            if (relationsResult.isFailure()) {
                toast.error(I18n.t("trip.add.toast.title.saved"), relationsResult.getError().message());
                return;
            }

            String message = createMode
                    ? formatMessage("trip.add.toast.trip.ready", tripTitle)
                    : formatMessage("trip.add.toast.trip.updated", tripTitle);
            EditorDraftStorage.clearTripDraft();
            toast.success(I18n.t("trip.add.toast.title.saved"), message);
            getRouter().popBackStack();
        });
        result.onFailure(error -> toast.error(I18n.t("trip.add.toast.title.saved"), error.message()));
    }

    @FXML
    private void onChooseCoverImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.t("trip.add.dialog.cover.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.t("trip.add.dialog.cover.filter"), "*.png", "*.jpg", "*.jpeg", "*.svg")
        );

        File file = chooser.showOpenDialog(uploadArea.getScene() == null ? null : uploadArea.getScene().getWindow());
        if (file != null) {
            handleCoverImage(file);
        }
    }

    @FXML
    private void onUploadDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles() && isSupportedImageFile(event.getDragboard().getFiles().getFirst())) {
            event.acceptTransferModes(TransferMode.COPY);
            addUploadActiveState(true);
        }
        event.consume();
    }

    @FXML
    private void onUploadDragExited(DragEvent event) {
        addUploadActiveState(false);
        event.consume();
    }

    @FXML
    private void onUploadDragDropped(DragEvent event) {
        boolean completed = false;
        if (event.getDragboard().hasFiles()) {
            File file = event.getDragboard().getFiles().getFirst();
            if (isSupportedImageFile(file)) {
                handleCoverImage(file);
                completed = true;
            }
        }

        addUploadActiveState(false);
        event.setDropCompleted(completed);
        event.consume();
    }

    private void bindLocalizedText() {
        Localization.bindText(generalSectionHeader.titleProperty(), "trip.add.section.general");
        Localization.bindText(tripTitleLabel.textProperty(), "trip.add.field.title");
        Localization.bindText(countriesLabel.textProperty(), "trip.add.field.countries");
        Localization.bindText(startDateLabel.textProperty(), "trip.add.field.startDate");
        Localization.bindText(endDateLabel.textProperty(), "trip.add.field.endDate");
        Localization.bindText(descriptionLabel.textProperty(), "trip.add.field.description");
        Localization.bindText(routesSectionHeader.titleProperty(), "trip.add.section.routes");
        Localization.bindText(placesSectionHeader.titleProperty(), "trip.add.section.places");
        Localization.bindText(imageUploadPanel.sectionTitleProperty(), "trip.add.section.cover");
        Localization.bindText(imageUploadPanel.uploadTitleProperty(), "trip.add.upload.title");
        Localization.bindText(imageUploadPanel.uploadSubtitleProperty(), "trip.add.upload.subtitle");
        Localization.bindText(metaSectionHeader.titleProperty(), "trip.add.section.meta");
        Localization.bindText(categoryLabel.textProperty(), "trip.add.field.category");
        Localization.bindText(tagsLabel.textProperty(), "trip.add.field.tags");

        Localization.bindText(addCountryButton.textProperty(), "trip.add.action.addCountry");
        Localization.bindText(addRouteButton.textProperty(), "trip.add.action.addRoute");
        Localization.bindText(addPlaceButton.textProperty(), "trip.add.action.addPlace");
        Localization.bindText(routeCreateButton.textProperty(), "trip.add.action.createRoute");
        Localization.bindText(placeCreateButton.textProperty(), "trip.add.action.createPlace");
    }

    private void refreshLocalizedUi() {
        String selectedCategory = selectedValue(categorySelectModel);

        categorySelectModel = createSelectModel(createCategoryEntries(), "trip.add.select.category");

        categorySelectModel.setSelectedItem(findEntry(categorySelectModel, selectedCategory));

        categorySelectContainer.getChildren().setAll(createSelectView(categorySelectModel));

        tagPickerInput.setPlaceholderText(I18n.t("trip.add.select.tag"));
        tagPickerInput.setPopupTitle(I18n.t("trip.add.tag.popupTitle"));
        syncTagPicker();

        if (routePickerContainer.isVisible()) {
            refreshRoutePicker();
        }
        if (placePickerContainer.isVisible()) {
            refreshPlacePicker();
        }
        renderCountryChips();
        renderRoutes();
        renderPlaces();
    }

    private void loadAvailableCategories() {
        if (categoriesComponent == null) {
            availableCategories = List.of();
            return;
        }

        var result = categoriesComponent.loadAll();
        if (result.isFailure()) {
            logWarnWithError("Failed to load categories for trip editor", result.getError());
            availableCategories = List.of();
            return;
        }

        availableCategories = result.getValue();
    }

    private void loadAvailableTags() {
        if (tagService == null) {
            availableTags = List.of();
            return;
        }

        List<TagOption> items = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE);

        while (true) {
            var result = tagService.getTags(new GetTagsRequest(pageRequest, null));
            if (result.isFailure()) {
                logWarnWithError("Failed to load tags for trip editor", result.getError());
                availableTags = List.of();
                return;
            }

            Page<TagResponse> page = result.getValue();
            page.items().stream()
                    .map(tag -> new TagOption(tag.id(), safeText(tag.name())))
                    .filter(tag -> tag.id() != null && !tag.id().isBlank() && !tag.label().isBlank())
                    .forEach(items::add);

            if (!page.hasNext()) {
                break;
            }
            pageRequest = pageRequest.next();
        }

        availableTags = items;
    }

    private List<Entry<String>> createCategoryEntries() {
        if (categoriesComponent == null || availableCategories == null || availableCategories.isEmpty()) {
            return List.of();
        }
        return categoriesComponent.toEntries(availableCategories);
    }

    private void populateHeader(String tripName, String tripDates) {
        if (createMode) {
            currentTripDisplayName = I18n.t("trip.add.default.name");
            return;
        }
        currentTripDisplayName = tripName == null || tripName.isBlank() ? I18n.t("trip.add.default.name") : tripName;
    }

    private void populateForm(
            String tripName,
            String tripCountry,
            String tripCategory,
            String tripTagsCsv,
            String tripStartDate,
            String tripEndDate
    ) {
        titleInput.setText(createMode ? "" : safeText(tripName));
        descriptionInput.setText(createMode ? "" : buildTripDescription(tripName, tripCountry, tripCategory));

        selectedCountryIds.clear();
        selectedCountryLabelsById.clear();
        if (tripCountry != null && !tripCountry.isBlank()) {
            // Legacy route argument contains country name only; use it as both id and label fallback.
            selectedCountryIds.add(tripCountry);
            selectedCountryLabelsById.put(tripCountry, tripCountry);
        }
        renderCountryChips();

        selectedTagLabels.clear();
        selectedTagIds.clear();
        parseCsv(tripTagsCsv).stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .forEach(tag -> {
                    selectedTagLabels.add(tag);
                    String tagId = findTagIdByLabel(tag);
                    if (tagId != null && !tagId.isBlank()) {
                        selectedTagIds.add(tagId);
                    }
                });
        syncTagPicker();

        categorySelectModel.setSelectedItem(findEntry(categorySelectModel, tripCategory));

        startDateInput.setValue(parseDate(tripStartDate));
        endDateInput.setValue(parseDate(tripEndDate));
    }

    private void renderCountryChips() {
        countriesFlow.getChildren().clear();
        if (selectedCountryIds.isEmpty()) {
            Label placeholder = new Label(I18n.t("trip.add.empty.countries"));
            placeholder.getStyleClass().add("trip-editor-chip-placeholder");
            countriesFlow.getChildren().add(placeholder);
            return;
        }

        for (String countryId : selectedCountryIds) {
            String label = selectedCountryLabelsById.getOrDefault(countryId, countryId);
            Button chip = new Button(label + "  x");
            chip.setText(label + "  x");
            chip.setFocusTraversable(false);
            chip.getStyleClass().addAll("trip-editor-chip", "trip-editor-chip-soft");
            chip.setOnAction(event -> {
                selectedCountryIds.remove(countryId);
                selectedCountryLabelsById.remove(countryId);
                renderCountryChips();
            });
            countriesFlow.getChildren().add(chip);
        }
    }

    private void initializeCountrySelector() {
        if (countryService == null) {
            InputItem placeholder = new InputItem("trip.add.select.country", FieldVariant.GHOST);
            placeholder.setDisable(true);
            countrySelectContainer.getChildren().setAll(placeholder);
            return;
        }

        Countries countriesModel = Countries.builder(countryService)
                .placeholderKey("trip.add.select.country")
                .noResultKey("search.noResult")
                .variant(FieldVariant.GHOST)
                .searchOnTyping(true)
                .onResultSelected(entry -> pendingCountryEntry = entry)
                .onLoadFailed(error -> toast.error(I18n.t(error.code())))
                .build();

        countrySelectView = new CountriesView(countriesModel);
        countrySelectContainer.getChildren().setAll(countrySelectView);
    }

    private void renderRoutes() {
        routeListContainer.getChildren().clear();
        if (routeItems.isEmpty()) {
            Region emptyState = createEmptyState(I18n.t("trip.add.empty.routes"));
            emptyState.prefWidthProperty().bind(routeListContainer.widthProperty());
            routeListContainer.getChildren().add(emptyState);
            return;
        }

        for (RouteItem item : routeItems) {
            routeListContainer.getChildren().add(buildRouteCard(item));
        }
    }

    private void renderPlaces() {
        placesFlow.getChildren().clear();
        List<PlaceItem> displayedPlaces = displayedPlaceItems();
        if (displayedPlaces.isEmpty()) {
            Region emptyState = createEmptyState(I18n.t("trip.add.empty.places"));
            emptyState.prefWidthProperty().bind(placesFlow.widthProperty());
            placesFlow.getChildren().add(emptyState);
            return;
        }

        for (PlaceItem item : displayedPlaces) {
            placesFlow.getChildren().add(buildPlaceCard(item));
        }
    }

    private VBox buildRouteCard(RouteItem item) {
        EditorMediaCardView card = new EditorMediaCardView();
        card.setPreviewImage(loadImage(item.imagePath()));
        card.setTitle(item.title());
        card.setSubtitle(item.subtitle());
        card.setOnOpen(() -> openRoute(item));
        card.setOnRemove(() -> {
            routeItems.remove(item);
            renderRoutes();
            renderPlaces();
            refreshRoutePicker();
            refreshPlacePicker();
        });
        return card;
    }

    private VBox buildPlaceCard(PlaceItem item) {
        EditorMediaCardView card = new EditorMediaCardView();
        card.setPreviewImage(loadImage(item.imagePath()));
        card.setTitle(item.title());
        card.setSubtitle(item.subtitle());
        card.setRemoveVisible(item.isManual());
        if (item.isManual()) {
            card.setOnRemove(() -> {
                placeItems.removeIf(existing -> item.id().equals(existing.id()));
                renderPlaces();
                refreshPlacePicker();
            });
        }
        return card;
    }

    private Region createEmptyState(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("trip-editor-empty-state");

        StackPane pane = new StackPane(label);
        pane.getStyleClass().add("trip-editor-empty-card");
        pane.setMaxWidth(Double.MAX_VALUE);
        return pane;
    }

    private void showCoverImage(String imagePath, String tripName) {
        coverImagePath = imagePath;
        if (imagePath == null || imagePath.isBlank()) {
            coverPreview.setImage(null);
            coverPreview.setVisible(false);
            coverPreview.setManaged(false);
            uploadPlaceholder.setVisible(true);
            uploadPlaceholder.setManaged(true);
            selectedImageLabel.setVisible(false);
            selectedImageLabel.setManaged(false);
            selectedImageLabel.setText("");
            return;
        }

        String fileName = extractImageName(imagePath, tripName);
        selectedImageLabel.setText(
                fileName
        );
        selectedImageLabel.setVisible(true);
        selectedImageLabel.setManaged(true);

        if (isVectorImagePath(imagePath)) {
            coverPreview.setImage(null);
            coverPreview.setViewport(null);
            coverPreview.setVisible(false);
            coverPreview.setManaged(false);
            uploadPlaceholder.setVisible(true);
            uploadPlaceholder.setManaged(true);
            return;
        }

        Image image = loadImage(imagePath);
        setCoverPreviewImage(image);
        coverPreview.setVisible(true);
        coverPreview.setManaged(false);
        uploadPlaceholder.setVisible(false);
        uploadPlaceholder.setManaged(false);
    }

    private void handleCoverImage(File file) {
        if (!isSupportedImageFile(file)) {
            toast.warning(I18n.t("trip.add.toast.image.unsupported"));
            return;
        }

        coverImagePath = file.getAbsolutePath();
        coverImageDirty = true;
        selectedImageLabel.setText(file.getName());
        selectedImageLabel.setVisible(true);
        selectedImageLabel.setManaged(true);

        if (isVectorImage(file)) {
            coverPreview.setImage(null);
            coverPreview.setViewport(null);
            coverPreview.setVisible(false);
            coverPreview.setManaged(false);
            uploadPlaceholder.setVisible(true);
            uploadPlaceholder.setManaged(true);
            return;
        }

        Image image = new Image(file.toURI().toString(), true);
        image.errorProperty().addListener((obs, oldVal, isError) -> {
            if (Boolean.TRUE.equals(isError)) {
                coverPreview.setImage(null);
                coverPreview.setViewport(null);
                coverPreview.setVisible(false);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(true);
                uploadPlaceholder.setManaged(true);
                toast.warning(I18n.t("trip.add.toast.image.previewUnavailable"));
            }
        });
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0 && !image.isError()) {
                setCoverPreviewImage(image);
                coverPreview.setVisible(true);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(false);
                uploadPlaceholder.setManaged(false);
            }
        });
    }

    private void configureButtonIcon(Button button, String iconLiteral) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(14);
        icon.getStyleClass().add("app-btn-icon");
        button.setGraphic(icon);
    }

    private void initializeActionPickers() {
        configurePickerButtonIcons();
        initializePickerSearches();
        setRoutePickerVisible(false);
        setPlacePickerVisible(false);
    }

    private void initializePickerSearches() {
        Search<RouteItem> routeSearchModel = Search.<RouteItem>builder(this::searchRouteEntries)
                .placeholderKey("trip.add.picker.route.search")
                .noResultKey("trip.add.menu.route.empty")
                .variant(FieldVariant.GHOST)
                .displayMode(SearchDisplayMode.INLINE)
                .size(SearchSize.MIDDLE)
                .maxVisibleResults(8)
                .showOnEmptyQuery(true)
                .onResultSelected(entry -> addExistingRoute(entry.getValue()))
                .build();

        routeSearchView = new SearchView<>(routeSearchModel);
        routeSearchContainer.getChildren().setAll(routeSearchView);

        Search<PlaceItem> placeSearchModel = Search.<PlaceItem>builder(this::searchPlaceEntries)
                .placeholderKey("trip.add.picker.place.search")
                .noResultKey("trip.add.menu.place.empty")
                .variant(FieldVariant.GHOST)
                .displayMode(SearchDisplayMode.INLINE)
                .size(SearchSize.MIDDLE)
                .maxVisibleResults(8)
                .showOnEmptyQuery(true)
                .onResultSelected(entry -> addExistingPlace(entry.getValue()))
                .build();

        placeSearchView = new SearchView<>(placeSearchModel);
        placeSearchContainer.getChildren().setAll(placeSearchView);
    }

    private void setRoutePickerVisible(boolean visible) {
        routePickerContainer.setVisible(visible);
        routePickerContainer.setManaged(visible);
        addRouteButton.getStyleClass().remove("trip-editor-add-action-button-expanded");
        if (visible) {
            addRouteButton.getStyleClass().add("trip-editor-add-action-button-expanded");
            refreshRoutePicker();
        } else {
            routeSearchView.getSearchField().clear();
        }
    }

    private void setPlacePickerVisible(boolean visible) {
        placePickerContainer.setVisible(visible);
        placePickerContainer.setManaged(visible);
        addPlaceButton.getStyleClass().remove("trip-editor-add-action-button-expanded");
        if (visible) {
            addPlaceButton.getStyleClass().add("trip-editor-add-action-button-expanded");
            refreshPlacePicker();
        } else {
            placeSearchView.getSearchField().clear();
        }
    }

    private void configurePickerButtonIcons() {
        routeCreateButton.setGraphic(createPickerIcon("fth-edit-2", "trip-editor-picker-create-icon", 14));
        placeCreateButton.setGraphic(createPickerIcon("fth-edit-2", "trip-editor-picker-create-icon", 14));
    }

    private FontIcon createPickerIcon(String literal, String styleClass, int size) {
        FontIcon icon = new FontIcon(literal);
        icon.setIconSize(size);
        icon.getStyleClass().add(styleClass);
        return icon;
    }

    private void refreshRoutePicker() {
        availableRouteCandidates = loadRouteCandidates();
        if (routeSearchView != null) {
            routeSearchView.getSearchField().setText(routeSearchView.getSearchField().getText());
        }
    }

    private void refreshPlacePicker() {
        availablePlaceCandidates = loadPlaceCandidates();
        if (placeSearchView != null) {
            placeSearchView.getSearchField().setText(placeSearchView.getSearchField().getText());
        }
    }

    private List<Entry<RouteItem>> searchRouteEntries(String query) {
        return filterRoutes(query).stream()
                .map(route -> Entry.builder(route, route.title()).icon("fth-map").build())
                .toList();
    }

    private List<Entry<PlaceItem>> searchPlaceEntries(String query) {
        return filterPlaces(query).stream()
                .map(place -> Entry.builder(place, place.title()).icon("fth-map-pin").build())
                .toList();
    }

    private List<RouteItem> filterRoutes(String query) {
        String normalized = normalizeNullable(query);
        if (normalized == null) {
            return availableRouteCandidates;
        }

        String lowerQuery = normalized.toLowerCase(Locale.ROOT);
        return availableRouteCandidates.stream()
                .filter(route -> matchesQuery(route.title(), route.subtitle(), lowerQuery))
                .toList();
    }

    private List<PlaceItem> filterPlaces(String query) {
        String normalized = normalizeNullable(query);
        if (normalized == null) {
            return availablePlaceCandidates;
        }

        String lowerQuery = normalized.toLowerCase(Locale.ROOT);
        return availablePlaceCandidates.stream()
                .filter(place -> matchesQuery(place.title(), place.subtitle(), lowerQuery))
                .toList();
    }

    private boolean matchesQuery(String title, String subtitle, String query) {
        return (title != null && title.toLowerCase(Locale.ROOT).contains(query))
                || (subtitle != null && subtitle.toLowerCase(Locale.ROOT).contains(query));
    }


    private void openCreateRoute() {
        RouterArgument args = new RouterArgument();
        String targetTripId = tripId == null || tripId.isBlank() ? "0" : tripId;
        String targetTripName = titleInput.getText() == null || titleInput.getText().isBlank()
                ? currentTripDisplayName
                : titleInput.getText().trim();

        args.addArgument("tripId", targetTripId);
        args.addArgument("tripName", targetTripName == null || targetTripName.isBlank() ? "New Trip" : targetTripName);
        args.addArgument("editorReturnTarget", EditorDraftStorage.TARGET_TRIP);
        getRouter().moveto(RouteIds.ADD_ROUTE, args);
    }

    private void openRoute(RouteItem route) {
        if (route == null || route.id() == null || route.id().isBlank()) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("routeId", route.id());
        getRouter().moveto(RouteIds.ROUTE_DETAILS, args);
    }

    private void openCreatePlace() {
        RouterArgument args = new RouterArgument();
        String targetTripId = tripId == null || tripId.isBlank() ? "0" : tripId;
        String targetTripName = titleInput.getText() == null || titleInput.getText().isBlank()
                ? currentTripDisplayName
                : titleInput.getText().trim();

        args.addArgument("tripId", targetTripId);
        args.addArgument(
                "tripName",
                targetTripName == null || targetTripName.isBlank() ? I18n.t("trip.add.default.name") : targetTripName
        );
        args.addArgument("editorReturnTarget", EditorDraftStorage.TARGET_TRIP);
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    private List<RouteItem> loadRouteCandidates() {
        if (routeService == null) {
            return List.of();
        }
        return loadAllRoutes().stream()
                .map(this::toRouteItem)
                .filter(route -> route.id() != null && !containsRoute(route.id()))
                .toList();
    }

    private List<PlaceItem> loadPlaceCandidates() {
        if (placeService == null) {
            return List.of();
        }
        return loadAllPlaces().stream()
                .map(this::toManualPlaceItem)
                .filter(place -> place.id() != null && !containsPlace(place.id()))
                .toList();
    }

    private List<RouteResponse> loadAllRoutes() {
        List<RouteResponse> routes = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE);

        while (true) {
            var result = routeService.getRoutes(new GetRoutesRequest(pageRequest, null));
            if (result.isFailure()) {
                logWarnWithError("Failed to load routes for trip picker", result.getError());
                toast.warning(I18n.t("trip.add.toast.routes.loadFailed"));
                return List.of();
            }

            Page<RouteResponse> page = result.getValue();
            routes.addAll(page.items());
            if (!page.hasNext()) {
                return routes;
            }
            pageRequest = pageRequest.next();
        }
    }

    private List<PlaceResponse> loadAllPlaces() {
        List<PlaceResponse> places = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE);

        while (true) {
            var result = placeService.getPlaces(new GetPlacesRequest(pageRequest, null));
            if (result.isFailure()) {
                logWarnWithError("Failed to load places for trip picker", result.getError());
                toast.warning(I18n.t("trip.add.toast.places.loadFailed"));
                return List.of();
            }

            Page<PlaceResponse> page = result.getValue();
            places.addAll(page.items());
            if (!page.hasNext()) {
                return places;
            }
            pageRequest = pageRequest.next();
        }
    }

    private void addExistingRoute(RouteItem route) {
        if (containsRoute(route.id())) {
            toast.info(I18n.t("trip.add.toast.route.duplicate"));
            return;
        }

        routeItems.add(route);
        renderRoutes();
        renderPlaces();
        refreshRoutePicker();
        refreshPlacePicker();
        setRoutePickerVisible(false);
        toast.success(I18n.t("trip.add.toast.route.added.title"), route.title());
    }

    private void addExistingPlace(PlaceItem place) {
        if (containsPlace(place.id())) {
            toast.info(I18n.t("trip.add.toast.place.duplicate"));
            return;
        }

        placeItems.add(place.manualCopy());
        renderPlaces();
        refreshPlacePicker();
        setPlacePickerVisible(false);
        toast.success(I18n.t("trip.add.toast.place.added.title"), place.title());
    }

    private boolean containsRoute(String routeId) {
        return routeId != null && routeItems.stream().anyMatch(item -> routeId.equals(item.id()));
    }

    private boolean containsPlace(String placeId) {
        return placeId != null && displayedPlaceItems().stream().anyMatch(item -> placeId.equals(item.id()));
    }

    private List<PlaceItem> displayedPlaceItems() {
        Map<String, PlaceItem> itemsById = new java.util.LinkedHashMap<>();
        for (PlaceItem item : placeItems) {
            itemsById.putIfAbsent(item.id(), item);
        }
        for (RouteItem route : routeItems) {
            for (PlaceItem item : route.derivedPlaces()) {
                itemsById.putIfAbsent(item.id(), item);
            }
        }
        return new ArrayList<>(itemsById.values());
    }

    private EditorDraftStorage.TripDraft captureTripDraft() {
        return new EditorDraftStorage.TripDraft(
                tripId,
                normalizeNullable(titleInput.getText()),
                normalizeNullable(descriptionInput.getText()),
                currentTripDisplayName,
                tripStatus,
                startDateInput.getValue(),
                endDateInput.getValue(),
                selectedValue(categorySelectModel),
                new LinkedHashSet<>(selectedCountryIds),
                new java.util.LinkedHashMap<>(selectedCountryLabelsById),
                new LinkedHashSet<>(selectedTagLabels),
                new LinkedHashSet<>(selectedTagIds),
                coverImagePath,
                coverImageDirty,
                routeItems.stream()
                        .map(route -> new EditorDraftStorage.RouteDraftItem(
                                route.id(),
                                route.title(),
                                route.subtitle(),
                                route.imagePath(),
                                route.derivedPlaces().stream()
                                        .map(place -> new EditorDraftStorage.PlaceDraftItem(
                                                place.id(),
                                                place.title(),
                                                place.subtitle(),
                                                place.imagePath(),
                                                place.sourceType(),
                                                place.sourceRouteId(),
                                                null,
                                                null
                                        ))
                                        .toList()
                        ))
                        .toList(),
                placeItems.stream()
                        .map(place -> new EditorDraftStorage.PlaceDraftItem(
                                place.id(),
                                place.title(),
                                place.subtitle(),
                                place.imagePath(),
                                place.sourceType(),
                                place.sourceRouteId(),
                                null,
                                null
                        ))
                        .toList()
        );
    }

    private boolean matchesTripDraft(EditorDraftStorage.TripDraft draft) {
        if (draft == null) {
            return false;
        }
        return normalizeTripKey(tripId).equals(normalizeTripKey(draft.tripId()));
    }

    private void applyTripDraft(EditorDraftStorage.TripDraft draft) {
        titleInput.setText(safeText(draft.title()));
        descriptionInput.setText(safeText(draft.description()));
        currentTripDisplayName = draft.currentTripDisplayName() == null || draft.currentTripDisplayName().isBlank()
                ? I18n.t("trip.add.default.name")
                : draft.currentTripDisplayName();
        tripStatus = draft.tripStatus();
        startDateInput.setValue(draft.startDate());
        endDateInput.setValue(draft.endDate());

        selectedCountryIds.clear();
        selectedCountryIds.addAll(draft.selectedCountryIds());
        selectedCountryLabelsById.clear();
        selectedCountryLabelsById.putAll(draft.selectedCountryLabelsById());
        renderCountryChips();

        selectedTagLabels.clear();
        selectedTagLabels.addAll(draft.selectedTagLabels());
        selectedTagIds.clear();
        selectedTagIds.addAll(draft.selectedTagIds());
        syncTagPicker();

        categorySelectModel.setSelectedItem(findEntry(categorySelectModel, draft.categoryValue()));

        routeItems.clear();
        draft.routes().stream()
                .map(route -> new RouteItem(
                        route.id(),
                        route.title(),
                        route.subtitle(),
                        route.imagePath(),
                        route.derivedPlaces().stream()
                                .map(place -> new PlaceItem(
                                        place.id(),
                                        place.title(),
                                        place.subtitle(),
                                        place.imagePath(),
                                        place.sourceType() == null ? TripPlaceSourceType.ROUTE : place.sourceType(),
                                        place.sourceRouteId()
                                ))
                                .toList()
                ))
                .forEach(routeItems::add);

        placeItems.clear();
        draft.manualPlaces().stream()
                .map(place -> new PlaceItem(
                        place.id(),
                        place.title(),
                        place.subtitle(),
                        place.imagePath(),
                        place.sourceType() == null ? TripPlaceSourceType.MANUAL : place.sourceType(),
                        place.sourceRouteId()
                ))
                .forEach(placeItems::add);

        coverImageDirty = draft.coverImageDirty();
        showCoverImage(draft.coverImagePath(), draft.title());
        renderRoutes();
        renderPlaces();
    }

    private void consumeReturnedEditorResults() {
        RouteResponse createdRoute = EditorDraftStorage.consumePendingRoute(EditorDraftStorage.TARGET_TRIP);
        if (createdRoute != null) {
            addExistingRoute(toRouteItem(createdRoute));
        }

        PlaceResponse createdPlace = EditorDraftStorage.consumePendingPlace(EditorDraftStorage.TARGET_TRIP);
        if (createdPlace != null) {
            addExistingPlace(toManualPlaceItem(createdPlace));
        }
    }

    private String normalizeTripKey(String value) {
        if (value == null || value.isBlank() || "0".equals(value.trim())) {
            return "";
        }
        return value.trim();
    }


    private RouteItem toRouteItem(RouteResponse response) {
        String subtitle = response.description() == null || response.description().isBlank()
                ? formatMessage("trip.add.route.length", String.format(Locale.US, "%.1f", response.length()))
                : response.description();
        return new RouteItem(
                response.id(),
                safeText(response.title()).isBlank() ? I18n.t("trip.add.fallback.route") : response.title(),
                subtitle,
                imagePath(response.coverImage() == null ? null : response.coverImage().url()),
                response.places() == null
                        ? List.of()
                        : response.places().stream()
                                .map(place -> toRouteDerivedPlaceItem(place, response.id()))
                                .toList()
        );
    }

    private PlaceItem toManualPlaceItem(PlaceResponse response) {
        String subtitle = response.country() != null && response.country().name() != null && !response.country().name().isBlank()
                ? response.country().name()
                : safeText(response.description());
        return new PlaceItem(
                response.id(),
                safeText(response.title()).isBlank() ? I18n.t("trip.add.fallback.place") : response.title(),
                subtitle,
                imagePath(response.coverImage() == null ? null : response.coverImage().url()),
                TripPlaceSourceType.MANUAL,
                null
        );
    }

    private PlaceItem toRouteDerivedPlaceItem(PlaceResponse response, String routeId) {
        String subtitle = response.country() != null && response.country().name() != null && !response.country().name().isBlank()
                ? response.country().name()
                : safeText(response.description());
        return new PlaceItem(
                response.id(),
                safeText(response.title()).isBlank() ? I18n.t("trip.add.fallback.place") : response.title(),
                subtitle,
                imagePath(response.coverImage() == null ? null : response.coverImage().url()),
                TripPlaceSourceType.ROUTE,
                routeId
        );
    }

    private String imagePath(Path path) {
        return path == null ? DEFAULT_IMAGE : path.toString();
    }

    private void bindUploadPanelHandlers() {
        uploadArea.setOnMouseClicked(event -> onChooseCoverImage());
        uploadArea.setOnDragOver(this::onUploadDragOver);
        uploadArea.setOnDragExited(this::onUploadDragExited);
        uploadArea.setOnDragDropped(this::onUploadDragDropped);
    }

    private void addUploadActiveState(boolean active) {
        if (active) {
            if (!uploadArea.getStyleClass().contains("editor-upload-area-active")) {
                uploadArea.getStyleClass().add("editor-upload-area-active");
            }
            return;
        }

        uploadArea.getStyleClass().remove("editor-upload-area-active");
    }

    private void installRoundedClip(StackPane target, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(target.widthProperty());
        clip.heightProperty().bind(target.heightProperty());
        target.setClip(clip);
    }

    private void updateFullScreenMode(boolean fullScreen) {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(fullScreen);
    }

    private void configureTagPicker() {
        tagPickerInput.setAllowCustomTags(true);
        tagPickerInput.setAvailableTags(availableTagLabels());
        tagPickerInput.setOnSelectionChanged(tags -> {
            selectedTagLabels.clear();
            selectedTagLabels.addAll(tags);
            selectedTagIds.clear();
            tags.stream()
                    .map(this::findTagIdByLabel)
                    .filter(tagId -> tagId != null && !tagId.isBlank())
                    .forEach(selectedTagIds::add);
        });
    }

    private void syncTagPicker() {
        tagPickerInput.setAvailableTags(availableTagLabels());
        tagPickerInput.setSelectedTags(selectedTagLabels());
    }

    private List<String> availableTagLabels() {
        return availableTags.stream()
                .map(TagOption::label)
                .filter(label -> label != null && !label.isBlank())
                .toList();
    }

    private List<String> selectedTagLabels() {
        if (!selectedTagLabels.isEmpty()) {
            return selectedTagLabels.stream().toList();
        }
        return selectedTagIds.stream()
                .map(this::findTagLabelById)
                .filter(label -> label != null && !label.isBlank())
                .toList();
    }

    private String findTagIdByLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        return availableTags.stream()
                .filter(tag -> label.equalsIgnoreCase(tag.label()))
                .map(TagOption::id)
                .findFirst()
                .orElse(null);
    }

    private String findTagLabelById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return availableTags.stream()
                .filter(tag -> id.equals(tag.id()))
                .map(TagOption::label)
                .findFirst()
                .orElse(null);
    }

    private ColorTheme pickTagColor(String label) {
        ColorTheme[] palette = {
                ColorTheme.RED_DARK,
                ColorTheme.RED,
                ColorTheme.ROSE,
                ColorTheme.ORANGE,
                ColorTheme.AMBER,
                ColorTheme.YELLOW,
                ColorTheme.GOLDEN_BROWN,
                ColorTheme.LIME,
                ColorTheme.GREEN,
                ColorTheme.INDIGO,
                ColorTheme.VIOLET,
                ColorTheme.STEEL_BLUE,
                ColorTheme.BLUE,
                ColorTheme.CYAN,
                ColorTheme.TEAL,
                ColorTheme.SAGE,
                ColorTheme.BROWN,
                ColorTheme.PURPLE,
                ColorTheme.PINK,
                ColorTheme.GRAY
        };
        return palette[Math.floorMod(label == null ? 0 : label.hashCode(), palette.length)];
    }

    private void initializeCoverPreview() {
        coverPreview.setPreserveRatio(false);
        coverPreview.fitWidthProperty().bind(uploadArea.widthProperty());
        coverPreview.fitHeightProperty().bind(uploadArea.heightProperty());
        uploadArea.widthProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
        uploadArea.heightProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
        coverPreview.imageProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
    }

    private void setCoverPreviewImage(Image image) {
        coverPreview.setImage(image);
        updateCoverPreviewViewport();
        if (image == null) {
            coverPreview.setViewport(null);
            return;
        }

        image.widthProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
        image.heightProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
        image.progressProperty().addListener((obs, oldVal, newVal) -> updateCoverPreviewViewport());
    }

    private void updateCoverPreviewViewport() {
        Image image = coverPreview.getImage();
        if (image == null) {
            coverPreview.setViewport(null);
            return;
        }

        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double viewportWidth = uploadArea.getWidth();
        double viewportHeight = uploadArea.getHeight();

        if (imageWidth <= 0 || imageHeight <= 0 || viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }

        double imageRatio = imageWidth / imageHeight;
        double viewportRatio = viewportWidth / viewportHeight;

        if (imageRatio > viewportRatio) {
            double cropWidth = imageHeight * viewportRatio;
            double x = (imageWidth - cropWidth) / 2.0;
            coverPreview.setViewport(new Rectangle2D(x, 0, cropWidth, imageHeight));
            return;
        }

        double cropHeight = imageWidth / viewportRatio;
        double y = (imageHeight - cropHeight) / 2.0;
        coverPreview.setViewport(new Rectangle2D(0, y, imageWidth, cropHeight));
    }

    private Select<String> createSelectModel(List<Entry<String>> entries, String placeholderKey) {
        return Select.<String>builder()
                .placeholder(I18n.t(placeholderKey))
                .variant(FieldVariant.GHOST)
                .items(entries)
                .build();
    }

    private SelectView<String> createSelectView(Select<String> model) {
        SelectView<String> view = new SelectView<>();
        view.update(model);
        view.setMaxWidth(Double.MAX_VALUE);
        view.getComboBox().setMaxWidth(Double.MAX_VALUE);
        view.getComboBox().getStyleClass().add("trip-editor-select");
        view.getComboBox().skinProperty().addListener((obs, oldSkin, newSkin) ->
                Platform.runLater(() -> styleSelectChevron(view)));
        Platform.runLater(() -> styleSelectChevron(view));
        view.getComboBox().showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                Platform.runLater(() -> styleSelectPopup(view));
            }
        });
        return view;
    }

    private void styleSelectPopup(SelectView<String> view) {
        Node listView = view.getComboBox().lookup(".list-view");
        if (listView != null && !listView.getStyleClass().contains("trip-editor-select-popup")) {
            listView.getStyleClass().add("trip-editor-select-popup");
        }
    }

    private void styleSelectChevron(SelectView<String> view) {
        Node arrowButton = view.getComboBox().lookup(".arrow-button");
        if (!(arrowButton instanceof StackPane buttonPane)) {
            return;
        }

        boolean alreadyStyled = buttonPane.getChildren().stream()
                .anyMatch(node -> node.getStyleClass().contains("trip-editor-select-chevron"));
        if (alreadyStyled) {
            return;
        }

        FontIcon chevronIcon = new FontIcon("fth-chevron-down");
        chevronIcon.setIconSize(14);
        chevronIcon.getStyleClass().addAll("app-tag-picker-toggle-icon", "trip-editor-select-chevron");
        buttonPane.getChildren().setAll(chevronIcon);
    }

    private Entry<String> toEntry(String value) {
        return Entry.builder(value, value).build();
    }

    private Entry<String> findEntry(Select<String> model, String value) {
        if (model == null || value == null || value.isBlank()) {
            return null;
        }
        return model.getItems().stream()
                .filter(entry -> value.equals(entry.getValue()) || value.equals(entry.getLabel()))
                .findFirst()
                .orElse(null);
    }

    private String selectedValue(Select<String> model) {
        if (model == null || model.getSelectedItem() == null) {
            return null;
        }
        return model.getSelectedItem().getValue();
    }

    private String readTripId(RouterArgument data) {
        if (data == null) {
            return null;
        }

        Object raw = data.getValue("tripId");
        return raw == null ? null : raw.toString();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private List<String> parseCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .toList();
    }

    private String buildTripDescription(String tripName, String tripCountry, String tripCategory) {
        List<String> parts = new ArrayList<>();
        if (tripName != null && !tripName.isBlank()) {
            parts.add(tripName);
        }
        if (tripCountry != null && !tripCountry.isBlank()) {
            parts.add("through " + tripCountry);
        }
        if (tripCategory != null && !tripCategory.isBlank()) {
            parts.add("focused on " + tripCategory.toLowerCase(Locale.ROOT));
        }
        return String.join(" ", parts);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private void loadExistingTrip(
            String fallbackTripName,
            String fallbackTripCountry,
            String fallbackTripCategory,
            String fallbackTripTags,
            String fallbackTripStartDate,
            String fallbackTripEndDate,
            String fallbackTripCoverUrl,
            String fallbackTripDates
    ) {
        routeItems.clear();
        placeItems.clear();
        renderRoutes();
        renderPlaces();

        if (tripId == null || tripId.isBlank()) {
            populateHeader(fallbackTripName, fallbackTripDates);
            populateForm(
                    fallbackTripName,
                    fallbackTripCountry,
                    fallbackTripCategory,
                    fallbackTripTags,
                    fallbackTripStartDate,
                    fallbackTripEndDate
            );
            coverImageDirty = false;
            showCoverImage(fallbackTripCoverUrl, fallbackTripName);
            return;
        }

        var result = tripService.getTripById(new GetTripByIdRequest(tripId));
        if (result.isFailure()) {
            logWarnWithError("Failed to load trip '" + tripId + "' from service, using router fallback", result.getError());
            populateHeader(fallbackTripName, fallbackTripDates);
            populateForm(
                    fallbackTripName,
                    fallbackTripCountry,
                    fallbackTripCategory,
                    fallbackTripTags,
                    fallbackTripStartDate,
                    fallbackTripEndDate
            );
            coverImageDirty = false;
            showCoverImage(fallbackTripCoverUrl, fallbackTripName);
            return;
        }

        applyTripResponse(result.getValue());
    }

    private void applyTripResponse(TripResponse trip) {
        currentTripDisplayName = trip.title() == null || trip.title().isBlank()
                ? I18n.t("trip.add.default.name")
                : trip.title();
        tripStatus = toLegacyTripStatus(trip.status());

        titleInput.setText(safeText(trip.title()));
        descriptionInput.setText(safeText(trip.description()));
        startDateInput.setValue(toLocalDate(trip.startedAt()));
        endDateInput.setValue(toLocalDate(trip.endedAt()));

        selectedCountryIds.clear();
        selectedCountryLabelsById.clear();
        if (trip.countries() != null) {
            for (var country : trip.countries()) {
                selectedCountryIds.add(country.id());
                selectedCountryLabelsById.put(country.id(), country.name());
            }
        }
        renderCountryChips();

        selectedTagLabels.clear();
        selectedTagIds.clear();
        if (trip.tags() != null) {
            trip.tags().stream()
                    .forEach(tag -> {
                        if (tag.name() != null && !tag.name().isBlank()) {
                            selectedTagLabels.add(tag.name().trim());
                        }
                        if (tag.id() != null && !tag.id().isBlank()) {
                            selectedTagIds.add(tag.id());
                        }
                    });
        }
        syncTagPicker();

        String categoryId = trip.category() == null ? null : trip.category().id();
        categorySelectModel.setSelectedItem(findEntry(categorySelectModel, categoryId));

        String coverPath = null;
        if (trip.images() != null && !trip.images().isEmpty()) {
            var image = trip.images().iterator().next();
            coverPath = image.url() == null ? null : image.url().toString();
        }
        coverImageDirty = false;
        showCoverImage(coverPath, trip.title());

        loadTripRoutes(trip.id());
        loadTripPlaces(trip.id());
    }

    private Result<Void> syncTripRelations(String targetTripId) {
        Result<Void> routeResult = replaceTripRoutes(targetTripId);
        if (routeResult.isFailure()) {
            return routeResult;
        }
        return replaceTripPlaces(targetTripId);
    }

    private Result<Set<String>> ensureSelectedTagsPersisted() {
        LinkedHashSet<String> resolvedIds = new LinkedHashSet<>();

        for (String label : selectedTagLabels()) {
            if (label == null || label.isBlank()) {
                continue;
            }

            String existingId = findTagIdByLabel(label);
            if (existingId != null && !existingId.isBlank()) {
                resolvedIds.add(existingId);
                continue;
            }

            if (tagService == null) {
                return Result.fail(new com.triplify.application.error.ApplicationError.Unexpected(
                        "Tag service is unavailable"
                ));
            }

            var createResult = tagService.createTag(new CreateTagRequest(label.trim(), pickTagColor(label)));
            if (createResult.isFailure()) {
                return Result.fail(createResult.getError());
            }

            TagResponse created = createResult.getValue();
            availableTags = new ArrayList<>(availableTags);
            availableTags.add(new TagOption(created.id(), created.name()));
            resolvedIds.add(created.id());
        }

        selectedTagIds.clear();
        selectedTagIds.addAll(resolvedIds);
        syncTagPicker();
        return Result.ok(new LinkedHashSet<>(resolvedIds));
    }

    private Result<Void> replaceTripRoutes(String targetTripId) {
        if (tripRouteService == null) {
            return Result.ok();
        }

        Result<List<TripRouteResponse>> existingResult = loadAllTripRoutes(targetTripId);
        if (existingResult.isFailure()) {
            return Result.fail(existingResult.getError());
        }

        for (TripRouteResponse tripRoute : existingResult.getValue()) {
            var deleteResult = tripRouteService.deleteTripRoute(new DeleteTripRouteRequest(tripRoute.id()));
            if (deleteResult.isFailure()) {
                return Result.fail(deleteResult.getError());
            }
        }

        for (int i = 0; i < routeItems.size(); i++) {
            RouteItem item = routeItems.get(i);
            var addResult = tripRouteService.addTripRoute(new AddTripRouteRequest(targetTripId, item.id(), i));
            if (addResult.isFailure()) {
                return Result.fail(addResult.getError());
            }
        }

        return Result.ok();
    }

    private Result<Void> replaceTripPlaces(String targetTripId) {
        if (tripPlaceService == null) {
            return Result.ok();
        }

        Result<List<TripPlaceResponse>> existingResult = loadAllTripPlaces(targetTripId, TripPlaceSourceType.MANUAL);
        if (existingResult.isFailure()) {
            return Result.fail(existingResult.getError());
        }

        for (TripPlaceResponse tripPlace : existingResult.getValue()) {
            var deleteResult = tripPlaceService.deleteTripPlace(new DeleteTripPlaceRequest(tripPlace.id()));
            if (deleteResult.isFailure()) {
                return Result.fail(deleteResult.getError());
            }
        }

        for (PlaceItem item : placeItems) {
            var addResult = tripPlaceService.addTripPlace(new AddTripPlaceRequest(
                    targetTripId,
                    item.id(),
                    null,
                    TripPlaceSourceType.MANUAL,
                    null,
                    null
            ));
            if (addResult.isFailure()) {
                return Result.fail(addResult.getError());
            }
        }

        return Result.ok();
    }

    private void loadTripRoutes(String targetTripId) {
        routeItems.clear();
        Result<List<TripRouteResponse>> result = loadAllTripRoutes(targetTripId);
        if (result.isFailure()) {
            logWarnWithError("Failed to load trip routes for trip '" + targetTripId + "'", result.getError());
            toast.warning(I18n.t("trip.add.toast.routes.loadFailed"));
            renderRoutes();
            return;
        }

        result.getValue().stream()
                .map(TripRouteResponse::route)
                .filter(route -> route != null)
                .map(this::toRouteItem)
                .forEach(routeItems::add);
        renderRoutes();
    }

    private void loadTripPlaces(String targetTripId) {
        placeItems.clear();
        Result<List<TripPlaceResponse>> result = loadAllTripPlaces(targetTripId, TripPlaceSourceType.MANUAL);
        if (result.isFailure()) {
            logWarnWithError("Failed to load trip places for trip '" + targetTripId + "'", result.getError());
            toast.warning(I18n.t("trip.add.toast.places.loadFailed"));
            renderPlaces();
            return;
        }

        result.getValue().stream()
                .map(TripPlaceResponse::place)
                .filter(place -> place != null)
                .map(this::toManualPlaceItem)
                .forEach(placeItems::add);
        renderPlaces();
    }

    private Result<List<TripRouteResponse>> loadAllTripRoutes(String targetTripId) {
        if (tripRouteService == null) {
            return Result.ok(List.of());
        }

        List<TripRouteResponse> items = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE);

        while (true) {
            var result = tripRouteService.getTripRoutes(new GetTripRoutesRequest(
                    pageRequest,
                    new GetTripRoutesRequest.Filter(targetTripId, null)
            ));
            if (result.isFailure()) {
                return Result.fail(result.getError());
            }

            Page<TripRouteResponse> page = result.getValue();
            items.addAll(page.items());
            if (!page.hasNext()) {
                return Result.ok(items);
            }
            pageRequest = pageRequest.next();
        }
    }

    private Result<List<TripPlaceResponse>> loadAllTripPlaces(String targetTripId) {
        return loadAllTripPlaces(targetTripId, null);
    }

    private Result<List<TripPlaceResponse>> loadAllTripPlaces(String targetTripId, TripPlaceSourceType sourceType) {
        if (tripPlaceService == null) {
            return Result.ok(List.of());
        }

        List<TripPlaceResponse> items = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE);

        while (true) {
            var result = tripPlaceService.getTripPlaces(new GetTripPlacesRequest(
                    pageRequest,
                    new GetTripPlacesRequest.Filter(targetTripId, sourceType, null, null, null, null),
                    new GetTripPlacesRequest.OrderBy(true)
            ));
            if (result.isFailure()) {
                return Result.fail(result.getError());
            }

            Page<TripPlaceResponse> page = result.getValue();
            items.addAll(page.items());
            if (!page.hasNext()) {
                return Result.ok(items);
            }
            pageRequest = pageRequest.next();
        }
    }

    private String resolveSelectedCategoryId() {
        String selected = selectedValue(categorySelectModel);
        if (selected == null || selected.isBlank()) {
            return null;
        }

        if (availableCategories != null) {
            for (CategoryResponse category : availableCategories) {
                if (selected.equals(category.id()) || selected.equals(category.name())) {
                    return category.id();
                }
            }
        }

        return selected;
    }

    private StatusEnum mapTripStatus(TripStatus status) {
        if (status == null) {
            return StatusEnum.PLANNED;
        }

        return switch (status) {
            case VISITED -> StatusEnum.VISITED;
            case ONGOING -> StatusEnum.ONGOING;
            case PLANNED, DRAFTED, REJECTED -> StatusEnum.PLANNED;
        };
    }

    private Instant toInstant(LocalDate value) {
        return value == null ? null : value.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private TripStatus toLegacyTripStatus(StatusEnum status) {
        if (status == null) {
            return TripStatus.PLANNED;
        }
        return switch (status) {
            case VISITED -> TripStatus.VISITED;
            case ONGOING -> TripStatus.ONGOING;
            case PLANNED, CANCELED -> TripStatus.PLANNED;
        };
    }

    private LocalDate toLocalDate(Instant value) {
        return value == null ? null : value.atZone(ZoneOffset.UTC).toLocalDate();
    }

    private String formatMessage(String key, Object... args) {
        return MessageFormat.format(I18n.t(key), args);
    }

    private Image loadImage(String imagePath) {
        String resolvedPath = imagePath == null || imagePath.isBlank() ? DEFAULT_IMAGE : imagePath;
        if (resolvedPath.startsWith("/")) {
            var resource = getClass().getResource(resolvedPath);
            if (resource != null) {
                return new Image(resource.toExternalForm(), true);
            }
        }

        File file = new File(resolvedPath);
        if (file.exists()) {
            return new Image(file.toURI().toString(), true);
        }

        var fallback = getClass().getResource(DEFAULT_IMAGE);
        return new Image(fallback.toExternalForm(), true);
    }

    private boolean isSupportedImageFile(File file) {
        String lowerName = file.getName().toLowerCase(Locale.ROOT);
        return lowerName.endsWith(".png")
                || lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".svg");
    }

    private boolean isVectorImage(File file) {
        return file.getName().toLowerCase(Locale.ROOT).endsWith(".svg");
    }

    private boolean isVectorImagePath(String imagePath) {
        return imagePath != null && imagePath.toLowerCase(Locale.ROOT).endsWith(".svg");
    }

    private String extractImageName(String imagePath, String tripName) {
        if (imagePath == null || imagePath.isBlank()) {
            return createMode ? "" : safeText(tripName) + " cover";
        }

        int slashIndex = Math.max(imagePath.lastIndexOf('/'), imagePath.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex + 1 < imagePath.length()) {
            return imagePath.substring(slashIndex + 1);
        }

        return imagePath;
    }

    private void logWarnWithError(String message, AppError error) {
        if (error == null) {
            log.warn("{}", message);
            return;
        }

        if (error instanceof com.triplify.application.error.ApplicationError.StorageFailure storage && storage.cause() != null) {
            log.warn("{} [code={}, message={}]", message, error.code(), error.message(), storage.cause());
            return;
        }

        if (error instanceof com.triplify.application.error.ApplicationError.FileFailure fileFailure && fileFailure.cause() != null) {
            log.warn("{} [code={}, message={}]", message, error.code(), error.message(), fileFailure.cause());
            return;
        }

        log.warn("{} [code={}, message={}]", message, error.code(), error.message());
    }


    private record TagOption(String id, String label) { }

    private record RouteItem(String id, String title, String subtitle, String imagePath, List<PlaceItem> derivedPlaces) { }

    private record PlaceItem(
            String id,
            String title,
            String subtitle,
            String imagePath,
            TripPlaceSourceType sourceType,
            String sourceRouteId
    ) {
        private boolean isManual() {
            return sourceType != TripPlaceSourceType.ROUTE;
        }

        private PlaceItem manualCopy() {
            return new PlaceItem(id, title, subtitle, imagePath, TripPlaceSourceType.MANUAL, null);
        }
    }
}
