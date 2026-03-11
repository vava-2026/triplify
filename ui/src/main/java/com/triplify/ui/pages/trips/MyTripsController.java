package com.triplify.ui.pages.trips;

import com.triplify.application.pagination.Pagination;
import com.triplify.application.request.SearchTripsRequest;
import com.triplify.application.request.TripSort;
import com.triplify.application.response.SearchTripsResponse;
import com.triplify.application.response.TripResponse;
import com.triplify.application.response.TripStatus;
import com.triplify.application.service.TripService;
import com.triplify.application.service.TripServiceImpl;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.trip.view.TripCardView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyTripsController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(MyTripsController.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final double CARD_MIN_WIDTH = 170;
    private static final double CARD_HEIGHT = 353;
    private static final double CARD_GAP = 12;
    private static final String FIRST_CARD_COVER = "/com/triplify/ui/shared/Images/trips/one.png";

    @FXML private ComboBox<String> countrySelect;
    @FXML private ComboBox<String> categorySelect;
    @FXML private ComboBox<String> tagSelect;
    @FXML private ComboBox<String> statusSelect;
    @FXML private ComboBox<String> startTimeSelect;
    @FXML private ComboBox<TripSort> sortSelect;
    @FXML private ScrollPane tripsScroll;
    @FXML private GridPane tripsGrid;

    private final TripService tripService = new TripServiceImpl();
    private int page = 1;
    private final int pageSize = 6;
    private boolean loading = false;
    private boolean hasMore = true;
    private boolean firstTripCoverApplied = false;
    private int currentColumns = 0;
    private double currentTileWidth = 0;

    @FXML
    private void initialize() {
        configureFilters();
        configureSort();
        configureGrid();
        attachListeners();
        attachScrollListener();
        refreshTrips(true);
    }

    @FXML
    public void onCreateTrip() {
        log.info("Create new trip clicked");
    }

    private void configureFilters() {
        countrySelect.setItems(FXCollections.observableArrayList(
                "All", "Ukraine", "Japan", "United States", "Kenya", "Czech Republic", "Canada"
        ));
        categorySelect.setItems(FXCollections.observableArrayList(
                "All", "Culture", "Tourism", "Nature", "Relax", "Memorial"
        ));
        tagSelect.setItems(FXCollections.observableArrayList(
                "All", "City", "Food", "Adventure", "Nature", "Family", "Shopping"
        ));
        statusSelect.setItems(FXCollections.observableArrayList(
                "All",
                TripStatus.VISITED.getLabel(),
                TripStatus.DRAFTED.getLabel(),
                TripStatus.PLANNED.getLabel(),
                TripStatus.ONGOING.getLabel(),
                TripStatus.REJECTED.getLabel()
        ));
        startTimeSelect.setItems(FXCollections.observableArrayList(
                "Any time", "Next 30 days", "Next 6 months", "Next year"
        ));

        countrySelect.getSelectionModel().selectFirst();
        categorySelect.getSelectionModel().selectFirst();
        tagSelect.getSelectionModel().selectFirst();
        statusSelect.getSelectionModel().selectFirst();
        startTimeSelect.getSelectionModel().selectFirst();
    }
    private void configureSort() {
        sortSelect.setItems(FXCollections.observableArrayList(TripSort.values()));
        sortSelect.getSelectionModel().select(TripSort.NEWEST_FIRST);
    }

    private void attachListeners() {
        countrySelect.valueProperty().addListener((obs, oldV, newV) -> refreshTrips(true));
        categorySelect.valueProperty().addListener((obs, oldV, newV) -> refreshTrips(true));
        tagSelect.valueProperty().addListener((obs, oldV, newV) -> refreshTrips(true));
        statusSelect.valueProperty().addListener((obs, oldV, newV) -> refreshTrips(true));
        startTimeSelect.valueProperty().addListener((obs, oldV, newV) -> refreshTrips(true));
        sortSelect.valueProperty().addListener((obs, oldV, newV) -> refreshTrips(true));
    }

    private void configureGrid() {
        tripsGrid.setHgap(CARD_GAP);
        tripsGrid.setVgap(CARD_GAP);
        tripsGrid.setAlignment(Pos.TOP_LEFT);
        tripsScroll.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (newBounds != null) {
                updateGridLayout(newBounds.getWidth());
            }
        });
        Platform.runLater(() -> {
            if (tripsScroll != null) {
                updateGridLayout(tripsScroll.getViewportBounds().getWidth());
            }
        });
    }

    private void attachScrollListener() {
        tripsScroll.vvalueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.doubleValue() >= 0.9) {
                refreshTrips(false);
            }
        });
    }

    private void refreshTrips(boolean resetPage) {
        if (loading) return;
        if (resetPage) {
            page = 1;
            hasMore = true;
            firstTripCoverApplied = false;
            tripsGrid.getChildren().clear();
            tripsGrid.getChildren().add(buildCreateTripCard());
            if (tripsScroll != null) {
                tripsScroll.setVvalue(0);
            }
            refreshGridLayout();
        }
        if (!hasMore) return;
        loading = true;
        SearchTripsRequest request = buildRequest();
        log.info("Trips search requested: {}", request);

        SearchTripsResponse response = tripService.searchTrips(request);
        renderTrips(response.trips(), resetPage);
        updatePagination(response.pagination());
        loading = false;
        ensureScrollableContent();
    }

    private SearchTripsRequest buildRequest() {
        String country = normalizeFilter(countrySelect.getValue());
        String category = normalizeFilter(categorySelect.getValue());
        String tag = normalizeFilter(tagSelect.getValue());
        TripStatus status = TripStatus.fromLabel(statusSelect.getValue());
        String startTime = normalizeStartTime(startTimeSelect.getValue());

        Pagination pagination = Pagination.request(page, pageSize);

        return new SearchTripsRequest(
                country,
                category,
                tag,
                status,
                startTime,
                sortSelect.getValue(),
                pagination
        );
    }

    private String normalizeFilter(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.equalsIgnoreCase("All") ? null : trimmed;
    }

    private String normalizeStartTime(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.equalsIgnoreCase("Any time") ? null : trimmed;
    }

    private void renderTrips(List<TripResponse> trips, boolean resetPage) {
        if (trips == null || trips.isEmpty()) {
            if (resetPage) {
                Label empty = new Label("No trips found");
                empty.getStyleClass().add("page-subtitle");
                tripsGrid.getChildren().add(empty);
            }
            hasMore = false;
            return;
        }

        int index = 0;
        for (TripResponse trip : trips) {
            TripResponse effectiveTrip = trip;
            if (!firstTripCoverApplied && index == 0) {
                effectiveTrip = new TripResponse(
                        trip.id(),
                        trip.name(),
                        trip.country(),
                        trip.category(),
                        trip.status(),
                        trip.startDate(),
                        trip.endDate(),
                        trip.coverKey(),
                        FIRST_CARD_COVER,
                        trip.tags()
                );
                firstTripCoverApplied = true;
            }
            String dateRange = formatDateRange(trip.startDate(), trip.endDate());
            TripCardView card = TripCardView.create(effectiveTrip, dateRange, () -> openTrip(trip, dateRange));
            tripsGrid.getChildren().add(card.getRoot());
            index++;
        }
        Platform.runLater(this::refreshGridLayout);
    }

    private Node buildCreateTripCard() {
        StackPane card = new StackPane();
        card.getStyleClass().add("trip-create-card");

        card.setPrefHeight(CARD_HEIGHT);
        card.setMinHeight(CARD_HEIGHT);
        card.setMaxHeight(CARD_HEIGHT);

        card.setOnMouseClicked(event -> onCreateTrip());

        VBox content = new VBox(6);
        content.setAlignment(Pos.CENTER);
        FontIcon icon = new FontIcon("fth-plus");
        icon.getStyleClass().add("trip-create-icon");

        Label title = new Label("Create New Trip");
        title.getStyleClass().add("trip-create-title");
        Label subtitle = new Label("Plan your next journey");
        subtitle.getStyleClass().add("trip-create-subtitle");

        content.getChildren().addAll(icon, title, subtitle);
        card.getChildren().add(content);
        return card;
    }

    private void refreshGridLayout() {
        if (tripsScroll == null) return;
        double width = tripsScroll.getViewportBounds().getWidth();
        if (width <= 0) {
            width = tripsScroll.getWidth();
        }
        updateGridLayout(width);
    }

    private void updateGridLayout(double viewportWidth) {
        if (tripsGrid == null || viewportWidth <= 0) return;

        double usableWidth = viewportWidth - getHorizontalInsets(tripsGrid.getInsets());
        if (usableWidth <= 0) return;

        int columns = resolveColumns(usableWidth);
        double tileWidth = resolveTileWidth(usableWidth, columns);

        boolean layoutChanged = columns != currentColumns || Math.abs(tileWidth - currentTileWidth) >= 0.5;
        currentColumns = columns;
        currentTileWidth = tileWidth;

        if (layoutChanged) {
            applyColumnConstraints(columns, tileWidth);
        }
        layoutCards(columns, tileWidth);
    }

    private double getHorizontalInsets(Insets insets) {
        if (insets == null) return 0;
        return insets.getLeft() + insets.getRight();
    }

    private int resolveColumns(double usableWidth) {
        double minWidthFor4Columns = 4 * CARD_MIN_WIDTH + 3 * CARD_GAP;
        double minWidthFor2Columns = 2 * CARD_MIN_WIDTH + CARD_GAP;

        if (usableWidth >= minWidthFor4Columns) {
            return 4;
        }
        if (usableWidth >= minWidthFor2Columns) {
            return 2;
        }
        return 1;
    }

    private double resolveTileWidth(double usableWidth, int columns) {
        double totalGap = (columns - 1) * CARD_GAP;
        double tileWidth = (usableWidth - totalGap) / columns;
        return Math.max(tileWidth, CARD_MIN_WIDTH);
    }

    private void applyColumnConstraints(int columns, double tileWidth) {
        tripsGrid.getColumnConstraints().clear();
        for (int i = 0; i < columns; i++) {
            ColumnConstraints constraint = new ColumnConstraints();
            constraint.setMinWidth(tileWidth);
            constraint.setPrefWidth(tileWidth);
            constraint.setMaxWidth(tileWidth);
            tripsGrid.getColumnConstraints().add(constraint);
        }
    }

    private void layoutCards(int columns, double tileWidth) {
        List<Node> nodes = tripsGrid.getChildren();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            int row = i / columns;
            int column = i % columns;
            GridPane.setRowIndex(node, row);
            GridPane.setColumnIndex(node, column);
            GridPane.setColumnSpan(node, 1);
            GridPane.setRowSpan(node, 1);
            applyCardWidth(node, tileWidth);
        }
    }

    private void applyCardWidth(Node node, double width) {
        if (!(node instanceof Region region)) {
            return;
        }
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
    }

    private void openTrip(TripResponse trip, String dateRange) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", trip.id());
        args.addArgument("tripName", trip.name());
        args.addArgument("tripCategory", trip.category());
        args.addArgument("tripStatus", trip.status());
        args.addArgument("tripDates", dateRange);
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }

    private void updatePagination(Pagination pagination) {
        if (pagination == null) {
            hasMore = false;
            return;
        }
        int totalPages = pagination.totalPages() == null ? 1 : pagination.totalPages();
        hasMore = pagination.page() < totalPages;
        page = pagination.page() + 1;
    }

    private void ensureScrollableContent() {
        Platform.runLater(() -> {
            if (loading || !hasMore) return;
            if (tripsScroll == null || tripsGrid == null) return;
            double viewportHeight = tripsScroll.getViewportBounds().getHeight();
            if (viewportHeight <= 0) return;
            double contentHeight = tripsGrid.getBoundsInLocal().getHeight();
            if (contentHeight <= viewportHeight + 1) {
                refreshTrips(false);
            }
        });
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null && end == null) return "Dates TBA";
        if (start != null && (end == null || start.equals(end))) {
            return start.format(DATE_FORMAT);
        }
        if (start != null && end != null) {
            if (start.getYear() == end.getYear() && start.getMonth() == end.getMonth()) {
                return String.format("%s %d - %d, %d",
                        start.getMonth().name().substring(0, 1) + start.getMonth().name().substring(1).toLowerCase(),
                        start.getDayOfMonth(),
                        end.getDayOfMonth(),
                        start.getYear()
                );
            }
            return start.format(DATE_FORMAT) + " - " + end.format(DATE_FORMAT);
        }
        return start == null ? end.format(DATE_FORMAT) : start.format(DATE_FORMAT);
    }
}
