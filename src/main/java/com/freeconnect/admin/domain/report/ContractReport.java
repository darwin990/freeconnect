package com.freeconnect.admin.domain.report;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Datos agregados de contratos.
 */
public record ContractReport(int total, int active, int completed, int cancelled,
                             Map<String, BigDecimal> volumeByCurrency,
                             Map<String, BigDecimal> commissionByCurrency) implements ReportElement {

    @Override
    public AdminReport accept(ReportVisitor visitor) {
        return visitor.visit(this);
    }
}
