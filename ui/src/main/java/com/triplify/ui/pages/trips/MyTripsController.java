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
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.trip.view.TripCardView;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MyTripsController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(MyTripsController.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML private ComboBox<String> countrySelect;
    @FXML private ComboBox<String> categorySelect;
    @FXML private ComboBox<String> tagSelect;
    @FXML private ComboBox<String> statusSelect;
    @FXML private ComboBox<String> startTimeSelect;
    @FXML private ComboBox<TripSort> sortSelect;
    @FXML private CardGridPane<TripResponse> cardGrid;

    private final TripService tripService = new TripServiceImpl();

    @FXML
    private void initialize() {
        configureFilters();
        configureSort();
        configureGrid();
        attachListeners();
        cardGrid.refresh();
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

    private void configureGrid() {
        cardGrid.setMinCardWidth(220);
        cardGrid.setMaxColumns(4);
        cardGrid.setPageSize(8);
        cardGrid.setEmptyText("No trips found");
        cardGrid.addPinnedNode(buildCreateTripCard());
        cardGrid.setCardFactory(this::buildTripCard);
        cardGrid.setPageLoader(this::loadTripsPage);
    }

    private void attachListeners() {
        countrySelect.valueProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        categorySelect.valueProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        tagSelect.valueProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        statusSelect.valueProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        startTimeSelect.valueProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        sortSelect.valueProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
    }

    private CardGridPane.PageResult<TripResponse> loadTripsPage(int page, int pageSize) {
        String country = normalizeFilter(countrySelect.getValue());
        String category = normalizeFilter(categorySelect.getValue());
        String tag = normalizeFilter(tagSelect.getValue());
        TripStatus status = TripStatus.fromLabel(statusSelect.getValue());
        String startTime = normalizeStartTime(startTimeSelect.getValue());

        SearchTripsRequest request = new SearchTripsRequest(
                country, category, tag, status, startTime,
                sortSelect.getValue(),
                Pagination.request(page, pageSize)
        );
        log.info("Trips search requested: {}", request);

        SearchTripsResponse response = tripService.searchTrips(request);
        return new CardGridPane.PageResult<>(response.trips(), response.pagination());
    }

    private Node buildTripCard(TripResponse trip) {
        String dateRange = formatDateRange(trip.startDate(), trip.endDate());
        TripCardView card = TripCardView.create(trip, dateRange, () -> openTrip(trip, dateRange));
        return card.getRoot();
    }

    private Node buildCreateTripCard() {
        StackPane card = new StackPane();
        card.getStyleClass().add("trip-create-card");
        card.setMaxWidth(Double.MAX_VALUE);
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

    private void openTrip(TripResponse trip, String dateRange) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", Integer.valueOf(String.valueOf(trip.id())));
        args.addArgument("tripName", trip.name());
        args.addArgument("tripCategory", trip.category());
        args.addArgument("tripStatus", trip.status());
        args.addArgument("tripDates", dateRange);
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
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
