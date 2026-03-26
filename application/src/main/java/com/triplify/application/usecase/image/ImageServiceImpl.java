package com.triplify.application.usecase.image;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class ImageServiceImpl implements ImageService {

    @Override
    public Result<ImageResponse> addImage(AddImageRequest request) {
        // TODO: implement image creation and attach to exactly one owner.
        return Result.fail(new ApplicationError.Unexpected("TODO: ImageService.addImage"));
    }

    @Override
    public Result<Page<ImageResponse>> getImages(GetImagesRequest request) {
        // TODO: implement image retrieval with pagination, filters and ordering.
        return Result.fail(new ApplicationError.Unexpected("TODO: ImageService.getImages"));
    }
}

