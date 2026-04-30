package com.triplify.ui.pages.calendar;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.GetTripsForCalendarRequest;
import com.triplify.application.usecase.trip.dto.GetUndatedTripsRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.pages.trips.view.TripCardView;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.triplify.ui.shared.util.DisplayUtils.formatDateRange;
import static com.triplify.ui.shared.util.DisplayUtils.toLocalDate;

public class CalendarController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(CalendarController.class);
    private static final String DEFAULT_COVER = "/com/triplify/ui/pages/trips/images/one.png";
    private static final int UNDATED_PAGE_SIZE = 10;

    // FXML
    @FXML private HBox navBar;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private Label monthYearLabel;
    @FXML private GridPane dayHeaderRow;
    @FXML private ScrollPane calendarScroll;
    @FXML private GridPane calendarGrid;
    @FXML private VBox selectedTripPanel;
    @FXML private ImageView selectedCoverImage;
    @FXML private Label selectedTripTitle;
    @FXML private Label selectedCategoryChip;
    @FXML private Label undatedTitleLabel;
    @FXML private CardGridPane<TripResponse> undatedTripsGrid;

    // Injected
    @Inject private TripService tripService;

    // State
    private YearMonth currentMonth;
    private StatusEnum selectedStatus;
    private Select<StatusEnum> statusSelect;
    private final Map<UUID, List<Node>> chipsByTripId = new HashMap<>();
    private TripResponse selectedTrip;
    private List<TripResponse> currentMonthTrips = List.of();

    @FXML
    private void initialize() {
        currentMonth = YearMonth.now();
        buildDayHeaders();
        buildStatusFilter();
        setupNavButtons();
        setupUndatedGrid();
    }

    @Override
    public void onLifecycleShow() {
        refreshAll();
    }

    // ── Setup ──────────────────────────────────────────────────────────

    private void buildDayHeaders() {
        DayOfWeek[] days = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };
        String[] keys = {
            "calendar.day.mon", "calendar.day.tue", "calendar.day.wed",
            "calendar.day.thu", "calendar.day.fri", "calendar.day.sat", "calendar.day.sun"
        };

        dayHeaderRow.getColumnConstraints().clear();
        for (int col = 0; col < 7; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.SOMETIMES);
            dayHeaderRow.getColumnConstraints().add(cc);

            Label lbl = new Label();
            lbl.getStyleClass().add("day-header-cell");
            lbl.setMaxWidth(Double.MAX_VALUE);
            Localization.bindText(lbl.textProperty(), keys[col]);
            dayHeaderRow.add(lbl, col, 0);
        }
    }

    private void buildStatusFilter() {
        statusSelect = Select.<StatusEnum>builder()
                .placeholder(I18n.t("trips.filter.status"))
                .size(AppComponentSize.BIG)
                .items(List.of(
                        Entry.builder((StatusEnum) null, Localization.textBinding("trips.filter.all")).build(),
                        Entry.builder(StatusEnum.PLANNED, Localization.textBinding("trip.status.planned")).build(),
                        Entry.builder(StatusEnum.ONGOING, Localization.textBinding("trip.status.ongoing")).build(),
                        Entry.builder(StatusEnum.VISITED, Localization.textBinding("trip.status.visited")).build(),
                        Entry.builder(StatusEnum.CANCELED, Localization.textBinding("trip.status.canceled")).build()
                ))
                .build();

        if (!statusSelect.getItems().isEmpty()) {
            statusSelect.setSelectedItem(statusSelect.getItems().get(0));
        }

        statusSelect.selectedItemProperty().addListener((obs, oldV, newV) -> refreshAll());

        SelectView<StatusEnum> filterView = new SelectView<>();
        filterView.update(statusSelect);
        filterView.setPrefWidth(160);
        filterView.setMaxWidth(160);
        filterView.getComboBox().setPrefWidth(160);

        navBar.getChildren().add(filterView);
    }

    private void setupNavButtons() {
        prevMonthBtn.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshAll();
        });
        nextMonthBtn.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshAll();
        });
    }

    private void setupUndatedGrid() {
        Localization.bindText(undatedTitleLabel.textProperty(), "calendar.panel.undated.title");
        undatedTripsGrid.setManualLoadMore(true);
        undatedTripsGrid.setMaxColumns(1);
        undatedTripsGrid.setMinCardWidth(240);
        undatedTripsGrid.setPageSize(UNDATED_PAGE_SIZE);
        undatedTripsGrid.setEmptyText(I18n.t("calendar.panel.undated.empty"));
        undatedTripsGrid.setCardFactory(this::buildUndatedCard);
        undatedTripsGrid.setPageLoader(this::loadUndatedPage);
    }

    // ── Data refresh ──────────────────────────────────────────────────

    private void refreshAll() {
        selectedStatus = statusSelect != null && statusSelect.getSelectedItem() != null
                ? statusSelect.getSelectedItem().getValue()
                : null;
        updateMonthLabel();
        loadMonthTrips();
        undatedTripsGrid.refresh();
        clearSelectedTrip();
    }

    private void updateMonthLabel() {
        String formatted = currentMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        );
        monthYearLabel.setText(formatted);
    }

    private void loadMonthTrips() {
        var result = tripService.getTripsForCalendar(
                new GetTripsForCalendarRequest(currentMonth, selectedStatus)
        );
        if (result.isFailure()) {
            log.warn("Failed to load calendar trips: {}", result.getError().message());
            currentMonthTrips = List.of();
        } else {
            currentMonthTrips = result.getValue();
        }
        buildCalendarGrid();
    }

    // ── Calendar grid construction ────────────────────────────────────

    private void buildCalendarGrid() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();
        chipsByTripId.clear();

        for (int col = 0; col < 7; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.SOMETIMES);
            calendarGrid.getColumnConstraints().add(cc);
        }

        int startOffset = currentMonth.atDay(1).getDayOfWeek().getValue() - 1; // Mon=0
        int daysInMonth = currentMonth.lengthOfMonth();
        int totalCells = startOffset + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);

        for (int row = 0; row < rows; row++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(84);
            rc.setVgrow(Priority.SOMETIMES);
            calendarGrid.getRowConstraints().add(rc);
        }

        for (int i = 0; i < rows * 7; i++) {
            int dayNum = i - startOffset + 1;
            int col = i % 7;
            int row = i / 7;

            Node cell;
            if (dayNum < 1 || dayNum > daysInMonth) {
                VBox filler = new VBox();
                filler.getStyleClass().add("calendar-cell-filler");
                cell = filler;
            } else {
                cell = buildDayCell(currentMonth.atDay(dayNum));
            }
            calendarGrid.add(cell, col, row);
        }
    }

    private VBox buildDayCell(LocalDate date) {
        VBox cell = new VBox(2);
        cell.getStyleClass().add("calendar-cell");
        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("calendar-cell-today");
        }

        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.getStyleClass().add("day-number");
        dayNum.setMaxWidth(Double.MAX_VALUE);
        cell.getChildren().add(dayNum);

        for (TripResponse trip : currentMonthTrips) {
            if (tripOccursOnDay(trip, date)) {
                Node chip = buildChip(trip, date);
                cell.getChildren().add(chip);
                chipsByTripId.computeIfAbsent(trip.id(), k -> new ArrayList<>()).add(chip);
            }
        }
        return cell;
    }

    private boolean tripOccursOnDay(TripResponse trip, LocalDate date) {
        LocalDate start = trip.startedAt() != null
                ? trip.startedAt().atZone(ZoneOffset.UTC).toLocalDate() : null;
        LocalDate end = trip.endedAt() != null
                ? trip.endedAt().atZone(ZoneOffset.UTC).toLocalDate() : null;

        if (start != null && end != null) return !date.isBefore(start) && !date.isAfter(end);
        if (start != null) return date.equals(start);
        if (end != null) return date.equals(end);
        return false;
    }

    private Node buildChip(TripResponse trip, LocalDate cellDate) {
        boolean isSpan = trip.startedAt() != null && trip.endedAt() != null;
        String statusKey = trip.status() != null ? trip.status().name().toLowerCase() : "planned";

        HBox chip = new HBox();
        chip.getStyleClass().addAll("trip-chip",
                isSpan ? "trip-chip-span" : "trip-chip-point",
                "trip-chip-" + statusKey);

        if (isSpan) {
            LocalDate effectiveStart = trip.startedAt().atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate monthFirst = currentMonth.atDay(1);
            boolean isFirstCell = cellDate.equals(effectiveStart) || cellDate.equals(monthFirst);

            Label lbl = new Label(trip.title());
            lbl.getStyleClass().add("trip-chip-label");
            lbl.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lbl, Priority.ALWAYS);
            lbl.setVisible(isFirstCell);
            lbl.setManaged(isFirstCell);
            chip.getChildren().add(lbl);
        }

        Tooltip.install(chip, new Tooltip(trip.title()));
        chip.setOnMouseClicked(e -> {
            e.consume();
            handleChipClick(trip);
        });
        return chip;
    }

    // ── Selection ─────────────────────────────────────────────────────

    private void handleChipClick(TripResponse trip) {
        boolean isSame = selectedTrip != null && selectedTrip.id().equals(trip.id());
        clearSelectedTrip();
        if (!isSame) {
            selectedTrip = trip;
            highlightTrip(trip);
            showSelectedTripPanel(trip);
        }
    }

    private void highlightTrip(TripResponse trip) {
        for (Map.Entry<UUID, List<Node>> entry : chipsByTripId.entrySet()) {
            boolean isSelected = entry.getKey().equals(trip.id());
            for (Node chip : entry.getValue()) {
                if (isSelected) {
                    if (!chip.getStyleClass().contains("trip-chip-selected")) {
                        chip.getStyleClass().add("trip-chip-selected");
                    }
                } else {
                    chip.getStyleClass().remove("trip-chip-selected");
                }
            }
        }
    }

    private void clearSelectedTrip() {
        for (List<Node> chips : chipsByTripId.values()) {
            for (Node chip : chips) {
                chip.getStyleClass().remove("trip-chip-selected");
            }
        }
        selectedTrip = null;
        selectedTripPanel.setVisible(false);
        selectedTripPanel.setManaged(false);
    }

    private void showSelectedTripPanel(TripResponse trip) {
        selectedTripTitle.setText(trip.title());

        if (trip.category() != null) {
            Localization.bindLocalizedText(selectedCategoryChip.textProperty(), trip.category());
        } else {
            selectedCategoryChip.textProperty().unbind();
            selectedCategoryChip.setText("");
        }

        String imagePath = trip.coverImage() != null ? trip.coverImage().url().toString() : null;
        Image img = EditorUtils.loadImage(imagePath, DEFAULT_COVER, CalendarController.class);
        selectedCoverImage.setImage(img);

        selectedTripPanel.setVisible(true);
        selectedTripPanel.setManaged(true);
    }

    // ── Undated grid ──────────────────────────────────────────────────

    private Node buildUndatedCard(TripResponse trip) {
        String dateRange = formatDateRange(toLocalDate(trip.startedAt()), toLocalDate(trip.endedAt()));
        TripCardView card = TripCardView.create(trip, dateRange, () -> openTrip(trip));
        return card.getRoot();
    }

    private CardGridPane.PageResult<TripResponse> loadUndatedPage(int page, int pageSize) {
        var request = new GetUndatedTripsRequest(
                new PageRequest(Math.max(0, page - 1), pageSize),
                selectedStatus
        );
        var result = tripService.getUndatedTrips(request);
        if (result.isFailure()) {
            log.warn("Failed to load undated trips: {}", result.getError().message());
            return new CardGridPane.PageResult<>(List.of(),
                    Pagination.request(page, pageSize).withTotals(0));
        }
        var pageData = result.getValue();
        int totalPages = pageData.hasNext() ? page + 1 : page;
        return new CardGridPane.PageResult<>(pageData.items(),
                new Pagination(page, pageSize, null, totalPages));
    }

    private void openTrip(TripResponse trip) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", trip.id().toString());
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }
}
