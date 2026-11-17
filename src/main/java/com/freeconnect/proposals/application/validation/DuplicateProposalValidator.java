package com.freeconnect.proposals.application.validation;

import com.freeconnect.proposals.domain.ProposalRepository;
import com.freeconnect.proposals.domain.validation.ProposalValidationContext;
import com.freeconnect.proposals.domain.validation.ProposalValidator;
import com.freeconnect.shared.exception.ConflictException;

/**
 * Valida que no exista otra propuesta activa del mismo freelancer al proyecto.
 */
public class DuplicateProposalValidator extends ProposalValidator {

    private final ProposalRepository repository;

    public DuplicateProposalValidator(ProposalRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doValidate(ProposalValidationContext context) {
        if (repository.existsActiveByProjectAndFreelancer(context.project().id(), context.freelancerId())) {
            throw new ConflictException("Ya existe una propuesta activa suya para este proyecto");
        }
    }
}
