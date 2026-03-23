package com.triplify.domain.error;

public sealed interface ImageError extends DomainError permits ImageError.NotFound, ImageError.InvalidFormat, ImageError.TooLarge {

    record NotFound(String imageId) implements ImageError {
        @Override
        public String code() {
            return "error.image.not.found";
        }

        @Override
        public String message() {
            return "Image '%s' not found".formatted(imageId);
        }
    }

    record InvalidFormat(String format) implements ImageError {
        @Override
        public String code() {
            return "error.image.invalid.format";
        }

        @Override
        public String message() {
            return "Image format '%s' is not supported".formatted(format);
        }
    }

    record TooLarge(long sizeBytes, long maxBytes) implements ImageError {
        @Override
        public String code() {
            return "error.image.too.large";
        }

        @Override
        public String message() {
            return "Image size %d bytes exceeds maximum of %d bytes".formatted(sizeBytes, maxBytes);
        }
    }
}

