package com.freeconnect.projects.application;

import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectCategory;
import com.freeconnect.projects.domain.ProjectStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de proyecto para la API.
 */
public record ProjectView(UUID id, UUID clientId, String title, String description, ProjectCategory category,
                          BigDecimal budgetAmount, String currency, Modality modality, ProjectStatus status,
                          List<String> skills, LocalDate deadline, Instant publishedAt, Instant createdAt) {

    public static ProjectView from(Project project) {
        return new ProjectView(project.id(), project.clientId(), project.title(), project.description(),
                project.category(), project.budget().amount(), project.budget().currency(), project.modality(),
                project.status(), project.requiredSkills().stream().map(skill -> skill.name()).toList(),
                project.deadline(), project.publishedAt(), project.createdAt());
    }
}
