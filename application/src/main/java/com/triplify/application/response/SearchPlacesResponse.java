package com.triplify.application.response;

import com.triplify.application.pagination.Pagination;
import java.util.List;

public record SearchPlacesResponse(
        List<PlaceResponse> places,
        Pagination pagination
) {}
