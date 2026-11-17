package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-06: registrar un entregable en un hito.
 */
@Service
public class AddDeliverableUseCase implements UseCase<AddDeliverableUseCase.Command, ContractView> {

    private final ContractRepository contractRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public AddDeliverableUseCase(ContractRepository contractRepository, CurrentUserProvider currentUser,
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
        currentUser.requireOwner(contract.freelancerId(), "el contrato");
        contract.addDeliverable(command.milestoneId(), command.title(), command.description(), command.url(),
                actorId);
        Contract saved = contractRepository.save(contract);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ContractView.from(saved);
    }

    public record Command(UUID contractId, UUID milestoneId, String title, String description, String url) {
    }
}
