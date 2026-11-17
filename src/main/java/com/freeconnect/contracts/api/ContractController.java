package com.freeconnect.contracts.api;

import com.freeconnect.contracts.api.dto.AddCommentRequest;
import com.freeconnect.contracts.api.dto.AddDeliverableRequest;
import com.freeconnect.contracts.api.dto.AddTaskRequest;
import com.freeconnect.contracts.api.dto.CancelContractRequest;
import com.freeconnect.contracts.api.dto.RejectMilestoneRequest;
import com.freeconnect.contracts.api.dto.ToggleTaskRequest;
import com.freeconnect.contracts.application.AddDeliverableCommentUseCase;
import com.freeconnect.contracts.application.AddDeliverableTaskUseCase;
import com.freeconnect.contracts.application.AddDeliverableUseCase;
import com.freeconnect.contracts.application.ApproveMilestoneUseCase;
import com.freeconnect.contracts.application.CancelContractUseCase;
import com.freeconnect.contracts.application.ContractView;
import com.freeconnect.contracts.application.GetContractUseCase;
import com.freeconnect.contracts.application.ListContractsUseCase;
import com.freeconnect.contracts.application.RejectMilestoneUseCase;
import com.freeconnect.contracts.application.SubmitMilestoneUseCase;
import com.freeconnect.contracts.application.ToggleDeliverableTaskUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de contratos, hitos y entregables (RF-05 y RF-06).
 */
@RestController
@RequestMapping("/api/v1/contracts")
@Tag(name = "Contratos", description = "Hitos, entregables, tareas y comentarios del contrato")
public class ContractController {

    private final ListContractsUseCase listContractsUseCase;
    private final GetContractUseCase getContractUseCase;
    private final AddDeliverableUseCase addDeliverableUseCase;
    private final SubmitMilestoneUseCase submitMilestoneUseCase;
    private final ApproveMilestoneUseCase approveMilestoneUseCase;
    private final RejectMilestoneUseCase rejectMilestoneUseCase;
    private final CancelContractUseCase cancelContractUseCase;
    private final AddDeliverableTaskUseCase addDeliverableTaskUseCase;
    private final ToggleDeliverableTaskUseCase toggleDeliverableTaskUseCase;
    private final AddDeliverableCommentUseCase addDeliverableCommentUseCase;

    public ContractController(ListContractsUseCase listContractsUseCase, GetContractUseCase getContractUseCase,
                              AddDeliverableUseCase addDeliverableUseCase,
                              SubmitMilestoneUseCase submitMilestoneUseCase,
                              ApproveMilestoneUseCase approveMilestoneUseCase,
                              RejectMilestoneUseCase rejectMilestoneUseCase,
                              CancelContractUseCase cancelContractUseCase,
                              AddDeliverableTaskUseCase addDeliverableTaskUseCase,
                              ToggleDeliverableTaskUseCase toggleDeliverableTaskUseCase,
                              AddDeliverableCommentUseCase addDeliverableCommentUseCase) {
        this.listContractsUseCase = listContractsUseCase;
        this.getContractUseCase = getContractUseCase;
        this.addDeliverableUseCase = addDeliverableUseCase;
        this.submitMilestoneUseCase = submitMilestoneUseCase;
        this.approveMilestoneUseCase = approveMilestoneUseCase;
        this.rejectMilestoneUseCase = rejectMilestoneUseCase;
        this.cancelContractUseCase = cancelContractUseCase;
        this.addDeliverableTaskUseCase = addDeliverableTaskUseCase;
        this.toggleDeliverableTaskUseCase = toggleDeliverableTaskUseCase;
        this.addDeliverableCommentUseCase = addDeliverableCommentUseCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar mis contratos como cliente o freelancer")
    public List<ContractView> list() {
        return listContractsUseCase.execute(null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Detalle de un contrato con hitos y entregables")
    public ContractView get(@PathVariable UUID id) {
        return getContractUseCase.execute(id);
    }

    @PostMapping("/{id}/milestones/{milestoneId}/deliverables")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Registrar un entregable en un hito")
    public ContractView addDeliverable(@PathVariable UUID id, @PathVariable UUID milestoneId,
                                       @Valid @RequestBody AddDeliverableRequest request) {
        return addDeliverableUseCase.execute(new AddDeliverableUseCase.Command(id, milestoneId, request.title(),
                request.description(), request.url()));
    }

    @PostMapping("/{id}/milestones/{milestoneId}/submit")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Enviar un hito a revision")
    public ContractView submitMilestone(@PathVariable UUID id, @PathVariable UUID milestoneId) {
        return submitMilestoneUseCase.execute(new SubmitMilestoneUseCase.Command(id, milestoneId));
    }

    @PostMapping("/{id}/milestones/{milestoneId}/approve")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Aprobar un hito (Command pattern)")
    public ContractView approveMilestone(@PathVariable UUID id, @PathVariable UUID milestoneId) {
        return approveMilestoneUseCase.execute(new ApproveMilestoneUseCase.Command(id, milestoneId));
    }

    @PostMapping("/{id}/milestones/{milestoneId}/reject")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Rechazar un hito enviado a revision")
    public ContractView rejectMilestone(@PathVariable UUID id, @PathVariable UUID milestoneId,
                                        @Valid @RequestBody RejectMilestoneRequest request) {
        return rejectMilestoneUseCase.execute(new RejectMilestoneUseCase.Command(id, milestoneId,
                request.reason()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancelar un contrato (Command pattern)")
    public ContractView cancel(@PathVariable UUID id, @Valid @RequestBody CancelContractRequest request) {
        return cancelContractUseCase.execute(new CancelContractUseCase.Command(id, request.reason()));
    }

    @PostMapping("/{id}/milestones/{milestoneId}/deliverables/{deliverableId}/tasks")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Agregar una tarea a un entregable (Composite)")
    public ContractView addTask(@PathVariable UUID id, @PathVariable UUID milestoneId,
                                @PathVariable UUID deliverableId, @Valid @RequestBody AddTaskRequest request) {
        return addDeliverableTaskUseCase.execute(new AddDeliverableTaskUseCase.Command(id, milestoneId,
                deliverableId, request.title()));
    }

    @PatchMapping("/{id}/milestones/{milestoneId}/deliverables/{deliverableId}/tasks/{taskId}")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Marcar o desmarcar una tarea")
    public ContractView toggleTask(@PathVariable UUID id, @PathVariable UUID milestoneId,
                                   @PathVariable UUID deliverableId, @PathVariable UUID taskId,
                                   @RequestBody ToggleTaskRequest request) {
        return toggleDeliverableTaskUseCase.execute(new ToggleDeliverableTaskUseCase.Command(id, milestoneId,
                deliverableId, taskId, request.done()));
    }

    @PostMapping("/{id}/milestones/{milestoneId}/deliverables/{deliverableId}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Comentar un entregable")
    public ContractView addComment(@PathVariable UUID id, @PathVariable UUID milestoneId,
                                   @PathVariable UUID deliverableId, @Valid @RequestBody AddCommentRequest request) {
        return addDeliverableCommentUseCase.execute(new AddDeliverableCommentUseCase.Command(id, milestoneId,
                deliverableId, request.body()));
    }
}
