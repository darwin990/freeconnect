package com.freeconnect.contracts.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Comentario sobre un entregable.
 */
public record AddCommentRequest(@NotBlank @Size(max = 2000) String body) {
}
