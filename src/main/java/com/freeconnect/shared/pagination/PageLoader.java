package com.freeconnect.shared.pagination;

/**
 * Carga una pagina bajo demanda (desacopla el iterador del repositorio).
 */
@FunctionalInterface
public interface PageLoader<T> {

    PagedResult<T> load(int page, int size);
}
