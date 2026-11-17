package com.freeconnect.messaging.infrastructure.persistence;

import com.freeconnect.messaging.domain.Conversation;
import com.freeconnect.messaging.domain.ConversationRepository;
import com.freeconnect.messaging.domain.Message;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA de conversaciones.
 */
@Repository
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final ConversationJpaRepository repository;

    public ConversationRepositoryAdapter(ConversationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Conversation save(Conversation conversation) {
        ConversationJpaEntity entity = repository.findById(conversation.id())
                .orElseGet(() -> new ConversationJpaEntity(conversation.id()));
        entity.setContractId(conversation.contractId());
        entity.setClientId(conversation.clientId());
        entity.setFreelancerId(conversation.freelancerId());
        entity.setSubject(conversation.subject());
        entity.setStatus(conversation.status());
        entity.setCreatedAt(conversation.createdAt());
        entity.setUpdatedAt(conversation.updatedAt());
        entity.setLastMessageAt(conversation.lastMessageAt());
        syncMessages(conversation.messages(), entity.getMessages());
        repository.save(entity);
        return conversation;
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findByContract(UUID contractId) {
        return repository.findByContractId(contractId).map(this::toDomain);
    }

    @Override
    public List<Conversation> findByParticipant(UUID userId) {
        return repository.findByParticipant(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByContract(UUID contractId) {
        return repository.existsByContractId(contractId);
    }

    private void syncMessages(List<Message> domain, List<ConversationMessageJpaEntity> entities) {
        Map<UUID, ConversationMessageJpaEntity> existing = new HashMap<>();
        entities.forEach(entity -> existing.put(entity.getId(), entity));
        Set<UUID> domainIds = new HashSet<>(domain.stream().map(Message::id).toList());
        entities.removeIf(entity -> !domainIds.contains(entity.getId()));
        for (Message message : domain) {
            ConversationMessageJpaEntity entity = existing.get(message.id());
            if (entity == null) {
                entity = new ConversationMessageJpaEntity(message.id());
                entities.add(entity);
            }
            entity.setSenderId(message.senderId());
            entity.setBody(message.body());
            entity.setSentAt(message.sentAt());
            entity.setReadAt(message.readAt());
        }
    }

    private Conversation toDomain(ConversationJpaEntity entity) {
        List<Message> messages = entity.getMessages().stream()
                .map(message -> new Message(message.getId(), message.getSenderId(), message.getBody(),
                        message.getSentAt(), message.getReadAt()))
                .toList();
        return Conversation.rehydrate(entity.getId(), entity.getContractId(), entity.getClientId(),
                entity.getFreelancerId(), entity.getSubject(), entity.getStatus(), entity.getCreatedAt(),
                entity.getUpdatedAt(), entity.getLastMessageAt(), messages);
    }
}
