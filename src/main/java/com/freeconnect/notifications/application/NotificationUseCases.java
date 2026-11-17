package com.freeconnect.notifications.application;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de consulta y reintento de notificaciones.
 */
@Service
public class NotificationUseCases {

    private final NotificationRepository repository;
    private final NotificationService notificationService;
    private final CurrentUserProvider currentUser;

    public NotificationUseCases(NotificationRepository repository, NotificationService notificationService,
                                CurrentUserProvider currentUser) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<NotificationView> listMine() {
        return NotificationView.from(repository.findByRecipient(currentUser.requireUserId()));
    }

    @Transactional
    public NotificationView retry(UUID notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notificacion", notificationId));
        currentUser.requireOwner(notification.recipientId(), "la notificacion");
        notificationService.retry(notification);
        return NotificationView.from(notification);
    }
}
