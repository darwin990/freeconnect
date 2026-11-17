package com.freeconnect.proposals.domain;

import com.freeconnect.shared.domain.AggregateRoot;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.ValidationException;
import java.time.Instant;
import java.util.UUID;

/**
 * Agregado de propuesta (RF-04).
 *
 * <p>Solo se acepta/retira una propuesta en estado SUBMITTED; cada transicion
 * emite eventos de dominio para notificacion y auditoria.</p>
 */
public class Proposal extends AggregateRoot {

    private final UUID id;
    private final UUID projectId;
    private final UUID freelancerId;
    private final String coverLetter;
    private final Money amount;
    private final int estimatedDays;
    private ProposalStatus status;
    private Integer evaluationScore;
    private String evaluationNote;
    private final Instant createdAt;
    private Instant updatedAt;

    private Proposal(UUID id, UUID projectId, UUID freelancerId, String coverLetter, Money amount,
                     int estimatedDays, ProposalStatus status, Integer evaluationScore, String evaluationNote,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.freelancerId = freelancerId;
        this.coverLetter = coverLetter;
        this.amount = amount;
        this.estimatedDays = estimatedDays;
        this.status = status;
        this.evaluationScore = evaluationScore;
        this.evaluationNote = evaluationNote;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Proposal submit(UUID projectId, UUID freelancerId, String coverLetter, Money amount,
                                  int estimatedDays) {
        if (coverLetter == null || coverLetter.isBlank()) {
            throw new ValidationException("La carta de presentacion es obligatoria");
        }
        if (amount == null || amount.amount().signum() <= 0) {
            throw new ValidationException("El monto propuesto debe ser mayor a cero");
        }
        if (estimatedDays <= 0 || estimatedDays > 730) {
            throw new ValidationException("Los dias estimados deben estar entre 1 y 730");
        }
        Instant now = Instant.now();
        Proposal proposal = new Proposal(UUID.randomUUID(), projectId, freelancerId, coverLetter.trim(), amount,
                estimatedDays, ProposalStatus.SUBMITTED, null, null, now, now);
        proposal.registerEvent(new ProposalSubmittedEvent(proposal.id, projectId, freelancerId, amount, now));
        return proposal;
    }

    public static Proposal rehydrate(UUID id, UUID projectId, UUID freelancerId, String coverLetter, Money amount,
                                     int estimatedDays, ProposalStatus status, Integer evaluationScore,
                                     String evaluationNote, Instant createdAt, Instant updatedAt) {
        return new Proposal(id, projectId, freelancerId, coverLetter, amount, estimatedDays, status,
                evaluationScore, evaluationNote, createdAt, updatedAt);
    }

    public void withdraw(UUID actorId) {
        requireStatus(ProposalStatus.SUBMITTED, "retirar");
        this.status = ProposalStatus.WITHDRAWN;
        this.updatedAt = Instant.now();
        registerEvent(new ProposalWithdrawnEvent(id, projectId, freelancerId, updatedAt, actorId));
    }

    public void accept(UUID actorId) {
        requireStatus(ProposalStatus.SUBMITTED, "aceptar");
        this.status = ProposalStatus.ACCEPTED;
        this.updatedAt = Instant.now();
        registerEvent(new ProposalAcceptedEvent(id, projectId, freelancerId, updatedAt, actorId));
    }

    public void reject(UUID actorId) {
        requireStatus(ProposalStatus.SUBMITTED, "rechazar");
        this.status = ProposalStatus.REJECTED;
        this.updatedAt = Instant.now();
        registerEvent(new ProposalRejectedEvent(id, projectId, freelancerId, updatedAt, actorId));
    }

    public void evaluate(int score, String note, UUID actorId) {
        requireStatus(ProposalStatus.SUBMITTED, "evaluar");
        if (score < 1 || score > 5) {
            throw new ValidationException("La evaluacion debe estar entre 1 y 5");
        }
        this.evaluationScore = score;
        this.evaluationNote = note;
        this.updatedAt = Instant.now();
        registerEvent(new ProposalEvaluatedEvent(id, projectId, score, updatedAt, actorId));
    }

    private void requireStatus(ProposalStatus expected, String action) {
        if (status != expected) {
            throw new DomainException("INVALID_PROPOSAL_TRANSITION",
                    "No se puede %s una propuesta en estado %s".formatted(action, status));
        }
    }

    public boolean isActive() {
        return status == ProposalStatus.SUBMITTED;
    }

    public UUID id() {
        return id;
    }

    public UUID projectId() {
        return projectId;
    }

    public UUID freelancerId() {
        return freelancerId;
    }

    public String coverLetter() {
        return coverLetter;
    }

    public Money amount() {
        return amount;
    }

    public int estimatedDays() {
        return estimatedDays;
    }

    public ProposalStatus status() {
        return status;
    }

    public Integer evaluationScore() {
        return evaluationScore;
    }

    public String evaluationNote() {
        return evaluationNote;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
