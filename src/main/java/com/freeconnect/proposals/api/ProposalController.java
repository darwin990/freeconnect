package com.freeconnect.proposals.api;

import com.freeconnect.contracts.api.dto.AcceptProposalRequest;
import com.freeconnect.contracts.application.AcceptProposalUseCase;
import com.freeconnect.contracts.application.ContractView;
import com.freeconnect.contracts.domain.milestone.MilestoneSpec;
import com.freeconnect.proposals.api.dto.EvaluateProposalRequest;
import com.freeconnect.proposals.api.dto.SubmitProposalRequest;
import com.freeconnect.proposals.application.EvaluateProposalUseCase;
import com.freeconnect.proposals.application.ListProjectProposalsUseCase;
import com.freeconnect.proposals.application.ProposalView;
import com.freeconnect.proposals.application.RejectProposalUseCase;
import com.freeconnect.proposals.application.SubmitProposalUseCase;
import com.freeconnect.proposals.application.WithdrawProposalUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de propuestas (RF-04).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Propuestas", description = "Envio, retiro, evaluacion y rechazo de propuestas")
public class ProposalController {

    private final SubmitProposalUseCase submitProposalUseCase;
    private final ListProjectProposalsUseCase listProjectProposalsUseCase;
    private final WithdrawProposalUseCase withdrawProposalUseCase;
    private final EvaluateProposalUseCase evaluateProposalUseCase;
    private final RejectProposalUseCase rejectProposalUseCase;
    private final AcceptProposalUseCase acceptProposalUseCase;

    public ProposalController(SubmitProposalUseCase submitProposalUseCase,
                              ListProjectProposalsUseCase listProjectProposalsUseCase,
                              WithdrawProposalUseCase withdrawProposalUseCase,
                              EvaluateProposalUseCase evaluateProposalUseCase,
                              RejectProposalUseCase rejectProposalUseCase,
                              AcceptProposalUseCase acceptProposalUseCase) {
        this.submitProposalUseCase = submitProposalUseCase;
        this.listProjectProposalsUseCase = listProjectProposalsUseCase;
        this.withdrawProposalUseCase = withdrawProposalUseCase;
        this.evaluateProposalUseCase = evaluateProposalUseCase;
        this.rejectProposalUseCase = rejectProposalUseCase;
        this.acceptProposalUseCase = acceptProposalUseCase;
    }

    @PostMapping("/projects/{projectId}/proposals")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Enviar una propuesta a un proyecto publicado")
    public ResponseEntity<ProposalView> submit(@PathVariable UUID projectId,
                                               @Valid @RequestBody SubmitProposalRequest request) {
        ProposalView created = submitProposalUseCase.execute(new SubmitProposalUseCase.Command(projectId,
                request.coverLetter(), request.amount(), request.currency(), request.estimatedDays()));
        return ResponseEntity.created(URI.create("/api/v1/proposals/" + created.id())).body(created);
    }

    @GetMapping("/projects/{projectId}/proposals")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar propuestas del proyecto (cliente dueno) o las propias (freelancer)")
    public List<ProposalView> list(@PathVariable UUID projectId) {
        return listProjectProposalsUseCase.execute(projectId);
    }

    @DeleteMapping("/proposals/{id}")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Retirar una propuesta propia")
    public ProposalView withdraw(@PathVariable UUID id) {
        return withdrawProposalUseCase.execute(id);
    }

    @PostMapping("/proposals/{id}/evaluate")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Evaluar una propuesta recibida")
    public ProposalView evaluate(@PathVariable UUID id, @Valid @RequestBody EvaluateProposalRequest request) {
        return evaluateProposalUseCase.execute(new EvaluateProposalUseCase.Command(id, request.score(),
                request.note()));
    }

    @PostMapping("/proposals/{id}/reject")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Rechazar una propuesta recibida")
    public ProposalView reject(@PathVariable UUID id) {
        return rejectProposalUseCase.execute(id);
    }

    @PostMapping("/proposals/{id}/accept")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Aceptar una propuesta y generar el contrato con hitos")
    public ResponseEntity<ContractView> accept(@PathVariable UUID id,
                                               @RequestBody(required = false) AcceptProposalRequest request) {
        List<MilestoneSpec> milestones = request == null || request.milestones() == null
                ? List.of()
                : request.milestones().stream()
                        .map(milestone -> new MilestoneSpec(milestone.title(), milestone.description(),
                                milestone.amount(), milestone.dueDate()))
                        .toList();
        ContractView contract = acceptProposalUseCase.execute(new AcceptProposalUseCase.Command(id,
                request == null ? null : request.title(),
                request == null ? null : request.scope(),
                request == null ? null : request.startDate(),
                request == null ? null : request.endDate(),
                request == null ? null : request.commissionPlan(),
                request == null ? null : request.milestoneType(),
                request == null || request.parts() == null ? 1 : request.parts(),
                milestones));
        return ResponseEntity.created(URI.create("/api/v1/contracts/" + contract.id())).body(contract);
    }
}
