package com.freeconnect.proposals.infrastructure.persistence;

import com.freeconnect.proposals.domain.Proposal;
import com.freeconnect.proposals.domain.ProposalRepository;
import com.freeconnect.proposals.domain.ProposalStatus;
import com.freeconnect.shared.domain.Money;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA del repositorio de propuestas.
 */
@Repository
public class ProposalRepositoryAdapter implements ProposalRepository {

    private final ProposalJpaRepository repository;

    public ProposalRepositoryAdapter(ProposalJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Proposal save(Proposal proposal) {
        ProposalJpaEntity entity = repository.findById(proposal.id())
                .orElseGet(() -> new ProposalJpaEntity(proposal.id(), proposal.projectId(),
                        proposal.freelancerId(), proposal.coverLetter(), proposal.amount().amount(),
                        proposal.amount().currency(), proposal.estimatedDays(), proposal.status(),
                        proposal.evaluationScore(), proposal.evaluationNote(), proposal.createdAt(),
                        proposal.updatedAt()));
        entity.setStatus(proposal.status());
        entity.setEvaluationScore(proposal.evaluationScore());
        entity.setEvaluationNote(proposal.evaluationNote());
        entity.setUpdatedAt(proposal.updatedAt());
        repository.save(entity);
        return proposal;
    }

    @Override
    public Optional<Proposal> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Proposal> findByProject(UUID projectId) {
        return repository.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Proposal> findByProjectAndStatus(UUID projectId, ProposalStatus status) {
        return repository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, status).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Proposal> findByFreelancer(UUID freelancerId) {
        return repository.findByFreelancerIdOrderByCreatedAtDesc(freelancerId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsActiveByProjectAndFreelancer(UUID projectId, UUID freelancerId) {
        return repository.existsByProjectIdAndFreelancerIdAndStatus(projectId, freelancerId,
                ProposalStatus.SUBMITTED);
    }

    private Proposal toDomain(ProposalJpaEntity entity) {
        return Proposal.rehydrate(entity.getId(), entity.getProjectId(), entity.getFreelancerId(),
                entity.getCoverLetter(), Money.of(entity.getAmount(), entity.getCurrency()),
                entity.getEstimatedDays(), entity.getStatus(), entity.getEvaluationScore(),
                entity.getEvaluationNote(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
