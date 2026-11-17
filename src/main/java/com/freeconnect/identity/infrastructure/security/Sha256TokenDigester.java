package com.freeconnect.identity.infrastructure.security;

import com.freeconnect.identity.domain.TokenDigester;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * Digest SHA-256 de tokens opacos antes de persistirlos.
 */
@Component
public class Sha256TokenDigester implements TokenDigester {

    @Override
    public String digest(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
