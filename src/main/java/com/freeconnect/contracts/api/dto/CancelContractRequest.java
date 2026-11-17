package com.freeconnect.contracts.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Motivo de cancelacion de un contrato.
 */
public record CancelContractRequest(@NotBlank @Size(max = 500) String reason) {
}
