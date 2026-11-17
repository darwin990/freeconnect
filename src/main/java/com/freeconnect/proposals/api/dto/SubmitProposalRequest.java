package com.freeconnect.proposals.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Payload para enviar una propuesta.
 */
public record SubmitProposalRequest(
        @NotBlank @Size(min = 30, max = 3000) String coverLetter,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @Size(min = 3, max = 3) String currency,
        @Min(1) @Max(730) int estimatedDays) {
}
