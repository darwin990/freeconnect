package com.freeconnect.contracts.domain.command;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractStatus;
import java.util.UUID;

/**
 * Cancela un contrato y permite revertir la operacion.
 */
public class CancelContractCommand implements ContractCommand {

    private final Contract contract;
    private final UUID actorId;
    private final String reason;
    private ContractStatus previousStatus;

    public CancelContractCommand(Contract contract, UUID actorId, String reason) {
        this.contract = contract;
        this.actorId = actorId;
        this.reason = reason;
    }

    @Override
    public String description() {
        return "Cancelar contrato " + contract.id();
    }

    @Override
    public void execute() {
        this.previousStatus = contract.status();
        contract.cancel(actorId, reason);
    }

    @Override
    public void undo() {
        if (previousStatus != null) {
            contract.restoreStatus(previousStatus);
        }
    }
}
