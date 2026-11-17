package com.freeconnect.notifications.infrastructure.persistence;

import com.freeconnect.notifications.domain.NotificationStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data de notificaciones.
 */
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    List<NotificationJpaEntity> findTop50ByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(
            Collection<NotificationStatus> statuses, int attempts);
}
