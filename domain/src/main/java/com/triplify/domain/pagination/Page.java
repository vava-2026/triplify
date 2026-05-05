package com.triplify.domain.pagination;

import java.util.List;
import java.util.function.Function;

/**
 * Represents one page of paginated results.
 *
 * @param items actual items returned for the current page; this may be empty
 * @param page zero-based page index that was requested
 * @param size requested page size; this is the page capacity, not {@code items.size()}
 * @param hasNext whether a next page exists
 * @param <T> item type
 */
public record Page<T>(List<T> items, int page, int size, boolean hasNext) {

    public Page {
        items = List.copyOf(items);

        if (page < 0) {
            throw new IllegalArgumentException("Page index must be zero or greater.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }
        if (items.size() > size) {
            throw new IllegalArgumentException("Items size cannot be greater than requested page size.");
        }
    }

    public static <T> Page<T> of(List<T> items, PageRequest request, boolean hasNext) {
        return new Page<>(items, request.page(), request.size(), hasNext);
    }

    /**
     * Creates an empty page for the given request.
     * <p>
     * The returned page has no items and zero total elements, but it keeps the
     * requested page index and page size from the request.
     */
    public static <T> Page<T> empty(PageRequest request) {
        return new Page<>(List.of(), request.page(), request.size(), false);
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return !hasNext();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public <U> Page<U> map(Function<T, U> mapper) {
        return new Page<>(items.stream().map(mapper).toList(), page, size, hasNext);
    }
}
