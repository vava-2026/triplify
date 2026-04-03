package com.triplify.ui.pages.trips;

import com.google.inject.Inject;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.response.TripStatus;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.GetRoutesRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.countries.model.Countries;
import com.triplify.ui.shared.component.countries.view.CountriesView;
import com.triplify.ui.shared.component.date_picker.DatePickerItem;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.tag_picker.TagPickerItem;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.upload_panel.view.ImageUploadPanelView;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.Localization;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
import java.time.LocalDate;
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

    private static final List<String> AVAILABLE_CATEGORIES = List.of(
            "Culture", "Tourism", "Nature", "Relax", "Memorial", "Food"
    );
    private static final List<String> AVAILABLE_TAGS = List.of(
            "City", "Adventure", "Food", "Study", "Hike", "Relax", "Family", "Photography"
    );
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
    @FXML private VBox tagPickerContainer;
    @FXML private VBox routeListContainer;
    @FXML private FlowPane placesFlow;
    @FXML private VBox routePickerContainer;
    @FXML private VBox routePickerPanel;
    @FXML private TextField routeSearchField;
    @FXML private VBox routePickerResultsContainer;
    @FXML private Button routeCreateButton;
    @FXML private Button routePickerCloseButton;
    @FXML private VBox placePickerContainer;
    @FXML private VBox placePickerPanel;
    @FXML private TextField placeSearchField;
    @FXML private VBox placePickerResultsContainer;
    @FXML private Button placeCreateButton;
    @FXML private Button placePickerCloseButton;

    @FXML private Button addCountryButton;
    @FXML private Button addRouteButton;
    @FXML private Button addPlaceButton;
    @FXML private Button saveButton;
    @FXML private Button discardButton;

    @Inject private ToastService toast;
    @Inject private RouteService routeService;
    @Inject private PlaceService placeService;
    @Inject private CountryService countryService;

    private final Set<String> selectedCountryIds = new LinkedHashSet<>();
    private final Map<String, String> selectedCountryLabelsById = new java.util.LinkedHashMap<>();
    private final Set<String> selectedTags = new LinkedHashSet<>();
    private final List<RouteItem> routeItems = new ArrayList<>();
    private final List<PlaceItem> placeItems = new ArrayList<>();

    private InputItem titleInput;
    private TextAreaItem descriptionInput;
    private Long tripId;
    private boolean createMode;
    private TripStatus tripStatus;
    private String coverImagePath;
    private String currentTripDisplayName = "New Trip";
    private DatePickerItem startDateInput;
    private DatePickerItem endDateInput;
    private TagPickerItem tagPickerInput;
    private CountriesView countrySelectView;
    private Entry<String> pendingCountryEntry;
    private Select<String> categorySelectModel;
    private StackPane uploadArea;
    private ImageView coverPreview;
    private VBox uploadPlaceholder;
    private Label selectedImageLabel;
    private List<RouteItem> availableRouteCandidates = List.of();
    private List<PlaceItem> availablePlaceCandidates = List.of();

    @FXML
    public void initialize() {
        titleInput = createInput("input.placeholder.tripTitle");
        descriptionInput = createTextArea("input.placeholder.tripDescription");
        startDateInput = createDatePickerItem("dd/MM/yyyy");
        endDateInput = createDatePickerItem("dd/MM/yyyy");
        tagPickerInput = createTagPickerItem();

        titleInputContainer.getChildren().add(titleInput);
        descriptionInputContainer.getChildren().add(descriptionInput);
        startDateContainer.getChildren().add(startDateInput);
        endDateContainer.getChildren().add(endDateInput);
        tagPickerContainer.getChildren().add(tagPickerInput);
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
        configureButtonIcon(saveButton, "fth-save");
        configureButtonIcon(discardButton, "fth-trash-2");

        installRoundedClip(uploadArea, 16);
        bindLocalizedText();
        initializeCountrySelector();
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
        createMode = tripId == null || tripId <= 0;
        tripStatus = data == null ? null : data.getValue("tripStatus");

        String tripName = data == null ? null : data.getValue("tripName");
        String tripCountry = data == null ? null : data.getValue("tripCountry");
        String tripCategory = data == null ? null : data.getValue("tripCategory");
        String tripCoverUrl = data == null ? null : data.getValue("tripCoverUrl");
        String tripTags = data == null ? null : data.getValue("tripTags");
        String tripDates = data == null ? null : data.getValue("tripDates");
        String tripStartDate = data == null ? null : data.getValue("tripStartDate");
        String tripEndDate = data == null ? null : data.getValue("tripEndDate");

        populateHeader(tripName, tripDates);
        populateForm(tripName, tripCountry, tripCategory, tripTags, tripStartDate, tripEndDate);
        populateDemoLists(tripName, tripCountry);
        showCoverImage(tripCoverUrl, tripName);

        log.info("Trip editor opened: id={}, name={}, createMode={}", tripId, tripName, createMode);
    }

    @Override
    public void onLifecycleShow() {
        updateFullScreenMode(false);
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
        openCreateRoute();
    }

    @FXML
    private void onCreatePlace() {
        setPlacePickerVisible(false);
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

        String tripTitle = titleInput.getText().trim();
        String message = createMode
                ? formatMessage("trip.add.toast.trip.ready", tripTitle)
                : formatMessage("trip.add.toast.trip.updated", tripTitle);
        toast.success(I18n.t("trip.add.toast.title.saved"), message);
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
        Localization.bindText(saveButton.textProperty(), "trip.add.action.save");
        Localization.bindText(discardButton.textProperty(), "trip.add.action.discard");
        Localization.bindText(routeSearchField.promptTextProperty(), "trip.add.picker.route.search");
        Localization.bindText(placeSearchField.promptTextProperty(), "trip.add.picker.place.search");
    }

    private void refreshLocalizedUi() {
        String selectedCategory = selectedValue(categorySelectModel);

        categorySelectModel = createSelectModel(AVAILABLE_CATEGORIES, "trip.add.select.category");

        categorySelectModel.setSelectedItem(findEntry(categorySelectModel, selectedCategory));

        categorySelectContainer.getChildren().setAll(createSelectView(categorySelectModel));

        tagPickerInput.setPlaceholderText(I18n.t("trip.add.select.tag"));
        tagPickerInput.setPopupTitle(I18n.t("trip.add.tag.popupTitle"));
        tagPickerInput.setAvailableTags(AVAILABLE_TAGS);
        tagPickerInput.setSelectedTags(selectedTags);

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

        selectedTags.clear();
        parseCsv(tripTagsCsv).forEach(selectedTags::add);
        syncTagPicker();

        categorySelectModel.setSelectedItem(findEntry(categorySelectModel, tripCategory));

        startDateInput.setValue(parseDate(tripStartDate));
        endDateInput.setValue(parseDate(tripEndDate));
    }

    private void populateDemoLists(String tripName, String tripCountry) {
        routeItems.clear();
        placeItems.clear();

        if (createMode) {
            renderRoutes();
            renderPlaces();
            return;
        }

        String fallbackCountry = safeText(tripCountry).isBlank() ? "Trip route" : tripCountry;
        routeItems.add(new RouteItem(
                "demo-route-1",
                safeText(tripName).isBlank() ? fallbackCountry + " Centre" : tripName + " Centre",
                "Scenic transfer",
                DEFAULT_IMAGE
        ));
        routeItems.add(new RouteItem(
                "demo-route-2",
                fallbackCountry + " Highlights",
                "City walk",
                DEFAULT_IMAGE
        ));

        placeItems.add(new PlaceItem(
                "demo-place-1",
                fallbackCountry + " Cathedral",
                safeText(tripCountry).isBlank() ? "Old Town" : tripCountry,
                DEFAULT_IMAGE
        ));
        placeItems.add(new PlaceItem(
                "demo-place-2",
                safeText(tripName).isBlank() ? "Central Park" : tripName + " Viewpoint",
                "Popular stop",
                DEFAULT_IMAGE
        ));

        renderRoutes();
        renderPlaces();
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
        Countries countriesModel = Countries.builder(countryService)
                .placeholderKey("trip.add.select.country")
                .noResultKey("search.noResult")
                .variant(FieldVariant.FILLED)
                .searchOnTyping(true)
                .onResultSelected(entry -> pendingCountryEntry = entry)
                .onLoadFailed(error -> toast.error(I18n.t(error.code())))
                .build();

        countrySelectView = new CountriesView(countriesModel);
        countrySelectView.getStyleClass().add("trip-editor-country-picker");
        countrySelectContainer.getChildren().setAll(countrySelectView);
    }

    private void renderRoutes() {
        routeListContainer.getChildren().clear();
        if (routeItems.isEmpty()) {
            routeListContainer.getChildren().add(createEmptyState(I18n.t("trip.add.empty.routes")));
            return;
        }

        for (RouteItem item : routeItems) {
            routeListContainer.getChildren().add(buildRouteCard(item));
        }
    }

    private void renderPlaces() {
        placesFlow.getChildren().clear();
        if (placeItems.isEmpty()) {
            placesFlow.getChildren().add(createEmptyState(I18n.t("trip.add.empty.places")));
            return;
        }

        for (PlaceItem item : placeItems) {
            placesFlow.getChildren().add(buildPlaceCard(item));
        }
    }

    private VBox buildRouteCard(RouteItem item) {
        VBox root = new VBox();
        root.getStyleClass().add("trip-editor-route-card");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView preview = createImageView(item.imagePath(), 86, 58);
        VBox textBox = new VBox(4);
        Label title = new Label(item.title());
        title.getStyleClass().add("trip-editor-route-title");
        Label subtitle = new Label(item.subtitle());
        subtitle.getStyleClass().add("trip-editor-route-subtitle");
        textBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button editButton = createInlineIconButton("fth-edit-2", () ->
                toast.info(I18n.t("trip.add.toast.route.edit.title"), formatMessage("trip.add.toast.route.edit.body", item.title())));
        Button removeButton = createInlineIconButton("fth-trash-2", () -> {
            routeItems.remove(item);
            renderRoutes();
        });
        actions.getChildren().addAll(editButton, removeButton);

        row.getChildren().addAll(preview, textBox, actions);
        root.getChildren().add(row);
        return root;
    }

    private VBox buildPlaceCard(PlaceItem item) {
        VBox card = new VBox(8);
        card.getStyleClass().add("trip-editor-place-card");

        ImageView preview = createImageView(item.imagePath(), 152, 96);
        Label title = new Label(item.title());
        title.getStyleClass().add("trip-editor-place-title");
        Label subtitle = new Label(item.subtitle());
        subtitle.getStyleClass().add("trip-editor-place-subtitle");

        card.getChildren().addAll(preview, title, subtitle);
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

    private Button createInlineIconButton(String iconLiteral, Runnable action) {
        Button button = new Button();
        button.setFocusTraversable(false);
        button.getStyleClass().add("trip-editor-icon-button");
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(14);
        icon.getStyleClass().add("trip-editor-icon-button-glyph");
        button.setGraphic(icon);
        button.setOnAction(event -> action.run());
        return button;
    }

    private ImageView createImageView(String imagePath, double width, double height) {
        ImageView view = new ImageView(loadImage(imagePath));
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        view.getStyleClass().add("trip-editor-thumb");

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        view.setClip(clip);
        return view;
    }

    private void showCoverImage(String imagePath, String tripName) {
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
        routeSearchField.textProperty().addListener((obs, oldValue, newValue) -> renderRoutePickerResults());
        placeSearchField.textProperty().addListener((obs, oldValue, newValue) -> renderPlacePickerResults());
        setRoutePickerVisible(false);
        setPlacePickerVisible(false);
    }

    private void setRoutePickerVisible(boolean visible) {
        routePickerContainer.setVisible(visible);
        routePickerContainer.setManaged(visible);
        addRouteButton.getStyleClass().remove("trip-editor-outline-button-expanded");
        if (visible) {
            addRouteButton.getStyleClass().add("trip-editor-outline-button-expanded");
            refreshRoutePicker();
            Platform.runLater(routeSearchField::requestFocus);
        } else {
            routeSearchField.clear();
        }
    }

    private void setPlacePickerVisible(boolean visible) {
        placePickerContainer.setVisible(visible);
        placePickerContainer.setManaged(visible);
        addPlaceButton.getStyleClass().remove("trip-editor-outline-button-expanded");
        if (visible) {
            addPlaceButton.getStyleClass().add("trip-editor-outline-button-expanded");
            refreshPlacePicker();
            Platform.runLater(placeSearchField::requestFocus);
        } else {
            placeSearchField.clear();
        }
    }

    private void configurePickerButtonIcons() {
        routePickerCloseButton.setText("");
        placePickerCloseButton.setText("");
        routePickerCloseButton.setGraphic(createPickerIcon("fth-x", "trip-editor-picker-close-icon", 18));
        placePickerCloseButton.setGraphic(createPickerIcon("fth-x", "trip-editor-picker-close-icon", 18));
        routeCreateButton.setGraphic(createPickerIcon("fth-edit-2", "trip-editor-picker-create-icon", 18));
        placeCreateButton.setGraphic(createPickerIcon("fth-edit-2", "trip-editor-picker-create-icon", 18));
    }

    private FontIcon createPickerIcon(String literal, String styleClass, int size) {
        FontIcon icon = new FontIcon(literal);
        icon.setIconSize(size);
        icon.getStyleClass().add(styleClass);
        return icon;
    }

    private void refreshRoutePicker() {
        availableRouteCandidates = loadRouteCandidates();
        renderRoutePickerResults();
    }

    private void refreshPlacePicker() {
        availablePlaceCandidates = loadPlaceCandidates();
        renderPlacePickerResults();
    }

    private void renderRoutePickerResults() {
        renderPickerResults(
                routePickerResultsContainer,
                filterRoutes(routeSearchField.getText()),
                route -> addExistingRoute((RouteItem) route),
                "trip.add.menu.route.empty"
        );
    }

    private void renderPlacePickerResults() {
        renderPickerResults(
                placePickerResultsContainer,
                filterPlaces(placeSearchField.getText()),
                place -> addExistingPlace((PlaceItem) place),
                "trip.add.menu.place.empty"
        );
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

    private void renderPickerResults(
            VBox container,
            List<?> items,
            java.util.function.Consumer<Object> onSelect,
            String emptyKey
    ) {
        container.getChildren().clear();
        if (items.isEmpty()) {
            container.setMinHeight(70);
            container.setPrefHeight(70);
            Label emptyLabel = new Label(I18n.t(emptyKey));
            emptyLabel.getStyleClass().add("trip-editor-picker-empty");
            container.getChildren().add(emptyLabel);
            return;
        }

        container.setMinHeight(0);
        container.setPrefHeight(356);
        for (Object item : items) {
            Button button = new Button();
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.getStyleClass().add("trip-editor-picker-item");
            button.setGraphic(buildPickerItemGraphic(item));
            button.setOnAction(event -> onSelect.accept(item));
            container.getChildren().add(button);
        }
    }

    private HBox buildPickerItemGraphic(Object item) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        FontIcon icon = new FontIcon(item instanceof RouteItem ? "fth-map" : "fth-map-pin");
        icon.setIconSize(14);
        icon.getStyleClass().add("trip-editor-picker-item-icon");

        VBox textBox = new VBox(2);
        Label title = new Label(item instanceof RouteItem route ? route.title() : ((PlaceItem) item).title());
        title.getStyleClass().add("trip-editor-picker-item-title");

        String subtitleText = item instanceof RouteItem route ? route.subtitle() : ((PlaceItem) item).subtitle();
        Label subtitle = new Label(subtitleText == null ? "" : subtitleText);
        subtitle.getStyleClass().add("trip-editor-picker-item-subtitle");
        subtitle.setManaged(subtitleText != null && !subtitleText.isBlank());
        subtitle.setVisible(subtitleText != null && !subtitleText.isBlank());

        textBox.getChildren().addAll(title, subtitle);
        row.getChildren().addAll(icon, textBox);
        return row;
    }

    private void openCreateRoute() {
        RouterArgument args = new RouterArgument();
        int targetTripId = tripId == null ? 0 : tripId.intValue();
        String targetTripName = titleInput.getText() == null || titleInput.getText().isBlank()
                ? currentTripDisplayName
                : titleInput.getText().trim();

        args.addArgument("tripId", targetTripId);
        args.addArgument("tripName", targetTripName == null || targetTripName.isBlank() ? "New Trip" : targetTripName);
        getRouter().moveto(RouteIds.ADD_ROUTE, args);
    }

    private void openCreatePlace() {
        RouterArgument args = new RouterArgument();
        int targetTripId = tripId == null ? 0 : tripId.intValue();
        String targetTripName = titleInput.getText() == null || titleInput.getText().isBlank()
                ? currentTripDisplayName
                : titleInput.getText().trim();

        args.addArgument("tripId", targetTripId);
        args.addArgument(
                "tripName",
                targetTripName == null || targetTripName.isBlank() ? I18n.t("trip.add.default.name") : targetTripName
        );
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    private List<RouteItem> loadRouteCandidates() {
        if (routeService == null) {
            return List.of();
        }
        var result = routeService.getRoutes(new GetRoutesRequest(null, null));
        if (result.isFailure()) {
            log.warn("Failed to load routes for trip picker", result.getError());
            toast.warning(I18n.t("trip.add.toast.routes.loadFailed"));
            return List.of();
        }

        return result.getValue().items().stream()
                .map(this::toRouteItem)
                .filter(route -> route.id() != null && !containsRoute(route.id()))
                .toList();
    }

    private List<PlaceItem> loadPlaceCandidates() {
        if (placeService == null) {
            return List.of();
        }
        var result = placeService.getPlaces(new GetPlacesRequest(null, null));
        if (result.isFailure()) {
            log.warn("Failed to load places for trip picker", result.getError());
            toast.warning(I18n.t("trip.add.toast.places.loadFailed"));
            return List.of();
        }

        return result.getValue().items().stream()
                .map(this::toPlaceItem)
                .filter(place -> place.id() != null && !containsPlace(place.id()))
                .toList();
    }

    private void addExistingRoute(RouteItem route) {
        if (containsRoute(route.id())) {
            toast.info(I18n.t("trip.add.toast.route.duplicate"));
            return;
        }

        routeItems.add(route);
        renderRoutes();
        refreshRoutePicker();
        setRoutePickerVisible(false);
        toast.success(I18n.t("trip.add.toast.route.added.title"), route.title());
    }

    private void addExistingPlace(PlaceItem place) {
        if (containsPlace(place.id())) {
            toast.info(I18n.t("trip.add.toast.place.duplicate"));
            return;
        }

        placeItems.add(place);
        renderPlaces();
        refreshPlacePicker();
        setPlacePickerVisible(false);
        toast.success(I18n.t("trip.add.toast.place.added.title"), place.title());
    }

    private boolean containsRoute(String routeId) {
        return routeId != null && routeItems.stream().anyMatch(item -> routeId.equals(item.id()));
    }

    private boolean containsPlace(String placeId) {
        return placeId != null && placeItems.stream().anyMatch(item -> placeId.equals(item.id()));
    }

    private String formatCandidateLabel(Object candidate) {
        if (candidate instanceof RouteItem route) {
            return formatMenuLabel(route.title(), route.subtitle());
        }
        if (candidate instanceof PlaceItem place) {
            return formatMenuLabel(place.title(), place.subtitle());
        }
        return String.valueOf(candidate);
    }

    private String formatMenuLabel(String title, String subtitle) {
        if (subtitle == null || subtitle.isBlank()) {
            return title;
        }
        return title + " - " + subtitle;
    }

    private RouteItem toRouteItem(RouteResponse response) {
        String subtitle = response.description() == null || response.description().isBlank()
                ? formatMessage("trip.add.route.length", String.format(Locale.US, "%.1f", response.length()))
                : response.description();
        return new RouteItem(
                response.id(),
                safeText(response.title()).isBlank() ? I18n.t("trip.add.fallback.route") : response.title(),
                subtitle,
                imagePath(response.coverImage() == null ? null : response.coverImage().url())
        );
    }

    private PlaceItem toPlaceItem(PlaceResponse response) {
        String subtitle = response.country() != null && response.country().name() != null && !response.country().name().isBlank()
                ? response.country().name()
                : safeText(response.description());
        return new PlaceItem(
                response.id(),
                safeText(response.title()).isBlank() ? I18n.t("trip.add.fallback.place") : response.title(),
                subtitle,
                imagePath(response.coverImage() == null ? null : response.coverImage().url())
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

    private InputItem createInput(String placeholderKey) {
        InputItem input = new InputItem(placeholderKey, FieldVariant.FILLED);
        input.getStyleClass().add("trip-editor-field");
        return input;
    }

    private TextAreaItem createTextArea(String placeholderKey) {
        TextAreaItem input = new TextAreaItem(placeholderKey, FieldVariant.FILLED);
        input.getStyleClass().addAll("trip-editor-field", "trip-editor-textarea-field");
        input.setRows(6);
        return input;
    }

    private DatePickerItem createDatePickerItem(String formatPattern) {
        DatePickerItem input = new DatePickerItem(formatPattern, FieldVariant.FILLED);
        input.getStyleClass().add("trip-editor-date-input");
        return input;
    }

    private TagPickerItem createTagPickerItem() {
        TagPickerItem input = new TagPickerItem();
        input.setPlaceholderText(I18n.t("trip.add.select.tag"));
        input.setPopupTitle(I18n.t("trip.add.tag.popupTitle"));
        input.setAvailableTags(AVAILABLE_TAGS);
        input.setOnSelectionChanged(tags -> {
            selectedTags.clear();
            selectedTags.addAll(tags);
        });
        input.getStyleClass().add("trip-editor-tag-picker");
        return input;
    }

    private void syncTagPicker() {
        tagPickerInput.setAvailableTags(AVAILABLE_TAGS);
        tagPickerInput.setSelectedTags(selectedTags);
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

    private Select<String> createSelectModel(List<String> values, String placeholderKey) {
        return Select.<String>builder()
                .placeholder(I18n.t(placeholderKey))
                .variant(FieldVariant.FILLED)
                .items(values.stream().map(this::toEntry).toList())
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
                .filter(entry -> value.equals(entry.getValue()))
                .findFirst()
                .orElse(null);
    }

    private String selectedValue(Select<String> model) {
        if (model == null || model.getSelectedItem() == null) {
            return null;
        }
        return model.getSelectedItem().getValue();
    }

    private Long readTripId(RouterArgument data) {
        if (data == null) {
            return null;
        }

        Object raw = data.getValue("tripId");
        if (raw instanceof Number number) {
            return number.longValue();
        }
        return null;
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


    private record RouteItem(String id, String title, String subtitle, String imagePath) { }

    private record PlaceItem(String id, String title, String subtitle, String imagePath) { }
}
