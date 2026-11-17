package com.freeconnect.payments.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.contracts.domain.ContractStatus;
import com.freeconnect.payments.domain.Payment;
import com.freeconnect.payments.domain.PaymentGateway;
import com.freeconnect.payments.domain.PaymentRepository;
import com.freeconnect.payments.domain.PaymentRequest;
import com.freeconnect.payments.domain.PaymentResult;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.exception.ValidationException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de pagos: liberar un pago y consultar el historial.
 */
@Service
public class PaymentUseCases {

    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway gateway;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public PaymentUseCases(ContractRepository contractRepository, PaymentRepository paymentRepository,
                           PaymentGateway gateway, CurrentUserProvider currentUser,
                           DomainEventPublisher eventPublisher) {
        this.contractRepository = contractRepository;
        this.paymentRepository = paymentRepository;
        this.gateway = gateway;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentView release(UUID contractId, BigDecimal requestedAmount) {
        UUID actorId = currentUser.requireUserId();
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contrato", contractId));
        currentUser.requireOwner(contract.clientId(), "el contrato");
        if (contract.status() != ContractStatus.ACTIVE && contract.status() != ContractStatus.COMPLETED) {
            throw new DomainException("CONTRACT_NOT_PAYABLE", "El contrato no admite pagos en su estado actual");
        }
        Money amount = Money.of(requestedAmount == null ? contract.amount().amount() : requestedAmount,
                contract.amount().currency());
        if (amount.isGreaterThan(contract.amount()) || amount.amount().signum() <= 0) {
            throw new ValidationException("El pago debe ser mayor a cero y no superar el monto del contrato");
        }
        BigDecimal rate = contract.commission().amount()
                .divide(contract.amount().amount(), 4, RoundingMode.HALF_UP);
        Money commission = amount.percentage(rate);
        Payment payment = Payment.create(contractId, contract.clientId(), contract.freelancerId(), amount,
                commission, gateway.provider());
        PaymentResult result = gateway.charge(new PaymentRequest(payment.id(), contractId, amount.amount(),
                amount.currency()));
        if (result.successful()) {
            payment.markReleased(result.reference());
        } else {
            payment.markFailed(result.message());
        }
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return PaymentView.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentView> byContract(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contrato", contractId));
        currentUser.requireParticipant(contract.clientId(), contract.freelancerId(), "el contrato");
        return paymentRepository.findByContract(contractId).stream().map(PaymentView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentView> mine() {
        return paymentRepository.findByParticipant(currentUser.requireUserId()).stream()
                .map(PaymentView::from)
                .toList();
    }
}
