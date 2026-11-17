package com.freeconnect.payments.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Solicitud de cobro enviada a la pasarela.
 */
public record PaymentRequest(UUID paymentId, UUID contractId, BigDecimal amount, String currency) {
}
