package com.triplify.application.usecase.image;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.GetImageByIdRequest;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.image.dto.UpdateImageRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class ImageServiceImpl implements ImageService {

    @Override
    public Result<ImageResponse> addImage(AddImageRequest request) {
        // TODO: implement image creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: ImageService.addImage"));
    }

    @Override
    public Result<ImageResponse> getImageById(GetImageByIdRequest request) {
        // TODO: implement image retrieval by id.
        return Result.fail(new ApplicationError.Unexpected("TODO: ImageService.getImageById"));
    }

    @Override
    public Result<Page<ImageResponse>> getImages(GetImagesRequest request) {
        // TODO: implement image retrieval with pagination, filters and ordering.
        return Result.fail(new ApplicationError.Unexpected("TODO: ImageService.getImages"));
    }

    @Override
    public Result<ImageResponse> updateImage(UpdateImageRequest request) {
        // TODO: implement image metadata update.
        return Result.fail(new ApplicationError.Unexpected("TODO: ImageService.updateImage"));
    }

    @Override
    public Result<Void> deleteImage(DeleteImageRequest request) {
        // TODO: implement image deletion.
        return Result.fail(new ApplicationError.Unexpected("TODO: ImageService.deleteImage"));
    }
}
