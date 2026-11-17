package com.freeconnect.contracts.domain.milestone;

/**
 * Estrategias de generacion de hitos al aceptar una propuesta.
 */
public enum MilestoneType {
    /** Un unico hito por el total del contrato. */
    SINGLE,
    /** Varios hitos con montos iguales. */
    EQUAL_SPLIT,
    /** Hitos definidos explicitamente por el cliente. */
    CUSTOM
}
