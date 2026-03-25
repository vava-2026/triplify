package com.triplify.application.usecase.page;

import com.triplify.application.result.Result;

public interface PagesService {

    Result<Void> addPlace(AddPageRequest request);
}
