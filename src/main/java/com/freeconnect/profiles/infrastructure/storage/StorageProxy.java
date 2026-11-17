package com.freeconnect.profiles.infrastructure.storage;

import com.freeconnect.profiles.domain.ObjectStore;
import com.freeconnect.shared.exception.ForbiddenException;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.AuthenticatedUser;
import com.freeconnect.shared.security.CurrentUserProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Proxy de almacenamiento con control de acceso y cache de lecturas.
 *
 * <p><b>Patron: Proxy.</b> Los casos de uso trabajan con {@link ObjectStore}, pero
 * todas las operaciones pasan por este proxy: valida que el actor sea propietario
 * de la clave ({@code portfolio/{userId}/...}) o ADMIN y evita lecturas repetidas
 * contra el backend S3-compatible mediante cache Caffeine.</p>
 */
@Component
@Primary
public class StorageProxy implements ObjectStore {

    private static final String PREFIX = "portfolio/";

    private final StorageBackend backend;
    private final CurrentUserProvider currentUser;
    private final Cache<String, byte[]> cache = Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    public StorageProxy(StorageBackend backend, CurrentUserProvider currentUser) {
        this.backend = backend;
        this.currentUser = currentUser;
    }

    @Override
    public StoredObject put(String key, byte[] content, String contentType) {
        authorize(key);
        backend.put(key, content);
        cache.put(key, content.clone());
        return new StoredObject(key, content, contentType, backend.url(key));
    }

    @Override
    public Optional<StoredObject> get(String key) {
        authorize(key);
        byte[] cached = cache.getIfPresent(key);
        if (cached != null) {
            return Optional.of(new StoredObject(key, cached.clone(), "application/octet-stream", backend.url(key)));
        }
        return backend.get(key).map(bytes -> {
            cache.put(key, bytes.clone());
            return new StoredObject(key, bytes, "application/octet-stream", backend.url(key));
        });
    }

    public static String keyFor(UUID userId, UUID itemId, String fileName) {
        return ObjectStore.keyFor(userId, itemId, fileName);
    }

    private void authorize(String key) {
        if (!key.startsWith(PREFIX)) {
            throw new ForbiddenException("Solo se permite acceso a objetos de portafolio");
        }
        AuthenticatedUser user = currentUser.require();
        if (user.isAdmin()) {
            return;
        }
        String[] parts = key.split("/");
        if (parts.length < 3) {
            throw new NotFoundException("Objeto de almacenamiento", key);
        }
        UUID owner = UUID.fromString(parts[1]);
        if (!owner.equals(user.id())) {
            throw ForbiddenException.notOwner("el objeto de portafolio");
        }
    }
}
