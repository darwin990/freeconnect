package com.freeconnect.admin.api;

import com.freeconnect.admin.application.AdminModerationUseCase;
import com.freeconnect.admin.application.AdminReportsUseCase;
import com.freeconnect.admin.domain.report.AdminReport;
import com.freeconnect.projects.application.ProjectView;
import com.freeconnect.projects.domain.ProjectStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API administrativa: reportes (Visitor) y moderacion.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administracion", description = "Reportes y moderacion de la plataforma")
public class AdminController {

    private final AdminReportsUseCase reportsUseCase;
    private final AdminModerationUseCase moderationUseCase;

    public AdminController(AdminReportsUseCase reportsUseCase, AdminModerationUseCase moderationUseCase) {
        this.reportsUseCase = reportsUseCase;
        this.moderationUseCase = moderationUseCase;
    }

    @GetMapping("/reports")
    @Operation(summary = "Reportes agregados de proyectos, contratos y usuarios (Visitor)")
    public List<AdminReport> reports() {
        return reportsUseCase.execute(null);
    }

    @PostMapping("/projects/{id}/moderate")
    @Operation(summary = "Pausar o cancelar un proyecto por moderacion")
    public ProjectView moderateProject(@PathVariable UUID id, @Valid @RequestBody ModerateProjectRequest request) {
        return ProjectView.from(moderationUseCase.moderateProject(id, request.status()));
    }

    @PostMapping("/users/{id}/suspend")
    @Operation(summary = "Suspender una cuenta de usuario")
    public SuspendResult suspendUser(@PathVariable UUID id) {
        var user = moderationUseCase.suspendUser(id);
        return new SuspendResult(user.id(), user.status().name());
    }

    /**
     * Accion de moderacion permitida.
     */
    public record ModerateProjectRequest(@NotNull ProjectStatus status) {
    }

    /**
     * Resultado de suspender una cuenta.
     */
    public record SuspendResult(UUID id, String status) {
    }
}
