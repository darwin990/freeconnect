package com.freeconnect.contracts.domain.command;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractStatus;
import com.freeconnect.contracts.domain.Milestone;
import com.freeconnect.contracts.domain.MilestoneStatus;
import java.util.UUID;

/**
 * Aprueba un hito y permite revertir la operacion.
 */
public class CompleteMilestoneCommand implements ContractCommand {

    private final Contract contract;
    private final UUID milestoneId;
    private final UUID actorId;
    private MilestoneStatus previousMilestoneStatus;
    private ContractStatus previousContractStatus;

    public CompleteMilestoneCommand(Contract contract, UUID milestoneId, UUID actorId) {
        this.contract = contract;
        this.milestoneId = milestoneId;
        this.actorId = actorId;
    }

    @Override
    public String description() {
        return "Aprobar hito " + milestoneId;
    }

    @Override
    public void execute() {
        Milestone milestone = contract.milestone(milestoneId);
        this.previousMilestoneStatus = milestone.status();
        this.previousContractStatus = contract.status();
        contract.approveMilestone(milestoneId, actorId);
    }

    @Override
    public void undo() {
        if (previousMilestoneStatus == null || previousContractStatus == null) {
            return;
        }
        contract.milestone(milestoneId).restoreStatus(previousMilestoneStatus);
        contract.restoreStatus(previousContractStatus);
    }
}
