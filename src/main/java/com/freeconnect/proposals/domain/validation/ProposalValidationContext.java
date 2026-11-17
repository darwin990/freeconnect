package com.freeconnect.proposals.domain.validation;

import com.freeconnect.profiles.domain.FreelancerProfile;
import com.freeconnect.projects.domain.Project;
import com.freeconnect.shared.domain.Money;
import java.util.UUID;

/**
 * Contexto compartido por la cadena de validacion de propuestas.
 */
public record ProposalValidationContext(Project project, FreelancerProfile freelancer, UUID freelancerId,
                                        Money amount) {
}
