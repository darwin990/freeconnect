package com.freeconnect.contracts.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para registrar un entregable.
 */
public record AddDeliverableRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 4000) String description,
        @Size(max = 1000) String url) {
}
