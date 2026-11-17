package com.freeconnect.admin.application;

import com.freeconnect.admin.domain.report.AdminReport;
import com.freeconnect.admin.domain.report.ContractReport;
import com.freeconnect.admin.domain.report.ProjectReport;
import com.freeconnect.admin.domain.report.ReportVisitor;
import com.freeconnect.admin.domain.report.UserReport;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Visitante que traduce los agregados a un reporte con metricas.
 */
public class StatisticsReportVisitor implements ReportVisitor {

    private final Clock clock;

    public StatisticsReportVisitor(Clock clock) {
        this.clock = clock;
    }

    @Override
    public AdminReport visit(ProjectReport report) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("total", report.total());
        metrics.put("published", report.published());
        metrics.put("inProgress", report.inProgress());
        metrics.put("finished", report.finished());
        metrics.put("cancelled", report.cancelled());
        metrics.put("budgetByCurrency", report.budgetByCurrency());
        return new AdminReport("PROJECTS", metrics, Instant.now(clock));
    }

    @Override
    public AdminReport visit(ContractReport report) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("total", report.total());
        metrics.put("active", report.active());
        metrics.put("completed", report.completed());
        metrics.put("cancelled", report.cancelled());
        metrics.put("volumeByCurrency", report.volumeByCurrency());
        metrics.put("commissionByCurrency", report.commissionByCurrency());
        return new AdminReport("CONTRACTS", metrics, Instant.now(clock));
    }

    @Override
    public AdminReport visit(UserReport report) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("total", report.total());
        metrics.put("clients", report.clients());
        metrics.put("freelancers", report.freelancers());
        metrics.put("admins", report.admins());
        metrics.put("suspended", report.suspended());
        return new AdminReport("USERS", metrics, Instant.now(clock));
    }
}
