package com.triplify.application.usecase.image;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.GetImageByIdRequest;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.image.dto.UpdateImageRequest;
import com.triplify.domain.error.ImageError;
import com.triplify.domain.model.Image;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.ImageRepository;
import com.triplify.domain.result.Result;
import com.triplify.domain.service.ImageSaver;
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

    // Validation constants
    static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB
    static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    private final ImageRepository imageRepository;
    private final ImageSaver imageStorageService;

    @Inject
    public ImageServiceImpl(ImageRepository imageRepository, ImageSaver imageStorageService) {
        this.imageRepository = imageRepository;
        this.imageStorageService = imageStorageService;
    }

    @Override
    public Result<ImageResponse> addImage(AddImageRequest request) {
        Path sourcePath = request.image();

        Result<Void> validation = validateImageFile(sourcePath);
        if (validation.isFailure()) {
            return Result.fail(validation.getError());
        }

        try {
            Path storedPath = imageStorageService.store(sourcePath);
            Image image = new Image(storedPath);
            image.updateDescription(request.description());
            imageRepository.save(image);
            log.info("Image added: id={}", image.getId());
            return Result.ok(toResponse(image));
        } catch (RuntimeException e) {
            log.error("Failed to add image from path '{}'", sourcePath, e);
            return Result.fail(new ApplicationError.StorageFailure("addImage", e));
        }
    }t

    @Override
    public Result<ImageResponse> getImageById(GetImageByIdRequest request) {
        UUID id;
        try {
            id = UUID.fromString(request.id());
        } catch (IllegalArgumentException e) {
            return Result.fail(new ImageError.NotFound(request.id()));
        }

        return imageRepository.findById(id)
                .map(image -> Result.<ImageResponse>ok(toResponse(image)))
                .orElseGet(() -> Result.fail(new ImageError.NotFound(request.id())));
    }

    @Override
    public Result<Page<ImageResponse>> getImages(GetImagesRequest request) {
        GetImagesRequest.Filter filter = request.filter();
        GetImagesRequest.OrderBy orderBy = request.orderBy();

        try {
            Page<Image> page = imageRepository.findAll(
                    request.pageRequest(),
                    filter != null ? filter.uploadedFrom() : null,
                    filter != null ? filter.uploadedTo() : null,
                    orderBy != null && orderBy.uploadTimeAsc()
            );
            return Result.ok(page.map(this::toResponse));
        } catch (RuntimeException e) {
            log.error("Failed to query images", e);
            return Result.fail(new ApplicationError.StorageFailure("getImages", e));
        }
    }

    @Override
    public Result<ImageResponse> updateImage(UpdateImageRequest request) {
        UUID id;
        try {
            id = UUID.fromString(request.id());
        } catch (IllegalArgumentException e) {
            return Result.fail(new ImageError.NotFound(request.id()));
        }

        Optional<Image> existing = imageRepository.findById(id);
        if (existing.isEmpty()) {
            return Result.fail(new ImageError.NotFound(request.id()));
        }

        Image image = existing.get();

        Path oldStoredPath = null;
        Path newStoredPath = null;

        if (request.image() != null) {
            // Validate and store new image file, then replace old one
            Result<Void> validation = validateImageFile(request.image());
            if (validation.isFailure()) {
                return Result.fail(validation.getError());
            }

            oldStoredPath = image.getUrl();
            try {
                newStoredPath = imageStorageService.store(request.image());
            } catch (RuntimeException e) {
                log.error("Failed to store replacement image for id='{}'", id, e);
                return Result.fail(new ApplicationError.StorageFailure("updateImage.store", e));
            }

            // Reconstruct with the new path (url is final in the domain model)
            image = new Image(image.getId(), newStoredPath, request.description(), image.getUploadedAt());
        } else {
            image.updateDescription(request.description());
        }

        try {
            imageRepository.update(image);

            // Remove old file only after DB update succeeds to avoid broken references.
            if (oldStoredPath != null) {
                try {
                    imageStorageService.delete(oldStoredPath);
                } catch (RuntimeException e) {
                    log.warn("Failed to delete old image file '{}' after replacement", oldStoredPath, e);
                }
            }

            log.info("Image updated: id={}", image.getId());
            return Result.ok(toResponse(image));
        } catch (RuntimeException e) {
            // Best-effort rollback for newly stored replacement file.
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
        UUID id;
        try {
            id = UUID.fromString(request.id());
        } catch (IllegalArgumentException e) {
            return Result.fail(new ImageError.NotFound(request.id()));
        }

        Optional<Image> existing = imageRepository.findById(id);
        if (existing.isEmpty()) {
            return Result.fail(new ImageError.NotFound(request.id()));
        }

        Image image = existing.get();

        try {
            imageRepository.delete(id);
        } catch (RuntimeException e) {
            log.error("Failed to delete image record id='{}'", id, e);
            return Result.fail(new ApplicationError.StorageFailure("deleteImage.record", e));
        }

        // Best-effort physical cleanup: metadata is already gone.
        try {
            imageStorageService.delete(image.getUrl());
        } catch (RuntimeException e) {
            log.warn("Image record deleted but failed to remove file '{}' for id='{}'", image.getUrl(), id, e);
        }

        log.info("Image deleted: id={}", id);
        return Result.ok();
    }

    // ── validation ───────────────────────────────────────────────────────────

    private Result<Void> validateImageFile(Path imageFile) {
        if (imageFile == null) {
            return Result.fail(new ApplicationError.FileFailure("validate", new IOException("File path is null")));
        }

        if (!Files.exists(imageFile)) {
            return Result.fail(new ApplicationError.FileFailure("validate", new IOException("File not found: " + imageFile)));
        }

        if (!Files.isRegularFile(imageFile)) {
            return Result.fail(new ApplicationError.FileFailure("validate", new IOException("Not a regular file: " + imageFile)));
        }

        if (!Files.isReadable(imageFile)) {
            return Result.fail(new ApplicationError.FileFailure("validate", new IOException("File is not readable: " + imageFile)));
        }

        String extension = extractExtension(imageFile.getFileName().toString());
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension)) {
            return Result.fail(new ImageError.InvalidFormat(extension.isEmpty() ? "unknown" : extension));
        }

        try {
            long size = Files.size(imageFile);
            if (size > MAX_IMAGE_SIZE_BYTES) {
                return Result.fail(new ImageError.TooLarge(size, MAX_IMAGE_SIZE_BYTES));
            }
        } catch (IOException e) {
            return Result.fail(new ApplicationError.FileFailure("validate size", e));
        }

        if (!hasValidMagicBytes(imageFile, extension)) {
            return Result.fail(new ImageError.InvalidFormat(extension));
        }

        return Result.ok();
    }

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

    // ── helpers ──────────────────────────────────────────────────────────────

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private ImageResponse toResponse(Image image) {
        return new ImageResponse(
                image.getId().toString(),
                image.getUrl(),
                image.getDescription(),
                image.getUploadedAt()
        );
    }
}
