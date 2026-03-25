package com.triplify.application.usecase.page;

import com.triplify.application.error.ValidationMapper;
import com.triplify.application.error.ValidationResult;
import com.triplify.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PagesServiceImpl implements PagesService {

    private static final Logger log = LoggerFactory.getLogger(PagesServiceImpl.class);

    @Override
    public Result<Void> addPlace(AddPageRequest request) {
        ValidationResult<AddPageRequest> validation = ValidationMapper.validate(request);
        if (validation.isFailure()) {
            log.debug("Add place validation failed: {}", validation.getViolations());
            return Result.failure(validation.getErrors());
        }

        log.info(
                "Mock add place request accepted: tripId={}, title='{}', country='{}', latitude={}, longitude={}, coverImagePath={}",
                request.tripId(),
                request.title(),
                request.country(),
                request.latitude(),
                request.longitude(),
                request.coverImagePath()
        );

        return Result.success();
    }
}
