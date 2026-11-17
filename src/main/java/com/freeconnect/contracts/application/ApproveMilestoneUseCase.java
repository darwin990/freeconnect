package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.contracts.domain.command.CompleteMilestoneCommand;
import com.freeconnect.contracts.domain.command.ContractCommandInvoker;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-06: aprobar un hito (Command pattern, con historial deshacible).
 */
@Service
public class ApproveMilestoneUseCase implements UseCase<ApproveMilestoneUseCase.Command, ContractView> {

    private final ContractRepository contractRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public ApproveMilestoneUseCase(ContractRepository contractRepository, CurrentUserProvider currentUser,
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
        currentUser.requireOwner(contract.clientId(), "el contrato");
        ContractCommandInvoker invoker = new ContractCommandInvoker();
        invoker.run(new CompleteMilestoneCommand(contract, command.milestoneId(), actorId));
        Contract saved = contractRepository.save(contract);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ContractView.from(saved);
    }

    public record Command(UUID contractId, UUID milestoneId) {
    }
}
