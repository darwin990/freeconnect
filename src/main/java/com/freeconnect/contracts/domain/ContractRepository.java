package com.freeconnect.contracts.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de contratos.
 */
public interface ContractRepository {

    Contract save(Contract contract);

    Optional<Contract> findById(UUID id);

    List<Contract> findByClient(UUID clientId);

    List<Contract> findByFreelancer(UUID freelancerId);

    boolean existsByProposal(UUID proposalId);

    List<Contract> findAll();
}
