package com.triplify.application.service;

import com.triplify.application.pagination.Pagination;
import com.triplify.application.request.PlaceSort;
import com.triplify.application.request.SearchPlacesRequest;
import com.triplify.application.response.PlaceResponse;
import com.triplify.application.response.PlaceStatus;
import com.triplify.application.response.SearchPlacesResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;


public class TripPlaceServiceImpl implements PlaceService {
    private static final String COVER = "/com/triplify/ui/pages/trips/images/two.png";

    // Fake seed data
    private static final List<PlaceResponse> SEED = List.of(
            new PlaceResponse(1, "Eiffel Tower", "Paris", "2024-03-10", PlaceStatus.VISITED, COVER),
            new PlaceResponse(2, "Louvre Museum", "Paris", "2024-03-11", PlaceStatus.VISITED, COVER),
            new PlaceResponse(3, "Notre-Dame", "Paris", "2024-03-11", PlaceStatus.VISITED, COVER),
            new PlaceResponse(4, "Montmartre", "Paris", "2024-03-12", PlaceStatus.VISITED, COVER),
            new PlaceResponse(5, "Palace of Versailles", "Paris", "2024-03-13", PlaceStatus.VISITED, COVER),
            new PlaceResponse(6, "Sainte-Chapelle", "Paris", "2024-03-12", PlaceStatus.VISITED, COVER),
            new PlaceResponse(7, "Musée d'Orsay", "Paris", "2024-03-14", PlaceStatus.VISITED, COVER),
            new PlaceResponse(8, "Seine River Cruise", "Paris", "2024-03-10", PlaceStatus.VISITED, COVER),
            new PlaceResponse(9, "Champs-Élysées", "Paris", "2024-03-11", PlaceStatus.VISITED, COVER),
            new PlaceResponse(10, "Père Lachaise", "Paris", "2024-03-15", PlaceStatus.VISITED, COVER)
    );

    @Override
    public SearchPlacesResponse searchPlaces(SearchPlacesRequest request) {
        Stream<PlaceResponse> stream = SEED.stream();

        // filter by tripId would be done via repository;
        if (request.status()   != null) stream = stream.filter(p -> p.status() == request.status());

        List<PlaceResponse> filtered = sort(stream, request.sort());

        int total    = filtered.size();
        int page     = request.pagination().page();
        int pageSize = request.pagination().size();
        int from     = (page - 1) * pageSize;  // page is 1-indexed
        int to       = Math.min(from + pageSize, total);

        return new SearchPlacesResponse(
                filtered.subList(from, to),
                Pagination.response(page, pageSize, total)
        );
    }

    private List<PlaceResponse> sort(Stream<PlaceResponse> stream, PlaceSort sort) {
        if (sort == null) return stream.toList();
        Comparator<PlaceResponse> cmp = switch (sort) {
            case NEWEST_FIRST -> Comparator.comparing(PlaceResponse::date, Comparator.nullsLast(Comparator.reverseOrder()));
            case OLDEST_FIRST -> Comparator.comparing(PlaceResponse::date, Comparator.nullsLast(Comparator.naturalOrder()));
            case NAME_AZ      -> Comparator.comparing(PlaceResponse::name);
            case NAME_ZA      -> Comparator.comparing(PlaceResponse::name).reversed();
        };
        return stream.sorted(cmp).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}
