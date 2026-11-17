package com.freeconnect.projects.api.dto;

import com.freeconnect.projects.domain.ProjectStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Payload de cambio de estado de proyecto.
 */
public record ChangeProjectStatusRequest(@NotNull ProjectStatus status) {
}
