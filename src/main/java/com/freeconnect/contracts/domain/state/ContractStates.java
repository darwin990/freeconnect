package com.freeconnect.contracts.domain.state;

import com.freeconnect.contracts.domain.ContractStatus;
import java.util.Map;
import java.util.Set;

/**
 * Registro inmutable de estados del contrato.
 */
public final class ContractStates {

    private static final Map<ContractStatus, ContractState> REGISTRY = Map.of(
            ContractStatus.DRAFT, new SimpleContractState(ContractStatus.DRAFT,
                    Set.of(ContractStatus.ACTIVE, ContractStatus.CANCELLED)),
            ContractStatus.ACTIVE, new SimpleContractState(ContractStatus.ACTIVE,
                    Set.of(ContractStatus.COMPLETED, ContractStatus.CANCELLED)),
            ContractStatus.COMPLETED, new SimpleContractState(ContractStatus.COMPLETED, Set.of()),
            ContractStatus.CANCELLED, new SimpleContractState(ContractStatus.CANCELLED, Set.of()));

    private ContractStates() {
    }

    public static ContractState of(ContractStatus status) {
        return REGISTRY.get(status);
    }

    private record SimpleContractState(ContractStatus status, Set<ContractStatus> allowedTransitions)
            implements ContractState {
    }
}
