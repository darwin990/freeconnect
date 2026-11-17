package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.contracts.domain.Deliverable;
import com.freeconnect.contracts.domain.composite.TaskItem;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: agregar una tarea a un entregable (Composite).
 */
@Service
public class AddDeliverableTaskUseCase implements UseCase<AddDeliverableTaskUseCase.Command, ContractView> {

    private final ContractRepository contractRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public AddDeliverableTaskUseCase(ContractRepository contractRepository, CurrentUserProvider currentUser,
                                     DomainEventPublisher eventPublisher) {
        this.contractRepository = contractRepository;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ContractView execute(Command command) {
        Contract contract = contractRepository.findById(command.contractId())
                .orElseThrow(() -> new NotFoundException("Contrato", command.contractId()));
        currentUser.requireOwner(contract.freelancerId(), "el contrato");
        Deliverable deliverable = contract.milestone(command.milestoneId())
                .requireDeliverable(command.deliverableId());
        TaskItem task = deliverable.addTask(command.title());
        Contract saved = contractRepository.save(contract);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ContractView.from(saved);
    }

    public record Command(UUID contractId, UUID milestoneId, UUID deliverableId, String title) {
    }
}
