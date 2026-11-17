package com.freeconnect.projects.application;

import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.ProjectCategory;
import com.freeconnect.projects.domain.ProjectSearchCriteria;
import com.freeconnect.projects.domain.ProjectStatus;
import com.freeconnect.projects.domain.filter.ProjectFilterParser;
import com.freeconnect.shared.pagination.PageQuery;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * Query de busqueda de proyectos (RF-03): filtros estructurados + lenguaje de filtros.
 */
public record SearchProjectsQuery(String text, ProjectCategory category, Modality modality, ProjectStatus status,
                                  BigDecimal minBudget, BigDecimal maxBudget, Set<String> skills, String filter,
                                  PageQuery page, UUID creatorId) {

    public ProjectSearchCriteria toCriteria() {
        return new ProjectSearchCriteria(text, category, modality, status, minBudget, maxBudget, creatorId,
                skills, ProjectFilterParser.parse(filter), page);
    }
}
