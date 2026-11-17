package com.freeconnect.shared.api;

import com.freeconnect.shared.pagination.PagedResult;
import java.util.List;

/**
 * Envoltorio JSON uniforme para respuestas paginadas.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages,
                              boolean hasNext) {

    public static <T> PageResponse<T> from(PagedResult<T> result) {
        return new PageResponse<>(result.content(), result.page(), result.size(), result.totalElements(),
                result.totalPages(), result.hasNext());
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size) {
        return new PageResponse<>(content, page, size, content.size(), 1, false);
    }
}
