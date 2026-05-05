package com.triplify.ui.pages.trips;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.triplify.application.usecase.category.CategoryService;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.GetRouteByIdRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.tag.TagService;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.AddTripRequest;
import com.triplify.application.usecase.trip.dto.GetTripByIdRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.trip.dto.UpdateTripRequest;
import com.triplify.application.usecase.tripplace.TripPlaceService;
import com.triplify.application.usecase.tripplace.dto.ReplaceManualTripPlacesRequest;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.triproute.TripRouteService;
import com.triplify.application.usecase.triproute.dto.ReplaceTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.result.Result;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.pages.WindowedPageController;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.pages.categories.Categories;
import com.triplify.ui.pages.countries.model.Countries;
import com.triplify.ui.pages.countries.view.CountriesView;
import com.triplify.ui.shared.component.date_picker.DatePickerItem;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.media_card.view.EditorMediaCardView;
import com.triplify.ui.pages.places.model.Places;
import com.triplify.ui.pages.routes.model.Routes;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.model.SearchDisplayMode;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.component.tag_picker.TagPickerItem;
import com.triplify.ui.shared.component.upload_panel.view.ImageUploadPanelView;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import static com.triplify.ui.shared.util.DisplayUtils.toInstant;
import static com.triplify.ui.shared.util.DisplayUtils.toLocalDate;
import com.triplify.ui.shared.util.EditorUtils;
import static com.triplify.ui.shared.util.EditorUtils.configureButtonIcon;
import static com.triplify.ui.shared.util.EditorUtils.formatMessage;
import static com.triplify.ui.shared.util.EditorUtils.installRoundedClip;
import static com.triplify.ui.shared.util.EditorUtils.normalizeKey;
import static com.triplify.ui.shared.util.EditorUtils.normalizeNullable;
import static com.triplify.ui.shared.util.EditorUtils.safeText;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;
import com.triplify.ui.storage.EditorDraftStorage;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import rahulstech.jfx.routing.element.RouterArgument;

public class AddTripController extends WindowedPageController {

    private static final Logger log = LoggerFactory.getLogger(AddTripController.class);
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
    @FXML private VBox actionButtonsContainer;

    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;
    @Inject private UserSessionContext userSessionContext;
    @Inject private TripService tripService;
    @Inject private CategoryService categoryService;
    @Inject private TagService tagService;
    @Inject private RouteService routeService;
    @Inject private PlaceService placeService;
    @Inject private TripRouteService tripRouteService;
    @Inject private TripPlaceService tripPlaceService;
    @Inject private CountryService countryService;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private final Set<String> selectedCountryIds = new LinkedHashSet<>();
    private final Map<String, String> selectedCountryLabelsById = new LinkedHashMap<>();
    private final List<RouteItem> routeItems = new ArrayList<>();
    private final List<PlaceItem> placeItems = new ArrayList<>();

    private InputItem titleInput;
    private TextAreaItem descriptionInput;
    private String tripId;
    private boolean createMode;
    private StatusEnum tripStatus;
    private String coverImagePath;
    private String currentTripDisplayName;
    private DatePickerItem startDateInput;
    private DatePickerItem endDateInput;
    private CountriesView countrySelectView;
    private Categories categoriesComponent;
    private Entry<String> pendingCountryEntry;
    private Select<String> categorySelectModel;
    private SelectView<String> categorySelectView;
    private StackPane uploadArea;
    private SearchView<RouteItem> routeSearchView;
    private SearchView<PlaceItem> placeSearchView;
    private Routes routesModel;
    private Places placesModel;
    private List<CategoryResponse> availableCategories = List.of();
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

        contentFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        EditorUtils.initializeCoverPreview(imageUploadPanel.getCoverPreview(), uploadArea);
        bindUploadPanelHandlers();

        configureButtonIcon(addCountryButton, "fth-plus");
        configureButtonIcon(addRouteButton, "fth-plus");
        configureButtonIcon(addPlaceButton, "fth-plus");
        createActionButtons();

        installRoundedClip(uploadArea, 16);
        bindLocalizedText();
        configureTagPicker();
        initializeCountrySelector();
        categoriesComponent = Categories.builder(categoryService).build();
        loadAvailableCategories();
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
        Object rawTripId = data == null ? null : data.getValue("tripId");
        tripId = rawTripId == null ? null : rawTripId.toString();
        createMode = tripId == null || tripId.isBlank() || "0".equals(tripId);
        tripStatus = data == null ? null : data.getValue("tripStatus");

        EditorDraftStorage.TripDraft draft = EditorDraftStorage.consumeTripDraft();
        if (matchesTripDraft(draft)) {
            applyTripDraft(draft);
        } else if (createMode) {
            currentTripDisplayName = I18n.t("trip.add.default.name");
        } else {
            loadExistingTrip();
        }

        consumeReturnedEditorResults();
        log.info("Trip editor opened: id={}, createMode={}", tripId, createMode);
    }

    @Override
    public void onWindowedShow() {
        EditorDraftStorage.clearTripDraft();
        consumeReturnedEditorResults();
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
        if (!isProUser() && !routeItems.isEmpty()) {
            toast.error(I18n.t("trip.add.toast.route.pro.required"));
            return;
        }
        boolean nextState = !routePickerContainer.isVisible();
        setRoutePickerVisible(nextState);
        if (nextState) setPlacePickerVisible(false);
    }

    @FXML
    private void onAddPlace() {
        boolean nextState = !placePickerContainer.isVisible();
        setPlacePickerVisible(nextState);
        if (nextState) setRoutePickerVisible(false);
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
        if (!isProUser() && !routeItems.isEmpty()) {
            toast.error(I18n.t("trip.add.toast.route.pro.required"));
            setRoutePickerVisible(false);
            return;
        }
        setRoutePickerVisible(false);
        EditorDraftStorage.saveTripDraft(captureTripDraft());
        RouterArgument args = new RouterArgument();
        String targetTripId = tripId == null || tripId.isBlank() ? "0" : tripId;
        String targetTripName = titleInput.getText().isBlank() ? currentTripDisplayName : titleInput.getText().trim();
        args.addArgument("tripId", targetTripId);
        args.addArgument("tripName", targetTripName.isBlank() ? I18n.t("trip.add.default.name") : targetTripName);
        args.addArgument("editorReturnTarget", EditorDraftStorage.TARGET_TRIP);
        getRouter().moveto(RouteIds.ADD_ROUTE, args);
    }

    @FXML
    private void onCreatePlace() {
        setPlacePickerVisible(false);
        EditorDraftStorage.saveTripDraft(captureTripDraft());
        RouterArgument args = new RouterArgument();
        String targetTripId = tripId == null || tripId.isBlank() ? "0" : tripId;
        String targetTripName = titleInput.getText().isBlank() ? currentTripDisplayName : titleInput.getText().trim();
        args.addArgument("tripId", targetTripId);
        args.addArgument("tripName", targetTripName.isBlank() ? I18n.t("trip.add.default.name") : targetTripName);
        args.addArgument("editorReturnTarget", EditorDraftStorage.TARGET_TRIP);
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    @FXML
    private void onSave() {
        clearFieldErrors();

        Map<String, Consumer<String>> fieldHandlers = Map.of(
                "title", message -> titleInput.showError(message),
                "categoryId", message -> {
                    if (categorySelectView != null) {
                        categorySelectView.showError(message);
                    }
                },
                "countryIds", message -> {
                    if (countrySelectView != null) {
                        countrySelectView.showError(message);
                    }
                }
        );

        String tripTitle = titleInput.getText().trim();
        StatusEnum status = tripStatus != null ? tripStatus : StatusEnum.PLANNED;
        Instant startedAt = toInstant(startDateInput.getValue());
        Instant endedAt = toInstant(endDateInput.getValue());
        UUID categoryId = EditorUtils.parseUUID(resolveSelectedCategoryId());
        Set<UUID> tagIds = tagPickerInput.getSelectedTagIds();
        Set<UUID> countryIds = selectedCountryIds.stream()
            .map(EditorUtils::parseUUID)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Path coverImage = coverImagePath == null || coverImagePath.isBlank()
            ? null
            : Path.of(coverImagePath);

        var result = createMode ?
                tripService.addTrip(new AddTripRequest(
                        categoryId,
                        tripTitle,
                        descriptionInput.getText(),
                        status,
                        startedAt,
                        endedAt,
                        tagIds,
                        coverImage,
                        countryIds
                )) :
                tripService.updateTrip(new UpdateTripRequest(
                        UUID.fromString(tripId),
                        categoryId,
                        tripTitle,
                        descriptionInput.getText(),
                        status,
                        startedAt,
                        endedAt,
                        tagIds,
                        coverImage,
                        countryIds
                ));

        result.onSuccess(savedTrip -> {
            var relationsResult = syncTripRelations(savedTrip.id());
            if (relationsResult.isFailure()) {
                errorHandler.handle(relationsResult.getError(), fieldHandlers);
                return;
            }
            String message = createMode
                    ? formatMessage("trip.add.toast.trip.ready", tripTitle)
                    : formatMessage("trip.add.toast.trip.updated", tripTitle);
            EditorDraftStorage.clearTripDraft();
            toast.success(I18n.t("trip.add.toast.title.saved"), message);
            getRouter().popBackStack();
        });
        result.onFailure(error -> errorHandler.handle(error, fieldHandlers));
    }

    @FXML
    private void onChooseCoverImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.t("trip.add.dialog.cover.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.t("trip.add.dialog.cover.filter"), "*.png", "*.jpg", "*.jpeg")
        );
        File file = chooser.showOpenDialog(uploadArea.getScene() == null ? null : uploadArea.getScene().getWindow());
        if (file != null) {
            EditorUtils.handleCoverImageFile(
                    file, imageUploadPanel.getCoverPreview(), uploadArea,
                    imageUploadPanel.getUploadPlaceholder(), imageUploadPanel.getSelectedImageLabel(),
                    toast, "trip.add.toast.image.previewUnavailable",
                    path -> {
                        coverImagePath = path;
                        coverImageDirty = true;
                    }
            );
        }
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

    private void clearFieldErrors() {
        titleInput.clearError();
        categorySelectView.clearError();
        countrySelectView.clearError();
    }

    private void createActionButtons() {
        Button saveButton = AppButtonView.builder(fxmlLoader)
                .labelBinding(Localization.textBinding("trip.add.action.save"))
                .variant(ButtonVariant.PRIMARY)
                .icon("fth-save")
                .onAction(this::onSave)
                .build();
        saveButton.getStyleClass().add("editor-action-button");
        saveButton.setMaxWidth(Double.MAX_VALUE);

        Button discardButton = AppButtonView.builder(fxmlLoader)
                .labelBinding(Localization.textBinding("trip.add.action.discard"))
                .variant(ButtonVariant.DANGER_OUTLINE)
                .icon("fth-trash-2")
                .onAction(this::onDiscard)
                .build();
        discardButton.getStyleClass().add("editor-action-button");
        discardButton.setMaxWidth(Double.MAX_VALUE);

        actionButtonsContainer.getChildren().setAll(saveButton, discardButton);
    }

    private void refreshLocalizedUi() {
        String selectedCategory = selectedValue(categorySelectModel);
        categorySelectModel = Select.<String>builder()
                .placeholder(I18n.t("trip.add.select.category"))
                .variant(FieldVariant.GHOST)
                .items(categoriesComponent.toEntries(availableCategories))
                .build();
        categorySelectModel.setSelectedItem(findEntry(categorySelectModel, selectedCategory));
        categorySelectView = createSelectView(categorySelectModel);
        categorySelectContainer.getChildren().setAll(categorySelectView);

        tagPickerInput.setPlaceholderText(I18n.t("trip.add.select.tag"));
        tagPickerInput.setPopupTitle(I18n.t("trip.add.tag.popupTitle"));

        if (routePickerContainer.isVisible()) refreshRoutePicker();
        if (placePickerContainer.isVisible()) refreshPlacePicker();
        renderCountryChips();
        renderRoutes();
        renderPlaces();
    }

    private void loadAvailableCategories() {
        var result = categoriesComponent.loadAll();
        if (result.isFailure()) {
            log.warn("Failed to load categories [code={}, message={}]", result.getError().code(), result.getError().message());
            availableCategories = List.of();
            return;
        }
        availableCategories = result.getValue();
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
        List<PlaceItem> displayed = displayedPlaceItems();
        if (displayed.isEmpty()) {
            Region emptyState = createEmptyState(I18n.t("trip.add.empty.places"));
            emptyState.prefWidthProperty().bind(placesFlow.widthProperty());
            placesFlow.getChildren().add(emptyState);
            return;
        }
        for (PlaceItem item : displayed) {
            placesFlow.getChildren().add(buildPlaceCard(item));
        }
    }

    private VBox buildRouteCard(RouteItem item) {
        EditorMediaCardView card = new EditorMediaCardView();
        card.setPreviewImage(EditorUtils.loadImage(item.imagePath(), DEFAULT_IMAGE, getClass()));
        card.setTitle(item.title());
        card.setSubtitle(item.subtitle());
        card.setCursor(Cursor.HAND);
        card.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (isClickOnButton(event.getTarget())) return;
            RouterArgument args = new RouterArgument();
            args.addArgument("routeId", item.id());
            getRouter().moveto(RouteIds.ROUTE_DETAILS, args);
            event.consume();
        });
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
        card.setPreviewImage(EditorUtils.loadImage(item.imagePath(), DEFAULT_IMAGE, getClass()));
        card.setTitle(item.title());
        card.setSubtitle(item.subtitle());
        card.setRemoveVisible(item.isManual());
        card.setCursor(Cursor.HAND);
        card.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (isClickOnButton(event.getTarget())) return;
            RouterArgument args = new RouterArgument();
            args.addArgument("placeId", item.id());
            getRouter().moveto(RouteIds.PLACE_DETAILS, args);
            event.consume();
        });
        if (item.isManual()) {
            card.setOnRemove(() -> {
                placeItems.removeIf(existing -> item.id().equals(existing.id()));
                renderPlaces();
                refreshPlacePicker();
            });
        }
        return card;
    }

    private boolean isClickOnButton(Object target) {
        Node current = target instanceof Node node ? node : null;
        while (current != null) {
            if (current instanceof Button) return true;
            current = current.getParent();
        }
        return false;
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
            imageUploadPanel.getCoverPreview().setImage(null);
            imageUploadPanel.getCoverPreview().setVisible(false);
            imageUploadPanel.getCoverPreview().setManaged(false);
            imageUploadPanel.getUploadPlaceholder().setVisible(true);
            imageUploadPanel.getUploadPlaceholder().setManaged(true);
            imageUploadPanel.getSelectedImageLabel().setVisible(false);
            imageUploadPanel.getSelectedImageLabel().setManaged(false);
            imageUploadPanel.getSelectedImageLabel().setText("");
            return;
        }

        int slashIndex = Math.max(imagePath.lastIndexOf('/'), imagePath.lastIndexOf('\\'));
        String fileName = slashIndex >= 0 && slashIndex + 1 < imagePath.length()
                ? imagePath.substring(slashIndex + 1)
                : (tripName != null && !tripName.isBlank() ? safeText(tripName) + " cover" : imagePath);
        imageUploadPanel.getSelectedImageLabel().setText(fileName);
        imageUploadPanel.getSelectedImageLabel().setVisible(true);
        imageUploadPanel.getSelectedImageLabel().setManaged(true);

        EditorUtils.setCoverPreviewImage(imageUploadPanel.getCoverPreview(), uploadArea,
                EditorUtils.loadImage(imagePath, DEFAULT_IMAGE, getClass()));
        imageUploadPanel.getCoverPreview().setVisible(true);
        imageUploadPanel.getCoverPreview().setManaged(false);
        imageUploadPanel.getUploadPlaceholder().setVisible(false);
        imageUploadPanel.getUploadPlaceholder().setManaged(false);
    }

    private void initializeActionPickers() {
        FontIcon routeIcon = new FontIcon("fth-edit-2");
        routeIcon.setIconSize(14);
        routeIcon.getStyleClass().add("trip-editor-picker-create-icon");
        routeCreateButton.setGraphic(routeIcon);

        FontIcon placeIcon = new FontIcon("fth-edit-2");
        placeIcon.setIconSize(14);
        placeIcon.getStyleClass().add("trip-editor-picker-create-icon");
        placeCreateButton.setGraphic(placeIcon);

        initializePickerSearches();
        setRoutePickerVisible(false);
        setPlacePickerVisible(false);
    }

    private void initializePickerSearches() {
        routesModel = Routes.builder(routeService)
                .onLoadFailed(error -> {
                    log.warn("Failed to load routes [code={}, message={}]", error.code(), error.message());
                    toast.warning(I18n.t("trip.add.toast.routes.loadFailed"));
                })
                .build();
        placesModel = Places.builder(placeService)
                .onLoadFailed(error -> {
                    log.warn("Failed to load places [code={}, message={}]", error.code(), error.message());
                    toast.warning(I18n.t("trip.add.toast.places.loadFailed"));
                })
                .build();

        Search<RouteItem> routeSearchModel = Search.<RouteItem>builder(this::searchRouteEntries)
                .placeholderKey("trip.add.picker.route.search")
                .noResultKey("trip.add.menu.route.empty")
                .variant(FieldVariant.GHOST)
                .displayMode(SearchDisplayMode.INLINE)
                .size(AppComponentSize.MIDDLE)
                .maxVisibleResults(8)
                .showOnEmptyQuery(true)
                .onLoadMore(this::loadMoreRouteEntries)
                .onResultSelected(entry -> addExistingRoute(entry.getValue()))
                .build();
        routeSearchView = new SearchView<>(routeSearchModel);
        routeSearchContainer.getChildren().setAll(routeSearchView);

        Search<PlaceItem> placeSearchModel = Search.<PlaceItem>builder(this::searchPlaceEntries)
                .placeholderKey("trip.add.picker.place.search")
                .noResultKey("trip.add.menu.place.empty")
                .variant(FieldVariant.GHOST)
                .displayMode(SearchDisplayMode.INLINE)
                .size(AppComponentSize.MIDDLE)
                .maxVisibleResults(8)
                .showOnEmptyQuery(true)
                .onLoadMore(this::loadMorePlaceEntries)
                .onResultSelected(entry -> addExistingPlace(entry.getValue()))
                .build();
        placeSearchView = new SearchView<>(placeSearchModel);
        placeSearchContainer.getChildren().setAll(placeSearchView);
    }

    private void setRoutePickerVisible(boolean visible) {
        routePickerContainer.setVisible(visible);
        routePickerContainer.setManaged(visible);
        EditorUtils.toggleStyleClass(addRouteButton, "trip-editor-add-action-button-expanded", visible);
        if (visible) {
            refreshRoutePicker();
        } else {
            routeSearchView.getSearchField().clear();
        }
    }

    private void setPlacePickerVisible(boolean visible) {
        placePickerContainer.setVisible(visible);
        placePickerContainer.setManaged(visible);
        EditorUtils.toggleStyleClass(addPlaceButton, "trip-editor-add-action-button-expanded", visible);
        if (visible) {
            refreshPlacePicker();
        } else {
            placeSearchView.getSearchField().clear();
        }
    }

    private void refreshRoutePicker() {
        routesModel.reset();
        routeSearchView.getSearchField().setText(routeSearchView.getSearchField().getText());
    }

    private void refreshPlacePicker() {
        placesModel.reset();
        placeSearchView.getSearchField().setText(placeSearchView.getSearchField().getText());
    }

    private List<Entry<RouteItem>> searchRouteEntries(String query) {
        return toRouteEntries(routesModel.search(query));
    }

    private List<Entry<RouteItem>> loadMoreRouteEntries(String query) {
        return toRouteEntries(routesModel.loadMore(query));
    }

    private List<Entry<RouteItem>> toRouteEntries(List<RouteResponse> routes) {
        return routes.stream()
                .filter(route -> !containsRoute(route.id().toString()))
                .map(route -> {
                    RouteItem item = toRouteItem(route);
                    return Entry.builder(item, item.title()).icon("fth-map").build();
                })
                .toList();
    }

    private List<Entry<PlaceItem>> searchPlaceEntries(String query) {
        return toPlaceEntries(placesModel.search(query));
    }

    private List<Entry<PlaceItem>> loadMorePlaceEntries(String query) {
        return toPlaceEntries(placesModel.loadMore(query));
    }

    private List<Entry<PlaceItem>> toPlaceEntries(List<PlaceResponse> places) {
        return places.stream()
                .filter(place -> !containsPlace(place.id().toString()))
                .map(place -> {
                    PlaceItem item = toManualPlaceItem(place);
                    return Entry.builder(item, item.title()).icon("fth-map-pin").build();
                })
                .toList();
    }

    private void addExistingRoute(RouteItem route) {
        if (!isProUser() && !routeItems.isEmpty()) {
            toast.error(I18n.t("trip.add.toast.route.pro.required"));
            setRoutePickerVisible(false);
            return;
        }
        if (containsRoute(route.id())) {
            toast.info(I18n.t("trip.add.toast.route.duplicate"));
            return;
        }
        RouteItem routeWithPlaces = route;
        if (route.derivedPlaces().isEmpty()) {
            var fullResult = routeService.getRouteById(new GetRouteByIdRequest(UUID.fromString(route.id())));
            if (fullResult.isSuccess()) {
                routeWithPlaces = toRouteItem(fullResult.getValue());
            } else {
                log.warn("Failed to load route places [routeId={}, code={}, message={}]",
                        route.id(), fullResult.getError().code(), fullResult.getError().message());
            }
        }
        routeItems.add(routeWithPlaces);
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

    private boolean isProUser() {
        return userSessionContext.getCurrent()
                .map(user -> user.role() == RoleEnum.PRO_USER || user.role() == RoleEnum.CONFIGURATION_MANAGER)
                .orElse(false);
    }

    private boolean containsRoute(String routeId) {
        return routeId != null && routeItems.stream().anyMatch(item -> routeId.equals(item.id()));
    }

    private boolean containsPlace(String placeId) {
        return placeId != null && displayedPlaceItems().stream().anyMatch(item -> placeId.equals(item.id()));
    }

    private List<PlaceItem> displayedPlaceItems() {
        Map<String, PlaceItem> itemsById = new LinkedHashMap<>();
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

    private Result<Void> syncTripRelations(UUID savedTripId) {
        List<UUID> routeIds = routeItems.stream().map(r -> UUID.fromString(r.id())).toList();
        Result<Void> routeResult = tripRouteService.replaceTripRoutes(new ReplaceTripRoutesRequest(savedTripId, routeIds));
        if (routeResult.isFailure()) return routeResult;

        List<UUID> placeIds = placeItems.stream().map(p -> UUID.fromString(p.id())).toList();
        return tripPlaceService.replaceManualTripPlaces(new ReplaceManualTripPlacesRequest(savedTripId, placeIds));
    }

    private void loadExistingTrip() {
        var result = tripService.getTripById(new GetTripByIdRequest(UUID.fromString(tripId)));
        if (result.isFailure()) {
            log.warn("Failed to load trip [tripId={}, code={}, message={}]", tripId, result.getError().code(), result.getError().message());
            toast.error(I18n.t("trip.add.toast.title.saved"), result.getError().message());
            Platform.runLater(() -> getRouter().popBackStack());
            return;
        }
        applyTripResponse(result.getValue());
    }

    private void applyTripResponse(TripResponse trip) {
        currentTripDisplayName = trip.title() == null || trip.title().isBlank()
                ? I18n.t("trip.add.default.name")
                : trip.title();
        tripStatus = trip.status();

        titleInput.setText(safeText(trip.title()));
        descriptionInput.setText(safeText(trip.description()));
        startDateInput.setValue(toLocalDate(trip.startedAt()));
        endDateInput.setValue(toLocalDate(trip.endedAt()));

        selectedCountryIds.clear();
        selectedCountryLabelsById.clear();
        if (trip.countries() != null) {
            for (var country : trip.countries()) {
                selectedCountryIds.add(country.id().toString());
                selectedCountryLabelsById.put(country.id().toString(), country.name());
            }
        }
        renderCountryChips();

        tagPickerInput.setSelectedTagIds(
            trip.tags() == null
                ? Set.of()
                : trip.tags().stream()
                    .map(TagResponse::id)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new))
        );

        String categoryId = trip.category() == null ? null : trip.category().id().toString();
        categorySelectModel.setSelectedItem(findEntry(categorySelectModel, categoryId));

        String coverPath = trip.coverImage() == null || trip.coverImage().url() == null
                ? null
                : trip.coverImage().url().toString();
        coverImageDirty = false;
        showCoverImage(coverPath, trip.title());

        loadTripRoutes(trip.id());
        loadTripPlaces(trip.id());
    }

    private void loadTripRoutes(UUID targetTripId) {
        routeItems.clear();
        var result = tripRouteService.getAllTripRoutes(targetTripId);
        if (result.isFailure()) {
            log.warn("Failed to load trip routes [tripId={}, code={}, message={}]", targetTripId, result.getError().code(), result.getError().message());
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

    private void loadTripPlaces(UUID targetTripId) {
        placeItems.clear();
        var result = tripPlaceService.getAllManualTripPlaces(targetTripId);
        if (result.isFailure()) {
            log.warn("Failed to load trip places [tripId={}, code={}, message={}]", targetTripId, result.getError().code(), result.getError().message());
            toast.warning(I18n.t("trip.add.toast.places.loadFailed"));
            renderPlaces();
            return;
        }
        result.getValue().stream()
                .map(TripPlaceResponse::place)
                .filter(Objects::nonNull)
                .map(this::toManualPlaceItem)
                .forEach(placeItems::add);
        renderPlaces();
    }

    private void consumeReturnedEditorResults() {
        RouteResponse createdRoute = EditorDraftStorage.consumePendingRoute(EditorDraftStorage.TARGET_TRIP);
        if (createdRoute != null) addExistingRoute(toRouteItem(createdRoute));

        PlaceResponse createdPlace = EditorDraftStorage.consumePendingPlace(EditorDraftStorage.TARGET_TRIP);
        if (createdPlace != null) addExistingPlace(toManualPlaceItem(createdPlace));
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
                new LinkedHashMap<>(selectedCountryLabelsById),
                new LinkedHashSet<>(tagPickerInput.getSelectedTagIds()),
                coverImagePath,
                coverImageDirty,
                routeItems.stream()
                        .map(route -> new EditorDraftStorage.RouteDraftItem(
                                route.id(), route.title(), route.subtitle(), route.imagePath(),
                                route.derivedPlaces().stream()
                                        .map(place -> new EditorDraftStorage.PlaceDraftItem(
                                                place.id(), place.title(), place.subtitle(), place.imagePath(),
                                                place.sourceType(), place.sourceRouteId(), null, null))
                                        .toList()))
                        .toList(),
                placeItems.stream()
                        .map(place -> new EditorDraftStorage.PlaceDraftItem(
                                place.id(), place.title(), place.subtitle(), place.imagePath(),
                                place.sourceType(), place.sourceRouteId(), null, null))
                        .toList()
        );
    }

    private boolean matchesTripDraft(EditorDraftStorage.TripDraft draft) {
        return draft != null && normalizeKey(tripId).equals(normalizeKey(draft.tripId()));
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

        tagPickerInput.setSelectedTagIds(draft.selectedTagIds());

        categorySelectModel.setSelectedItem(findEntry(categorySelectModel, draft.categoryValue()));

        routeItems.clear();
        draft.routes().stream()
                .map(route -> new RouteItem(
                        route.id(), route.title(), route.subtitle(), route.imagePath(),
                        route.derivedPlaces().stream()
                                .map(place -> new PlaceItem(
                                        place.id(), place.title(), place.subtitle(), place.imagePath(),
                                        place.sourceType() == null ? TripPlaceSourceType.ROUTE : place.sourceType(),
                                        place.sourceRouteId()))
                                .toList()))
                .forEach(routeItems::add);

        placeItems.clear();
        draft.manualPlaces().stream()
                .map(place -> new PlaceItem(
                        place.id(), place.title(), place.subtitle(), place.imagePath(),
                        place.sourceType() == null ? TripPlaceSourceType.MANUAL : place.sourceType(),
                        place.sourceRouteId()))
                .forEach(placeItems::add);

        coverImageDirty = draft.coverImageDirty();
        showCoverImage(draft.coverImagePath(), draft.title());
        renderRoutes();
        renderPlaces();
    }

    private void configureTagPicker() {
        tagPickerInput.setAllowCustomTags(true);
        tagPickerInput.configureTagService(tagService, message ->
                toast.warning(I18n.t("trip.add.toast.tag.operation.failed"), message)
        );
    }

    private void bindUploadPanelHandlers() {
        imageUploadPanel.setOnUploadClicked(event -> onChooseCoverImage());
        imageUploadPanel.setOnImageFileSelected(file -> EditorUtils.handleCoverImageFile(
                file, imageUploadPanel.getCoverPreview(), uploadArea,
                imageUploadPanel.getUploadPlaceholder(), imageUploadPanel.getSelectedImageLabel(),
                toast, "trip.add.toast.image.previewUnavailable",
                path -> {
                    coverImagePath = path;
                    coverImageDirty = true;
                }
        ));
        imageUploadPanel.setOnUnsupportedImageFile(file -> toast.warning(I18n.t("trip.add.toast.image.unsupported")));
        imageUploadPanel.installDefaultImageDragAndDrop();
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
            if (isShowing) Platform.runLater(() -> styleSelectPopup(view));
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
        if (!(arrowButton instanceof StackPane buttonPane)) return;
        boolean alreadyStyled = buttonPane.getChildren().stream()
                .anyMatch(node -> node.getStyleClass().contains("trip-editor-select-chevron"));
        if (alreadyStyled) return;
        FontIcon chevronIcon = new FontIcon("fth-chevron-down");
        chevronIcon.setIconSize(14);
        chevronIcon.getStyleClass().addAll("app-tag-picker-toggle-icon", "trip-editor-select-chevron");
        buttonPane.getChildren().setAll(chevronIcon);
    }

    private Entry<String> findEntry(Select<String> model, String value) {
        if (model == null || value == null || value.isBlank()) return null;
        return model.getItems().stream()
                .filter(entry -> value.equals(entry.getValue()) || value.equals(entry.getLabel()))
                .findFirst()
                .orElse(null);
    }

    private String selectedValue(Select<String> model) {
        return model == null || model.getSelectedItem() == null ? null : model.getSelectedItem().getValue();
    }

    private String resolveSelectedCategoryId() {
        String selected = selectedValue(categorySelectModel);
        if (selected == null || selected.isBlank()) return null;
        for (CategoryResponse category : availableCategories) {
            if (selected.equals(category.id().toString()) || selected.equals(category.name())) {
                return category.id().toString();
            }
        }
        return selected;
    }

    private RouteItem toRouteItem(RouteResponse response) {
        String subtitle = response.description() == null || response.description().isBlank()
                ? formatMessage("trip.add.route.length", String.format(Locale.US, "%.1f", response.length()))
                : response.description();
        return new RouteItem(
                response.id().toString(),
                safeText(response.title()).isBlank() ? I18n.t("trip.add.fallback.route") : response.title(),
                subtitle,
                imagePath(response.coverImage() == null ? null : response.coverImage().url()),
                response.places() == null
                        ? List.of()
                        : response.places().stream()
                                .map(place -> toRouteDerivedPlaceItem(place, response.id().toString()))
                                .toList()
        );
    }

    private PlaceItem toManualPlaceItem(PlaceResponse response) {
        return new PlaceItem(
                response.id().toString(),
                safeText(response.title()).isBlank() ? I18n.t("trip.add.fallback.place") : response.title(),
                placeSubtitle(response),
                imagePath(response.coverImage() == null ? null : response.coverImage().url()),
                TripPlaceSourceType.MANUAL,
                null
        );
    }

    private PlaceItem toRouteDerivedPlaceItem(PlaceResponse response, String routeId) {
        return new PlaceItem(
                response.id().toString(),
                safeText(response.title()).isBlank() ? I18n.t("trip.add.fallback.place") : response.title(),
                placeSubtitle(response),
                imagePath(response.coverImage() == null ? null : response.coverImage().url()),
                TripPlaceSourceType.ROUTE,
                routeId
        );
    }

    private String placeSubtitle(PlaceResponse response) {
        return response.country() != null && response.country().name() != null && !response.country().name().isBlank()
                ? response.country().name()
                : safeText(response.description());
    }

    private String imagePath(java.nio.file.Path path) {
        return path == null ? DEFAULT_IMAGE : path.toString();
    }

    private record RouteItem(String id, String title, String subtitle, String imagePath, List<PlaceItem> derivedPlaces) {}

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
