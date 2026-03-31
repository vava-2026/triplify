package com.triplify.ui.pages.trips;

import com.google.inject.Inject;
import com.triplify.application.response.TripStatus;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.date_picker.DatePickerItem;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.tag_picker.TagPickerItem;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TripDetailsController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(TripDetailsController.class);

    private static final List<String> AVAILABLE_COUNTRIES = List.of(
            "Ukraine", "Greece", "Italy", "France", "Japan", "United States", "Kenya", "Canada"
    );
    private static final List<String> AVAILABLE_CATEGORIES = List.of(
            "Culture", "Tourism", "Nature", "Relax", "Memorial", "Food"
    );
    private static final List<String> AVAILABLE_TAGS = List.of(
            "City", "Adventure", "Food", "Study", "Hike", "Relax", "Family", "Photography"
    );
    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";

    @FXML private VBox contentContainer;
    @FXML private FlowPane contentFlow;

    @FXML private VBox titleInputContainer;
    @FXML private FlowPane countriesFlow;
    @FXML private VBox countrySelectContainer;
    @FXML private VBox startDateContainer;
    @FXML private VBox endDateContainer;
    @FXML private VBox descriptionInputContainer;

    @FXML private StackPane uploadArea;
    @FXML private ImageView coverPreview;
    @FXML private VBox uploadPlaceholder;
    @FXML private Label selectedImageLabel;

    @FXML private VBox categorySelectContainer;
    @FXML private VBox tagPickerContainer;
    @FXML private VBox routeListContainer;
    @FXML private FlowPane placesFlow;

    @FXML private Button addCountryButton;
    @FXML private Button addRouteButton;
    @FXML private Button addPlaceButton;
    @FXML private Button saveButton;
    @FXML private Button discardButton;

    @Inject private ToastService toast;

    private final Set<String> selectedCountries = new LinkedHashSet<>();
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
    private Select<String> countrySelectModel;
    private Select<String> categorySelectModel;

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

        contentFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        initializeCoverPreview();

        configureButtonIcon(addCountryButton, "fth-plus");
        configureButtonIcon(addRouteButton, "fth-plus");
        configureButtonIcon(addPlaceButton, "fth-plus");
        configureButtonIcon(saveButton, "fth-save");
        configureButtonIcon(discardButton, "fth-trash-2");

        installRoundedClip(uploadArea, 16);
        initializeSelects();
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
        String country = selectedValue(countrySelectModel);
        if (country == null || country.isBlank()) {
            toast.info("Choose a country first.");
            return;
        }

        if (selectedCountries.add(country)) {
            renderCountryChips();
        }
        countrySelectModel.setSelectedItem(null);
    }

    @FXML
    private void onAddRoute() {
        RouterArgument args = new RouterArgument();
        int targetTripId = tripId == null ? 0 : tripId.intValue();
        String targetTripName = titleInput.getText() == null || titleInput.getText().isBlank()
                ? currentTripDisplayName
                : titleInput.getText().trim();

        args.addArgument("tripId", targetTripId);
        args.addArgument("tripName", targetTripName == null || targetTripName.isBlank() ? "New Trip" : targetTripName);
        getRouter().moveto(RouteIds.ADD_ROUTE, args);
    }

    @FXML
    private void onAddPlace() {
        RouterArgument args = new RouterArgument();
        int targetTripId = tripId == null ? 0 : tripId.intValue();
        String targetTripName = titleInput.getText() == null || titleInput.getText().isBlank()
                ? currentTripDisplayName
                : titleInput.getText().trim();

        args.addArgument("tripId", targetTripId);
        args.addArgument("tripName", targetTripName == null || targetTripName.isBlank() ? "New Trip" : targetTripName);
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    @FXML
    private void onSave() {
        if (!titleInput.validateRequired()) {
            return;
        }
        if (selectedCountries.isEmpty()) {
            toast.warning("Add at least one country.");
            return;
        }
        if (selectedValue(categorySelectModel) == null || selectedValue(categorySelectModel).isBlank()) {
            toast.warning("Choose a category.");
            return;
        }

        String tripTitle = titleInput.getText().trim();
        String message = createMode
                ? "Trip draft \"" + tripTitle + "\" is ready."
                : "Trip \"" + tripTitle + "\" was updated.";
        toast.success("Trip saved", message);
    }

    @FXML
    private void onChooseCoverImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose trip cover");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.svg")
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

    private void initializeSelects() {
        countrySelectModel = createSelectModel(AVAILABLE_COUNTRIES, "Choose a country");
        categorySelectModel = createSelectModel(AVAILABLE_CATEGORIES, "Choose a category");

        countrySelectContainer.getChildren().setAll(createSelectView(countrySelectModel));
        categorySelectContainer.getChildren().setAll(createSelectView(categorySelectModel));
    }

    private void populateHeader(String tripName, String tripDates) {
        if (createMode) {
            currentTripDisplayName = "New Trip";
            return;
        }
        currentTripDisplayName = tripName == null || tripName.isBlank() ? "Trip Details" : tripName;
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

        selectedCountries.clear();
        if (tripCountry != null && !tripCountry.isBlank()) {
            selectedCountries.add(tripCountry);
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
                safeText(tripName).isBlank() ? fallbackCountry + " Centre" : tripName + " Centre",
                "Scenic transfer",
                DEFAULT_IMAGE
        ));
        routeItems.add(new RouteItem(
                fallbackCountry + " Highlights",
                "City walk",
                DEFAULT_IMAGE
        ));

        placeItems.add(new PlaceItem(
                fallbackCountry + " Cathedral",
                safeText(tripCountry).isBlank() ? "Old Town" : tripCountry,
                DEFAULT_IMAGE
        ));
        placeItems.add(new PlaceItem(
                safeText(tripName).isBlank() ? "Central Park" : tripName + " Viewpoint",
                "Popular stop",
                DEFAULT_IMAGE
        ));

        renderRoutes();
        renderPlaces();
    }

    private void renderCountryChips() {
        renderChipGroup(countriesFlow, selectedCountries, country ->
                selectedCountries.remove(country));
    }

    private void renderChipGroup(
            FlowPane container,
            Set<String> values,
            java.util.function.Consumer<String> onRemove
    ) {
        container.getChildren().clear();
        if (values.isEmpty()) {
            Label placeholder = new Label("Nothing selected yet");
            placeholder.getStyleClass().add("trip-editor-chip-placeholder");
            container.getChildren().add(placeholder);
            return;
        }

        for (String value : values) {
            Button chip = new Button(value + "  ×");
            chip.setFocusTraversable(false);
            chip.getStyleClass().addAll("trip-editor-chip", "trip-editor-chip-soft");
            chip.setOnAction(event -> {
                onRemove.accept(value);
                renderCountryChips();
            });
            container.getChildren().add(chip);
        }
    }

    private void renderRoutes() {
        routeListContainer.getChildren().clear();
        if (routeItems.isEmpty()) {
            routeListContainer.getChildren().add(createEmptyState("No routes yet. Start by adding your first segment."));
            return;
        }

        for (RouteItem item : routeItems) {
            routeListContainer.getChildren().add(buildRouteCard(item));
        }
    }

    private void renderPlaces() {
        placesFlow.getChildren().clear();
        if (placeItems.isEmpty()) {
            placesFlow.getChildren().add(createEmptyState("No places linked yet."));
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
                toast.info("Route edit", item.title() + " can be adjusted here next."));
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
            toast.warning("Unsupported image format.");
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
                toast.warning("Image preview is unavailable, but the file is attached.");
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

    private void addUploadActiveState(boolean active) {
        if (active) {
            if (!uploadArea.getStyleClass().contains("trip-editor-upload-area-active")) {
                uploadArea.getStyleClass().add("trip-editor-upload-area-active");
            }
            return;
        }

        uploadArea.getStyleClass().remove("trip-editor-upload-area-active");
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
        input.setPlaceholderText("Choose a tag");
        input.setPopupTitle("All tags");
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

    private Select<String> createSelectModel(List<String> values, String placeholder) {
        return Select.<String>builder()
                .placeholder(placeholder)
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

    private record RouteItem(String title, String subtitle, String imagePath) { }

    private record PlaceItem(String title, String subtitle, String imagePath) { }
}
