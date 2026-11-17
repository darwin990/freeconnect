package com.freeconnect.audit.api;

import com.freeconnect.audit.application.JpaAuditRecorder;
import com.freeconnect.audit.application.JpaAuditRecorder.AuditLogView;
import com.freeconnect.shared.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de consulta de auditoria (solo administradores).
 */
@RestController
@RequestMapping("/api/v1/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Auditoria", description = "Trazabilidad de eventos de dominio")
public class AuditController {

    private final JpaAuditRecorder recorder;
    private final CurrentUserProvider currentUser;

    public AuditController(JpaAuditRecorder recorder, CurrentUserProvider currentUser) {
        this.recorder = recorder;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "Ultimos 100 eventos auditados")
    public List<AuditLogView> latest() {
        currentUser.requireAdmin();
        return recorder.latest().stream().map(AuditLogView::from).toList();
    }
}
