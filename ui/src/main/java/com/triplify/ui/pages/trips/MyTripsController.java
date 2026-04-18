package com.triplify.ui.pages.trips;

import com.google.inject.Inject;
import com.triplify.application.pagination.Pagination;
import com.triplify.application.request.TripSort;
import com.triplify.application.response.TripResponse;
import com.triplify.application.response.TripStatus;
import com.triplify.application.usecase.category.CategoryService;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.tag.TagService;
import com.triplify.application.usecase.tag.dto.GetTagsRequest;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.categories.model.Categories;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.countries.model.Countries;
import com.triplify.ui.shared.component.countries.view.CountriesView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.component.trip.view.TripCardView;
import com.triplify.ui.shared.model.FieldVariant;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MyTripsController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(MyTripsController.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final String ALL_OPTION = "All";

    @FXML private VBox countryFilterContainer;
    @FXML private VBox categorySelectContainer;
    @FXML private VBox tagSelectContainer;
    @FXML private javafx.scene.control.ComboBox<String> statusSelect;
    @FXML private javafx.scene.control.ComboBox<String> startTimeSelect;
    @FXML private javafx.scene.control.ComboBox<TripSort> sortSelect;
    @FXML private CardGridPane<TripResponse> cardGrid;

    @Inject private TripService tripService;
    @Inject private CountryService countryService;
    @Inject private CategoryService categoryService;
    @Inject private TagService tagService;
    private Categories categoriesComponent;
    private Select<String> categorySelectModel;
    private Select<String> tagSelectModel;
    private CountriesView countryFilterView;
    private List<CategoryResponse> availableCategories = List.of();

    @FXML
    private void initialize() {
        configureFilters();
        configureSort();
        configureGrid();
        attachListeners();
        cardGrid.refresh();
    }

    @Override
    public void onLifecycleShow() {
        if (cardGrid != null) {
            cardGrid.refresh();
        }
    }

    @FXML
    public void onCreateTrip() {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", UUID.randomUUID().toString());
        args.addArgument("tripName", "Create Trip");
        args.addArgument("tripStatus", TripStatus.DRAFTED);
        getRouter().moveto(RouteIds.ADD_TRIP, args);
    }

    private void configureFilters() {
        Countries countryFilterModel = Countries.builder(countryService)
                .placeholderKey("input.placeholder.country")
                .noResultKey("search.noResult")
                .variant(FieldVariant.FILLED)
                .searchOnTyping(true)
                .onResultSelected(entry -> cardGrid.refresh())
                .onLoadFailed(error -> log.warn("Failed to load country filter options: {}", error.code()))
                .build();
        countryFilterView = new CountriesView(countryFilterModel);
        countryFilterView.getStyleClass().add("trips-country-filter");
        countryFilterContainer.getChildren().setAll(countryFilterView);

        categoriesComponent = Categories.builder(categoryService).build();
        categorySelectModel = createCategorySelectModel(loadCategoryFilterEntries(), "Category");
        tagSelectModel = createSelectModelFromValues(loadTagFilterValues(), "Tags");
        statusSelect.setItems(javafx.collections.FXCollections.observableArrayList(
                "All",
                TripStatus.VISITED.getLabel(),
                TripStatus.DRAFTED.getLabel(),
                TripStatus.PLANNED.getLabel(),
                TripStatus.ONGOING.getLabel(),
                TripStatus.REJECTED.getLabel()
        ));
        startTimeSelect.setItems(javafx.collections.FXCollections.observableArrayList(
                "Any time", "Next 30 days", "Next 6 months", "Next year"
        ));

        statusSelect.getSelectionModel().selectFirst();
        startTimeSelect.getSelectionModel().selectFirst();
        selectFirst(categorySelectModel);
        selectFirst(tagSelectModel);

        categorySelectContainer.getChildren().setAll(createFilterSelectView(categorySelectModel, 130));
        tagSelectContainer.getChildren().setAll(createFilterSelectView(tagSelectModel, 120));
    }

    private void configureSort() {
        sortSelect.setItems(javafx.collections.FXCollections.observableArrayList(TripSort.values()));
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
        categorySelectModel.selectedItemProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        tagSelectModel.selectedItemProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        statusSelect.valueProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        startTimeSelect.valueProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        sortSelect.valueProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
    }

    private CardGridPane.PageResult<TripResponse> loadTripsPage(int page, int pageSize) {
        TripStatus statusFilter = TripStatus.fromLabel(statusSelect.getValue());
        if (statusFilter == TripStatus.DRAFTED || statusFilter == TripStatus.REJECTED) {
            return new CardGridPane.PageResult<>(List.of(), Pagination.request(page, pageSize).withTotals(0));
        }

        Instant now = Instant.now();
        Instant startedFrom = null;
        Instant startedTo = null;
        String startTimeFilter = normalizeStartTime(startTimeSelect.getValue());
        if (startTimeFilter != null) {
            startedFrom = now;
            startedTo = switch (startTimeFilter) {
                case "Next 30 days" -> now.plusSeconds(30L * 24 * 60 * 60);
                case "Next 6 months" -> now.plusSeconds(183L * 24 * 60 * 60);
                case "Next year" -> now.plusSeconds(365L * 24 * 60 * 60);
                default -> null;
            };
        }

        var request = new com.triplify.application.usecase.trip.dto.GetTripsRequest(
                new PageRequest(Math.max(0, page - 1), pageSize),
                new com.triplify.application.usecase.trip.dto.GetTripsRequest.Filter(
                        null,
                        normalizeFilter(countryFilterView == null ? null : countryFilterView.getSelectedCountryId()),
                        toDomainStatus(statusFilter),
                        normalizeFilter(selectedValue(categorySelectModel)),
                        null,
                        startedFrom,
                        startedTo
                ),
                new com.triplify.application.usecase.trip.dto.GetTripsRequest.OrderBy(sortSelect.getValue() == TripSort.OLDEST_FIRST)
        );

        var result = tripService.getTrips(request);
        if (result.isFailure()) {
            log.warn("Failed to load trips for My Trips page: {}", result.getError().message());
            return new CardGridPane.PageResult<>(List.of(), Pagination.request(page, pageSize).withTotals(0));
        }

        List<TripResponse> trips = result.getValue().items().stream()
                .map(this::toLegacyTrip)
                .filter(trip -> matchesTag(trip.tags(), normalizeFilter(selectedValue(tagSelectModel))))
                .sorted(resolveComparator(sortSelect.getValue()))
                .toList();

        int totalPages = result.getValue().hasNext() ? page + 1 : page;
        return new CardGridPane.PageResult<>(trips, new Pagination(page, pageSize, null, totalPages));
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
        args.addArgument("tripId", trip.id());
        args.addArgument("tripName", trip.name());
        args.addArgument("tripCountry", trip.country());
        args.addArgument("tripCategory", trip.category());
        args.addArgument("tripStatus", trip.status());
        args.addArgument("tripDates", dateRange);
        args.addArgument("tripStartDate", trip.startDate() == null ? null : trip.startDate().toString());
        args.addArgument("tripEndDate", trip.endDate() == null ? null : trip.endDate().toString());
        args.addArgument("tripCoverUrl", trip.coverUrl());
        args.addArgument("tripTags", trip.tags() == null ? "" : String.join(",", trip.tags()));
        getRouter().moveto(RouteIds.ADD_TRIP, args);
    }

    private TripResponse toLegacyTrip(com.triplify.application.usecase.trip.dto.TripResponse trip) {
        return new TripResponse(
                trip.id(),
                trip.title(),
                deriveCountryLabel(trip.countries()),
                trip.category() == null ? "" : trip.category().name(),
                toLegacyStatus(trip.status()),
                toLocalDate(trip.startedAt()),
                toLocalDate(trip.endedAt()),
                null,
                deriveCoverUrl(trip.images()),
                trip.tags() == null ? List.of() : trip.tags().stream().map(TagResponse::name).toList()
        );
    }

    private String deriveCountryLabel(java.util.Set<com.triplify.application.usecase.country.dto.CountryResponse> countries) {
        if (countries == null || countries.isEmpty()) {
            return "";
        }
        if (countries.size() == 1) {
            return countries.iterator().next().name();
        }
        return countries.iterator().next().name() + " +" + (countries.size() - 1);
    }

    private String deriveCoverUrl(java.util.Set<ImageResponse> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.iterator().next().url().toUri().toString();
    }

    private LocalDate toLocalDate(Instant value) {
        return value == null ? null : value.atZone(ZoneOffset.UTC).toLocalDate();
    }

    private TripStatus toLegacyStatus(StatusEnum status) {
        if (status == null) {
            return TripStatus.PLANNED;
        }
        return switch (status) {
            case VISITED -> TripStatus.VISITED;
            case ONGOING -> TripStatus.ONGOING;
            case PLANNED, CANCELED -> TripStatus.PLANNED;
        };
    }

    private StatusEnum toDomainStatus(TripStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case VISITED -> StatusEnum.VISITED;
            case PLANNED -> StatusEnum.PLANNED;
            case ONGOING -> StatusEnum.ONGOING;
            case DRAFTED, REJECTED -> null;
        };
    }

    private boolean matchesText(String actual, String expected) {
        if (expected == null || expected.isBlank()) return true;
        if (actual == null) return false;
        return actual.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
    }

    private boolean matchesTag(List<String> tags, String expected) {
        if (expected == null || expected.isBlank()) return true;
        if (tags == null || tags.isEmpty()) return false;
        String normalized = expected.toLowerCase(Locale.ROOT);
        return tags.stream().anyMatch(tag -> tag != null && tag.toLowerCase(Locale.ROOT).contains(normalized));
    }

    private Comparator<TripResponse> resolveComparator(TripSort sort) {
        return switch (sort) {
            case NAME_ASC -> Comparator.comparing(TripResponse::name, String.CASE_INSENSITIVE_ORDER);
            case OLDEST_FIRST -> Comparator.comparing(TripResponse::startDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case NEWEST_FIRST -> Comparator.comparing(TripResponse::startDate, Comparator.nullsLast(Comparator.reverseOrder()));
        };
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

    private Select<String> createSelectModelFromValues(List<String> values, String placeholder) {
        return Select.<String>builder()
                .placeholder(placeholder)
                .variant(FieldVariant.FILLED)
                .items(values.stream().map(this::toEntry).toList())
                .build();
    }

    private Select<String> createCategorySelectModel(List<Entry<String>> entries, String placeholder) {
        return Select.<String>builder()
                .placeholder(placeholder)
                .variant(FieldVariant.FILLED)
                .items(entries)
                .build();
    }

    private List<Entry<String>> loadCategoryFilterEntries() {
        if (categoriesComponent == null) {
            return List.of(Entry.builder("", ALL_OPTION).build());
        }

        var result = categoriesComponent.loadAll();
        if (result.isFailure()) {
            log.warn("Failed to load categories for My Trips filters: {}", result.getError().message());
            availableCategories = List.of();
            return List.of(Entry.builder("", ALL_OPTION).build());
        }

        availableCategories = result.getValue();
        return categoriesComponent.toEntriesWithAll(availableCategories, ALL_OPTION);
    }

    private List<String> loadTagFilterValues() {
        List<String> tags = new ArrayList<>();
        tags.add(ALL_OPTION);

        PageRequest pageRequest = PageRequest.defaultRequest();
        while (true) {
            var result = tagService.getTags(new GetTagsRequest(pageRequest, null));
            if (result.isFailure()) {
                log.warn("Failed to load tags for My Trips filters: {}", result.getError().message());
                return List.copyOf(new LinkedHashSet<>(tags));
            }

            for (TagResponse tag : result.getValue().items()) {
                if (tag == null || tag.name() == null || tag.name().isBlank()) {
                    continue;
                }
                tags.add(tag.name().trim());
            }

            if (!result.getValue().hasNext()) {
                break;
            }
            pageRequest = pageRequest.next();
        }

        return List.copyOf(new LinkedHashSet<>(tags));
    }

    private SelectView<String> createFilterSelectView(Select<String> model, double width) {
        SelectView<String> view = new SelectView<>();
        view.update(model);
        view.setPrefWidth(width);
        view.setMaxWidth(width);
        view.getComboBox().setPrefWidth(width);
        view.getComboBox().setMaxWidth(width);
        view.getComboBox().getStyleClass().add("trips-filter");
        return view;
    }

    private void selectFirst(Select<String> model) {
        if (model != null && !model.getItems().isEmpty()) {
            model.setSelectedItem(model.getItems().get(0));
        }
    }

    private Entry<String> toEntry(String value) {
        return Entry.builder(value, value).build();
    }

    private String selectedValue(Select<String> model) {
        if (model == null || model.getSelectedItem() == null) {
            return null;
        }
        return model.getSelectedItem().getValue();
    }
}
