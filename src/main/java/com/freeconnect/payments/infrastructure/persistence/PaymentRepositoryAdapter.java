package com.freeconnect.payments.infrastructure.persistence;

import com.freeconnect.payments.domain.Payment;
import com.freeconnect.payments.domain.PaymentRepository;
import com.freeconnect.shared.domain.Money;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA de pagos.
 */
@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository repository;

    public PaymentRepositoryAdapter(PaymentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = repository.findById(payment.id())
                .orElseGet(() -> new PaymentJpaEntity(payment.id()));
        entity.setContractId(payment.contractId());
        entity.setPayerId(payment.payerId());
        entity.setPayeeId(payment.payeeId());
        entity.setAmountAmount(payment.amount().amount());
        entity.setAmountCurrency(payment.amount().currency());
        entity.setCommissionAmount(payment.commission().amount());
        entity.setCommissionCurrency(payment.commission().currency());
        entity.setNetAmount(payment.netAmount().amount());
        entity.setNetCurrency(payment.netAmount().currency());
        entity.setStatus(payment.status());
        entity.setProvider(payment.provider());
        entity.setProviderReference(payment.providerReference());
        entity.setCreatedAt(payment.createdAt());
        entity.setProcessedAt(payment.processedAt());
        entity.setFailureReason(payment.failureReason());
        repository.save(entity);
        return payment;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Payment> findByContract(UUID contractId) {
        return repository.findByContractIdOrderByCreatedAtDesc(contractId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Payment> findByParticipant(UUID userId) {
        return repository.findByPayerIdOrPayeeIdOrderByCreatedAtDesc(userId, userId).stream()
                .map(this::toDomain)
                .toList();
    }

    private Payment toDomain(PaymentJpaEntity entity) {
        return Payment.rehydrate(entity.getId(), entity.getContractId(), entity.getPayerId(),
                entity.getPayeeId(), Money.of(entity.getAmountAmount(), entity.getAmountCurrency()),
                Money.of(entity.getCommissionAmount(), entity.getCommissionCurrency()),
                Money.of(entity.getNetAmount(), entity.getNetCurrency()), entity.getStatus(),
                entity.getProvider(), entity.getProviderReference(), entity.getCreatedAt(),
                entity.getProcessedAt(), entity.getFailureReason());
    }
}
