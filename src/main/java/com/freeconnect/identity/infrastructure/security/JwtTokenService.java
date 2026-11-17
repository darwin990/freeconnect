package com.freeconnect.identity.infrastructure.security;

import com.freeconnect.shared.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import com.freeconnect.identity.domain.AccessTokenIssuer;
import com.freeconnect.identity.domain.User;
import com.freeconnect.shared.config.SecurityProperties;
import com.freeconnect.shared.exception.UnauthorizedException;
import com.freeconnect.shared.security.AuthenticatedUser;
import org.springframework.stereotype.Component;

/**
 * Emisor/validador de JWT (HS256) con vida corta.
 *
 * <p>El token solo transporta id, email y rol; nunca datos personales sensibles.</p>
 */
@Component
public class JwtTokenService implements AccessTokenIssuer {

    private final SecretKey secretKey;
    private final String issuer;
    private final long accessTokenSeconds;

    public JwtTokenService(SecurityProperties properties) {
        String secret = properties.jwt().secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = properties.jwt().issuer();
        this.accessTokenSeconds = properties.jwt().accessTokenTtl().toSeconds();
    }

    @Override
    public IssuedAccessToken issue(User user) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(user.id().toString())
                .issuer(issuer)
                .claim("email", user.email().value())
                .claim("role", user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenSeconds)))
                .signWith(secretKey)
                .compact();
        return new IssuedAccessToken(token, accessTokenSeconds);
    }

    public AuthenticatedUser parse(String token) {
        try {
            Jws<Claims> jws = Jwts.parser().verifyWith(secretKey).requireIssuer(issuer).build()
                    .parseSignedClaims(token);
            Claims claims = jws.getPayload();
            return new AuthenticatedUser(
                    UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class),
                    Role.valueOf(claims.get("role", String.class)));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException("Token de acceso invalido o expirado");
        }
    }
}
