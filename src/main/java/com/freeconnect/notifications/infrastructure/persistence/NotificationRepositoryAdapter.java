package com.freeconnect.notifications.infrastructure.persistence;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationRepository;
import com.freeconnect.notifications.domain.NotificationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA del outbox de notificaciones.
 */
@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository repository;

    public NotificationRepositoryAdapter(NotificationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = repository.findById(notification.id())
                .orElseGet(() -> new NotificationJpaEntity(notification.id()));
        entity.setRecipientId(notification.recipientId());
        entity.setType(notification.type());
        entity.setChannel(notification.channel());
        entity.setTitle(notification.title());
        entity.setBody(notification.body());
        entity.setStatus(notification.status());
        entity.setAttempts(notification.attempts());
        entity.setCreatedAt(notification.createdAt());
        entity.setSentAt(notification.sentAt());
        entity.setLastError(notification.lastError());
        repository.save(entity);
        return notification;
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Notification> findByRecipient(UUID recipientId) {
        return repository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findRetryable(int maxAttempts) {
        return repository.findTop50ByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(
                        List.of(NotificationStatus.PENDING, NotificationStatus.FAILED), maxAttempts).stream()
                .map(this::toDomain)
                .toList();
    }

    private Notification toDomain(NotificationJpaEntity entity) {
        return Notification.rehydrate(entity.getId(), entity.getRecipientId(), entity.getType(),
                entity.getChannel(), entity.getTitle(), entity.getBody(), entity.getStatus(),
                entity.getAttempts(), entity.getCreatedAt(), entity.getSentAt(), entity.getLastError());
    }
}
