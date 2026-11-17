package com.freeconnect.shared.pagination;

import java.util.List;

/**
 * Pagina inmutable de resultados de aplicacion.
 */
public record PagedResult<T>(List<T> content, int page, int size, long totalElements) {

    public PagedResult {
        content = List.copyOf(content);
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("page debe ser >= 0 y size > 0");
        }
    }

    public static <T> PagedResult<T> of(List<T> content, int page, int size, long totalElements) {
        return new PagedResult<>(content, page, size, totalElements);
    }

    public static <T> PagedResult<T> empty(int page, int size) {
        return new PagedResult<>(List.of(), page, size, 0L);
    }

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean hasNext() {
        return page + 1 < totalPages();
    }

    public <R> PagedResult<R> map(java.util.function.Function<T, R> mapper) {
        return new PagedResult<>(content.stream().map(mapper).toList(), page, size, totalElements);
    }
}
