package com.triplify.application.shared;

public record Pagination(int page, int size, Integer totalItems, Integer totalPages) {

    public static Pagination request(int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        return new Pagination(safePage, safeSize, null, null);
    }

    public Pagination withTotals(int totalItems) {
        int safeTotal = Math.max(0, totalItems);
        int safeSize = Math.max(1, size);
        int pages = Math.max(1, (int) Math.ceil(safeTotal / (double) safeSize));
        int safePage = Math.min(page, pages);
        return new Pagination(safePage, safeSize, safeTotal, pages);
    }
}
