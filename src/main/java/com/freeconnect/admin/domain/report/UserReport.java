package com.freeconnect.admin.domain.report;

/**
 * Datos agregados de usuarios.
 */
public record UserReport(long total, long clients, long freelancers, long admins, long suspended)
        implements ReportElement {

    @Override
    public AdminReport accept(ReportVisitor visitor) {
        return visitor.visit(this);
    }
}
