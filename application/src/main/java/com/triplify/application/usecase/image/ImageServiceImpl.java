package com.triplify.application.usecase.image;

import com.google.inject.Inject;
import com.triplify.application.security.Authenticated;
import com.triplify.application.shared.error.ApplicationError;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.GetImageByIdRequest;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.domain.model.enums.ImageOwnerType;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.image.dto.LinkImageRequest;
import com.triplify.application.usecase.image.dto.UpdateImageRequest;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.statistic.StatisticService;
import com.triplify.application.usecase.statistic.dto.IncrementStatisticRequest;
import com.triplify.domain.error.ImageError;
import com.triplify.domain.model.Image;
import com.triplify.domain.model.enums.StatisticType;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.ImageRepository;
import com.triplify.domain.result.Result;
import com.triplify.domain.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ImageServiceImpl implements ImageService {
    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);

    static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB
    static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    private final ImageRepository imageRepository;
    private final ImageStorageService imageStorageService;
    private final StatisticService statisticService;
    private final UserSessionContext userSessionContext;

    @Inject
    public ImageServiceImpl(ImageRepository imageRepository, ImageStorageService imageStorageService, StatisticService statisticService, UserSessionContext userSessionContext) {
        this.imageRepository = imageRepository;
        this.imageStorageService = imageStorageService;
        this.statisticService = statisticService;
        this.userSessionContext = userSessionContext;
    }

    @Override
    public Result<ImageResponse> addImage(AddImageRequest request) {
        log.info("Adding new image with description='{}'", request.description());
        Path sourcePath = request.image();
        validateImageFile(sourcePath).orThrow();

        try {
            Path storedPath = imageStorageService.store(sourcePath);
            Image image = new Image(storedPath);
            image.updateDescription(request.description());
            imageRepository.save(image);
            if (request.description() == null || !request.description().startsWith("Badge image for ")) {
                statisticService.incrementStatistic(new IncrementStatisticRequest(userSessionContext.getCurrent().orElseThrow().userId(), StatisticType.PHOTOS_UPLOADED)).orThrow();
            }
            log.info("Image added: id={}", image.getId());
            return Result.ok(ImageResponse.from(image));
        } catch (RuntimeException e) {
            log.error("Failed to add image from path '{}'", sourcePath, e);
            return Result.fail(new ApplicationError.StorageFailure("addImage", e));
        }
    }

    @Override
    public Result<ImageResponse> getImageById(GetImageByIdRequest request) {
        log.info("Getting image with id='{}'", request.id());
        UUID id = request.id();

        return imageRepository.findById(id)
                .map(image -> Result.ok(ImageResponse.from(image)))
                .orElseGet(() -> Result.fail(new ImageError.NotFound(id.toString())));
    }

    @Override
    @Authenticated
    public Result<Page<ImageResponse>> getImages(GetImagesRequest request) {
        log.info("Getting images with page='{}', filter='{}', size='{}', orderBy='{}'", request.pageRequest().page(), request.filter(), request.pageRequest().size(), request.orderBy());
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        GetImagesRequest.Filter filter = request.filter();
        GetImagesRequest.OrderBy orderBy = request.orderBy();


        Page<Image> page = imageRepository.findAll(
                user.userId(),
                request.pageRequest(),
                request.filter().ownerId(),
                filter.ownerType(),
                filter.uploadedFrom(),
                filter.uploadedTo(),
                orderBy != null && orderBy.uploadTimeAsc()
        );
        log.info("Found {} items in images", page.items().size());
        return Result.ok(page.map(ImageResponse::from));
    }

    @Override
    public Result<ImageResponse> updateImage(UpdateImageRequest request) {
        log.info("Updating image with id='{}', description='{}'", request.id(), request.description());
        UUID id = request.id();

        Optional<Image> existing = imageRepository.findById(id);
        if (existing.isEmpty()) {
            return Result.fail(new ImageError.NotFound(id.toString()));
        }

        Image image = existing.get();

        Path oldStoredPath = null;
        Path newStoredPath = null;

        if (request.image() != null) {
            validateImageFile(request.image()).orThrow();

            oldStoredPath = image.getUrl();
            try {
                newStoredPath = imageStorageService.store(request.image());
            } catch (RuntimeException e) {
                log.error("Failed to store replacement image for id='{}'", id, e);
                return Result.fail(new ApplicationError.StorageFailure("updateImage.store", e));
            }

            image = new Image(image.getId(), newStoredPath, request.description(), image.getUploadedAt());
        } else {
            image.updateDescription(request.description());
        }

        try {
            imageRepository.update(image);

            if (oldStoredPath != null) {
                try {
                    imageStorageService.delete(oldStoredPath);
                } catch (RuntimeException e) {
                    log.warn("Failed to delete old image file '{}' after replacement", oldStoredPath, e);
                }
            }

            log.info("Image updated: id={}", image.getId());
            return Result.ok(ImageResponse.from(image));
        } catch (RuntimeException e) {
            if (newStoredPath != null) {
                try {
                    imageStorageService.delete(newStoredPath);
                } catch (RuntimeException cleanupError) {
                    log.warn("Failed to cleanup replacement image file '{}' after update failure", newStoredPath, cleanupError);
                }
            }
            log.error("Failed to update image id='{}'", id, e);
            return Result.fail(new ApplicationError.StorageFailure("updateImage", e));
        }
    }

    @Override
    public Result<Void> deleteImage(DeleteImageRequest request) {
        log.info("Deleting image with id='{}'", request.id());
        UUID id = request.id();

        Optional<Image> existing = imageRepository.findById(id);
        if (existing.isEmpty()) {
            return Result.fail(new ImageError.NotFound(id.toString()));
        }

        Image image = existing.get();

        try {
            imageRepository.delete(id);
        } catch (RuntimeException e) {
            log.error("Failed to delete image record id='{}'", id, e);
            return Result.fail(new ApplicationError.StorageFailure("deleteImage.record", e));
        }

        try {
            imageStorageService.delete(image.getUrl());
        } catch (RuntimeException e) {
            log.warn("Image record deleted but failed to remove file '{}' for id='{}'", image.getUrl(), id, e);
        }

        log.info("Image deleted: id={}", id);
        return Result.ok();
    }

    @Override
    public Result<Void> linkImage(LinkImageRequest request) {
        log.info("Linking image with id='{}' to owner='{}' type='{}'", request.imageId(), request.ownerId(), request.ownerType());
        UUID imageId = request.imageId();
        UUID ownerId = request.ownerId();
        ImageOwnerType ownerType = request.ownerType();

        if (imageRepository.findById(imageId).isEmpty()) {
            return Result.fail(new ImageError.NotFound(imageId.toString()));
        }

        try {
            imageRepository.linkToOwner(imageId, ownerId, ownerType);

            if ((ownerType == ImageOwnerType.TRIP_PLACE || ownerType == ImageOwnerType.TRIP_ROUTE)
                    && request.tripId() != null) {
                imageRepository.linkToOwner(imageId, request.tripId(), ImageOwnerType.TRIP);
            }

            log.info("Image linked: imageId={} ownerId={} ownerType={}", imageId, ownerId, ownerType);
            return Result.ok();
        } catch (RuntimeException e) {
            log.error("Failed to link image id='{}' to owner='{}' type='{}'", imageId, ownerId, ownerType, e);
            return Result.fail(new ApplicationError.StorageFailure("linkImage", e));
        }
    }

    private Result<Void> validateImageFile(Path imageFile) {
        log.debug("Validating image file '{}'", imageFile);
        if (imageFile == null) {
            log.debug("Image file is null");
            return Result.fail(new ImageError.InvalidFormat("missing"));
        }

        if (!Files.exists(imageFile) || !Files.isRegularFile(imageFile) || !Files.isReadable(imageFile)) {
            log.debug("Image file is not readable");
            return Result.fail(new ImageError.InvalidFormat("unreadable"));
        }

        long size;
        try {
            size = Files.size(imageFile);
        } catch (IOException e) {
            log.warn("Failed to get size of image file '{}'", imageFile, e);
            return Result.fail(new ApplicationError.FileFailure("validateImageFile.size", e));
        }

        if (size > MAX_IMAGE_SIZE_BYTES) {
            return Result.fail(new ImageError.TooLarge(size, MAX_IMAGE_SIZE_BYTES));
        }

        String filename = imageFile.getFileName() == null ? "" : imageFile.getFileName().toString();
        String extension = extractExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("Image file has invalid extension '{}'", extension);
            return Result.fail(new ImageError.InvalidFormat(extension.isBlank() ? "unknown" : extension));
        }

        if (!hasValidMagicBytes(imageFile, extension)) {
            log.warn("Image file has invalid magic bytes");
            return Result.fail(new ImageError.InvalidFormat(extension));
        }

        log.debug("Image file is valid");
        return Result.ok();
    }

    /**Checks whether the file actual extension matches extracted {@code extension}. Written by GitHub Copilot.*/
    private boolean hasValidMagicBytes(Path imageFile, String extension) {
        try (InputStream in = Files.newInputStream(imageFile)) {
            byte[] header = in.readNBytes(12);
            return switch (extension) {
                case "jpg", "jpeg" -> header.length >= 3
                        && (header[0] & 0xFF) == 0xFF
                        && (header[1] & 0xFF) == 0xD8
                        && (header[2] & 0xFF) == 0xFF;
                case "png" -> header.length >= 8
                        && (header[0] & 0xFF) == 0x89
                        && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                        && (header[4] & 0xFF) == 0x0D && (header[5] & 0xFF) == 0x0A
                        && (header[6] & 0xFF) == 0x1A && (header[7] & 0xFF) == 0x0A;
                case "gif" -> header.length >= 4
                        && header[0] == 'G' && header[1] == 'I'
                        && header[2] == 'F' && header[3] == '8';
                case "webp" -> header.length >= 12
                        && header[0] == 'R' && header[1] == 'I'
                        && header[2] == 'F' && header[3] == 'F'
                        && header[8] == 'W' && header[9] == 'E'
                        && header[10] == 'B' && header[11] == 'P';
                case "bmp" -> header.length >= 2
                        && header[0] == 'B' && header[1] == 'M';
                default -> false;
            };
        } catch (IOException e) {
            log.warn("Could not read magic bytes from '{}': {}", imageFile, e.getMessage());
            return false;
        }
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
