package com.freeconnect.identity.domain;

/**
 * Digestor de tokens opacos para almacenarlos sin exponerlos.
 */
public interface TokenDigester {

    String digest(String rawToken);
}
