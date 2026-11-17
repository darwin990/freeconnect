package com.freeconnect.payments.infrastructure.gateway;

import com.freeconnect.payments.domain.PaymentGateway;
import com.freeconnect.payments.domain.PaymentRequest;
import com.freeconnect.payments.domain.PaymentResult;
import java.math.RoundingMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adaptador del SDK legado al puerto de pagos.
 *
 * <p><b>Patron: Adapter.</b> Convierte {@code Money} decimal a centavos y el
 * codigo de autorizacion del SDK a {@link PaymentResult}, de modo que el dominio
 * nunca ve la API del proveedor.</p>
 */
@Component
@ConditionalOnProperty(name = "freeconnect.payments.provider", havingValue = "LEGACY")
public class LegacyPaymentGatewayAdapter implements PaymentGateway {

    private final LegacyGatewayClient client = new LegacyGatewayClient();

    @Override
    public PaymentResult charge(PaymentRequest request) {
        long cents = request.amount().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        String authorization = client.authorize(cents, request.currency(), request.paymentId().toString());
        return authorization == null
                ? PaymentResult.failure("La pasarela legada rechazo el cobro")
                : PaymentResult.success(authorization);
    }

    @Override
    public String provider() {
        return "LEGACY";
    }
}
