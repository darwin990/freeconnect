import { useState } from "react";
import { ArrowRight, Pause, Play, Send, Square, XCircle } from "lucide-react";
import { useChangeProjectStatus } from "@/features/projects/hooks";
import { projectTransitions } from "@/features/projects/transitions";
import type { Project, ProjectStatus } from "@/types/models";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { ConfirmDialog } from "@/shared/components/Modal";
import { projectStatusLabels } from "@/shared/utils/labels";

function labelFor(project: Project, target: ProjectStatus): string {
  if (target === "PUBLISHED") return project.status === "PAUSED" ? "Reanudar" : "Publicar";
  if (target === "PAUSED") return "Pausar";
  if (target === "IN_PROGRESS") return "Iniciar trabajo";
  if (target === "FINISHED") return "Finalizar";
  if (target === "CANCELLED") return "Cancelar";
  return projectStatusLabels[target];
}

function iconFor(target: ProjectStatus) {
  if (target === "PUBLISHED") return <Play className="size-4" />;
  if (target === "PAUSED") return <Pause className="size-4" />;
  if (target === "CANCELLED") return <XCircle className="size-4" />;
  if (target === "FINISHED") return <Square className="size-4" />;
  return <Send className="size-4" />;
}

export function ProjectStatusActions({ project }: { project: Project }) {
  const changeStatus = useChangeProjectStatus(project.id);
  const [cancelOpen, setCancelOpen] = useState(false);
  const allowed = projectTransitions[project.status] ?? [];

  if (allowed.length === 0) {
    return <p className="text-sm text-ink-500">El proyecto está en un estado final y no admite más transiciones.</p>;
  }

  const change = (target: ProjectStatus) => {
    if (target === "CANCELLED") {
      setCancelOpen(true);
      return;
    }
    changeStatus.mutate(target);
  };

  return (
    <div className="space-y-3">
      {changeStatus.isError ? <ErrorAlert error={changeStatus.error} /> : null}
      <div className="flex flex-wrap gap-2">
        {allowed.map((target) => (
          <Button
            key={target}
            size="sm"
            variant={target === "CANCELLED" ? "danger" : "outline"}
            icon={iconFor(target)}
            loading={changeStatus.isPending && changeStatus.variables === target}
            disabled={changeStatus.isPending}
            onClick={() => change(target)}
          >
            {labelFor(project, target)}
            {target === "IN_PROGRESS" && <ArrowRight className="size-4" aria-hidden="true" />}
          </Button>
        ))}
      </div>
      <ConfirmDialog
        open={cancelOpen}
        onClose={() => setCancelOpen(false)}
        title="Cancelar proyecto"
        description="El proyecto pasará a estado CANCELLED. Esta acción no se puede deshacer desde la interfaz."
        confirmLabel="Cancelar proyecto"
        cancelLabel="Volver"
        dangerous
        loading={changeStatus.isPending}
        onConfirm={() => {
          changeStatus.mutate("CANCELLED", {
            onSuccess: () => setCancelOpen(false),
          });
        }}
      />
    </div>
  );
}
