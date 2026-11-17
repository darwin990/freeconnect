package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.security.AuthenticatedUser;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: listar los contratos del usuario autenticado (cliente o freelancer).
 */
@Service
public class ListContractsUseCase implements UseCase<Void, List<ContractView>> {

    private final ContractRepository contractRepository;
    private final CurrentUserProvider currentUser;

    public ListContractsUseCase(ContractRepository contractRepository, CurrentUserProvider currentUser) {
        this.contractRepository = contractRepository;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractView> execute(Void ignored) {
        AuthenticatedUser user = currentUser.require();
        return (user.role() == com.freeconnect.shared.security.Role.FREELANCER
                ? contractRepository.findByFreelancer(user.id())
                : contractRepository.findByClient(user.id())).stream()
                .map(ContractView::from)
                .toList();
    }
}
