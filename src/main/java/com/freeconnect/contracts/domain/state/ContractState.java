package com.freeconnect.contracts.domain.state;

import com.freeconnect.contracts.domain.ContractStatus;
import java.util.Set;

/**
 * Estado del contrato.
 *
 * <p><b>Patron: State.</b> Las transiciones validas viven en el estado, no en el
 * agregado ni en los controladores.</p>
 */
public interface ContractState {

    ContractStatus status();

    Set<ContractStatus> allowedTransitions();

    default boolean canTransitionTo(ContractStatus target) {
        return allowedTransitions().contains(target);
    }
}
