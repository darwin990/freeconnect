package com.freeconnect.proposals.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Payload de evaluacion de propuesta.
 */
public record EvaluateProposalRequest(
        @Min(1) @Max(5) int score,
        @Size(max = 1000) String note) {
}
