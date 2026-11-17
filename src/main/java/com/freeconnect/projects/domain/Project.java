package com.freeconnect.projects.domain;

import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.projects.domain.state.ProjectState;
import com.freeconnect.projects.domain.state.ProjectStates;
import com.freeconnect.shared.domain.AggregateRoot;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.ValidationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Agregado raiz de proyecto (RF-03).
 *
 * <p><b>Patrones: Builder (construccion validada) y State (transiciones).</b>
 * Todo cambio de estado pasa por {@link #transitionTo(ProjectStatus, UUID)} y
 * emite {@link ProjectStatusChangedEvent}.</p>
 */
public class Project extends AggregateRoot {

    private final UUID id;
    private final UUID clientId;
    private String title;
    private String description;
    private ProjectCategory category;
    private Money budget;
    private Modality modality;
    private Set<Skill> requiredSkills;
    private LocalDate deadline;
    private ProjectStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;

    private Project(Builder builder) {
        this.id = builder.id;
        this.clientId = builder.clientId;
        this.title = builder.title;
        this.description = builder.description;
        this.category = builder.category;
        this.budget = builder.budget;
        this.modality = builder.modality;
        this.requiredSkills = new LinkedHashSet<>(builder.requiredSkills);
        this.deadline = builder.deadline;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.publishedAt = builder.publishedAt;
    }

    public static Builder builder(UUID clientId) {
        return new Builder(clientId);
    }

    public void updateDetails(String title, String description, Money budget, Modality modality,
                              LocalDate deadline, ProjectCategory category, Set<Skill> skills) {
        if (status == ProjectStatus.IN_PROGRESS || status == ProjectStatus.FINISHED
                || status == ProjectStatus.CANCELLED) {
            throw new DomainException("PROJECT_NOT_EDITABLE",
                    "Solo se pueden editar proyectos en borrador, publicados o pausados");
        }
        validateTitle(title);
        if (budget == null) {
            throw new ValidationException("El presupuesto es obligatorio");
        }
        this.title = title.trim();
        this.description = description;
        this.budget = budget;
        this.modality = Objects.requireNonNull(modality, "La modalidad es obligatoria");
        this.deadline = deadline;
        this.category = Objects.requireNonNull(category, "La categoria es obligatoria");
        this.requiredSkills = new LinkedHashSet<>(skills);
        this.updatedAt = Instant.now();
    }

    public void publish(UUID actorId) {
        transitionTo(ProjectStatus.PUBLISHED, actorId);
    }

    public void pause(UUID actorId) {
        transitionTo(ProjectStatus.PAUSED, actorId);
    }

    public void resume(UUID actorId) {
        transitionTo(ProjectStatus.PUBLISHED, actorId);
    }

    public void startWork(UUID actorId) {
        transitionTo(ProjectStatus.IN_PROGRESS, actorId);
    }

    public void finish(UUID actorId) {
        transitionTo(ProjectStatus.FINISHED, actorId);
    }

    public void cancel(UUID actorId) {
        transitionTo(ProjectStatus.CANCELLED, actorId);
    }

    /**
     * Unica puerta de entrada a los cambios de estado (State pattern).
     */
    public void transitionTo(ProjectStatus target, UUID actorId) {
        if (target == null) {
            throw new ValidationException("El estado destino es obligatorio");
        }
        ProjectState currentState = ProjectStates.of(status);
        if (!currentState.canTransitionTo(target)) {
            throw new DomainException("INVALID_PROJECT_TRANSITION",
                    "Transicion no permitida de %s a %s".formatted(status, target));
        }
        ProjectStatus previous = status;
        this.status = target;
        this.updatedAt = Instant.now();
        if (target == ProjectStatus.PUBLISHED && publishedAt == null) {
            this.publishedAt = this.updatedAt;
        }
        ProjectStates.of(target).onEnter(previous);
        registerEvent(new ProjectStatusChangedEvent(id, clientId, previous, target, updatedAt, actorId));
        if (target == ProjectStatus.PUBLISHED) {
            registerEvent(new ProjectPublishedEvent(id, clientId, title, updatedAt, actorId));
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank() || title.length() > 160) {
            throw new ValidationException("El titulo es obligatorio y admite hasta 160 caracteres");
        }
    }

    public UUID id() {
        return id;
    }

    public UUID clientId() {
        return clientId;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public ProjectCategory category() {
        return category;
    }

    public Money budget() {
        return budget;
    }

    public Modality modality() {
        return modality;
    }

    public Set<Skill> requiredSkills() {
        return Collections.unmodifiableSet(requiredSkills);
    }

    public LocalDate deadline() {
        return deadline;
    }

    public ProjectStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant publishedAt() {
        return publishedAt;
    }

    /**
     * <b>Patron: Builder.</b> Construye un proyecto completo validando invariantes
     * antes de permitir su uso.
     */
    public static final class Builder {
        private final UUID clientId;
        private UUID id = UUID.randomUUID();
        private String title;
        private String description;
        private ProjectCategory category;
        private Money budget;
        private Modality modality = Modality.REMOTE;
        private Set<Skill> requiredSkills = new LinkedHashSet<>();
        private LocalDate deadline;
        private ProjectStatus status = ProjectStatus.DRAFT;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();
        private Instant publishedAt;

        private Builder(UUID clientId) {
            this.clientId = Objects.requireNonNull(clientId, "clientId es obligatorio");
        }

        public Builder id(UUID value) {
            this.id = value;
            return this;
        }

        public Builder title(String value) {
            this.title = value;
            return this;
        }

        public Builder description(String value) {
            this.description = value;
            return this;
        }

        public Builder category(ProjectCategory value) {
            this.category = value;
            return this;
        }

        public Builder budget(Money value) {
            this.budget = value;
            return this;
        }

        public Builder modality(Modality value) {
            this.modality = value;
            return this;
        }

        public Builder requiredSkills(Set<Skill> value) {
            this.requiredSkills = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value);
            return this;
        }

        public Builder deadline(LocalDate value) {
            this.deadline = value;
            return this;
        }

        public Builder status(ProjectStatus value) {
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

        public Builder publishedAt(Instant value) {
            this.publishedAt = value;
            return this;
        }

        public Project build() {
            validateTitle(title);
            if (category == null) {
                throw new ValidationException("La categoria es obligatoria");
            }
            if (budget == null || budget.amount().signum() <= 0) {
                throw new ValidationException("El presupuesto debe ser mayor a cero");
            }
            if (modality == null) {
                throw new ValidationException("La modalidad es obligatoria");
            }
            return new Project(this);
        }
    }
}
