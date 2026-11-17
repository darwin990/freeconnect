package com.freeconnect.projects.application;

import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.ProjectCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Comando de creacion de proyecto.
 */
public record CreateProjectCommand(String title, String description, ProjectCategory category, BigDecimal budget,
                                   String currency, Modality modality, LocalDate deadline, Set<String> skills) {
}
