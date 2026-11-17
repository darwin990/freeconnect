package com.freeconnect.projects.api.dto;

import com.freeconnect.projects.domain.ProjectTemplate;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Vista de plantilla de proyecto para el catalogo publico.
 */
public record ProjectTemplateView(UUID id, String name, String title, String description, String category,
                                  BigDecimal budget, String currency, String modality, List<String> skills) {

    public static ProjectTemplateView from(ProjectTemplate template) {
        return new ProjectTemplateView(template.id(), template.name(), template.title(), template.description(),
                template.category().name(), template.suggestedBudget().amount(),
                template.suggestedBudget().currency(), template.modality().name(),
                template.skills().stream().map(skill -> skill.name()).toList());
    }
}
