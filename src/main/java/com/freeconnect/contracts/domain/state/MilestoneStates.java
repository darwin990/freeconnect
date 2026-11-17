package com.freeconnect.contracts.domain.state;

import com.freeconnect.contracts.domain.MilestoneStatus;
import java.util.Map;
import java.util.Set;

/**
 * Registro inmutable de estados del hito.
 */
public final class MilestoneStates {

    private static final Map<MilestoneStatus, MilestoneState> REGISTRY = Map.of(
            MilestoneStatus.PENDING, new SimpleMilestoneState(MilestoneStatus.PENDING,
                    Set.of(MilestoneStatus.IN_PROGRESS)),
            MilestoneStatus.IN_PROGRESS, new SimpleMilestoneState(MilestoneStatus.IN_PROGRESS,
                    Set.of(MilestoneStatus.SUBMITTED)),
            MilestoneStatus.SUBMITTED, new SimpleMilestoneState(MilestoneStatus.SUBMITTED,
                    Set.of(MilestoneStatus.APPROVED, MilestoneStatus.REJECTED)),
            MilestoneStatus.APPROVED, new SimpleMilestoneState(MilestoneStatus.APPROVED, Set.of()),
            MilestoneStatus.REJECTED, new SimpleMilestoneState(MilestoneStatus.REJECTED,
                    Set.of(MilestoneStatus.IN_PROGRESS)));

    private MilestoneStates() {
    }

    public static MilestoneState of(MilestoneStatus status) {
        return REGISTRY.get(status);
    }

    private record SimpleMilestoneState(MilestoneStatus status, Set<MilestoneStatus> allowedTransitions)
            implements MilestoneState {
    }
}
