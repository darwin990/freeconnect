package com.freeconnect.shared.ratelimit;

/**
 * Puerto de rate limiting (proteccion contra abuso / fuerza bruta).
 */
public interface RateLimiter {

    /**
     * @return true si la peticion puede procesarse
     */
    boolean tryAcquire(String key);
}
