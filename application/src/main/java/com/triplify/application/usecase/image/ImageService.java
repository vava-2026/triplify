package com.triplify.application.usecase.image;

import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.GetImageByIdRequest;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.image.dto.UpdateImageRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

/**
 * Application-level API for image operations.
 * <p>
 * Other application services should use this service when they need to create,
 * query, update, or delete images.
 * <p>
 * This service should not be used directly from the UI. Internally, it is
 * expected to use the domain's image storage service for physical file
 * handling.
 */
public interface ImageService {

    /**
     * Stores a new image and creates its metadata record.
     */
    Result<ImageResponse> addImage(AddImageRequest request);

    /**
     * Returns a single image by its identifier.
     */
    Result<ImageResponse> getImageById(GetImageByIdRequest request);

    /**
     * Returns a paged list of images and supports filtering by owner and upload
     * time.
     */
    Result<Page<ImageResponse>> getImages(GetImagesRequest request);

    /**
     * Updates image metadata and optionally replaces the image file.
     */
    Result<ImageResponse> updateImage(UpdateImageRequest request);

    /**
     * Deletes an image and its associated metadata.
     */
    Result<Void> deleteImage(DeleteImageRequest request);
}
