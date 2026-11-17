package com.freeconnect.profiles.infrastructure.storage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Backend en memoria para pruebas de integracion.
 */
@Component
@ConditionalOnProperty(name = "freeconnect.storage.provider", havingValue = "memory")
public class InMemoryStorageBackend implements StorageBackend {

    private final Map<String, byte[]> objects = new ConcurrentHashMap<>();

    @Override
    public void put(String key, byte[] content) {
        objects.put(key, content.clone());
    }

    @Override
    public Optional<byte[]> get(String key) {
        byte[] content = objects.get(key);
        return content == null ? Optional.empty() : Optional.of(content.clone());
    }

    @Override
    public String url(String key) {
        return "memory://" + key;
    }
}
