package com.freeconnect.payments.infrastructure.gateway;

import com.freeconnect.payments.domain.PaymentGateway;
import com.freeconnect.payments.domain.PaymentRequest;
import com.freeconnect.payments.domain.PaymentResult;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Pasarela simulada para desarrollo y pruebas.
 */
@Component
@ConditionalOnProperty(name = "freeconnect.payments.provider", havingValue = "MOCK", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult charge(PaymentRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            return PaymentResult.failure("Monto invalido");
        }
        return PaymentResult.success("MOCK-" + UUID.randomUUID());
    }

    @Override
    public String provider() {
        return "MOCK";
    }
}
