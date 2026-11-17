package com.freeconnect.messaging.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de conversaciones.
 */
public interface ConversationRepository {

    Conversation save(Conversation conversation);

    Optional<Conversation> findById(UUID id);

    Optional<Conversation> findByContract(UUID contractId);

    List<Conversation> findByParticipant(UUID userId);

    boolean existsByContract(UUID contractId);
}
