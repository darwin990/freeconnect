package com.freeconnect.identity.domain;

/**
 * Puerto de emision/validacion de tokens de acceso JWT.
 */
public interface AccessTokenIssuer {

    IssuedAccessToken issue(User user);

    record IssuedAccessToken(String token, long expiresInSeconds) {
    }
}
