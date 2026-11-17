package com.freeconnect.contracts.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data de contratos.
 */
public interface ContractJpaRepository extends JpaRepository<ContractJpaEntity, UUID> {

    List<ContractJpaEntity> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    List<ContractJpaEntity> findByFreelancerIdOrderByCreatedAtDesc(UUID freelancerId);

    boolean existsByProposalId(UUID proposalId);
}
