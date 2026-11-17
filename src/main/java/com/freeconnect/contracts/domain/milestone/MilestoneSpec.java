package com.freeconnect.contracts.domain.milestone;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Definicion de un hito provisto por el cliente al aceptar una propuesta.
 */
public record MilestoneSpec(String title, String description, BigDecimal amount, LocalDate dueDate) {
}
