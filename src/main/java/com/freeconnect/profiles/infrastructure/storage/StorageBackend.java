package com.freeconnect.profiles.infrastructure.storage;

import java.util.Optional;

/**
 * SPI interno de almacenamiento de bytes. Los adaptadores concretos (filesystem,
 * S3, memoria) implementan esta interfaz y son envueltos por {@link StorageProxy}.
 */
public interface StorageBackend {

    void put(String key, byte[] content);

    Optional<byte[]> get(String key);

    String url(String key);
}
