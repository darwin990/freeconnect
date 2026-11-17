package com.freeconnect.payments.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data de pagos.
 */
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    List<PaymentJpaEntity> findByContractIdOrderByCreatedAtDesc(UUID contractId);

    List<PaymentJpaEntity> findByPayerIdOrPayeeIdOrderByCreatedAtDesc(UUID payerId, UUID payeeId);
}
