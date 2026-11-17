package com.freeconnect.payments.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de pagos.
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    List<Payment> findByContract(UUID contractId);

    List<Payment> findByParticipant(UUID userId);
}
