import type { ReactNode } from "react";
import { BarChart3 } from "lucide-react";
import { useAdminReports } from "@/features/admin/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { CardSkeleton } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { formatDateTime } from "@/shared/utils/format";

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

      {query.isError ? (
        <ErrorAlert error={query.error} title="No se pudieron cargar los reportes" />
      ) : query.isLoading ? (
        <div className="grid gap-4 md:grid-cols-2">
          {Array.from({ length: 4 }).map((_, index) => (
            <CardSkeleton key={index} />
          ))}
        </div>
      ) : (query.data ?? []).length === 0 ? (
        <EmptyState
          title="Sin reportes disponibles"
          description="El backend no devolvió reportes agregados."
          icon={<BarChart3 className="size-6" aria-hidden="true" />}
        />
      ) : (
        <div className="grid gap-4 lg:grid-cols-2">
          {(query.data ?? []).map((report, index) => {
            const entries = Object.entries(report.metrics ?? {});
            return (
              <Card key={`${report.report}-${index}`}>
                <CardHeader
                  title={report.report}
                  description={`Generado ${formatDateTime(report.generatedAt)}`}
                  action={<BarChart3 className="size-5 text-ink-400" aria-hidden="true" />}
                />
                {entries.length === 0 ? (
                  <p className="mt-4 text-sm text-ink-500">Sin métricas.</p>
                ) : (
                  <dl className="mt-4 divide-y divide-ink-100 text-sm">
                    {entries.map(([key, value]) => (
                      <div key={key} className="flex items-start justify-between gap-4 py-2.5">
                        <dt className="font-medium text-ink-600">{key}</dt>
                        <dd className="text-right font-semibold text-ink-950">
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

      <Card>
        <CardHeader
          title="Origen de los datos"
          description="Los reportes agregados se generan en el backend con el patrón Visitor. El endpoint GET /admin/reports requiere rol ADMIN tanto en SecurityConfig como en @PreAuthorize."
        />
      </Card>
    </div>
  );
}
