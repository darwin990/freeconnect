import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { CalendarDays, MapPin, Wallet } from "lucide-react";
import type { Project } from "@/types/models";
import { Badge, StatusBadge } from "@/shared/components/Badge";
import { categoryLabels, modalityLabels, projectStatusLabels } from "@/shared/utils/labels";
import { formatDate, formatMoney } from "@/shared/utils/format";

export function ProjectCard({ project, actions }: { project: Project; actions?: ReactNode }) {
  return (
    <article className="group flex h-full flex-col rounded-2xl border border-ink-200 bg-white p-5 shadow-soft transition hover:-translate-y-0.5 hover:border-brand-300 hover:shadow-lg">
      <div className="flex flex-wrap items-center gap-2">
        <StatusBadge status={project.status} label={projectStatusLabels[project.status]} />
        <Badge tone="neutral">{categoryLabels[project.category]}</Badge>
        <Badge tone="neutral">{modalityLabels[project.modality]}</Badge>
      </div>
      <h3 className="mt-3 text-lg font-semibold leading-snug text-ink-950">
        <Link
          to={`/proyectos/${project.id}`}
          className="rounded focus-visible:outline focus-visible:outline-2 focus-visible:outline-brand-600 hover:text-brand-700"
        >
          {project.title}
        </Link>
      </h3>
      <p className="mt-2 line-clamp-3 text-sm text-ink-500">
        {project.description || "Sin descripción."}
      </p>
      <div className="mt-4 flex flex-wrap gap-x-4 gap-y-2 text-sm text-ink-600">
        <span className="inline-flex items-center gap-1.5">
          <Wallet className="size-4 text-ink-400" aria-hidden="true" />
          <strong>{formatMoney(project.budgetAmount, project.currency)}</strong>
        </span>
        <span className="inline-flex items-center gap-1.5">
          <MapPin className="size-4 text-ink-400" aria-hidden="true" />
          {modalityLabels[project.modality]}
        </span>
        <span className="inline-flex items-center gap-1.5">
          <CalendarDays className="size-4 text-ink-400" aria-hidden="true" />
          {formatDate(project.deadline)}
        </span>
      </div>
      {project.skills?.length ? (
        <div className="mt-4 flex flex-wrap gap-1.5">
          {project.skills.slice(0, 5).map((skill) => (
            <Badge key={skill} tone="brand">
              {skill}
            </Badge>
          ))}
          {project.skills.length > 5 && <Badge>+{project.skills.length - 5}</Badge>}
        </div>
      ) : null}
      <div className="mt-auto flex flex-wrap items-center justify-between gap-2 pt-5">
        <Link
          to={`/proyectos/${project.id}`}
          className="text-sm font-semibold text-brand-700 hover:underline"
        >
          Ver detalle del proyecto
        </Link>
        {actions}
      </div>
    </article>
  );
}
