package com.freeconnect.admin.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.freeconnect.admin.domain.report.AdminReport;
import com.freeconnect.admin.domain.report.ContractReport;
import com.freeconnect.admin.domain.report.ProjectReport;
import com.freeconnect.admin.domain.report.ReportElement;
import com.freeconnect.admin.domain.report.UserReport;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del patron Visitor sobre los reportes administrativos.
 */
class ReportVisitorTest {

    private final StatisticsReportVisitor visitor = new StatisticsReportVisitor(
            Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void eachReportIsRenderedWithItsOwnMetrics() {
        Map<String, BigDecimal> budget = new LinkedHashMap<>();
        budget.put("USD", new BigDecimal("2500.00"));
        List<ReportElement> elements = List.of(
                new ProjectReport(10, 4, 3, 2, 1, budget),
                new ContractReport(5, 2, 2, 1, budget, Map.of("USD", new BigDecimal("250.00"))),
                new UserReport(20, 8, 10, 2, 1));

        List<AdminReport> reports = elements.stream().map(element -> element.accept(visitor)).toList();

        assertEquals(List.of("PROJECTS", "CONTRACTS", "USERS"), reports.stream().map(AdminReport::report).toList());
        assertEquals(10, reports.get(0).metrics().get("total"));
        assertEquals(2, reports.get(1).metrics().get("completed"));
        assertEquals(1L, reports.get(2).metrics().get("suspended"));
    }

    @Test
    void reportsCarryTheVisitorClock() {
        AdminReport report = new ProjectReport(1, 1, 0, 0, 0, Map.of())
                .accept(visitor);

        assertEquals(Instant.parse("2026-01-15T10:00:00Z"), report.generatedAt());
        assertTrue(report.metrics().containsKey("budgetByCurrency"));
    }
}
