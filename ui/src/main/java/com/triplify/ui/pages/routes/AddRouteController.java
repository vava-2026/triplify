package com.triplify.ui.pages.routes;

import com.google.inject.Inject;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.ui.pages.places.model.Places;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.AddPlaceToRouteRequest;
import com.triplify.application.usecase.route.dto.AddRouteRequest;
import com.triplify.application.usecase.route.dto.DeletePlaceFromRouteRequest;
import com.triplify.application.usecase.route.dto.GetRouteByIdRequest;
import com.triplify.application.usecase.route.dto.RearrangePlacesInRouteRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.route.dto.UpdateRouteRequest;
import com.triplify.application.shared.GeoCalculator;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.pages.WindowedPageController;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.model.SearchDisplayMode;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.upload_panel.view.ImageUploadPanelView;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;
import com.triplify.ui.storage.EditorDraftStorage;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class AddRouteController extends WindowedPageController {

    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final Logger log = LoggerFactory.getLogger(AddRouteController.class);

    @FXML private VBox contentContainer;
    @FXML private FlowPane contentFlow;
    @FXML private Label routePageTitleLabel;
    @FXML private SectionHeaderView generalSectionHeader;
    @FXML private Label routeTitleLabel;
    @FXML private Label descriptionLabel;
    @FXML private SectionHeaderView placesSectionHeader;

    @FXML private VBox titleInputContainer;
    @FXML private VBox descriptionInputContainer;

    @FXML private ImageUploadPanelView imageUploadPanel;

    @FXML private VBox placesListContainer;
    @FXML private VBox placePickerContainer;
    @FXML private VBox placeSearchContainer;
    @FXML private Label routeLengthLabel;

    @FXML private Button addPlaceButton;
    @FXML private Button placeCreateButton;
    @FXML private VBox actionButtonsContainer;

    @Inject private ToastService toast;
    @Inject private RouteService routeService;
    @Inject private PlaceService placeService;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private Places placesModel;
    private final List<RoutePlaceItem> placeItems = new ArrayList<>();

    private String tripId;
    private String tripName;
    private String routeId;
    private String returnTarget;
    private boolean editMode;
    private boolean routeLoaded;
    private String coverImagePath;
    private InputItem titleInput;
    private TextAreaItem descriptionInput;
    private RoutePlaceItem draggedPlaceItem;
    private VBox draggedHandle;
    private HBox draggedRow;
    private Popup dragPreviewPopup;
    private ImageView dragPreviewImageView;
    private StackPane uploadArea;
    private ImageView coverPreview;
    private VBox uploadPlaceholder;
    private Label selectedImageLabel;
    private SearchView<RoutePlaceItem> placeSearchView;

    @FXML
    public void initialize() {
        log.info("Initializing AddRouteController");
        titleInput = createInput("input.placeholder.routeTitle");
        descriptionInput = createTextArea("input.placeholder.routeDescription");

        titleInputContainer.getChildren().add(titleInput);
        descriptionInputContainer.getChildren().add(descriptionInput);
        uploadArea = imageUploadPanel.getUploadArea();
        coverPreview = imageUploadPanel.getCoverPreview();
        uploadPlaceholder = imageUploadPanel.getUploadPlaceholder();
        selectedImageLabel = imageUploadPanel.getSelectedImageLabel();

        contentFlow.prefWrapLengthProperty().bind(contentContainer.widthProperty());
        contentContainer.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> finishDragging());
        contentContainer.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::updateDragPreview);
        EditorUtils.initializeCoverPreview(coverPreview, uploadArea);
        initializeDragPreview();
        bindUploadPanelHandlers();

        placesModel = Places.builder(placeService)
                .onLoadFailed(error -> toast.warning(I18n.t("route.add.toast.places.loadFailed")))
                .build();
        initializePlacePicker();

        EditorUtils.configureButtonIcon(addPlaceButton, "fth-plus");
        EditorUtils.configureButtonIcon(placeCreateButton, "fth-plus");
        createActionButtons();

        EditorUtils.installRoundedClip(uploadArea, 16);
        setPlacePickerVisible(false);
        bindLocalizedText();

        renderPlaces();
        I18n.bundleProperty().addListener((obs, oldBundle, newBundle) -> renderPlaces());
        log.info("Initialized AddRouteController");
    }

    @Override
    public void onLifecycleInitialize() {
        log.info("Getting AddRouteController attributes");
        resetFormState();
        RouterArgument data = getRouter().getCurrentData();
        tripId = data == null ? null : EditorUtils.normalizeKey(data.getValue("tripId"));
        tripName = data == null ? null : data.getValue("tripName");
        routeId = data == null ? null : EditorUtils.normalizeKey(data.getValue("routeId"));
        returnTarget = data == null ? null : data.getValue("editorReturnTarget");
        editMode = routeId != null && !routeId.isBlank();

        EditorDraftStorage.RouteDraft draft = EditorDraftStorage.consumeRouteDraft();
        if (matchesDraftContext(draft)) {
            applyDraft(draft);
            routeLoaded = true;
        }
        log.info("AddRouteController attributes retrieved");
    }

    @Override
    protected void onWindowedShow() {
        placesModel.reset();
        if (editMode && !routeLoaded) {
            loadRouteForEdit(routeId);
        }
        EditorDraftStorage.clearRouteDraft();
        consumeReturnedPlace();
        renderPlaces();
    }

    @FXML
    private void onSave() {
        clearFieldErrors();

        Path coverImage = coverImagePath == null || coverImagePath.isBlank() ? null : Path.of(coverImagePath);
        String title = titleInput.getText().trim();
        String description = EditorUtils.normalizeNullable(descriptionInput.getText());
        double length = calculateRouteLength();

        Map<String, Consumer<String>> fieldHandlers = Map.of(
                "title", message -> titleInput.showError(message)
        );

        if (editMode) {
            var result = routeService.updateRoute(new UpdateRouteRequest(UUID.fromString(routeId), coverImage, title, description, length));
            result.onSuccess(route -> onSaveSuccess(route, syncPlacesForRoute(route.id().toString())));
            result.onFailure(error -> errorHandler.handle(error, fieldHandlers));
            return;
        }

        var result = routeService.addRoute(new AddRouteRequest(coverImage, title, description, length));
        result.onSuccess(route -> onSaveSuccess(route, linkPlacesToRoute(route.id().toString())));
        result.onFailure(error -> errorHandler.handle(error, fieldHandlers));
    }

    @FXML
    private void onDiscard() {
        EditorDraftStorage.clearRouteDraft();
        getRouter().popBackStack();
    }

    @FXML
    private void onAddPlace() {
        setPlacePickerVisible(!placePickerContainer.isVisible());
    }

    @FXML
    private void onCreatePlace() {
        setPlacePickerVisible(false);
        EditorDraftStorage.saveRouteDraft(captureDraft());
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", tripId == null || tripId.isBlank() ? UUID.randomUUID().toString() : tripId);
        args.addArgument("tripName", tripName == null || tripName.isBlank() ? I18n.t("trip.add.default.name") : tripName);
        args.addArgument("editorReturnTarget", EditorDraftStorage.TARGET_ROUTE);
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    @FXML
    private void onChooseCoverImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.t("route.add.dialog.cover.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.t("route.add.dialog.cover.filter"), "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(uploadArea.getScene() == null ? null : uploadArea.getScene().getWindow());
        if (file != null) {
            handleCoverImage(file);
        }
    }

    private void onSaveSuccess(RouteResponse route, boolean allPlacesSynced) {
        EditorDraftStorage.clearRouteDraft();
        EditorDraftStorage.savePendingRoute(returnTarget, loadSavedRoute(route.id().toString(), route));
        String routeTitle = titleInput.getText().trim();
        String message = tripName == null || tripName.isBlank()
                ? EditorUtils.formatMessage("route.add.toast.ready", routeTitle)
                : EditorUtils.formatMessage("route.add.toast.addedToTrip", routeTitle, tripName);
        toast.success(I18n.t("route.add.toast.saved.title"), message);
        if (!allPlacesSynced) {
            toast.warning(I18n.t("route.add.toast.place.link.failed"));
        }
        getRouter().popBackStack();
    }

    private void handleCoverImage(File file) {
        if (!EditorUtils.isSupportedImageFile(file)) {
            toast.warning(I18n.t("route.add.toast.image.unsupported"));
            return;
        }

        coverImagePath = file.getAbsolutePath();
        selectedImageLabel.setText(file.getName());
        selectedImageLabel.setVisible(true);
        selectedImageLabel.setManaged(true);

        Image image = new Image(file.toURI().toString(), true);
        image.errorProperty().addListener((obs, oldVal, isError) -> {
            if (Boolean.TRUE.equals(isError)) {
                coverPreview.setImage(null);
                coverPreview.setViewport(null);
                coverPreview.setVisible(false);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(true);
                uploadPlaceholder.setManaged(true);
                log.warn("Failed to load cover image preview", image.getException());
                toast.warning(I18n.t("route.add.toast.image.previewUnavailable"));
            }
        });
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0 && !image.isError()) {
                EditorUtils.setCoverPreviewImage(coverPreview, uploadArea, image);
                coverPreview.setVisible(true);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(false);
                uploadPlaceholder.setManaged(false);
            }
        });
    }

    private void updateCoverImageUi(String path) {
        coverImagePath = path;
        if (path == null || path.isBlank()) {
            if (selectedImageLabel != null) {
                selectedImageLabel.setText("");
                selectedImageLabel.setVisible(false);
                selectedImageLabel.setManaged(false);
            }
            if (coverPreview != null) {
                coverPreview.setImage(null);
                coverPreview.setVisible(false);
                coverPreview.setManaged(false);
            }
            if (uploadPlaceholder != null) {
                uploadPlaceholder.setVisible(true);
                uploadPlaceholder.setManaged(true);
            }
        } else {
            if (selectedImageLabel != null) {
                selectedImageLabel.setText(new File(path).getName());
                selectedImageLabel.setVisible(true);
                selectedImageLabel.setManaged(true);
            }
            if (coverPreview != null) {
                EditorUtils.setCoverPreviewImage(coverPreview, uploadArea, EditorUtils.loadImage(path, DEFAULT_IMAGE, getClass()));
                coverPreview.setVisible(true);
                coverPreview.setManaged(false);
            }
            if (uploadPlaceholder != null) {
                uploadPlaceholder.setVisible(false);
                uploadPlaceholder.setManaged(false);
            }
        }
    }

    private void resetFormState() {
        tripId = null;
        tripName = null;
        routeId = null;
        returnTarget = null;
        editMode = false;
        routeLoaded = false;
        placeItems.clear();

        if (titleInput != null) {
            titleInput.setText("");
            titleInput.clearError();
        }
        if (descriptionInput != null) {
            descriptionInput.setText("");
            descriptionInput.clearError();
        }
        setPlacePickerVisible(false);
        updateCoverImageUi(null);
    }

    private void initializePlacePicker() {
        Search<RoutePlaceItem> searchModel = Search.<RoutePlaceItem>builder(this::searchPlaceEntries)
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

        placeSearchView = new SearchView<>(searchModel);
        placeSearchContainer.getChildren().setAll(placeSearchView);
    }

    private List<Entry<RoutePlaceItem>> searchPlaceEntries(String query) {
        return toPlaceEntries(placesModel.search(query));
    }

    private List<Entry<RoutePlaceItem>> loadMorePlaceEntries(String query) {
        return toPlaceEntries(placesModel.loadMore(query));
    }

    private List<Entry<RoutePlaceItem>> toPlaceEntries(List<PlaceResponse> places) {
        return places.stream()
                .filter(place -> placeItems.stream().noneMatch(linked -> linked.id().equals(place.id().toString())))
                .map(place -> Entry.builder(toRoutePlaceItem(place), place.title()).icon("fth-map-pin").build())
                .toList();
    }

    private void renderPlaces() {
        placesListContainer.getChildren().clear();
        if (placeItems.isEmpty()) {
            placesListContainer.getChildren().add(createEmptyState(I18n.t("route.add.empty.places")));
            updateRouteLengthLabel();
            return;
        }

        int index = 1;
        for (RoutePlaceItem item : placeItems) {
            placesListContainer.getChildren().add(buildPlaceRow(item, index++));
        }
        updateRouteLengthLabel();
    }

    private HBox buildPlaceRow(RoutePlaceItem item, int index) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("add-route-place-row");
        if (item.equals(draggedPlaceItem)) {
            row.getStyleClass().add("add-route-place-row-dragging");
        }

        Label orderLabel = new Label(String.valueOf(index));
        orderLabel.getStyleClass().add("add-route-place-index");

        ImageView preview = createImageView(item.imagePath(), 112, 72);

        VBox textBox = new VBox(4);
        Label title = new Label(item.title());
        title.getStyleClass().add("add-route-place-title");
        Label subtitle = new Label(item.subtitle());
        subtitle.getStyleClass().add("add-route-place-subtitle");
        textBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button removeButton = createInlineIconButton("fth-trash-2", () -> {
            placeItems.remove(item);
            renderPlaces();
        });
        Region handle = createHandle(item, row);
        actions.getChildren().addAll(removeButton, handle);

        row.setOnMouseDragEntered(event -> reorderPlaces(item));
        row.setOnMouseReleased(event -> finishDragging());

        row.getChildren().addAll(orderLabel, preview, textBox, actions);
        return row;
    }

    private Region createHandle(RoutePlaceItem item, HBox row) {
        VBox handle = new VBox(3);
        handle.setAlignment(Pos.CENTER);
        handle.getStyleClass().add("add-route-handle");
        handle.setCursor(Cursor.OPEN_HAND);

        for (int index = 0; index < 3; index++) {
            Circle dot = new Circle(1.8);
            dot.getStyleClass().add("add-route-handle-dot");
            handle.getChildren().add(dot);
        }

        handle.setOnMousePressed(event -> handle.setCursor(Cursor.CLOSED_HAND));
        handle.setOnDragDetected(event -> beginDragging(item, handle, row, event));
        handle.setOnMouseReleased(event -> {
            handle.setCursor(Cursor.OPEN_HAND);
            handle.getStyleClass().remove("add-route-handle-dragging");
            finishDragging();
        });
        return handle;
    }

    private void beginDragging(RoutePlaceItem item, VBox handle, HBox row, MouseEvent event) {
        draggedPlaceItem = item;
        draggedHandle = handle;
        draggedRow = row;
        handle.setCursor(Cursor.CLOSED_HAND);
        handle.getStyleClass().add("add-route-handle-dragging");
        if (!row.getStyleClass().contains("add-route-place-row-dragging")) {
            row.getStyleClass().add("add-route-place-row-dragging");
        }
        showDragPreview(row, event);
        handle.startFullDrag();
        event.consume();
    }

    private void reorderPlaces(RoutePlaceItem targetItem) {
        if (draggedPlaceItem == null || draggedPlaceItem.equals(targetItem)) {
            return;
        }

        int draggedIndex = placeItems.indexOf(draggedPlaceItem);
        int targetIndex = placeItems.indexOf(targetItem);
        if (draggedIndex < 0 || targetIndex < 0 || draggedIndex == targetIndex) {
            return;
        }

        Collections.swap(placeItems, draggedIndex, targetIndex);
        renderPlaces();
    }

    private void finishDragging() {
        if (draggedPlaceItem == null) {
            return;
        }

        if (draggedHandle != null) {
            draggedHandle.setCursor(Cursor.OPEN_HAND);
            draggedHandle.getStyleClass().remove("add-route-handle-dragging");
            draggedHandle = null;
        }
        if (draggedRow != null) {
            draggedRow.getStyleClass().remove("add-route-place-row-dragging");
            draggedRow = null;
        }
        hideDragPreview();
        draggedPlaceItem = null;
        renderPlaces();
    }

    private void initializeDragPreview() {
        dragPreviewImageView = new ImageView();
        dragPreviewImageView.setMouseTransparent(true);
        dragPreviewImageView.getStyleClass().add("add-route-drag-preview");

        StackPane previewRoot = new StackPane(dragPreviewImageView);
        previewRoot.setMouseTransparent(true);
        previewRoot.getStyleClass().add("add-route-drag-preview-shell");

        dragPreviewPopup = new Popup();
        dragPreviewPopup.setAutoHide(false);
        dragPreviewPopup.setAutoFix(false);
        dragPreviewPopup.setHideOnEscape(false);
        dragPreviewPopup.getContent().add(previewRoot);
    }

    private void showDragPreview(HBox row, MouseEvent event) {
        if (contentContainer.getScene() == null || contentContainer.getScene().getWindow() == null) {
            return;
        }

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage snapshot = row.snapshot(parameters, null);
        dragPreviewImageView.setImage(snapshot);

        if (!dragPreviewPopup.isShowing()) {
            dragPreviewPopup.show(
                    contentContainer.getScene().getWindow(),
                    event.getScreenX() - snapshot.getWidth() - 18,
                    event.getScreenY() - 12
            );
        }
        updateDragPreviewPosition(event);
    }

    private void updateDragPreview(MouseEvent event) {
        if (draggedPlaceItem == null) {
            return;
        }
        updateDragPreviewPosition(event);
    }

    private void updateDragPreviewPosition(MouseEvent event) {
        if (dragPreviewPopup == null || !dragPreviewPopup.isShowing()) {
            return;
        }

        double previewWidth = dragPreviewImageView.getImage() == null ? 0 : dragPreviewImageView.getImage().getWidth();
        dragPreviewPopup.setX(event.getScreenX() - previewWidth - 18);
        dragPreviewPopup.setY(event.getScreenY() - 12);
    }

    private void hideDragPreview() {
        if (dragPreviewPopup != null) {
            dragPreviewPopup.hide();
        }
        if (dragPreviewImageView != null) {
            dragPreviewImageView.setImage(null);
        }
    }

    private Region createEmptyState(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("add-route-empty-state");

        StackPane pane = new StackPane(label);
        pane.getStyleClass().add("add-route-empty-card");
        pane.setMaxWidth(Double.MAX_VALUE);
        return pane;
    }

    private Button createInlineIconButton(String iconLiteral, Runnable action) {
        Button button = new Button();
        button.setFocusTraversable(false);
        button.getStyleClass().add("add-route-icon-button");
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(14);
        icon.getStyleClass().add("add-route-icon-button-glyph");
        button.setGraphic(icon);
        button.setOnAction(event -> action.run());
        return button;
    }

    private ImageView createImageView(String imagePath, double width, double height) {
        ImageView view = new ImageView(EditorUtils.loadImage(imagePath, DEFAULT_IMAGE, getClass()));
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        view.getStyleClass().add("add-route-thumb");

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        view.setClip(clip);
        return view;
    }

    private void bindUploadPanelHandlers() {
        imageUploadPanel.setOnUploadClicked(event -> onChooseCoverImage());
        imageUploadPanel.setOnImageFileSelected(this::handleCoverImage);
        imageUploadPanel.setOnUnsupportedImageFile(file -> toast.warning(I18n.t("route.add.toast.image.unsupported")));
        imageUploadPanel.installDefaultImageDragAndDrop();
    }

    private void bindLocalizedText() {
        Localization.bindText(routePageTitleLabel.textProperty(), "route.add.page.title");
        Localization.bindText(generalSectionHeader.titleProperty(), "route.add.section.general");
        Localization.bindText(routeTitleLabel.textProperty(), "route.add.field.title");
        Localization.bindText(descriptionLabel.textProperty(), "route.add.field.description");
        Localization.bindText(placesSectionHeader.titleProperty(), "route.add.section.places");
        Localization.bindText(imageUploadPanel.sectionTitleProperty(), "route.add.section.cover");
        Localization.bindText(imageUploadPanel.uploadTitleProperty(), "route.add.upload.title");
        Localization.bindText(imageUploadPanel.uploadSubtitleProperty(), "route.add.upload.subtitle");
        Localization.bindText(addPlaceButton.textProperty(), "route.add.action.addPlace");
        Localization.bindText(placeCreateButton.textProperty(), "route.add.action.createPlace");
    }

    private void createActionButtons() {
        Button saveButton = AppButtonView.builder(fxmlLoader)
                .labelBinding(Localization.textBinding("route.add.action.save"))
                .variant(ButtonVariant.PRIMARY)
                .icon("fth-save")
                .onAction(this::onSave)
                .build();
        saveButton.getStyleClass().add("editor-action-button");
        saveButton.setMaxWidth(Double.MAX_VALUE);

        Button discardButton = AppButtonView.builder(fxmlLoader)
                .labelBinding(Localization.textBinding("route.add.action.discard"))
                .variant(ButtonVariant.DANGER_OUTLINE)
                .icon("fth-trash-2")
                .onAction(this::onDiscard)
                .build();
        discardButton.getStyleClass().add("editor-action-button");
        discardButton.setMaxWidth(Double.MAX_VALUE);
        actionButtonsContainer.getChildren().setAll(saveButton, discardButton);
    }

    private void setPlacePickerVisible(boolean visible) {
        placePickerContainer.setVisible(visible);
        placePickerContainer.setManaged(visible);
        if (!visible && placeSearchView != null) {
            placeSearchView.getSearchField().setText("");
        }
    }

    private void addExistingPlace(RoutePlaceItem candidate) {
        if (candidate == null) {
            return;
        }
        if (placeItems.stream().anyMatch(item -> item.id().equals(candidate.id()))) {
            toast.info(I18n.t("route.add.toast.place.duplicate"));
            return;
        }
        placeItems.add(candidate);
        toast.success(I18n.t("route.add.toast.place.added.title"), candidate.title());
        setPlacePickerVisible(false);
        renderPlaces();
    }

    private EditorDraftStorage.RouteDraft captureDraft() {
        return new EditorDraftStorage.RouteDraft(
                routeId,
                tripId,
                tripName,
                titleInput.getText().trim(),
                EditorUtils.normalizeNullable(descriptionInput.getText()),
                coverImagePath,
                placeItems.stream()
                        .map(item -> new EditorDraftStorage.PlaceDraftItem(
                                item.id(),
                                item.title(),
                                item.subtitle(),
                                item.imagePath(),
                                null,
                                null,
                                item.latitude(),
                                item.longitude()
                        ))
                        .toList()
        );
    }

    private boolean matchesDraftContext(EditorDraftStorage.RouteDraft draft) {
        if (draft == null) {
            return false;
        }
        return EditorUtils.normalizeKey(routeId).equals(EditorUtils.normalizeKey(draft.routeId()))
                && EditorUtils.normalizeKey(tripId).equals(EditorUtils.normalizeKey(draft.tripId()));
    }

    private void applyDraft(EditorDraftStorage.RouteDraft draft) {
        titleInput.setText(draft.title() == null ? "" : draft.title());
        descriptionInput.setText(draft.description() == null ? "" : draft.description());
        placeItems.clear();
        draft.places().stream()
                .map(item -> new RoutePlaceItem(
                        item.id(),
                        item.title(),
                        item.subtitle(),
                        item.imagePath(),
                        item.latitude(),
                        item.longitude()
                ))
                .forEach(placeItems::add);
        updateCoverImageUi(draft.coverImagePath());
    }

    private void consumeReturnedPlace() {
        PlaceResponse place = EditorDraftStorage.consumePendingPlace(EditorDraftStorage.TARGET_ROUTE);
        if (place == null) {
            return;
        }
        addExistingPlace(toRoutePlaceItem(place));
    }

    private void loadRouteForEdit(String routeId) {
        log.debug("Loading route for edit with ID: {}", routeId);
        var result = routeService.getRouteById(new GetRouteByIdRequest(UUID.fromString(routeId)));
        if (result.isFailure()) {
            log.error("Failed to load route for edit: {}", result.getError());
            errorHandler.handle(result.getError());
            getRouter().popBackStack();
            return;
        }
        log.debug("Route data loaded successfully for route ID: {}", routeId);

        RouteResponse route = result.getValue();
        titleInput.setText(route.title() == null ? "" : route.title());
        descriptionInput.setText(route.description() == null ? "" : route.description());

        String imagePath = route.coverImage() == null || route.coverImage().url() == null
                ? null
                : route.coverImage().url().toString();
        updateCoverImageUi(imagePath);

        placeItems.clear();
        if (route.places() != null) {
            route.places().stream()
                    .map(this::toRoutePlaceItem)
                    .forEach(placeItems::add);
        }
        routeLoaded = true;
        log.debug("Route loaded successfully for route ID: {}", routeId);
    }

    private RouteResponse loadSavedRoute(String routeId, RouteResponse fallback) {
        var result = routeService.getRouteById(new GetRouteByIdRequest(UUID.fromString(routeId)));
        if (result.isFailure()) {
            return fallback;
        }
        return result.getValue();
    }

    private boolean syncPlacesForRoute(String routeId) {
        RouteResponse currentRoute = loadSavedRoute(routeId, null);
        if (currentRoute == null) {
            return false;
        }

        List<String> currentPlaceIds = currentRoute.places() == null
                ? List.of()
                : currentRoute.places().stream().map(p -> p.id().toString()).toList();
        List<String> targetPlaceIds = placeItems.stream().map(RoutePlaceItem::id).toList();

        for (String existingPlaceId : currentPlaceIds) {
            if (targetPlaceIds.contains(existingPlaceId)) {
                continue;
            }
            var deleteResult = routeService.deletePlaceFromRoute(new DeletePlaceFromRouteRequest(UUID.fromString(routeId), UUID.fromString(existingPlaceId)));
            if (deleteResult.isFailure()) {
                errorHandler.handle(deleteResult.getError());
                return false;
            }
        }

        for (String targetPlaceId : targetPlaceIds) {
            if (currentPlaceIds.contains(targetPlaceId)) {
                continue;
            }
            var addResult = routeService.addPlaceToRoute(new AddPlaceToRouteRequest(UUID.fromString(routeId), UUID.fromString(targetPlaceId)));
            if (addResult.isFailure()) {
                errorHandler.handle(addResult.getError());
                return false;
            }
        }

        var rearrangeResult = routeService.rearrangePlacesInRoute(new RearrangePlacesInRouteRequest(UUID.fromString(routeId), targetPlaceIds.stream().map(UUID::fromString).toList()));
        if (rearrangeResult.isFailure()) {
            errorHandler.handle(rearrangeResult.getError());
            return false;
        }

        return true;
    }

    private void clearFieldErrors() {
        titleInput.clearError();
    }

    private boolean linkPlacesToRoute(String routeId) {
        for (RoutePlaceItem item : placeItems) {
            var linkResult = routeService.addPlaceToRoute(new AddPlaceToRouteRequest(UUID.fromString(routeId), UUID.fromString(item.id())));
            if (linkResult.isFailure()) {
                errorHandler.handle(linkResult.getError());
                return false;
            }
        }
        return true;
    }

    private void updateRouteLengthLabel() {
        routeLengthLabel.setText(String.format(Locale.US, I18n.t("route.add.length"), calculateRouteLength()));
    }

    private double calculateRouteLength() {
        if (placeItems.size() < 2) {
            return 0.0;
        }

        double totalDistanceKm = 0.0;
        for (int index = 1; index < placeItems.size(); index++) {
            RoutePlaceItem previous = placeItems.get(index - 1);
            RoutePlaceItem current = placeItems.get(index);
            totalDistanceKm += GeoCalculator.distanceKm(
                    previous.latitude(),
                    previous.longitude(),
                    current.latitude(),
                    current.longitude()
            );
        }
        return totalDistanceKm;
    }

    private RoutePlaceItem toRoutePlaceItem(PlaceResponse response) {
        String subtitle = response.country() != null && response.country().name() != null && !response.country().name().isBlank()
                ? response.country().name()
                : response.description();
        return new RoutePlaceItem(
                response.id().toString(),
                response.title(),
                subtitle,
                response.coverImage() == null || response.coverImage().url() == null ? DEFAULT_IMAGE : response.coverImage().url().toString(),
                response.latitude(),
                response.longitude()
        );
    }


    private InputItem createInput(String placeholderKey) {
        InputItem input = new InputItem(placeholderKey, FieldVariant.GHOST);
        input.getStyleClass().add("add-route-field");
        return input;
    }

    private TextAreaItem createTextArea(String placeholderKey) {
        TextAreaItem input = new TextAreaItem(placeholderKey, FieldVariant.GHOST);
        input.getStyleClass().addAll("add-route-field", "add-route-textarea-field");
        input.setRows(6);
        return input;
    }

    private record RoutePlaceItem(
            String id,
            String title,
            String subtitle,
            String imagePath,
            Double latitude,
            Double longitude
    ) { }
}
