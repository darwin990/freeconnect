import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Download, FileSpreadsheet, Plus, Sparkles } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { ProjectCard } from "@/features/projects/components/ProjectCard";
import { projectsApi } from "@/features/projects/api";
import { useCloneTemplate, useMyProjects, useProjectTemplates } from "@/features/projects/hooks";
import { triggerDownload } from "@/shared/api/client";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { CardSkeleton } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { categoryLabels, modalityLabels, projectStatusLabels } from "@/shared/utils/labels";
import { formatMoney } from "@/shared/utils/format";
import type { ProjectStatus } from "@/types/models";

type Filter = "ALL" | ProjectStatus;
const EDITABLE: ProjectStatus[] = ["DRAFT", "PUBLISHED", "PAUSED"];

export function MyProjectsPage() {
  useDocumentTitle("Mis proyectos");
  const { user } = useAuth();
  const navigate = useNavigate();
  const query = useMyProjects(user?.id);
  const templatesQuery = useProjectTemplates();
  const cloneTemplate = useCloneTemplate();

  const [filter, setFilter] = useState<Filter>("ALL");
  const [exporting, setExporting] = useState(false);
  const [exportError, setExportError] = useState<unknown>(null);

  const projects = useMemo(() => {
    const list = query.data ?? [];
    return filter === "ALL" ? list : list.filter((project) => project.status === filter);
  }, [query.data, filter]);

  const counts = useMemo(() => {
    const list = query.data ?? [];
    const result: Record<string, number> = { ALL: list.length };
    for (const status of Object.keys(projectStatusLabels) as ProjectStatus[]) {
      result[status] = list.filter((project) => project.status === status).length;
    }
    return result;
  }, [query.data]);

  const exportCsv = async () => {
    setExportError(null);
    setExporting(true);
    try {
      const { blob, filename } = await projectsApi.exportMine();
      triggerDownload(blob, filename.endsWith(".csv") ? filename : `${filename}.csv`);
    } catch (error) {
      setExportError(error);
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Cliente"
        title="Mis proyectos"
        description="Gestiona tus borradores, publicaciones y proyectos en curso. La lista se construye desde el catálogo público del backend y se filtra por tu usuario."
        actions={
          <div className="flex flex-wrap gap-2">
            <Button
              variant="outline"
              icon={<Download className="size-4" />}
              loading={exporting}
              onClick={() => void exportCsv()}
            >
              Exportar CSV
            </Button>
            <Link to="/app/proyectos/nuevo">
              <Button icon={<Plus className="size-4" />}>Nuevo proyecto</Button>
            </Link>
          </div>
        }
      />

      {exportError ? <ErrorAlert error={exportError} title="No se pudo exportar el CSV" /> : null}
      {cloneTemplate.isError ? (
        <ErrorAlert error={cloneTemplate.error} title="No se pudo crear el proyecto desde la plantilla" />
      ) : null}

      <div className="flex flex-wrap gap-2" role="tablist" aria-label="Filtrar proyectos por estado">
        {(["ALL", "DRAFT", "PUBLISHED", "PAUSED", "IN_PROGRESS", "FINISHED", "CANCELLED"] as Filter[]).map(
          (value) => (
            <Button
              key={value}
              size="sm"
              variant={filter === value ? "primary" : "outline"}
              role="tab"
              aria-selected={filter === value}
              onClick={() => setFilter(value)}
            >
              {value === "ALL" ? "Todos" : projectStatusLabels[value]} ({counts[value] ?? 0})
            </Button>
          ),
        )}
      </div>

      {query.isError ? (
        <ErrorAlert error={query.error} title="No se pudieron cargar tus proyectos" />
      ) : query.isLoading ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <CardSkeleton key={index} />
          ))}
        </div>
      ) : projects.length === 0 ? (
        <EmptyState
          title={query.data?.length ? "No hay proyectos con ese estado" : "Aún no has creado proyectos"}
          description="Crea un borrador, revísalo y publícalo para empezar a recibir propuestas."
          action={
            <Link to="/app/proyectos/nuevo">
              <Button icon={<Plus className="size-4" />}>Crear proyecto</Button>
            </Link>
          }
        />
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {projects.map((project) => (
              <ProjectCard
                key={project.id}
                project={project}
                actions={
                  EDITABLE.includes(project.status) ? (
                    <Link to={`/app/proyectos/${project.id}/editar`}>
                      <Button size="sm" variant="outline">Editar</Button>
                    </Link>
                  ) : null
                }
              />
            ))}
          </div>
          <p className="text-xs text-ink-500">
            El backend no expone un listado “mis proyectos”: la interfaz consulta el catálogo público y
            filtra por <span className="font-mono">{user?.id?.slice(0, 8)}</span> en el navegador. Se
            recomienda añadir <span className="font-mono">GET /projects/mine</span> en el backend.
          </p>
        </>
      )}

      <Card>
        <CardHeader
          title="Plantillas de proyecto"
          description="Crea un proyecto precargado a partir de una plantilla del backend y ajústalo antes de publicar."
          action={<FileSpreadsheet className="size-5 text-ink-400" aria-hidden="true" />}
        />
        {templatesQuery.isError ? (
          <ErrorAlert error={templatesQuery.error} className="mt-4" title="No se pudieron cargar las plantillas" />
        ) : templatesQuery.isLoading ? (
          <p className="mt-4 text-sm text-ink-500">Cargando plantillas…</p>
        ) : (templatesQuery.data ?? []).length === 0 ? (
          <p className="mt-4 text-sm text-ink-500">No hay plantillas disponibles.</p>
        ) : (
          <ul className="mt-4 grid gap-3 sm:grid-cols-2">
            {(templatesQuery.data ?? []).map((template) => (
              <li key={template.id} className="rounded-xl border border-ink-200 p-4">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="text-xs font-semibold uppercase tracking-wide text-brand-700">
                      {template.name}
                    </p>
                    <p className="mt-1 font-medium text-ink-900">{template.title}</p>
                  </div>
                  <Badge tone="neutral">{categoryLabels[template.category]}</Badge>
                </div>
                <p className="mt-1 text-xs text-ink-500">
                  {modalityLabels[template.modality]} · {formatMoney(template.budget, template.currency)}
                </p>
                <Button
                  className="mt-3"
                  size="sm"
                  variant="outline"
                  icon={<Sparkles className="size-4" />}
                  loading={cloneTemplate.isPending && cloneTemplate.variables === template.id}
                  onClick={() => {
                    cloneTemplate.mutate(template.id, {
                      onSuccess: (project) => navigate(`/app/proyectos/${project.id}/editar`),
                    });
                  }}
                >
                  Usar plantilla
                </Button>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}
