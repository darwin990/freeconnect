import { useMemo } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Plus } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { ProjectCard } from "@/features/projects/components/ProjectCard";
import { ProjectFilters } from "@/features/projects/components/ProjectFilters";
import { normalizeProjectFilters, readProjectFilters, writeProjectFilters } from "@/features/projects/filters";
import { useProjects } from "@/features/projects/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { Pagination } from "@/shared/components/Pagination";
import { CardSkeleton } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";

export function ProjectsPage() {
  useDocumentTitle("Proyectos");
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const filters = useMemo(() => readProjectFilters(searchParams), [searchParams]);
  const query = useProjects(normalizeProjectFilters(filters));

  const applyFilters = (next: typeof filters) => setSearchParams(writeProjectFilters(next));
  const resetFilters = () => setSearchParams(new URLSearchParams({ status: "PUBLISHED" }));

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Explorar"
        title="Proyectos publicados"
        description="Encuentra el siguiente proyecto por texto, categoría, modalidad, presupuesto, habilidad o lenguaje de filtros."
        actions={
          user?.role === "CLIENT" ? (
            <Link to="/app/proyectos/nuevo">
              <Button icon={<Plus className="size-4" />}>Publicar proyecto</Button>
            </Link>
          ) : null
        }
      />

      <ProjectFilters filters={filters} onApply={applyFilters} onReset={resetFilters} />

      {query.isError ? (
        <ErrorAlert error={query.error} title="No se pudieron cargar los proyectos" />
      ) : query.isLoading ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3" aria-busy="true">
          {Array.from({ length: 6 }).map((_, index) => (
            <CardSkeleton key={index} />
          ))}
        </div>
      ) : query.data && query.data.content.length > 0 ? (
        <>
          <div className="flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm text-ink-500">
              {query.data.totalElements} proyecto{query.data.totalElements === 1 ? "" : "s"} encontrado
              {query.data.totalElements === 1 ? "" : "s"}
            </p>
            {query.isFetching && <p className="text-xs text-ink-400">Actualizando…</p>}
          </div>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {query.data.content.map((project) => (
              <ProjectCard key={project.id} project={project} />
            ))}
          </div>
          <Pagination
            page={query.data.page}
            totalPages={query.data.totalPages}
            totalElements={query.data.totalElements}
            onPageChange={(page) => applyFilters({ ...filters, page })}
            label="proyectos"
          />
        </>
      ) : (
        <EmptyState
          title="No hay proyectos con esos filtros"
          description="Prueba a quitar filtros o busca con otras palabras clave. Solo se muestran proyectos publicados por defecto."
          action={
            <Button variant="outline" onClick={resetFilters}>
              Ver proyectos publicados
            </Button>
          }
        />
      )}
    </div>
  );
}
