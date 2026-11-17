package com.freeconnect.reviews.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data de resenas.
 */
public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, UUID> {

    List<ReviewJpaEntity> findByContractIdOrderByCreatedAtDesc(UUID contractId);

    List<ReviewJpaEntity> findByTargetIdOrderByCreatedAtDesc(UUID targetId);

    boolean existsByContractIdAndAuthorId(UUID contractId, UUID authorId);
}
