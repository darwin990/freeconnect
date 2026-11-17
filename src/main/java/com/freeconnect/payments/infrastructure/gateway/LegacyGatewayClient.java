package com.freeconnect.payments.infrastructure.gateway;

import java.util.UUID;

/**
 * SDK legado simulado con su propia firma (centavos, autorizacion por referencia).
 *
 * <p>Representa una dependencia externa que no debe filtrarse al dominio; el
 * {@link LegacyPaymentGatewayAdapter} la adapta al puerto {@code PaymentGateway}.</p>
 */
public class LegacyGatewayClient {

    /**
     * @return codigo de autorizacion o {@code null} si rechaza.
     */
    public String authorize(long amountInCents, String currency, String merchantReference) {
        if (amountInCents <= 0 || currency == null || currency.length() != 3) {
            return null;
        }
        return "LEGACY-" + merchantReference + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
