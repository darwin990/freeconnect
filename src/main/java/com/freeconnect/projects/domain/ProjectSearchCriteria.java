package com.freeconnect.projects.domain;

import com.freeconnect.projects.domain.filter.ProjectFilterExpression;
import com.freeconnect.shared.pagination.PageQuery;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Criterios de busqueda de proyectos (RF-03).
 */
public record ProjectSearchCriteria(String text, ProjectCategory category, Modality modality,
                                    ProjectStatus status, BigDecimal minBudget, BigDecimal maxBudget,
                                    UUID creatorId, Set<String> skills,
                                    ProjectFilterExpression expression, PageQuery page) {

    public ProjectSearchCriteria {
        skills = skills == null ? Set.of() : Set.copyOf(skills);
        expression = expression == null ? new ProjectFilterExpression.Always(true) : expression;
        page = Objects.requireNonNullElseGet(page, () -> PageQuery.of(0, 20));
    }

    public static ProjectSearchCriteria empty() {
        return new ProjectSearchCriteria(null, null, null, null, null, null, null, Set.of(), null,
                PageQuery.of(0, 20));
    }

    public ProjectSearchCriteria withPage(PageQuery newPage) {
        return new ProjectSearchCriteria(text, category, modality, status, minBudget, maxBudget, creatorId,
                skills, expression, newPage);
    }
}
