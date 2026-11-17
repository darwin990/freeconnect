package com.freeconnect.contracts.domain.command;

/**
 * Comando reversible sobre un contrato.
 *
 * <p><b>Patron: Command.</b> Encapsula una operacion de negocio con su
 * compensacion ({@link #undo()}), lo que permite deshacer el ultimo cambio de
 * estado sin que el caso de uso conozca los detalles.</p>
 */
public interface ContractCommand {

    String description();

    void execute();

    void undo();
}
