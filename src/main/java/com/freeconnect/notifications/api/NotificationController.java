package com.freeconnect.notifications.api;

import com.freeconnect.notifications.application.NotificationUseCases;
import com.freeconnect.notifications.application.NotificationView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de notificaciones del usuario autenticado.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notificaciones", description = "Outbox de notificaciones in-app, email y push")
public class NotificationController {

    private final NotificationUseCases useCases;

    public NotificationController(NotificationUseCases useCases) {
        this.useCases = useCases;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar mis notificaciones")
    public List<NotificationView> listMine() {
        return useCases.listMine();
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reintentar la entrega de una notificacion propia")
    public NotificationView retry(@PathVariable UUID id) {
        return useCases.retry(id);
    }
}
