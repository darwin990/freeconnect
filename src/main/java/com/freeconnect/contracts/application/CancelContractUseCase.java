package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.contracts.domain.command.CancelContractCommand;
import com.freeconnect.contracts.domain.command.ContractCommandInvoker;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: cancelar un contrato (Command pattern, con historial deshacible).
 */
@Service
public class CancelContractUseCase implements UseCase<CancelContractUseCase.Command, ContractView> {

    private final ContractRepository contractRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public CancelContractUseCase(ContractRepository contractRepository, CurrentUserProvider currentUser,
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
        ContractCommandInvoker invoker = new ContractCommandInvoker();
        invoker.run(new CancelContractCommand(contract, actorId, command.reason()));
        Contract saved = contractRepository.save(contract);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ContractView.from(saved);
    }

    public record Command(UUID contractId, String reason) {
    }
}
