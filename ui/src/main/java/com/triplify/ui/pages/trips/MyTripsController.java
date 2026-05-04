package com.triplify.ui.pages.trips;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.category.CategoryService;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.tag.TagService;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.GetTripsRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.pages.categories.Categories;
import com.triplify.ui.shared.component.add_card.view.AddCardView;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.tag_picker.TagPickerItem;
import com.triplify.ui.pages.countries.model.Countries;
import com.triplify.ui.pages.countries.view.CountriesView;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.pages.trips.view.TripCardView;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.Localization;

import static com.triplify.ui.shared.util.DisplayUtils.*;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
    private TagPickerItem tagFilterPicker;
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
                .searchOnTyping(true)
                .onResultSelected(entry -> cardGrid.refresh())
                .onLoadFailed(error -> log.warn("Failed to load country filter options: {}", error.code()))
                .build();
        countryFilterView = new CountriesView(countryFilterModel);
        countryFilterContainer.getChildren().setAll(countryFilterView);

        categoriesComponent = Categories.builder(categoryService).build();
        categorySelectModel = createCategorySelectModel(loadCategoryFilterEntries(), "Category");

        tagFilterPicker = new TagPickerItem();
        tagFilterPicker.setVariant(FieldVariant.FILLED);
        tagFilterPicker.setAllowCustomTags(false);
        tagFilterPicker.setPlaceholderKey("trips.filter.tag");
        tagFilterPicker.setPopupTitleKey("trip.add.tag.popupTitle");
        tagFilterPicker.setMaxWidth(Double.MAX_VALUE);
        tagFilterPicker.setOnSelectionChanged(selectedIds -> cardGrid.refresh());
        tagFilterPicker.configureTagService(tagService, error -> log.warn("Tag operation failed in trips filter: {}", error));

        statusSelectModel = Select.<StatusEnum>builder()
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

        startTimeSelectModel = Select.<String>builder()
                .placeholder(I18n.t("trips.filter.startTime"))
                .size(AppComponentSize.BIG)
                .items(List.of(
                        Entry.builder((String) null, Localization.textBinding("trips.filter.time.anyTime")).build(),
                        Entry.builder("next30days", Localization.textBinding("trips.filter.time.next30days")).build(),
                        Entry.builder("next6months", Localization.textBinding("trips.filter.time.next6months")).build(),
                        Entry.builder("nextYear", Localization.textBinding("trips.filter.time.nextYear")).build()
                ))
                .build();

        sortByModel = Select.<Boolean>builder()
                .placeholder(I18n.t("trips.sort.newestFirst"))
                .size(AppComponentSize.BIG)
                .items(List.of(
                        Entry.builder(false, Localization.textBinding("trips.sort.newestFirst")).build(),
                        Entry.builder(true, Localization.textBinding("trips.sort.oldestFirst")).build()
                ))
                .build();

        selectFirst(categorySelectModel);
        selectFirst(statusSelectModel);
        selectFirst(startTimeSelectModel);
        selectFirst(sortByModel);

        categorySelectContainer.getChildren().setAll(createFilterSelectView(categorySelectModel, 160));
        tagSelectContainer.getChildren().setAll(tagFilterPicker);
        statusSelectContainer.getChildren().setAll(createFilterSelectView(statusSelectModel, 160));
        startTimeSelectContainer.getChildren().setAll(createFilterSelectView(startTimeSelectModel, 180));
        sortSelectContainer.getChildren().setAll(createFilterSelectView(sortByModel, 180));

        Localization.bindText(sortByLabel.textProperty(), "trips.sort.label");
    }

    private void configureGrid() {
        cardGrid.setMinCardWidth(220);
        cardGrid.setMaxColumns(5);
        cardGrid.setPageSize(8);
        cardGrid.setEmptyTextKey("trips.empty");
        cardGrid.addPinnedNode(new AddCardView("trip.add.card.title", "trip.add.card.subtitle", this::onCreateTrip));
        cardGrid.setCardFactory(this::buildTripCard);
        cardGrid.setPageLoader(this::loadTripsPage);
    }

    private void attachListeners() {
        categorySelectModel.selectedItemProperty().addListener((obs, oldV, newV) -> cardGrid.refresh());
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
        Set<UUID> selectedTagIds = tagFilterPicker == null ? Set.of() : tagFilterPicker.getSelectedTagIds();

        var request = new GetTripsRequest(
                new PageRequest(Math.max(0, page - 1), pageSize),
                new GetTripsRequest.Filter(
                        null,
                        normalizeFilter(countryFilterView == null ? null : countryFilterView.getSelectedCountryId()),
                        statusFilter,
                        normalizeFilter(selectedValue(categorySelectModel)),
                        selectedTagIds,
                        startedFrom,
                        startedTo
                ),
                new GetTripsRequest.OrderBy(sortAsc)
        );

        var result = tripService.getTrips(request);
        if (result.isFailure()) {
            log.warn("Failed to load trips for My Trips page: {}", result.getError().message());
            return new CardGridPane.PageResult<>(List.of(), Pagination.request(page, pageSize).withTotals(0));
        }

        int totalPages = result.getValue().hasNext() ? page + 1 : page;
        return new CardGridPane.PageResult<>(result.getValue().items(), new Pagination(page, pageSize, null, totalPages));
    }

    private Node buildTripCard(TripResponse trip) {
        TripCardView card = TripCardView.create(trip, () -> openTrip(trip));
        return card.getRoot();
    }


    private void openTrip(TripResponse trip) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", trip.id().toString());
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }

    private String normalizeFilter(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.equalsIgnoreCase("All") ? null : trimmed;
    }

    private Select<String> createCategorySelectModel(List<Entry<String>> entries, String placeholder) {
        return Select.<String>builder()
                .placeholder(placeholder)
                .size(AppComponentSize.BIG)
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
