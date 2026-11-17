package com.freeconnect.contracts.domain.state;

import com.freeconnect.contracts.domain.MilestoneStatus;
import java.util.Set;

/**
 * Estado del hito (State).
 */
public interface MilestoneState {

    MilestoneStatus status();

    Set<MilestoneStatus> allowedTransitions();

    default boolean canTransitionTo(MilestoneStatus target) {
        return allowedTransitions().contains(target);
    }
}
