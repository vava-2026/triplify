package com.triplify.domain.error;

public sealed interface ImageError extends DomainError permits ImageError.NotFound, ImageError.InvalidFormat, ImageError.TooLarge {

    record NotFound(String imageId) implements ImageError {
        @Override
        public String code() {
            return "IMAGE_NOT_FOUND";
        }

        @Override
        public String message() {
            return "Image '%s' not found".formatted(imageId);
        }
    }

    record InvalidFormat(String format) implements ImageError {
        @Override
        public String code() {
            return "IMAGE_INVALID_FORMAT";
        }

        @Override
        public String message() {
            return "Image format '%s' is not supported".formatted(format);
        }
    }

    record TooLarge(long sizeBytes, long maxBytes) implements ImageError {
        @Override
        public String code() {
            return "IMAGE_TOO_LARGE";
        }

        @Override
        public String message() {
            return "Image size %d bytes exceeds maximum of %d bytes".formatted(sizeBytes, maxBytes);
        }
    }
}

