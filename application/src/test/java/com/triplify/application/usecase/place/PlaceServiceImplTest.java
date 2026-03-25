package com.triplify.application.usecase.place;

import com.triplify.application.validation.ValidatingProxy;
import com.triplify.domain.error.ValidationError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaceServiceImplTest {

    private final PlaceService service = ValidatingProxy.wrap(new PlaceServiceImpl(), PlaceService.class);

    @Test
    void addPlaceReturnsValidationErrorsForInvalidRequest() {
        AddPlaceRequest request = new AddPlaceRequest(
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
        ValidationError error = assertInstanceOf(ValidationError.class, result.getError());
        assertEquals(5, error.violations().size());
    }

    @Test
    void addPlaceReturnsSuccessForValidRequest() {
        AddPlaceRequest request = new AddPlaceRequest(
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
