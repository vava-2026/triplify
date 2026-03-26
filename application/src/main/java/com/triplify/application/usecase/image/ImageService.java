package com.triplify.application.usecase.image;

import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public interface ImageService {

    Result<ImageResponse> addImage(AddImageRequest request);

    Result<Page<ImageResponse>> getImages(GetImagesRequest request);
}
