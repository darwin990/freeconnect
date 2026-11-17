import { ScrollText } from "lucide-react";
import { useAdminAudit } from "@/features/admin/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { PageLoader } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { formatDateTime, shortId } from "@/shared/utils/format";

export function AdminAuditPage() {
  useDocumentTitle("Auditoría");
  const query = useAdminAudit();
  const entries = query.data ?? [];

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Administración"
        title="Auditoría"
        description="Últimos 100 eventos auditados por el backend: acciones sobre proyectos, usuarios, contratos y pagos."
        actions={
          <Button variant="outline" size="sm" onClick={() => void query.refetch()} loading={query.isFetching}>
            Actualizar
          </Button>
        }
      />

      {query.isError ? (
        <ErrorAlert error={query.error} title="No se pudo cargar la auditoría" />
      ) : query.isLoading ? (
        <PageLoader label="Cargando auditoría" />
      ) : entries.length === 0 ? (
        <EmptyState
          title="Sin eventos de auditoría"
          description="Aún no se han registrado acciones auditables."
          icon={<ScrollText className="size-6" aria-hidden="true" />}
        />
      ) : (
        <div className="overflow-x-auto rounded-2xl border border-ink-200 bg-white shadow-soft">
          <table className="min-w-full divide-y divide-ink-200 text-sm">
            <caption className="sr-only">Eventos de auditoría</caption>
            <thead className="bg-ink-50 text-left text-xs uppercase tracking-wide text-ink-500">
              <tr>
                <th scope="col" className="px-4 py-3 font-semibold">Fecha</th>
                <th scope="col" className="px-4 py-3 font-semibold">Acción</th>
                <th scope="col" className="px-4 py-3 font-semibold">Entidad</th>
                <th scope="col" className="px-4 py-3 font-semibold">Actor</th>
                <th scope="col" className="px-4 py-3 font-semibold">Detalles</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {entries.map((entry) => (
                <tr key={entry.id} className="align-top">
                  <td className="whitespace-nowrap px-4 py-3 text-ink-600">
                    {formatDateTime(entry.occurredAt)}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs font-semibold text-ink-900">{entry.action}</td>
                  <td className="px-4 py-3">
                    <p className="font-medium text-ink-800">{entry.entityType}</p>
                    <p className="font-mono text-xs text-ink-500">{shortId(entry.entityId)}</p>
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-ink-600">
                    {entry.actorId ? shortId(entry.actorId) : "sistema"}
                  </td>
                  <td className="max-w-md px-4 py-3 text-xs text-ink-600">
                    {entry.details || "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
