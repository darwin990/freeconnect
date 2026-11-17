package com.freeconnect.profiles.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para agregar una evidencia al portafolio.
 */
public record CreatePortfolioItemRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 1000) String url,
        @Size(max = 1000) String description) {
}
