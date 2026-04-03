package com.triplify.ui.pages.routes;

import com.google.inject.Inject;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.search.model.Search;
import com.triplify.ui.shared.component.search.view.SearchView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.upload_panel.view.ImageUploadPanelView;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AddRouteController extends SimpleLifecycleAwareController {

    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";

    @FXML private VBox contentContainer;
    @FXML private FlowPane contentFlow;

    @FXML private VBox titleInputContainer;
    @FXML private VBox descriptionInputContainer;

    @FXML private ImageUploadPanelView imageUploadPanel;

    @FXML private VBox placesListContainer;
    @FXML private VBox placeSearchContainer;
    @FXML private Label routeLengthLabel;

    @FXML private Button addPlaceButton;
    @FXML private Button saveButton;
    @FXML private Button discardButton;

    @Inject private ToastService toast;

    private final List<RoutePlaceItem> placeItems = new ArrayList<>();

    private Integer tripId;
    private String tripName;
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
    private String placeSearchQuery = "";

    @FXML
    public void initialize() {
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
        initializeCoverPreview();
        initializeDragPreview();
        bindUploadPanelHandlers();
        initializePlaceSearch();

        configureButtonIcon(addPlaceButton, "fth-plus");
        configureButtonIcon(saveButton, "fth-save");
        configureButtonIcon(discardButton, "fth-trash-2");

        installRoundedClip(uploadArea, 16);

        populateDemoPlaces();
        renderPlaces();
    }

    private void initializePlaceSearch() {
        Search<RoutePlaceItem> searchModel = Search.<RoutePlaceItem>builder(this::searchPlaces)
                .placeholderKey("trip.add.picker.place.search")
                .noResultKey("trip.add.menu.place.empty")
                .variant(FieldVariant.FILLED)
                .maxVisibleResults(8)
                .showOnEmptyQuery(true)
                .build();

        placeSearchView = new SearchView<>(searchModel);
        placeSearchView.getStyleClass().add("add-route-place-search");
        placeSearchContainer.getChildren().setAll(placeSearchView);
    }

    private List<Entry<RoutePlaceItem>> searchPlaces(String query) {
        placeSearchQuery = query == null ? "" : query.trim();
        renderPlaces();

        return filteredPlaces().stream()
                .map(place -> Entry.builder(place, place.title()).icon("fth-map-pin").build())
                .toList();
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        tripId = data == null ? null : data.getValue("tripId");
        tripName = data == null ? null : data.getValue("tripName");
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
    private void onSave() {
        if (!titleInput.validateRequired()) {
            return;
        }

        String routeTitle = titleInput.getText().trim();
        String message = tripName == null || tripName.isBlank()
                ? "Route \"" + routeTitle + "\" is ready."
                : "Route \"" + routeTitle + "\" added to " + tripName + ".";
        toast.success("Route saved", message);
        getRouter().popBackStack();
    }

    @FXML
    private void onDiscard() {
        getRouter().popBackStack();
    }

    @FXML
    private void onAddPlace() {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", tripId == null ? 0 : tripId);
        args.addArgument("tripName", tripName == null || tripName.isBlank() ? "New Trip" : tripName);
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    @FXML
    private void onChooseCoverImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose route cover");
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

    private void populateDemoPlaces() {
        placeItems.clear();
        placeItems.add(new RoutePlaceItem("Uzhhorod Cathedral", "Uzhhorod, Ukraine", DEFAULT_IMAGE));
        placeItems.add(new RoutePlaceItem("Central Park", "New-York, USA", DEFAULT_IMAGE));
    }

    private void renderPlaces() {
        placesListContainer.getChildren().clear();
        List<RoutePlaceItem> filtered = filteredPlaces();
        if (filtered.isEmpty()) {
            String emptyText = placeItems.isEmpty()
                    ? "No places in this route yet."
                    : "No places match your search.";
            placesListContainer.getChildren().add(createEmptyState(emptyText));
            routeLengthLabel.setText(String.format(Locale.US, "Length: %.1fkm", placeItems.size() * 1.25));
            return;
        }

        int index = 1;
        for (RoutePlaceItem item : filtered) {
            placesListContainer.getChildren().add(buildPlaceRow(item, index++));
        }
        routeLengthLabel.setText(String.format(Locale.US, "Length: %.1fkm", placeItems.size() * 1.25));
    }

    private List<RoutePlaceItem> filteredPlaces() {
        if (placeSearchQuery == null || placeSearchQuery.isBlank()) {
            return placeItems;
        }

        String normalized = placeSearchQuery.toLowerCase(Locale.ROOT);
        return placeItems.stream()
                .filter(item -> item.title() != null && item.title().toLowerCase(Locale.ROOT).contains(normalized)
                        || item.subtitle() != null && item.subtitle().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
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
        Button editButton = createInlineIconButton("fth-edit-2", () ->
                toast.info("Edit place", item.title() + " editing can be added next."));
        Button removeButton = createInlineIconButton("fth-trash-2", () -> {
            placeItems.remove(item);
            renderPlaces();
        });
        Region handle = createHandle(item, row);
        actions.getChildren().addAll(editButton, removeButton, handle);

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
        ImageView view = new ImageView(loadImage(imagePath));
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

    private void configureButtonIcon(Button button, String iconLiteral) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(14);
        icon.getStyleClass().add("app-btn-icon");
        button.setGraphic(icon);
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

    private InputItem createInput(String placeholderKey) {
        InputItem input = new InputItem(placeholderKey, FieldVariant.FILLED);
        input.getStyleClass().add("add-route-field");
        return input;
    }

    private TextAreaItem createTextArea(String placeholderKey) {
        TextAreaItem input = new TextAreaItem(placeholderKey, FieldVariant.FILLED);
        input.getStyleClass().addAll("add-route-field", "add-route-textarea-field");
        input.setRows(6);
        return input;
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

    private record RoutePlaceItem(String title, String subtitle, String imagePath) { }
}
