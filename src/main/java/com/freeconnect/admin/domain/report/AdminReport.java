package com.freeconnect.admin.domain.report;

import java.time.Instant;
import java.util.Map;

/**
 * Resultado de un reporte administrativo.
 */
public record AdminReport(String report, Map<String, Object> metrics, Instant generatedAt) {
}
