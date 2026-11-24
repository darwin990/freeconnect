import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { ShieldAlert } from "lucide-react";
import { useModerateProject } from "@/features/admin/hooks";
import { ProjectCard } from "@/features/projects/components/ProjectCard";
import { useProject, useProjects } from "@/features/projects/hooks";
import { useDebouncedValue } from "@/shared/hooks/useDebouncedValue";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge, StatusBadge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { Field, Input, Select } from "@/shared/components/FormControls";
import { ConfirmDialog } from "@/shared/components/Modal";
import { PageHeader } from "@/shared/components/PageHeader";
import { CardSkeleton } from "@/shared/components/Spinner";
import { Pagination } from "@/shared/components/Pagination";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { projectStatusLabels, projectStatusOptions } from "@/shared/utils/labels";
import { formatDate, formatMoney } from "@/shared/utils/format";
import type { Project, ProjectStatus } from "@/types/models";

type Pending = { project: Project; status: ProjectStatus };

export function AdminModerationPage() {
  useDocumentTitle("Moderación");
  const [searchParams] = useSearchParams();
  const focusId = searchParams.get("project") ?? undefined;
  const focusQuery = useProject(focusId);
  const [q, setQ] = useState("");
  const debouncedQ = useDebouncedValue(q);
  const [status, setStatus] = useState("PUBLISHED");
  const [page, setPage] = useState(0);
  const query = useProjects({ q: debouncedQ || undefined, status: status || undefined, page, size: 9 });
  const moderate = useModerateProject();
  const [pending, setPending] = useState<Pending | null>(null);

  const actionsFor = (project: Project) => (
    <>
      {!["PAUSED", "CANCELLED", "FINISHED"].includes(project.status) && (
        <Button size="sm" variant="outline" onClick={() => setPending({ project, status: "PAUSED" })}>
          Pausar
        </Button>
      )}
      {!["CANCELLED", "FINISHED"].includes(project.status) && (
        <Button size="sm" variant="danger" onClick={() => setPending({ project, status: "CANCELLED" })}>
          Cancelar
        </Button>
      )}
    </>
  );

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Administración"
        title="Moderación de proyectos"
        description="Pausa o cancela proyectos por moderación. Cada cambio de estado queda registrado en la auditoría."
      />

      {moderate.isError ? <ErrorAlert error={moderate.error} title="No se pudo moderar el proyecto" /> : null}

      {focusId && (
        <Card>
          <CardHeader
            title="Proyecto seleccionado"
            description="Acción directa sobre el proyecto abierto desde el detalle."
            action={<ShieldAlert className="size-5 text-amber-500" aria-hidden="true" />}
          />
          {focusQuery.isLoading ? (
            <p className="mt-4 text-sm text-ink-500">Cargando proyecto…</p>
          ) : focusQuery.isError ? (
            <ErrorAlert error={focusQuery.error} className="mt-4" title="No se pudo cargar el proyecto" />
          ) : focusQuery.data ? (
            <div className="mt-4 space-y-3">
              <div className="flex flex-wrap items-center gap-2">
                <StatusBadge status={focusQuery.data.status} label={projectStatusLabels[focusQuery.data.status]} />
                <Badge tone="neutral">{formatMoney(focusQuery.data.budgetAmount, focusQuery.data.currency)}</Badge>
                <span className="text-xs text-ink-500">Límite {formatDate(focusQuery.data.deadline)}</span>
              </div>
              <h2 className="text-lg font-semibold text-ink-950">{focusQuery.data.title}</h2>
              <p className="text-sm text-ink-600">{focusQuery.data.description || "Sin descripción."}</p>
              <div className="flex flex-wrap items-center gap-2 pt-1">
                {actionsFor(focusQuery.data)}
                <Link to={`/proyectos/${focusQuery.data.id}`} className="text-sm font-semibold text-brand-700 hover:underline">
                  Ver detalle
                </Link>
              </div>
            </div>
          ) : null}
        </Card>
      )}

      <Card>
        <CardHeader title="Buscar proyectos" description="Filtra por texto y estado para localizar el proyecto a moderar." />
        <div className="mt-4 grid gap-4 sm:grid-cols-[1fr_220px]">
          <Field label="Buscar" htmlFor="moderation-q">
            <Input
              id="moderation-q"
              placeholder="Título, descripción o cliente…"
              value={q}
              onChange={(event) => {
                setQ(event.target.value);
                setPage(0);
              }}
            />
          </Field>
          <Field label="Estado" htmlFor="moderation-status">
            <Select
              id="moderation-status"
              value={status}
              onChange={(event) => {
                setStatus(event.target.value);
                setPage(0);
              }}
            >
              <option value="">Todos</option>
              {projectStatusOptions.map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </Field>
        </div>
      </Card>

      {query.isError ? (
        <ErrorAlert error={query.error} title="No se pudieron cargar los proyectos" />
      ) : query.isLoading ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <CardSkeleton key={index} />
          ))}
        </div>
      ) : (query.data?.content.length ?? 0) === 0 ? (
        <EmptyState
          title="No hay proyectos con esos filtros"
          description="Prueba con otro estado o término de búsqueda."
          action={
            <Button
              variant="outline"
              onClick={() => {
                setQ("");
                setStatus("");
                setPage(0);
              }}
            >
              Quitar filtros
            </Button>
          }
        />
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {query.data?.content.map((project) => (
              <ProjectCard key={project.id} project={project} actions={actionsFor(project)} />
            ))}
          </div>
          {query.data && (
            <Pagination
              page={query.data.page}
              totalPages={query.data.totalPages}
              totalElements={query.data.totalElements}
              onPageChange={setPage}
              label="proyectos"
            />
          )}
        </>
      )}

      <ConfirmDialog
        open={Boolean(pending)}
        onClose={() => setPending(null)}
        title={pending?.status === "CANCELLED" ? "Cancelar proyecto por moderación" : "Pausar proyecto"}
        description={
          pending?.status === "CANCELLED"
            ? "El proyecto pasará a CANCELLED. Esta acción se registra en auditoría."
            : "El proyecto pasará a PAUSED y dejará de recibir propuestas hasta que se reanude."
        }
        confirmLabel={pending?.status === "CANCELLED" ? "Cancelar proyecto" : "Pausar proyecto"}
        dangerous={pending?.status === "CANCELLED"}
        loading={moderate.isPending}
        onConfirm={() => {
          if (!pending) return;
          moderate.mutate(
            { id: pending.project.id, status: pending.status },
            { onSuccess: () => setPending(null) },
          );
        }}
      >
        {pending && (
          <p className="text-sm text-ink-600">
            Proyecto: <strong>{pending.project.title}</strong>
          </p>
        )}
      </ConfirmDialog>
    </div>
  );
}
