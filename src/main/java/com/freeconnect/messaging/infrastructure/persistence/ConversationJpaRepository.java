package com.freeconnect.messaging.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repositorio Spring Data de conversaciones.
 */
public interface ConversationJpaRepository extends JpaRepository<ConversationJpaEntity, UUID> {

    Optional<ConversationJpaEntity> findByContractId(UUID contractId);

    boolean existsByContractId(UUID contractId);

    @Query("select c from ConversationJpaEntity c where c.clientId = :userId or c.freelancerId = :userId "
            + "order by c.updatedAt desc")
    List<ConversationJpaEntity> findByParticipant(UUID userId);
}
