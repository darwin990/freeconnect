package com.freeconnect.shared.domain;

import com.freeconnect.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Value object monetario inmutable usado por proyectos, propuestas y contratos.
 */
public record Money(BigDecimal amount, String currency) implements Comparable<Money> {

    public Money {
        if (amount == null || amount.signum() < 0) {
            throw new ValidationException("El monto debe ser mayor o igual a cero");
        }
        if (currency == null || currency.length() != 3) {
            throw new ValidationException("La moneda debe tener codigo ISO de 3 letras");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        currency = currency.toUpperCase(Locale.ROOT);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        BigDecimal result = amount.subtract(other.amount);
        return new Money(result.signum() < 0 ? BigDecimal.ZERO : result, currency);
    }

    public Money percentage(BigDecimal rate) {
        return new Money(amount.multiply(rate).setScale(2, RoundingMode.HALF_UP), currency);
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new ValidationException("Las monedas deben coincidir");
        }
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }
}
