package com.triplify.application.usecase.page;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagesServiceImplTest {

    private final PagesServiceImpl service = new PagesServiceImpl();

    @Test
    void addPlaceReturnsValidationErrorsForInvalidRequest() {
        AddPageRequest request = new AddPageRequest(
                null,
                "",
                "",
                "Description",
                null,
                null,
                null
        );

        var result = service.addPlace(request);

        assertTrue(result.isFailure());
        assertEquals(5, result.getErrors().size());
    }

    @Test
    void addPlaceReturnsSuccessForValidRequest() {
        AddPageRequest request = new AddPageRequest(
                42,
                "Bratislava Old Town",
                "Slovakia",
                "Historic center with cafes and narrow streets.",
                48.1485965,
                17.1077477,
                "C:/tmp/cover.jpg"
        );

        var result = service.addPlace(request);

        assertTrue(result.isSuccess());
    }
}
