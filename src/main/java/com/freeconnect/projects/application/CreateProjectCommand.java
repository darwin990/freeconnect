package com.freeconnect.projects.application;

import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.ProjectCategory;
import com.freeconnect.projects.domain.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Comando de creacion de proyecto.
 *
 * <p>{@code status} admite {@code DRAFT} (por defecto) o {@code PUBLISHED} para
 * publicar inmediatamente tras crear el borrador.</p>
 */
public record CreateProjectCommand(String title, String description, ProjectCategory category, BigDecimal budget,
                                   String currency, Modality modality, LocalDate deadline, Set<String> skills,
                                   ProjectStatus status) {
}
