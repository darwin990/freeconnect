package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.contracts.domain.Deliverable;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-06: comentar un entregable (ambas partes del contrato).
 */
@Service
public class AddDeliverableCommentUseCase
        implements UseCase<AddDeliverableCommentUseCase.Command, ContractView> {

    private final ContractRepository contractRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public AddDeliverableCommentUseCase(ContractRepository contractRepository, CurrentUserProvider currentUser,
                                        DomainEventPublisher eventPublisher) {
        this.contractRepository = contractRepository;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ContractView execute(Command command) {
        UUID actorId = currentUser.requireUserId();
        Contract contract = contractRepository.findById(command.contractId())
                .orElseThrow(() -> new NotFoundException("Contrato", command.contractId()));
        currentUser.requireParticipant(contract.clientId(), contract.freelancerId(), "el contrato");
        Deliverable deliverable = contract.milestone(command.milestoneId())
                .requireDeliverable(command.deliverableId());
        deliverable.addComment(actorId, command.body());
        Contract saved = contractRepository.save(contract);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ContractView.from(saved);
    }

    public record Command(UUID contractId, UUID milestoneId, UUID deliverableId, String body) {
    }
}
