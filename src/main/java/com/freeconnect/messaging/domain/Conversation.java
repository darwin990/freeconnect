package com.freeconnect.messaging.domain;

import com.freeconnect.messaging.domain.state.ConversationStates;
import com.freeconnect.shared.domain.AggregateRoot;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Conversacion entre cliente y freelancer de un contrato.
 *
 * <p><b>Patrones: State (archivado irreversible) y Observer (eventos de
 * mensaje consumidos por notificaciones).</b></p>
 */
public class Conversation extends AggregateRoot {

    private final UUID id;
    private final UUID contractId;
    private final UUID clientId;
    private final UUID freelancerId;
    private final String subject;
    private ConversationStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant lastMessageAt;
    private final List<Message> messages;

    private Conversation(UUID id, UUID contractId, UUID clientId, UUID freelancerId, String subject,
                         ConversationStatus status, Instant createdAt, Instant updatedAt, Instant lastMessageAt,
                         List<Message> messages) {
        this.id = id;
        this.contractId = contractId;
        this.clientId = clientId;
        this.freelancerId = freelancerId;
        this.subject = subject;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastMessageAt = lastMessageAt;
        this.messages = new ArrayList<>(messages);
    }

    public static Conversation open(UUID contractId, UUID clientId, UUID freelancerId, String subject,
                                    UUID actorId) {
        if (contractId == null || clientId == null || freelancerId == null) {
            throw new ValidationException("La conversacion requiere contrato y participantes");
        }
        Instant now = Instant.now();
        Conversation conversation = new Conversation(UUID.randomUUID(), contractId, clientId, freelancerId,
                subject == null || subject.isBlank() ? "Conversacion del contrato" : subject.trim(),
                ConversationStatus.ACTIVE, now, now, null, List.of());
        conversation.registerEvent(new ConversationOpenedEvent(conversation.id, contractId, clientId,
                freelancerId, now, actorId));
        return conversation;
    }

    public static Conversation rehydrate(UUID id, UUID contractId, UUID clientId, UUID freelancerId,
                                         String subject, ConversationStatus status, Instant createdAt,
                                         Instant updatedAt, Instant lastMessageAt, List<Message> messages) {
        return new Conversation(id, contractId, clientId, freelancerId, subject, status, createdAt, updatedAt,
                lastMessageAt, messages);
    }

    public Message postMessage(UUID senderId, String body) {
        if (status != ConversationStatus.ACTIVE) {
            throw new DomainException("CONVERSATION_ARCHIVED", "No se puede escribir en una conversacion archivada");
        }
        if (!isParticipant(senderId)) {
            throw new DomainException("NOT_A_PARTICIPANT", "El remitente no participa en la conversacion");
        }
        if (body == null || body.isBlank() || body.length() > 4000) {
            throw new ValidationException("El mensaje es obligatorio y admite hasta 4000 caracteres");
        }
        Message message = Message.send(senderId, body.trim());
        messages.add(message);
        this.lastMessageAt = message.sentAt();
        this.updatedAt = message.sentAt();
        UUID recipientId = senderId.equals(clientId) ? freelancerId : clientId;
        registerEvent(new MessageSentEvent(id, contractId, message.id(), senderId, recipientId, message.body(),
                message.sentAt()));
        return message;
    }

    public int markRead(UUID readerId) {
        requireParticipant(readerId);
        int unread = 0;
        for (Message message : messages) {
            if (!message.senderId().equals(readerId) && message.readAt() == null) {
                message.markRead();
                unread++;
            }
        }
        if (unread > 0) {
            this.updatedAt = Instant.now();
        }
        return unread;
    }

    public void archive(UUID actorId) {
        requireParticipant(actorId);
        if (!ConversationStates.of(status).canTransitionTo(ConversationStatus.ARCHIVED)) {
            throw new DomainException("INVALID_CONVERSATION_TRANSITION",
                    "La conversacion ya esta archivada");
        }
        ConversationStatus previous = status;
        this.status = ConversationStatus.ARCHIVED;
        this.updatedAt = Instant.now();
        registerEvent(new ConversationArchivedEvent(id, contractId, previous, status, updatedAt, actorId));
    }

    public boolean isParticipant(UUID userId) {
        return clientId.equals(userId) || freelancerId.equals(userId);
    }

    private void requireParticipant(UUID userId) {
        if (!isParticipant(userId)) {
            throw new DomainException("NOT_A_PARTICIPANT", "El usuario no participa en la conversacion");
        }
    }

    public long unreadFor(UUID userId) {
        return messages.stream()
                .filter(message -> !message.senderId().equals(userId) && message.readAt() == null)
                .count();
    }

    public UUID id() {
        return id;
    }

    public UUID contractId() {
        return contractId;
    }

    public UUID clientId() {
        return clientId;
    }

    public UUID freelancerId() {
        return freelancerId;
    }

    public String subject() {
        return subject;
    }

    public ConversationStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant lastMessageAt() {
        return lastMessageAt;
    }

    public List<Message> messages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Conversation conversation && Objects.equals(id, conversation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
