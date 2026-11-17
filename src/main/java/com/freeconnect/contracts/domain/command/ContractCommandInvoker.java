package com.freeconnect.contracts.domain.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Invocador de comandos con historial de deshacer.
 *
 * <p><b>Patron: Command (invoker).</b> Se crea una instancia por unidad de
 * trabajo para no compartir historial entre peticiones.</p>
 */
public class ContractCommandInvoker {

    private final Deque<ContractCommand> history = new ArrayDeque<>();

    public void run(ContractCommand command) {
        command.execute();
        history.push(command);
    }

    public boolean undoLast() {
        if (history.isEmpty()) {
            return false;
        }
        history.pop().undo();
        return true;
    }

    public List<String> history() {
        return history.stream().map(ContractCommand::description).toList();
    }
}
