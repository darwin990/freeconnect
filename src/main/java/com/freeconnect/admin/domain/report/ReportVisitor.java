package com.freeconnect.admin.domain.report;

/**
 * Visitante de reportes administrativos (double dispatch).
 */
public interface ReportVisitor {

    AdminReport visit(ProjectReport report);

    AdminReport visit(ContractReport report);

    AdminReport visit(UserReport report);
}
