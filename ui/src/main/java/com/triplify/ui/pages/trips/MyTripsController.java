package com.triplify.ui.pages.trips;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.category.CategoryService;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.tag.TagService;
import com.triplify.application.usecase.tag.dto.GetTagsRequest;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.i18n.I18n;
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
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.DisplayUtils.*;

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
import java.util.*;

public class MyTripsController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(MyTripsController.class);
    private static final String ALL_OPTION = "All";

    @FXML private VBox countryFilterContainer;
    @FXML private VBox categorySelectContainer;
    @FXML private VBox tagSelectContainer;
    @FXML private VBox statusSelectContainer;
    @FXML private VBox startTimeSelectContainer;
    @FXML private Label sortByLabel;
    @FXML private VBox sortSelectContainer;
    @FXML private CardGridPane<TripResponse> cardGrid;

    @Inject private TripService tripService;
    @Inject private CountryService countryService;
    @Inject private CategoryService categoryService;
    @Inject private TagService tagService;

    private Categories categoriesComponent;
    private Select<String> categorySelectModel;
    private Select<String> tagSelectModel;
    private Select<StatusEnum> statusSelectModel;
    private Select<String> startTimeSelectModel;
    private Select<Boolean> sortByModel;
    private CountriesView countryFilterView;
    private List<CategoryResponse> availableCategories = List.of();

    @FXML
    private void initialize() {
        configureFilters();
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
        args.addArgument("tripStatus", StatusEnum.PLANNED);
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

        statusSelectModel = Select.<StatusEnum>builder()
                .placeholder(I18n.t("trips.filter.status"))
                .variant(FieldVariant.FILLED)
                .items(List.of(
                        Entry.builder((StatusEnum) null, Localization.textBinding("trips.filter.all")).build(),
                        Entry.builder(StatusEnum.PLANNED, Localization.textBinding("trip.status.planned")).build(),
                        Entry.builder(StatusEnum.ONGOING, Localization.textBinding("trip.status.ongoing")).build(),
                        Entry.builder(StatusEnum.VISITED, Localization.textBinding("trip.status.visited")).build(),
                        Entry.builder(StatusEnum.CANCELED, Localization.textBinding("trip.status.canceled")).build()
                ))
                .build();

        startTimeSelectModel = Select.<String>builder()
                .placeholder(I18n.t("trips.filter.startTime"))
                .variant(FieldVariant.FILLED)
                .items(List.of(
                        Entry.builder((String) null, Localization.textBinding("trips.filter.time.anyTime")).build(),
                        Entry.builder("next30days", Localization.textBinding("trips.filter.time.next30days")).build(),
                        Entry.builder("next6months", Localization.textBinding("trips.filter.time.next6months")).build(),
                        Entry.builder("nextYear", Localization.textBinding("trips.filter.time.nextYear")).build()
                ))
                .build();

        sortByModel = Select.<Boolean>builder()
                .placeholder(I18n.t("trips.sort.newestFirst"))
                .variant(FieldVariant.FILLED)
                .items(List.of(
                        Entry.builder(false, Localization.textBinding("trips.sort.newestFirst")).build(),
                        Entry.builder(true, Localization.textBinding("trips.sort.oldestFirst")).build()
                ))
                .build();

        selectFirst(categorySelectModel);
        selectFirst(tagSelectModel);
        selectFirst(statusSelectModel);
        selectFirst(startTimeSelectModel);
        selectFirst(sortByModel);

        categorySelectContainer.getChildren().setAll(createFilterSelectView(categorySelectModel, 130));
        tagSelectContainer.getChildren().setAll(createFilterSelectView(tagSelectModel, 120));
        statusSelectContainer.getChildren().setAll(createFilterSelectView(statusSelectModel, 130));
        startTimeSelectContainer.getChildren().setAll(createFilterSelectView(startTimeSelectModel, 150));
        sortSelectContainer.getChildren().setAll(createFilterSelectView(sortByModel, 150));

        Localization.bindText(sortByLabel.textProperty(), "trips.sort.label");
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
        statusSelectModel.selectedItemProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        startTimeSelectModel.selectedItemProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
        sortByModel.selectedItemProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
    }

    private CardGridPane.PageResult<TripResponse> loadTripsPage(int page, int pageSize) {
        StatusEnum statusFilter = statusSelectModel.getSelectedItem() != null
                ? statusSelectModel.getSelectedItem().getValue()
                : null;

        Instant now = Instant.now();
        Instant startedFrom = null;
        Instant startedTo = null;
        String startTimeKey = startTimeSelectModel.getSelectedItem() != null
                ? startTimeSelectModel.getSelectedItem().getValue()
                : null;
        if (startTimeKey != null) {
            startedFrom = now;
            startedTo = switch (startTimeKey) {
                case "next30days" -> now.plusSeconds(30L * 24 * 60 * 60);
                case "next6months" -> now.plusSeconds(183L * 24 * 60 * 60);
                case "nextYear" -> now.plusSeconds(365L * 24 * 60 * 60);
                default -> null;
            };
        }

        boolean sortAsc = sortByModel.getSelectedItem() != null
                && Boolean.TRUE.equals(sortByModel.getSelectedItem().getValue());

        var request = new com.triplify.application.usecase.trip.dto.GetTripsRequest(
                new PageRequest(Math.max(0, page - 1), pageSize),
                new com.triplify.application.usecase.trip.dto.GetTripsRequest.Filter(
                        null,
                        normalizeFilter(countryFilterView == null ? null : countryFilterView.getSelectedCountryId()),
                        statusFilter,
                        normalizeFilter(selectedValue(categorySelectModel)),
                        null,
                        startedFrom,
                        startedTo
                ),
                new com.triplify.application.usecase.trip.dto.GetTripsRequest.OrderBy(sortAsc)
        );

        var result = tripService.getTrips(request);
        if (result.isFailure()) {
            log.warn("Failed to load trips for My Trips page: {}", result.getError().message());
            return new CardGridPane.PageResult<>(List.of(), Pagination.request(page, pageSize).withTotals(0));
        }

        List<TripResponse> trips = result.getValue().items().stream()
                .filter(trip -> matchesTag(trip.tags(), normalizeFilter(selectedValue(tagSelectModel))))
                .toList();

        int totalPages = result.getValue().hasNext() ? page + 1 : page;
        return new CardGridPane.PageResult<>(trips, new Pagination(page, pageSize, null, totalPages));
    }

    private Node buildTripCard(TripResponse trip) {
        String dateRange = formatDateRange(toLocalDate(trip.startedAt()), toLocalDate(trip.endedAt()));
        TripCardView card = TripCardView.create(trip, dateRange, () -> openTrip(trip));
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

    private void openTrip(TripResponse trip) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", trip.id().toString());
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }

    private boolean matchesTag(java.util.Set<TagResponse> tags, String expected) {
        if (expected == null || expected.isBlank()) return true;
        if (tags == null || tags.isEmpty()) return false;
        String normalized = expected.toLowerCase(Locale.ROOT);
        return tags.stream()
                .map(TagResponse::name)
                .filter(Objects::nonNull)
                .anyMatch(tag -> tag.toLowerCase(Locale.ROOT).contains(normalized));
    }

    private String normalizeFilter(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.equalsIgnoreCase("All") ? null : trimmed;
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

        var result = tagService.getTags(GetTagsRequest.defaultRequest());
        if (result.isFailure()) {
            log.warn("Failed to load tags for My Trips filters: {}", result.getError().message());
            return List.copyOf(new LinkedHashSet<>(tags));
        }

        for (TagResponse tag : result.getValue()) {
            if (tag == null || tag.name() == null || tag.name().isBlank()) {
                continue;
            }
            tags.add(tag.name().trim());
        }

        return List.copyOf(new LinkedHashSet<>(tags));
    }

    private <T> SelectView<T> createFilterSelectView(Select<T> model, double width) {
        SelectView<T> view = new SelectView<>();
        view.update(model);
        view.setPrefWidth(width);
        view.setMaxWidth(width);
        view.getComboBox().setPrefWidth(width);
        view.getComboBox().setMaxWidth(width);
        view.getComboBox().getStyleClass().add("trips-filter");
        return view;
    }

    private <T> void selectFirst(Select<T> model) {
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
