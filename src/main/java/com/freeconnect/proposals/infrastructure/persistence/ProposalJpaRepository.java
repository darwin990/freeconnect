package com.freeconnect.proposals.infrastructure.persistence;

import com.freeconnect.proposals.domain.ProposalStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProposalJpaRepository extends JpaRepository<ProposalJpaEntity, UUID> {

    List<ProposalJpaEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<ProposalJpaEntity> findByProjectIdAndStatusOrderByCreatedAtDesc(UUID projectId, ProposalStatus status);

    List<ProposalJpaEntity> findByFreelancerIdOrderByCreatedAtDesc(UUID freelancerId);

    boolean existsByProjectIdAndFreelancerIdAndStatus(UUID projectId, UUID freelancerId, ProposalStatus status);
}
