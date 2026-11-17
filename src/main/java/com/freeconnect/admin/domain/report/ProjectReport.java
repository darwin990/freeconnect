package com.freeconnect.admin.domain.report;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Datos agregados de proyectos.
 */
public record ProjectReport(int total, int published, int inProgress, int finished, int cancelled,
                            Map<String, BigDecimal> budgetByCurrency) implements ReportElement {

    @Override
    public AdminReport accept(ReportVisitor visitor) {
        return visitor.visit(this);
    }
}
