package com.freeconnect.contracts.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Tarea de un entregable.
 */
public record AddTaskRequest(@NotBlank @Size(max = 180) String title) {
}
