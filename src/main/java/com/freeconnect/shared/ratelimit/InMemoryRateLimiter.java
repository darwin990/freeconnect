package com.freeconnect.shared.ratelimit;

import com.freeconnect.shared.config.SecurityProperties;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * Rate limiter de ventana fija por clave (IP o usuario).
 *
 * <p><b>Patron: Strategy.</b> Es una implementacion de {@link RateLimiter};
 * en despliegue con Redis puede sustituirse por una variante distribuida sin
 * cambiar el filtro HTTP.</p>
 */
@Component
public class InMemoryRateLimiter implements RateLimiter {

    private final int capacity;
    private final long windowSeconds;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public InMemoryRateLimiter(SecurityProperties properties) {
        this.capacity = properties.rateLimit().capacity();
        this.windowSeconds = Math.max(1, properties.rateLimit().window().toSeconds());
    }

    @Override
    public boolean tryAcquire(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket());
        return bucket.increment(capacity, windowSeconds);
    }

    private static final class Bucket {
        private final AtomicInteger counter = new AtomicInteger();
        private volatile long windowStart = Instant.now().getEpochSecond();

        boolean increment(int capacity, long windowSeconds) {
            long now = Instant.now().getEpochSecond();
            if (now - windowStart >= windowSeconds) {
                synchronized (this) {
                    if (now - windowStart >= windowSeconds) {
                        windowStart = now;
                        counter.set(0);
                    }
                }
            }
            return counter.incrementAndGet() <= capacity;
        }
    }
}
