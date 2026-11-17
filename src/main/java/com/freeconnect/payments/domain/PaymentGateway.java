package com.freeconnect.payments.domain;

/**
 * Puerto de pasarela de pago.
 *
 * <p><b>Patron: Adapter (puerto).</b> El dominio define este contrato y cada
 * proveedor se adapta en infraestructura sin filtrar su SDK al negocio.</p>
 */
public interface PaymentGateway {

    PaymentResult charge(PaymentRequest request);

    String provider();
}
