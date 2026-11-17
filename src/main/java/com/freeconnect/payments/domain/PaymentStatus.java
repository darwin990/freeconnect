package com.freeconnect.payments.domain;

/**
 * Estado de un pago.
 */
public enum PaymentStatus {
    PENDING,
    RELEASED,
    FAILED,
    REFUNDED
}
