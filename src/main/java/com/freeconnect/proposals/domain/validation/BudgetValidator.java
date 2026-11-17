package com.freeconnect.proposals.domain.validation;

import com.freeconnect.shared.exception.DomainException;

/**
 * Valida que la propuesta no supere el presupuesto del proyecto.
 */
public class BudgetValidator extends ProposalValidator {

    @Override
    protected void doValidate(ProposalValidationContext context) {
        if (context.amount().isGreaterThan(context.project().budget())) {
            throw new DomainException("PROPOSAL_BUDGET_EXCEEDED",
                    "El monto propuesto supera el presupuesto del proyecto");
        }
    }
}
