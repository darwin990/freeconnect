package com.freeconnect.shared.pagination;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Iterador que recorre resultados paginados sin cargar todo en memoria.
 *
 * <p><b>Patron: Iterator.</b> Desacopla al consumidor (exportaciones, procesos
 * batch) del repositorio y del tamano total del conjunto de resultados.</p>
 */
public class PagedIterator<T> implements Iterator<T> {

    private static final int MAX_PAGES = 10_000;

    private final PageLoader<T> pageLoader;
    private final int pageSize;
    private int nextPage;
    private Iterator<T> currentPage = Collections.emptyIterator();
    private boolean exhausted;

    public PagedIterator(PageLoader<T> pageLoader, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize debe ser positivo");
        }
        this.pageLoader = pageLoader;
        this.pageSize = pageSize;
    }

    @Override
    public boolean hasNext() {
        if (currentPage.hasNext()) {
            return true;
        }
        if (exhausted || nextPage >= MAX_PAGES) {
            return false;
        }
        PagedResult<T> page = pageLoader.load(nextPage, pageSize);
        nextPage++;
        currentPage = page.content().iterator();
        if (page.content().size() < pageSize || nextPage >= page.totalPages()) {
            exhausted = true;
        }
        return currentPage.hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No hay mas resultados paginados");
        }
        return currentPage.next();
    }

    public Stream<T> stream() {
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }
}
