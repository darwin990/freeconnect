import { useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { ArrowLeft, CheckCircle2, ClipboardList, Star, XCircle } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { useMyProjects, useProject } from "@/features/projects/hooks";
import { AcceptProposalDialog } from "@/features/proposals/components/AcceptProposalDialog";
import { EvaluateProposalDialog } from "@/features/proposals/components/EvaluateProposalDialog";
import { ProposalCard } from "@/features/proposals/components/ProposalCard";
import { useProjectProposals, useRejectProposal } from "@/features/proposals/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge, StatusBadge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { ConfirmDialog } from "@/shared/components/Modal";
import { PageHeader } from "@/shared/components/PageHeader";
import { PageLoader } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { proposalStatusLabels, projectStatusLabels } from "@/shared/utils/labels";
import { formatDate, formatMoney } from "@/shared/utils/format";
import type { Proposal, ProposalStatus } from "@/types/models";

type Filter = "ALL" | ProposalStatus;

export function ProposalsPage() {
  useDocumentTitle("Propuestas");
  const { user } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const projectId = searchParams.get("project") ?? undefined;

  const myProjects = useMyProjects(user?.id);
  const projectQuery = useProject(projectId);
  const project = projectQuery.data;
  const isOwner = Boolean(project && user && project.clientId === user.id);
  const proposalsQuery = useProjectProposals(projectId, Boolean(projectId) && isOwner);
  const reject = useRejectProposal(projectId ?? "");

  const [filter, setFilter] = useState<Filter>("ALL");
  const [acceptTarget, setAcceptTarget] = useState<Proposal | null>(null);
  const [evaluateTarget, setEvaluateTarget] = useState<Proposal | null>(null);
  const [rejectTarget, setRejectTarget] = useState<Proposal | null>(null);

  const proposals = useMemo(() => {
    const list = proposalsQuery.data ?? [];
    return filter === "ALL" ? list : list.filter((proposal) => proposal.status === filter);
  }, [proposalsQuery.data, filter]);

  const counts = useMemo(() => {
    const list = proposalsQuery.data ?? [];
    const result: Record<string, number> = { ALL: list.length };
    for (const status of Object.keys(proposalStatusLabels) as ProposalStatus[]) {
      result[status] = list.filter((proposal) => proposal.status === status).length;
    }
    return result;
  }, [proposalsQuery.data]);

  if (projectId && projectQuery.isLoading) return <PageLoader label="Cargando propuestas del proyecto" />;

  if (!projectId) {
    return (
      <div className="space-y-6">
        <PageHeader
          title="Gestión de propuestas"
          description="Selecciona uno de tus proyectos para revisar, evaluar y aceptar propuestas."
        />
        {myProjects.isError ? (
          <ErrorAlert error={myProjects.error} title="No se pudieron cargar tus proyectos" />
        ) : myProjects.isLoading ? (
          <PageLoader label="Cargando proyectos" />
        ) : (myProjects.data ?? []).length === 0 ? (
          <EmptyState
            title="No tienes proyectos"
            description="Crea un proyecto y publícalo para recibir propuestas."
            icon={<ClipboardList className="size-6" aria-hidden="true" />}
            action={
              <Link to="/app/proyectos/nuevo">
                <Button>Crear proyecto</Button>
              </Link>
            }
          />
        ) : (
          <ul className="grid gap-3 md:grid-cols-2">
            {(myProjects.data ?? []).map((item) => (
              <li key={item.id} className="rounded-2xl border border-ink-200 bg-white p-4 shadow-soft">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div>
                    <StatusBadge status={item.status} label={projectStatusLabels[item.status]} />
                    <h2 className="mt-2 font-semibold text-ink-950">{item.title}</h2>
                    <p className="mt-1 text-xs text-ink-500">
                      {formatMoney(item.budgetAmount, item.currency)} · Límite {formatDate(item.deadline)}
                    </p>
                  </div>
                  <Link
                    to={`/app/propuestas?project=${item.id}`}
                    className="text-sm font-semibold text-brand-700 hover:underline"
                  >
                    Ver propuestas
                  </Link>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    );
  }

  if (projectQuery.isError) {
    return (
      <div className="space-y-4">
        <Link to="/app/propuestas" className="inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
          <ArrowLeft className="size-4" aria-hidden="true" /> Volver a proyectos
        </Link>
        <ErrorAlert error={projectQuery.error} title="No se pudo cargar el proyecto" />
      </div>
    );
  }

  if (!project) return null;

  if (!isOwner) {
    return (
      <div className="space-y-4">
        <Link to="/app/propuestas" className="inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
          <ArrowLeft className="size-4" aria-hidden="true" /> Volver
        </Link>
        <ErrorAlert
          error={new Error("Solo el cliente propietario del proyecto puede gestionar sus propuestas.")}
          title="Acceso restringido"
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <Link to="/app/propuestas" className="inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
          <ArrowLeft className="size-4" aria-hidden="true" /> Volver a proyectos
        </Link>
        <Link to={`/proyectos/${project.id}`} className="text-sm font-semibold text-brand-700 hover:underline">
          Ver proyecto público
        </Link>
      </div>

      <PageHeader
        eyebrow="Propuestas recibidas"
        title={project.title}
        description={`Estado del proyecto: ${projectStatusLabels[project.status]} · Presupuesto ${formatMoney(project.budgetAmount, project.currency)}`}
      />

      {reject.isError ? <ErrorAlert error={reject.error} title="No se pudo rechazar la propuesta" /> : null}

      <div className="flex flex-wrap gap-2" role="tablist" aria-label="Filtrar propuestas por estado">
        {(["ALL", "SUBMITTED", "ACCEPTED", "REJECTED", "WITHDRAWN"] as Filter[]).map((value) => (
          <Button
            key={value}
            size="sm"
            variant={filter === value ? "primary" : "outline"}
            role="tab"
            aria-selected={filter === value}
            onClick={() => setFilter(value)}
          >
            {value === "ALL" ? "Todas" : proposalStatusLabels[value]} ({counts[value] ?? 0})
          </Button>
        ))}
      </div>

      {proposalsQuery.isError ? (
        <ErrorAlert error={proposalsQuery.error} title="No se pudieron cargar las propuestas" />
      ) : proposalsQuery.isLoading ? (
        <PageLoader label="Cargando propuestas" />
      ) : proposals.length === 0 ? (
        <EmptyState
          title="No hay propuestas en esta vista"
          description="Cuando un freelancer envíe una propuesta aparecerá aquí."
          icon={<ClipboardList className="size-6" aria-hidden="true" />}
        />
      ) : (
        <div className="space-y-4">
          {proposals.map((proposal) => (
            <ProposalCard
              key={proposal.id}
              proposal={proposal}
              actions={
                proposal.status === "SUBMITTED" ? (
                  <>
                    <Button
                      size="sm"
                      variant="outline"
                      icon={<Star className="size-4" />}
                      onClick={() => setEvaluateTarget(proposal)}
                    >
                      Evaluar
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      icon={<XCircle className="size-4" />}
                      onClick={() => setRejectTarget(proposal)}
                    >
                      Rechazar
                    </Button>
                    <Button
                      size="sm"
                      icon={<CheckCircle2 className="size-4" />}
                      onClick={() => setAcceptTarget(proposal)}
                    >
                      Aceptar y contratar
                    </Button>
                  </>
                ) : (
                  <Badge tone="neutral">Sin acciones disponibles</Badge>
                )
              }
            />
          ))}
        </div>
      )}

      <Card>
        <CardHeader
          title="Reglas de aceptación"
          description="Aceptar una propuesta crea el contrato y pone el proyecto en progreso. La suma de hitos no puede superar el monto propuesto."
        />
        <p className="mt-3 text-sm text-ink-600">
          Al aceptar se abre el diálogo de contrato, donde puedes elegir plan de comisión, fechas y tipo
          de hitos (único, reparto igual o personalizado).
        </p>
      </Card>

      {acceptTarget && (
        <AcceptProposalDialog
          proposal={acceptTarget}
          projectTitle={project.title}
          open={Boolean(acceptTarget)}
          onClose={() => setAcceptTarget(null)}
          onAccepted={(contractId) => {
            setAcceptTarget(null);
            navigate(`/app/contratos/${contractId}`);
          }}
        />
      )}

      {evaluateTarget && (
        <EvaluateProposalDialog
          proposal={evaluateTarget}
          open={Boolean(evaluateTarget)}
          onClose={() => setEvaluateTarget(null)}
        />
      )}

      <ConfirmDialog
        open={Boolean(rejectTarget)}
        onClose={() => setRejectTarget(null)}
        title="Rechazar propuesta"
        description="La propuesta pasará a estado REJECTED. El freelancer podrá ver el resultado."
        confirmLabel="Rechazar propuesta"
        dangerous
        loading={reject.isPending}
        onConfirm={() => {
          if (!rejectTarget) return;
          reject.mutate(rejectTarget.id, { onSuccess: () => setRejectTarget(null) });
        }}
      >
        {reject.isError ? <ErrorAlert error={reject.error} /> : null}
      </ConfirmDialog>
    </div>
  );
}
