package com.freeconnect.projects.api.dto;

import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.ProjectCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Payload de creacion/edicion de proyecto.
 */
public record ProjectRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 4000) String description,
        @NotNull ProjectCategory category,
        @NotNull @DecimalMin(value = "0.01") BigDecimal budget,
        @Size(min = 3, max = 3) String currency,
        Modality modality,
        LocalDate deadline,
        Set<@Size(max = 80) String> skills) {
}
