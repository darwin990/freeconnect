package com.freeconnect.messaging.application;

import com.freeconnect.messaging.domain.Conversation;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de conversaciones y mensajes (RF-06).
 */
@Service
public class MessagingUseCases {

    private final ConversationMediator mediator;
    private final com.freeconnect.messaging.domain.ConversationRepository conversations;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public MessagingUseCases(ConversationMediator mediator,
                             com.freeconnect.messaging.domain.ConversationRepository conversations,
                             CurrentUserProvider currentUser, DomainEventPublisher eventPublisher) {
        this.mediator = mediator;
        this.conversations = conversations;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ConversationView open(UUID contractId) {
        UUID actorId = currentUser.requireUserId();
        Conversation conversation = mediator.resolveOrOpen(contractId, actorId);
        eventPublisher.publishAll(conversation.pullDomainEvents());
        return ConversationView.from(conversation, actorId);
    }

    @Transactional(readOnly = true)
    public List<ConversationView> listMine() {
        UUID viewerId = currentUser.requireUserId();
        return conversations.findByParticipant(viewerId).stream()
                .map(conversation -> ConversationView.from(conversation, viewerId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationView get(UUID conversationId) {
        UUID viewerId = currentUser.requireUserId();
        return ConversationView.from(load(conversationId, viewerId), viewerId);
    }

    @Transactional
    public ConversationView send(UUID conversationId, String body) {
        UUID actorId = currentUser.requireUserId();
        Conversation conversation = load(conversationId, actorId);
        conversation.postMessage(actorId, body);
        Conversation saved = conversations.save(conversation);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ConversationView.from(saved, actorId);
    }

    @Transactional
    public ConversationView markRead(UUID conversationId) {
        UUID actorId = currentUser.requireUserId();
        Conversation conversation = load(conversationId, actorId);
        conversation.markRead(actorId);
        Conversation saved = conversations.save(conversation);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ConversationView.from(saved, actorId);
    }

    @Transactional
    public ConversationView archive(UUID conversationId) {
        UUID actorId = currentUser.requireUserId();
        Conversation conversation = load(conversationId, actorId);
        conversation.archive(actorId);
        Conversation saved = conversations.save(conversation);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ConversationView.from(saved, actorId);
    }

    private Conversation load(UUID conversationId, UUID viewerId) {
        Conversation conversation = conversations.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversacion", conversationId));
        currentUser.requireParticipant(conversation.clientId(), conversation.freelancerId(), "la conversacion");
        return conversation;
    }
}
