package com.freeconnect.profiles.infrastructure.storage;

import com.freeconnect.shared.config.StorageProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Backend local de objetos (perfil dev). Simula el layout de un bucket S3.
 */
@Component
@ConditionalOnProperty(name = "freeconnect.storage.provider", havingValue = "filesystem", matchIfMissing = true)
public class FileSystemStorageBackend implements StorageBackend {

    private final Path basePath;
    private final String publicBaseUrl;

    public FileSystemStorageBackend(StorageProperties properties) {
        this.basePath = Path.of(properties.localPath()).toAbsolutePath().normalize();
        this.publicBaseUrl = properties.publicBaseUrl();
    }

    @Override
    public void put(String key, byte[] content) {
        try {
            Path target = resolveSafely(key);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("No fue posible almacenar el objeto " + key, ex);
        }
    }

    @Override
    public Optional<byte[]> get(String key) {
        try {
            Path target = resolveSafely(key);
            return Files.exists(target) ? Optional.of(Files.readAllBytes(target)) : Optional.empty();
        } catch (java.io.IOException ex) {
            return Optional.empty();
        }
    }

    @Override
    public String url(String key) {
        return publicBaseUrl.endsWith("/") ? publicBaseUrl + key : publicBaseUrl + "/" + key;
    }

    private Path resolveSafely(String key) {
        String sanitized = key.replace("\\", "/").replace("..", "");
        Path target = basePath.resolve(sanitized).normalize();
        if (!target.startsWith(basePath)) {
            throw new IllegalArgumentException("Clave de almacenamiento invalida");
        }
        return target;
    }
}
