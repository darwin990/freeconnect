package com.freeconnect.contracts.domain;

import com.freeconnect.contracts.domain.state.ContractStates;
import com.freeconnect.shared.domain.AggregateRoot;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.ValidationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raiz de contrato (RF-05).
 *
 * <p><b>Patrones: Builder (construccion validada) y State (transiciones).</b>
 * El contrato agrupa hitos ({@link Milestone}) y calcula su progreso mediante
 * la jerarquia Composite. Se completa automaticamente cuando todos los hitos
 * quedan aprobados.</p>
 */
public class Contract extends AggregateRoot {

    private final UUID id;
    private final UUID projectId;
    private final UUID proposalId;
    private final UUID clientId;
    private final UUID freelancerId;
    private final String title;
    private final String scope;
    private final Money amount;
    private final Money commission;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<Milestone> milestones;
    private ContractStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    private Contract(Builder builder) {
        this.id = builder.id;
        this.projectId = builder.projectId;
        this.proposalId = builder.proposalId;
        this.clientId = builder.clientId;
        this.freelancerId = builder.freelancerId;
        this.title = builder.title;
        this.scope = builder.scope;
        this.amount = builder.amount;
        this.commission = builder.commission;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.milestones = new ArrayList<>(builder.milestones);
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.completedAt = builder.completedAt;
    }

    public static Builder builder(UUID projectId, UUID proposalId, UUID clientId, UUID freelancerId) {
        return new Builder(projectId, proposalId, clientId, freelancerId);
    }

    /**
     * Registra el evento de creacion. Lo invoca la fabrica de contratos una sola vez.
     */
    public void markCreated(UUID actorId) {
        registerEvent(new ContractCreatedEvent(id, projectId, proposalId, clientId, freelancerId, amount,
                createdAt));
    }

    public Milestone milestone(UUID milestoneId) {
        return requireMilestone(milestoneId);
    }

    public void activate(UUID actorId) {
        transitionTo(ContractStatus.ACTIVE, actorId);
    }

    public void submitMilestone(UUID milestoneId, UUID actorId) {
        requireActive();
        Milestone milestone = requireMilestone(milestoneId);
        milestone.submit(actorId);
        drainMilestoneEvents(milestone);
    }

    public Deliverable addDeliverable(UUID milestoneId, String deliverableTitle, String description, String url,
                                      UUID actorId) {
        requireActive();
        Milestone milestone = requireMilestone(milestoneId);
        Deliverable deliverable = milestone.addDeliverable(deliverableTitle, description, url, actorId);
        drainMilestoneEvents(milestone);
        this.updatedAt = Instant.now();
        return deliverable;
    }

    public void approveMilestone(UUID milestoneId, UUID actorId) {
        requireActive();
        Milestone milestone = requireMilestone(milestoneId);
        milestone.approve(actorId);
        drainMilestoneEvents(milestone);
        this.updatedAt = Instant.now();
        if (milestones.stream().allMatch(Milestone::isApproved)) {
            complete(actorId);
        }
    }

    public void rejectMilestone(UUID milestoneId, UUID actorId, String reason) {
        requireActive();
        Milestone milestone = requireMilestone(milestoneId);
        milestone.reject(actorId, reason);
        drainMilestoneEvents(milestone);
        this.updatedAt = Instant.now();
    }

    public void complete(UUID actorId) {
        transitionTo(ContractStatus.COMPLETED, actorId);
        this.completedAt = updatedAt;
        registerEvent(new ContractCompletedEvent(id, projectId, clientId, freelancerId, completedAt, actorId));
    }

    public void cancel(UUID actorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("Debe indicar el motivo de la cancelacion");
        }
        transitionTo(ContractStatus.CANCELLED, actorId);
    }

    /**
     * Restauracion usada exclusivamente por el undo de comandos (compensacion).
     */
    public void restoreStatus(ContractStatus previous) {
        this.status = previous;
        this.updatedAt = Instant.now();
        if (previous != ContractStatus.COMPLETED) {
            this.completedAt = null;
        }
    }

    public void transitionTo(ContractStatus target, UUID actorId) {
        if (target == null) {
            throw new ValidationException("El estado destino es obligatorio");
        }
        if (!ContractStates.of(status).canTransitionTo(target)) {
            throw new DomainException("INVALID_CONTRACT_TRANSITION",
                    "Transicion no permitida de %s a %s".formatted(status, target));
        }
        ContractStatus previous = status;
        this.status = target;
        this.updatedAt = Instant.now();
        registerEvent(new ContractStatusChangedEvent(id, projectId, previous, target, updatedAt, actorId));
    }

    private void requireActive() {
        if (status != ContractStatus.ACTIVE) {
            throw new DomainException("CONTRACT_NOT_ACTIVE",
                    "El contrato debe estar activo para registrar avances");
        }
    }

    private Milestone requireMilestone(UUID milestoneId) {
        return milestones.stream()
                .filter(milestone -> milestone.id().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new DomainException("MILESTONE_NOT_FOUND",
                        "El hito no pertenece al contrato"));
    }

    private void drainMilestoneEvents(Milestone milestone) {
        milestone.pullDomainEvents().forEach(this::registerEvent);
    }

    /**
     * Progreso global (0-100) calculado con la jerarquia Composite.
     */
    public int progress() {
        if (milestones.isEmpty()) {
            return 0;
        }
        return milestones.stream().mapToInt(Milestone::progress).sum() / milestones.size();
    }

    public long approvedMilestones() {
        return milestones.stream().filter(Milestone::isApproved).count();
    }

    public boolean isParticipant(UUID userId) {
        return clientId.equals(userId) || freelancerId.equals(userId);
    }

    public UUID id() {
        return id;
    }

    public UUID projectId() {
        return projectId;
    }

    public UUID proposalId() {
        return proposalId;
    }

    public UUID clientId() {
        return clientId;
    }

    public UUID freelancerId() {
        return freelancerId;
    }

    public String title() {
        return title;
    }

    public String scope() {
        return scope;
    }

    public Money amount() {
        return amount;
    }

    public Money commission() {
        return commission;
    }

    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate() {
        return endDate;
    }

    public List<Milestone> milestones() {
        return Collections.unmodifiableList(milestones);
    }

    public ContractStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    /**
     * <b>Patron: Builder.</b> Valida invariantes del contrato antes de exponerlo.
     */
    public static final class Builder {
        private final UUID projectId;
        private final UUID proposalId;
        private final UUID clientId;
        private final UUID freelancerId;
        private UUID id = UUID.randomUUID();
        private String title;
        private String scope;
        private Money amount;
        private Money commission;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<Milestone> milestones = List.of();
        private ContractStatus status = ContractStatus.DRAFT;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();
        private Instant completedAt;

        private Builder(UUID projectId, UUID proposalId, UUID clientId, UUID freelancerId) {
            this.projectId = Objects.requireNonNull(projectId, "projectId es obligatorio");
            this.proposalId = Objects.requireNonNull(proposalId, "proposalId es obligatorio");
            this.clientId = Objects.requireNonNull(clientId, "clientId es obligatorio");
            this.freelancerId = Objects.requireNonNull(freelancerId, "freelancerId es obligatorio");
        }

        public Builder id(UUID value) {
            this.id = value;
            return this;
        }

        public Builder title(String value) {
            this.title = value;
            return this;
        }

        public Builder scope(String value) {
            this.scope = value;
            return this;
        }

        public Builder amount(Money value) {
            this.amount = value;
            return this;
        }

        public Builder commission(Money value) {
            this.commission = value;
            return this;
        }

        public Builder startDate(LocalDate value) {
            this.startDate = value;
            return this;
        }

        public Builder endDate(LocalDate value) {
            this.endDate = value;
            return this;
        }

        public Builder milestones(List<Milestone> value) {
            this.milestones = value == null ? List.of() : List.copyOf(value);
            return this;
        }

        public Builder status(ContractStatus value) {
            this.status = value;
            return this;
        }

        public Builder createdAt(Instant value) {
            this.createdAt = value;
            return this;
        }

        public Builder updatedAt(Instant value) {
            this.updatedAt = value;
            return this;
        }

        public Builder completedAt(Instant value) {
            this.completedAt = value;
            return this;
        }

        public Contract build() {
            if (title == null || title.isBlank() || title.length() > 180) {
                throw new ValidationException("El titulo del contrato es obligatorio (maximo 180 caracteres)");
            }
            if (scope == null || scope.isBlank()) {
                throw new ValidationException("El alcance del contrato es obligatorio");
            }
            if (amount == null || amount.amount().signum() <= 0) {
                throw new ValidationException("El monto del contrato debe ser mayor a cero");
            }
            if (commission == null) {
                throw new ValidationException("La comision del contrato es obligatoria");
            }
            if (milestones.isEmpty()) {
                throw new ValidationException("El contrato requiere al menos un hito");
            }
            if (milestones.stream().anyMatch(milestone -> !milestone.contractId().equals(id))) {
                throw new ValidationException("Todos los hitos deben pertenecer al contrato");
            }
            Money sum = milestones.stream().map(Milestone::amount).reduce(Money.zero(amount.currency()), Money::plus);
            if (sum.isGreaterThan(amount)) {
                throw new ValidationException("La suma de los hitos no puede superar el monto del contrato");
            }
            return new Contract(this);
        }
    }
}
