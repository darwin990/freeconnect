package com.freeconnect.shared.pagination;

/**
 * Parametros de paginacion normalizados (page 0-based, size acotado).
 */
public record PageQuery(int page, int size) {

    private static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }
        size = Math.min(size, MAX_SIZE);
    }

    public static PageQuery of(int page, int size) {
        return new PageQuery(page, size);
    }
}
