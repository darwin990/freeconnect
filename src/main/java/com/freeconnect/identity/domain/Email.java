package com.freeconnect.identity.domain;

import com.freeconnect.shared.exception.ValidationException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Value object inmutable de correo electronico.
 */
public record Email(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new ValidationException("Correo electronico invalido");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }

    public static Email of(String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
