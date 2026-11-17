package com.freeconnect.proposals.domain.validation;

/**
 * Eslabon de la cadena de validacion de propuestas.
 *
 * <p><b>Patron: Chain of Responsibility.</b> Cada regla decide si la propuesta
 * puede continuar; el cliente arma la cadena sin conocer las reglas concretas.</p>
 */
public abstract class ProposalValidator {

    private ProposalValidator next;

    public ProposalValidator setNext(ProposalValidator nextValidator) {
        this.next = nextValidator;
        return nextValidator;
    }

    public final void validate(ProposalValidationContext context) {
        doValidate(context);
        if (next != null) {
            next.validate(context);
        }
    }

    protected abstract void doValidate(ProposalValidationContext context);
}
