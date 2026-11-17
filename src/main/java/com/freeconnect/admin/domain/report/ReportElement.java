package com.freeconnect.admin.domain.report;

/**
 * Elemento visitable de un reporte.
 *
 * <p><b>Patron: Visitor.</b> Los reportes exponen sus datos sin conocer como se
 * agregan; nuevas salidas (CSV, PDF, metricas) agregan un visitante sin tocar
 * los elementos.</p>
 */
public interface ReportElement {

    AdminReport accept(ReportVisitor visitor);
}
