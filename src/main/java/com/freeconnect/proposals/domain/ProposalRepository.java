package com.freeconnect.proposals.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de propuestas.
 */
public interface ProposalRepository {

    Proposal save(Proposal proposal);

    Optional<Proposal> findById(UUID id);

    List<Proposal> findByProject(UUID projectId);

    List<Proposal> findByProjectAndStatus(UUID projectId, ProposalStatus status);

    List<Proposal> findByFreelancer(UUID freelancerId);

    boolean existsActiveByProjectAndFreelancer(UUID projectId, UUID freelancerId);
}
