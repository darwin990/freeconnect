package com.freeconnect.contracts.infrastructure.persistence;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA del repositorio de contratos.
 */
@Repository
public class ContractRepositoryAdapter implements ContractRepository {

    private final ContractJpaRepository repository;

    public ContractRepositoryAdapter(ContractJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Contract save(Contract contract) {
        ContractJpaEntity entity = repository.findById(contract.id())
                .orElseGet(() -> new ContractJpaEntity(contract.id()));
        ContractJpaMapper.copyToEntity(contract, entity);
        repository.save(entity);
        return contract;
    }

    @Override
    public Optional<Contract> findById(UUID id) {
        return repository.findById(id).map(ContractJpaMapper::toDomain);
    }

    @Override
    public List<Contract> findByClient(UUID clientId) {
        return repository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(ContractJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Contract> findByFreelancer(UUID freelancerId) {
        return repository.findByFreelancerIdOrderByCreatedAtDesc(freelancerId).stream()
                .map(ContractJpaMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByProposal(UUID proposalId) {
        return repository.existsByProposalId(proposalId);
    }

    @Override
    public List<Contract> findAll() {
        return repository.findAll().stream().map(ContractJpaMapper::toDomain).toList();
    }
}
