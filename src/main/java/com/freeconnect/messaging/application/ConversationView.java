package com.freeconnect.messaging.application;

import com.freeconnect.messaging.domain.Conversation;
import com.freeconnect.messaging.domain.ConversationStatus;
import com.freeconnect.messaging.domain.Message;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Vista de conversacion con sus mensajes.
 */
public record ConversationView(UUID id, UUID contractId, UUID clientId, UUID freelancerId, String subject,
                               ConversationStatus status, long unread, Instant createdAt, Instant updatedAt,
                               Instant lastMessageAt, List<MessageView> messages) {

    public static ConversationView from(Conversation conversation, UUID viewerId) {
        List<MessageView> messages = conversation.messages().stream()
                .sorted(Comparator.comparing(Message::sentAt))
                .map(MessageView::from)
                .toList();
        return new ConversationView(conversation.id(), conversation.contractId(), conversation.clientId(),
                conversation.freelancerId(), conversation.subject(), conversation.status(),
                conversation.unreadFor(viewerId), conversation.createdAt(), conversation.updatedAt(),
                conversation.lastMessageAt(), messages);
    }

    public record MessageView(UUID id, UUID senderId, String body, Instant sentAt, Instant readAt) {

        static MessageView from(Message message) {
            return new MessageView(message.id(), message.senderId(), message.body(), message.sentAt(),
                    message.readAt());
        }
    }
}
