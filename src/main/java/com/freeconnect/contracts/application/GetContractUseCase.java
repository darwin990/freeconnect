package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultar el detalle de un contrato.
 */
@Service
public class GetContractUseCase implements UseCase<UUID, ContractView> {

    private final ContractRepository contractRepository;
    private final CurrentUserProvider currentUser;

    public GetContractUseCase(ContractRepository contractRepository, CurrentUserProvider currentUser) {
        this.contractRepository = contractRepository;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public ContractView execute(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contrato", contractId));
        currentUser.requireParticipant(contract.clientId(), contract.freelancerId(), "el contrato");
        return ContractView.from(contract);
    }
}
