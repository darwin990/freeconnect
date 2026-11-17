import type { ReactNode } from "react";
import { BarChart3, Clock, Layers, ShieldCheck } from "lucide-react";
import { useAdminReports } from "@/features/admin/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { CardSkeleton } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { formatDateTime } from "@/shared/utils/format";
import { humanizeKey } from "@/shared/utils/labels";
import { cn } from "@/shared/utils/cn";

const reportTones = [
  "bg-brand-50 text-brand-700 ring-brand-100",
  "bg-accent-50 text-accent-700 ring-accent-100",
  "bg-info-50 text-info-700 ring-info-100",
  "bg-warning-50 text-warning-700 ring-warning-100",
];

function MetricValue({ value }: { value: unknown }): ReactNode {
  if (value === null || value === undefined) return "—";
  if (typeof value === "object") {
    return <code className="break-all text-xs text-ink-600">{JSON.stringify(value)}</code>;
  }
  return String(value);
}

export function AdminReportsPage() {
  useDocumentTitle("Reportes");
  const query = useAdminReports();
  const reports = query.data ?? [];
  const totalMetrics = reports.reduce(
    (total, report) => total + Object.keys(report.metrics ?? {}).length,
    0,
  );
  const latest = reports
    .map((report) => report.generatedAt)
    .filter(Boolean)
    .sort()
    .at(-1);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Administración"
        title="Reportes agregados"
        description="Métricas de proyectos, contratos y usuarios generadas por el backend."
        actions={
          <Button
            variant="outline"
            size="sm"
            onClick={() => void query.refetch()}
            loading={query.isFetching}
          >
            Actualizar
          </Button>
        }
      />

      <div className="grid gap-3 sm:grid-cols-3">
        <SummaryStat
          icon={<BarChart3 className="size-4" />}
          label="Reportes"
          value={query.isLoading ? "…" : String(reports.length)}
        />
        <SummaryStat
          icon={<Layers className="size-4" />}
          label="Métricas agregadas"
          value={query.isLoading ? "…" : String(totalMetrics)}
        />
        <SummaryStat
          icon={<Clock className="size-4" />}
          label="Última generación"
          value={latest ? formatDateTime(latest) : "—"}
        />
      </div>

      {query.isError ? (
        <ErrorAlert error={query.error} title="No se pudieron cargar los reportes" />
      ) : query.isLoading ? (
        <div className="grid gap-4 md:grid-cols-2">
          {Array.from({ length: 4 }).map((_, index) => (
            <CardSkeleton key={index} />
          ))}
        </div>
      ) : reports.length === 0 ? (
        <EmptyState
          title="Sin reportes disponibles"
          description="El backend no devolvió reportes agregados."
          icon={<BarChart3 className="size-6" aria-hidden="true" />}
        />
      ) : (
        <div className="grid gap-4 lg:grid-cols-2">
          {reports.map((report, index) => {
            const entries = Object.entries(report.metrics ?? {});
            return (
              <Card key={`${report.report}-${index}`} className="animate-fade-up">
                <CardHeader
                  title={humanizeKey(report.report)}
                  description={`Generado ${formatDateTime(report.generatedAt)}`}
                  action={
                    <span
                      className={cn(
                        "flex size-9 items-center justify-center rounded-xl ring-1 ring-inset",
                        reportTones[index % reportTones.length],
                      )}
                    >
                      <BarChart3 className="size-4" aria-hidden="true" />
                    </span>
                  }
                />
                {entries.length === 0 ? (
                  <p className="mt-4 text-sm text-ink-500">Sin métricas.</p>
                ) : (
                  <dl className="mt-4 divide-y divide-ink-100 text-sm dark:divide-slate-800">
                    {entries.map(([key, value]) => (
                      <div key={key} className="flex items-start justify-between gap-4 py-2.5">
                        <dt className="font-medium text-ink-600 dark:text-slate-300">
                          {humanizeKey(key)}
                        </dt>
                        <dd className="text-right font-semibold text-ink-950 dark:text-white">
                          <MetricValue value={value} />
                        </dd>
                      </div>
                    ))}
                  </dl>
                )}
              </Card>
            );
          })}
        </div>
      )}

      <Card className="border-info-200 bg-info-50/60 dark:border-info-700 dark:bg-info-900/20">
        <div className="flex gap-3">
          <span className="flex size-9 shrink-0 items-center justify-center rounded-xl bg-info-100 text-info-700 ring-1 ring-inset ring-info-200">
            <ShieldCheck className="size-4" aria-hidden="true" />
          </span>
          <div>
            <h2 className="text-sm font-semibold text-ink-950 dark:text-white">
              Origen de los datos
            </h2>
            <p className="mt-1 text-sm leading-relaxed text-ink-600 dark:text-slate-300">
              Los reportes agregados se generan en el backend con el patrón Visitor. El endpoint
              GET /admin/reports requiere rol ADMIN tanto en SecurityConfig como en @PreAuthorize.
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
}

function SummaryStat({
  icon,
  label,
  value,
}: {
  icon: ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-center gap-3 rounded-2xl border border-ink-200 bg-white p-4 shadow-card dark:border-slate-800 dark:bg-slate-900">
      <span className="flex size-9 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-700 ring-1 ring-inset ring-brand-100 dark:bg-brand-600/15 dark:text-brand-200 dark:ring-brand-600/30">
        {icon}
      </span>
      <div className="min-w-0">
        <p className="text-xs font-medium text-ink-500">{label}</p>
        <p className="truncate text-lg font-bold tracking-tight text-ink-950 dark:text-white">
          {value}
        </p>
      </div>
    </div>
  );
}
