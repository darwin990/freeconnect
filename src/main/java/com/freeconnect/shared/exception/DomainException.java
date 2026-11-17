package com.freeconnect.shared.exception;

/**
 * Violacion de una regla de negocio. Se traduce a HTTP 422.
 */
public class DomainException extends RuntimeException {

    private final String code;

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DomainException(String message) {
        this("BUSINESS_RULE_VIOLATION", message);
    }

    public String code() {
        return code;
    }
}
