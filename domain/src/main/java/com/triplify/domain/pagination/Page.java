package com.triplify.domain.pagination;

import java.util.List;
import java.util.function.Function;

public record Page<T>(List<T> items, long totalElements, int page, int size) {

    public Page {
        items = List.copyOf(items);

        if (totalElements < 0) {
            throw new IllegalArgumentException("Total elements must be zero or greater.");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page index must be zero or greater.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }
    }

    public static <T> Page<T> of(List<T> items, PageRequest request, long totalElements) {
        return new Page<>(items, totalElements, request.page(), request.size());
    }

    public static <T> Page<T> empty(PageRequest request) {
        return new Page<>(List.of(), 0L, request.page(), request.size());
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }

    public boolean hasNext() {
        return page < totalPages() - 1;
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
        return new Page<>(items.stream().map(mapper).toList(), totalElements, page, size);
    }
}
