package com.freeconnect.payments.domain;

/**
 * Resultado de la pasarela de pago.
 */
public record PaymentResult(boolean successful, String reference, String message) {

    public static PaymentResult success(String reference) {
        return new PaymentResult(true, reference, null);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message);
    }
}
