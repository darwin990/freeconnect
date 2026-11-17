package com.freeconnect.profiles.domain;

import java.util.Optional;

/**
 * Puerto de almacenamiento de objetos (S3-compatible o filesystem en desarrollo).
 */
public interface ObjectStore {

    StoredObject put(String key, byte[] content, String contentType);

    Optional<StoredObject> get(String key);

    /** Convencion de claves por propietario: portfolio/{userId}/{itemId}/{file}. */
    static String keyFor(java.util.UUID userId, java.util.UUID itemId, String fileName) {
        String safeName = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        return "portfolio/" + userId + "/" + itemId + "/" + safeName;
    }

    record StoredObject(String key, byte[] content, String contentType, String url) {
    }
}
