package com.freeconnect.contracts.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Motivo de rechazo de un hito.
 */
public record RejectMilestoneRequest(@NotBlank @Size(max = 500) String reason) {
}
