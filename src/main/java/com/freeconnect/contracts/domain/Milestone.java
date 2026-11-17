package com.freeconnect.contracts.domain;

import com.freeconnect.contracts.domain.composite.WorkComponent;
import com.freeconnect.contracts.domain.state.MilestoneStates;
import com.freeconnect.shared.domain.AggregateRoot;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.ValidationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Hito del contrato.
 *
 * <p><b>Patrones: Composite (contenedor de entregables) y State (transiciones).</b>
 * Un hito no puede aprobarse sin entregables y solo el estado SUBMITTED admite
 * aprobacion/rechazo.</p>
 */
public class Milestone extends AggregateRoot implements WorkComponent {

    private final UUID id;
    private final UUID contractId;
    private final String title;
    private final String description;
    private final Money amount;
    private final LocalDate dueDate;
    private final int position;
    private MilestoneStatus status;
    private final List<Deliverable> deliverables;

    private Milestone(UUID id, UUID contractId, String title, String description, Money amount, LocalDate dueDate,
                      int position, MilestoneStatus status, List<Deliverable> deliverables) {
        this.id = id;
        this.contractId = contractId;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.dueDate = dueDate;
        this.position = position;
        this.status = status;
        this.deliverables = new ArrayList<>(deliverables);
    }

    public static Milestone create(UUID contractId, String title, String description, Money amount,
                                   LocalDate dueDate, int position) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("El titulo del hito es obligatorio");
        }
        if (amount == null) {
            throw new ValidationException("El valor del hito es obligatorio");
        }
        return new Milestone(UUID.randomUUID(), contractId, title.trim(), description, amount, dueDate, position,
                MilestoneStatus.PENDING, List.of());
    }

    public static Milestone rehydrate(UUID id, UUID contractId, String title, String description, Money amount,
                                      LocalDate dueDate, int position, MilestoneStatus status,
                                      List<Deliverable> deliverables) {
        return new Milestone(id, contractId, title, description, amount, dueDate, position, status, deliverables);
    }

    public Deliverable addDeliverable(String deliverableTitle, String deliverableDescription, String url,
                                      UUID actorId) {
        if (status == MilestoneStatus.APPROVED) {
            throw new DomainException("MILESTONE_CLOSED", "No se pueden agregar entregables a un hito aprobado");
        }
        if (status == MilestoneStatus.PENDING) {
            transitionTo(MilestoneStatus.IN_PROGRESS, actorId, "Primer entregable registrado");
        }
        Deliverable deliverable = Deliverable.submit(deliverableTitle, deliverableDescription, url);
        deliverables.add(deliverable);
        registerEvent(new DeliverableSubmittedEvent(contractId, id, deliverable.id(), deliverable.title(),
                Instant.now(), actorId));
        return deliverable;
    }

    public Deliverable requireDeliverable(UUID deliverableId) {
        return deliverables.stream()
                .filter(deliverable -> deliverable.id().equals(deliverableId))
                .findFirst()
                .orElseThrow(() -> new DomainException("DELIVERABLE_NOT_FOUND",
                        "El entregable no pertenece al hito"));
    }

    public void submit(UUID actorId) {
        if (deliverables.isEmpty()) {
            throw new DomainException("MILESTONE_WITHOUT_DELIVERABLES",
                    "No se puede enviar a revision un hito sin entregables");
        }
        transitionTo(MilestoneStatus.SUBMITTED, actorId, "Hito enviado a revision");
    }

    public void approve(UUID actorId) {
        requireStatus(MilestoneStatus.SUBMITTED);
        transitionTo(MilestoneStatus.APPROVED, actorId, "Hito aprobado por el cliente");
        deliverables.forEach(Deliverable::approve);
    }

    public void reject(UUID actorId, String reason) {
        requireStatus(MilestoneStatus.SUBMITTED);
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("Debe indicar el motivo del rechazo");
        }
        transitionTo(MilestoneStatus.REJECTED, actorId, reason);
        deliverables.forEach(Deliverable::reject);
    }

    /**
     * Restauracion usada exclusivamente por el undo de comandos (compensacion).
     */
    public void restoreStatus(MilestoneStatus previous) {
        this.status = previous;
    }

    private void requireStatus(MilestoneStatus expected) {
        if (status != expected) {
            throw new DomainException("INVALID_MILESTONE_TRANSITION",
                    "La operacion requiere estado %s y el hito esta en %s".formatted(expected, status));
        }
    }

    private void transitionTo(MilestoneStatus target, UUID actorId, String reason) {
        if (!MilestoneStates.of(status).canTransitionTo(target)) {
            throw new DomainException("INVALID_MILESTONE_TRANSITION",
                    "Transicion no permitida de %s a %s".formatted(status, target));
        }
        MilestoneStatus previous = status;
        this.status = target;
        registerEvent(new MilestoneStatusChangedEvent(contractId, id, previous, target, Instant.now(), actorId,
                reason));
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public List<WorkComponent> children() {
        return List.copyOf(deliverables);
    }

    @Override
    public int ownProgress() {
        return switch (status) {
            case PENDING -> 0;
            case IN_PROGRESS -> 30;
            case SUBMITTED -> 70;
            case APPROVED -> 100;
            case REJECTED -> 10;
        };
    }

    @Override
    public int progress() {
        if (deliverables.isEmpty()) {
            return ownProgress();
        }
        int deliverableProgress = deliverables.stream().mapToInt(Deliverable::progress).sum() / deliverables.size();
        return (deliverableProgress + ownProgress()) / 2;
    }

    public UUID contractId() {
        return contractId;
    }

    public String description() {
        return description;
    }

    public Money amount() {
        return amount;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public int position() {
        return position;
    }

    public MilestoneStatus status() {
        return status;
    }

    public List<Deliverable> deliverables() {
        return Collections.unmodifiableList(deliverables);
    }

    public boolean isApproved() {
        return status == MilestoneStatus.APPROVED;
    }
}
