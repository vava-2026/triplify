package com.triplify.domain.pagination;

/**
 * Represents a request for one page of results.
 *
 * @param page zero-based page index to load
 * @param size requested page size; this must be greater than zero even if the
 *             resulting page is empty
 */
public record PageRequest(int page, int size) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must be zero or greater.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }
    }

    public static PageRequest defaultRequest() {
        return new PageRequest(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public int offset() {
        return page * size;
    }

    public PageRequest next() {
        return new PageRequest(page + 1, size);
    }

    public PageRequest previous() {
        return page == 0 ? this : new PageRequest(page - 1, size);
    }
}
