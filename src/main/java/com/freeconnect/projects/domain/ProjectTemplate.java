package com.freeconnect.projects.domain;

import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.exception.ValidationException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Plantilla clonable de proyecto.
 *
 * <p><b>Patron: Prototype.</b> {@link #copy()} produce una plantilla independiente
 * (nuevo id y copia profunda de habilidades) y {@link #instantiate(UUID)} genera un
 * proyecto en DRAFT listo para editar.</p>
 */
public class ProjectTemplate implements Prototype<ProjectTemplate> {

    private final UUID id;
    private final String name;
    private final String title;
    private final String description;
    private final ProjectCategory category;
    private final Money suggestedBudget;
    private final Modality modality;
    private final Set<Skill> skills;
    private final int deadlineOffsetDays;

    public ProjectTemplate(UUID id, String name, String title, String description, ProjectCategory category,
                           Money suggestedBudget, Modality modality, Set<Skill> skills, int deadlineOffsetDays) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("La plantilla requiere nombre");
        }
        this.id = id;
        this.name = name;
        this.title = title;
        this.description = description;
        this.category = category;
        this.suggestedBudget = suggestedBudget;
        this.modality = modality;
        this.skills = new LinkedHashSet<>(skills);
        this.deadlineOffsetDays = deadlineOffsetDays;
    }

    @Override
    public ProjectTemplate copy() {
        return new ProjectTemplate(UUID.randomUUID(), name + " (copia)", title, description, category,
                suggestedBudget, modality, new LinkedHashSet<>(skills), deadlineOffsetDays);
    }

    /** Materializa la plantilla como proyecto en estado DRAFT. */
    public Project instantiate(UUID clientId) {
        return Project.builder(clientId)
                .title(title)
                .description(description)
                .category(category)
                .budget(suggestedBudget)
                .modality(modality)
                .requiredSkills(new LinkedHashSet<>(skills))
                .deadline(deadlineOffsetDays > 0 ? LocalDate.now().plusDays(deadlineOffsetDays) : null)
                .build();
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
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

    public Money suggestedBudget() {
        return suggestedBudget;
    }

    public Modality modality() {
        return modality;
    }

    public Set<Skill> skills() {
        return Set.copyOf(skills);
    }

    public int deadlineOffsetDays() {
        return deadlineOffsetDays;
    }
}
