package com.freeconnect.profiles.domain;

import com.freeconnect.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.util.Locale;

/**
 * Value object de tarifa horaria.
 */
public record HourlyRate(BigDecimal amount, String currency) {

    public HourlyRate {
        if (amount == null || amount.signum() < 0) {
            throw new ValidationException("La tarifa horaria debe ser mayor o igual a cero");
        }
        if (currency == null || currency.length() != 3) {
            throw new ValidationException("La moneda debe tener codigo ISO de 3 letras");
        }
        currency = currency.toUpperCase(Locale.ROOT);
        amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public static HourlyRate of(BigDecimal amount, String currency) {
        return new HourlyRate(amount, currency);
    }
}
