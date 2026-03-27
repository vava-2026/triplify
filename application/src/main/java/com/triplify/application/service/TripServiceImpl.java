package com.triplify.application.service;

import com.triplify.application.pagination.Pagination;
import com.triplify.application.request.SearchTripsRequest;
import com.triplify.application.request.TripSort;
import com.triplify.application.response.SearchTripsResponse;
import com.triplify.application.response.TripResponse;
import com.triplify.application.response.TripStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class TripServiceImpl implements TripService {

    private static final Logger log = LoggerFactory.getLogger(TripServiceImpl.class);
    private static final int DEFAULT_PAGE_SIZE = 6;

    private static final String COVER = "/com/triplify/ui/pages/trips/images/one.png";

    private final List<TripResponse> trips = List.of(
            new TripResponse(
                    101,
                    "Uzhorod Vacation",
                    "Ukraine",
                    "Culture",
                    TripStatus.VISITED,
                    LocalDate.of(2021, 5, 9),
                    LocalDate.of(2021, 5, 11),
                    "uzhorod",
                    COVER,
                    List.of("City", "Food")
            ),
            new TripResponse(
                    102,
                    "Kyoto Autumn",
                    "Japan",
                    "Culture",
                    TripStatus.DRAFTED,
                    LocalDate.of(2023, 10, 12),
                    LocalDate.of(2023, 10, 24),
                    "kyoto",
                    COVER,
                    List.of("Leaves", "Temple")
            ),
            new TripResponse(
                    103,
                    "Arsen's Mother",
                    "Ukraine",
                    "Memorial",
                    TripStatus.REJECTED,
                    LocalDate.of(2001, 9, 9),
                    LocalDate.of(2001, 9, 11),
                    "cemetery",
                    COVER,
                    List.of("Family")
            ),
            new TripResponse(
                    104,
                    "New-York",
                    "United States",
                    "Tourism",
                    TripStatus.PLANNED,
                    LocalDate.of(2026, 12, 16),
                    LocalDate.of(2026, 12, 24),
                    "newyork",
                    COVER,
                    List.of("City", "Shopping")
            ),
            new TripResponse(
                    105,
                    "Safari Trip",
                    "Kenya",
                    "Nature",
                    TripStatus.ONGOING,
                    LocalDate.of(2024, 1, 30),
                    LocalDate.of(2024, 2, 8),
                    "safari",
                    COVER,
                    List.of("Wildlife", "Adventure")
            ),
            new TripResponse(
                    106,
                    "Prague Weekend",
                    "Czech Republic",
                    "Culture",
                    TripStatus.VISITED,
                    LocalDate.of(2022, 4, 1),
                    LocalDate.of(2022, 4, 3),
                    "prague",
                    COVER,
                    List.of("City", "Walks")
            ),
            new TripResponse(
                    107,
                    "Lake Retreat",
                    "Canada",
                    "Relax",
                    TripStatus.PLANNED,
                    LocalDate.of(2025, 7, 12),
                    LocalDate.of(2025, 7, 19),
                    "lake",
                    COVER,
                    List.of("Relax", "Nature")
            ),
            new TripResponse(
                    108,
                    "Lake Retreat",
                    "Canada",
                    "Relax",
                    TripStatus.PLANNED,
                    LocalDate.of(2025, 7, 12),
                    LocalDate.of(2025, 7, 19),
                    "lake",
                    COVER,
                    List.of("Relax", "Nature")
            ),
            new TripResponse(
                    109,
                    "Lake Retreat",
                    "Canada",
                    "Relax",
                    TripStatus.PLANNED,
                    LocalDate.of(2025, 7, 12),
                    LocalDate.of(2025, 7, 19),
                    "lake",
                    COVER,
                    List.of("Relax", "Nature")
            ),
            new TripResponse(
                    110,
                    "Lake Retreat",
                    "Canada",
                    "Relax",
                    TripStatus.PLANNED,
                    LocalDate.of(2025, 7, 12),
                    LocalDate.of(2025, 7, 19),
                    "lake",
                    COVER,
                    List.of("Relax", "Nature")
            ),
            new TripResponse(
                    111,
                    "Lake Retreat",
                    "Canada",
                    "Relax",
                    TripStatus.PLANNED,
                    LocalDate.of(2025, 7, 12),
                    LocalDate.of(2025, 7, 19),
                    "lake",
                    COVER,
                    List.of("Relax", "Nature")
            ),
            new TripResponse(
                    112,
                    "Lake Retreat",
                    "Canada",
                    "Relax",
                    TripStatus.PLANNED,
                    LocalDate.of(2025, 7, 12),
                    LocalDate.of(2025, 7, 19),
                    "lake",
                    COVER,
                    List.of("Relax", "Nature")
            ),
            new TripResponse(
                    113,
                    "Lake Retreat",
                    "Canada",
                    "Relax",
                    TripStatus.PLANNED,
                    LocalDate.of(2025, 7, 12),
                    LocalDate.of(2025, 7, 19),
                    "lake",
                    COVER,
                    List.of("Relax", "Nature")
            )
    );

    @Override
    public SearchTripsResponse searchTrips(SearchTripsRequest request) {
        SearchTripsRequest safeRequest = request == null
                ? SearchTripsRequest.empty(Pagination.request(1, DEFAULT_PAGE_SIZE))
                : request;
        TripSort sort = safeRequest.sort() == null ? TripSort.NEWEST_FIRST : safeRequest.sort();
        Pagination pagination = safeRequest.pagination() == null
                ? Pagination.request(1, DEFAULT_PAGE_SIZE)
                : safeRequest.pagination();

        log.info(
                "Search trips: country={}, category={}, tag={}, status={}, startTime={}, sort={}, page={}, size={}",
                safeRequest.country(),
                safeRequest.category(),
                safeRequest.tag(),
                safeRequest.status(),
                safeRequest.startTime(),
                sort,
                pagination.page(),
                pagination.size()
        );

        List<TripResponse> filtered = trips.stream()
                .filter(trip -> matchesText(trip.country(), safeRequest.country()))
                .filter(trip -> matchesText(trip.category(), safeRequest.category()))
                .filter(trip -> matchesStatus(trip.status(), safeRequest.status()))
                .filter(trip -> matchesTag(trip.tags(), safeRequest.tag()))
                .sorted(resolveComparator(sort))
                .collect(Collectors.toList());

        Pagination pageInfo = pagination.withTotals(filtered.size());
        int fromIndex = Math.max(0, (pageInfo.page() - 1) * pageInfo.size());
        int toIndex = Math.min(filtered.size(), fromIndex + pageInfo.size());
        List<TripResponse> pageItems = fromIndex >= filtered.size()
                ? List.of()
                : filtered.subList(fromIndex, toIndex);

        return new SearchTripsResponse(pageItems, pageInfo);
    }

    private boolean matchesText(String actual, String expected) {
        if (expected == null || expected.isBlank()) return true;
        if (actual == null) return false;
        return actual.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
    }

    private boolean matchesStatus(TripStatus actual, TripStatus expected) {
        if (expected == null) return true;
        return Objects.equals(actual, expected);
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
            case NEWEST_FIRST -> Comparator.comparing(
                    TripResponse::startDate,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
        };
    }
}
