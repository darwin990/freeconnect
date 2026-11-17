package com.freeconnect.proposals.domain.validation;

import com.freeconnect.profiles.domain.Availability;
import com.freeconnect.shared.exception.DomainException;

/**
 * Valida que el freelancer tenga un perfil y disponibilidad declarada.
 */
public class AvailabilityValidator extends ProposalValidator {

    @Override
    protected void doValidate(ProposalValidationContext context) {
        if (context.freelancer() == null) {
            throw new DomainException("FREELANCER_PROFILE_REQUIRED",
                    "Debe completar su perfil profesional antes de enviar propuestas");
        }
        if (context.freelancer().availability() == Availability.UNAVAILABLE) {
            throw new DomainException("FREELANCER_UNAVAILABLE",
                    "Su perfil esta marcado como no disponible");
        }
    }
}
