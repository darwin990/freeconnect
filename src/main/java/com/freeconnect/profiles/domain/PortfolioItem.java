package com.freeconnect.profiles.domain;

import com.freeconnect.shared.exception.ValidationException;
import java.util.UUID;

/**
 * Elemento de portafolio con enlace a evidencia (S3-compatible).
 */
public record PortfolioItem(UUID id, String title, String url, String description, String storageKey) {

    public static PortfolioItem create(String title, String url, String description) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("El titulo del portafolio es obligatorio");
        }
        return new PortfolioItem(UUID.randomUUID(), title.trim(), url, description, null);
    }

    public PortfolioItem withUrl(String newUrl) {
        return new PortfolioItem(id, title, newUrl, description, storageKey);
    }

    public PortfolioItem withStorage(String newUrl, String newStorageKey) {
        return new PortfolioItem(id, title, newUrl, description, newStorageKey);
    }
}
