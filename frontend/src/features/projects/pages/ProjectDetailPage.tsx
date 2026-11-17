import { useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import {
  ArrowLeft,
  CalendarDays,
  ExternalLink,
  FileText,
  MessageSquareText,
  Pencil,
  ShieldAlert,
  Users,
  Wallet,
} from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { ProjectStatusActions } from "@/features/projects/components/ProjectStatusActions";
import { useProject } from "@/features/projects/hooks";
import { ProposalCard } from "@/features/proposals/components/ProposalCard";
import { SubmitProposalForm } from "@/features/proposals/components/SubmitProposalForm";
import { useProjectProposals, useWithdrawProposal } from "@/features/proposals/hooks";
import { isApiError } from "@/shared/api/errors";
import { Alert, ErrorAlert } from "@/shared/components/Alert";
import { Badge, StatusBadge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { ConfirmDialog } from "@/shared/components/Modal";
import { PageLoader } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { categoryLabels, modalityLabels, projectStatusLabels, proposalStatusLabels } from "@/shared/utils/labels";
import { formatDate, formatDateTime, formatMoney, shortId } from "@/shared/utils/format";

export function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const { user } = useAuth();
  const projectQuery = useProject(id);
  const [withdrawOpen, setWithdrawOpen] = useState(false);

  const project = projectQuery.data;
  useDocumentTitle(project?.title ?? "Proyecto");

  const isOwner = Boolean(project && user && project.clientId === user.id);
  const canViewProposals = Boolean(
    user && project && (user.role === "ADMIN" || isOwner || user.role === "FREELANCER"),
  );
  const proposalsQuery = useProjectProposals(id, canViewProposals);
  const withdraw = useWithdrawProposal(id ?? "");

  if (projectQuery.isLoading) return <PageLoader label="Cargando proyecto" />;
  if (projectQuery.isError) {
    return (
      <div className="space-y-4">
        <Link to="/proyectos" className="inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
          <ArrowLeft className="size-4" /> Volver a proyectos
        </Link>
        <ErrorAlert error={projectQuery.error} title="No se pudo cargar el proyecto" />
      </div>
    );
  }
  if (!project) return null;

  const proposals = proposalsQuery.data ?? [];
  const proposalsForbidden = proposalsQuery.isError && isApiError(proposalsQuery.error) && proposalsQuery.error.status === 403;
  const ownProposals = user ? proposals.filter((proposal) => proposal.freelancerId === user.id) : [];
  const activeOwnProposal = ownProposals.find((proposal) => proposal.status === "SUBMITTED");
  const created = Boolean((location.state as { created?: boolean } | null)?.created);
  const updated = Boolean((location.state as { updated?: boolean } | null)?.updated);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <Link to="/proyectos" className="inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
          <ArrowLeft className="size-4" aria-hidden="true" /> Volver a proyectos
        </Link>
        <div className="flex flex-wrap gap-2">
          {isOwner && (
            <Link to={`/app/proyectos/${project.id}/editar`}>
              <Button variant="outline" size="sm" icon={<Pencil className="size-4" />}>
                Editar proyecto
              </Button>
            </Link>
          )}
          {user?.role === "ADMIN" && (
            <Link to={`/app/admin/moderacion?project=${project.id}`}>
              <Button variant="outline" size="sm" icon={<ShieldAlert className="size-4" />}>
                Moderar
              </Button>
            </Link>
          )}
        </div>
      </div>

      {created && <Alert tone="success" title="Proyecto creado">Se guardó como borrador. Publícalo para recibir propuestas.</Alert>}
      {updated && <Alert tone="success" title="Proyecto actualizado">Los cambios se guardaron correctamente.</Alert>}

      <div className="flex flex-wrap items-center gap-2">
        <StatusBadge status={project.status} label={projectStatusLabels[project.status]} />
        <Badge tone="neutral">{categoryLabels[project.category]}</Badge>
        <Badge tone="neutral">{modalityLabels[project.modality]}</Badge>
        {project.publishedAt && <span className="text-xs text-ink-500">Publicado {formatDateTime(project.publishedAt)}</span>}
      </div>

      <h1 className="text-3xl font-bold tracking-tight text-ink-950">{project.title}</h1>

      <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
        <div className="space-y-6">
          <Card>
            <h2 className="text-lg font-semibold text-ink-900">Descripción del proyecto</h2>
            <p className="mt-3 whitespace-pre-line text-sm leading-relaxed text-ink-700">
              {project.description || "El cliente no añadió una descripción."}
            </p>
            {project.skills?.length ? (
              <div className="mt-5">
                <h3 className="text-sm font-semibold text-ink-800">Habilidades requeridas</h3>
                <div className="mt-2 flex flex-wrap gap-1.5">
                  {project.skills.map((skill) => (
                    <Badge key={skill} tone="brand">{skill}</Badge>
                  ))}
                </div>
              </div>
            ) : null}
          </Card>

          {isOwner && (
            <Card>
              <CardHeader
                title="Estado del proyecto"
                description="Las transiciones las valida el backend con el patrón State."
              />
              <div className="mt-4">
                <ProjectStatusActions project={project} />
              </div>
            </Card>
          )}

          {isOwner && (
            <Card>
              <CardHeader
                title="Propuestas recibidas"
                description="Evalúa, rechaza o acepta propuestas para generar el contrato."
                action={
                  <Link to={`/app/propuestas?project=${project.id}`}>
                    <Button size="sm" variant="outline">Gestionar propuestas</Button>
                  </Link>
                }
              />
              {proposalsQuery.isLoading ? (
                <p className="mt-4 text-sm text-ink-500">Cargando propuestas…</p>
              ) : proposalsForbidden ? (
                <p className="mt-4 text-sm text-ink-500">No hay propuestas visibles.</p>
              ) : proposalsQuery.isError ? (
                <ErrorAlert error={proposalsQuery.error} className="mt-4" />
              ) : proposals.length === 0 ? (
                <EmptyState
                  title="Sin propuestas todavía"
                  description="Cuando un freelancer envíe una propuesta aparecerá aquí."
                  icon={<Users className="size-6" aria-hidden="true" />}
                />
              ) : (
                <div className="mt-4 space-y-3">
                  {proposals.slice(0, 3).map((proposal) => (
                    <ProposalCard key={proposal.id} proposal={proposal} />
                  ))}
                  {proposals.length > 3 && (
                    <p className="text-sm text-ink-500">y {proposals.length - 3} propuesta(s) más.</p>
                  )}
                </div>
              )}
            </Card>
          )}

          {user?.role === "FREELANCER" && (
            <Card>
              <CardHeader
                title="Tu propuesta"
                description="Solo puedes mantener una propuesta activa por proyecto."
              />
              {proposalsQuery.isLoading ? (
                <p className="mt-4 text-sm text-ink-500">Comprobando propuestas…</p>
              ) : proposalsQuery.isError && !proposalsForbidden ? (
                <ErrorAlert error={proposalsQuery.error} className="mt-4" />
              ) : ownProposals.length > 0 ? (
                <div className="mt-4 space-y-3">
                  {ownProposals.map((proposal) => (
                    <ProposalCard
                      key={proposal.id}
                      proposal={proposal}
                      actions={
                        proposal.status === "SUBMITTED" ? (
                          <Button size="sm" variant="danger" onClick={() => setWithdrawOpen(true)}>
                            Retirar propuesta
                          </Button>
                        ) : (
                          <Badge tone="neutral">{proposalStatusLabels[proposal.status]}</Badge>
                        )
                      }
                    />
                  ))}
                </div>
              ) : project.status === "PUBLISHED" ? (
                <div className="mt-4">
                  <SubmitProposalForm project={project} />
                </div>
              ) : (
                <Alert tone="info" className="mt-4">
                  Este proyecto no está publicado; no admite propuestas nuevas en este momento.
                </Alert>
              )}
              {activeOwnProposal && (
                <p className="mt-3 text-xs text-ink-500">
                  Ya tienes una propuesta activa. Puedes retirarla para enviar otra si el proyecto sigue publicado.
                </p>
              )}
              <ConfirmDialog
                open={withdrawOpen}
                onClose={() => setWithdrawOpen(false)}
                title="Retirar propuesta"
                description="La propuesta pasará a estado WITHDRAWN y el cliente ya no podrá aceptarla."
                confirmLabel="Retirar"
                dangerous
                loading={withdraw.isPending}
                onConfirm={() => {
                  if (!activeOwnProposal) return;
                  withdraw.mutate(activeOwnProposal.id, {
                    onSuccess: () => setWithdrawOpen(false),
                  });
                }}
              >
                {withdraw.isError ? <ErrorAlert error={withdraw.error} /> : null}
              </ConfirmDialog>
            </Card>
          )}

          {!user && (
            <Card>
              <div className="flex flex-col items-start gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h2 className="text-lg font-semibold text-ink-900">¿Quieres enviar una propuesta?</h2>
                  <p className="mt-1 text-sm text-ink-500">Inicia sesión como freelancer para postularte.</p>
                </div>
                <div className="flex gap-2">
                  <Link to="/login"><Button variant="outline">Iniciar sesión</Button></Link>
                  <Link to="/registro"><Button>Crear cuenta</Button></Link>
                </div>
              </div>
            </Card>
          )}
        </div>

        <aside className="space-y-6">
          <Card>
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink-500">Resumen</h2>
            <dl className="mt-4 space-y-4 text-sm">
              <div className="flex items-start justify-between gap-3">
                <dt className="flex items-center gap-2 text-ink-500"><Wallet className="size-4" /> Presupuesto</dt>
                <dd className="text-right font-bold text-ink-950">{formatMoney(project.budgetAmount, project.currency)}</dd>
              </div>
              <div className="flex items-start justify-between gap-3">
                <dt className="flex items-center gap-2 text-ink-500"><CalendarDays className="size-4" /> Fecha límite</dt>
                <dd className="font-medium text-ink-900">{formatDate(project.deadline)}</dd>
              </div>
              <div className="flex items-start justify-between gap-3">
                <dt className="flex items-center gap-2 text-ink-500"><FileText className="size-4" /> Categoría</dt>
                <dd className="font-medium text-ink-900">{categoryLabels[project.category]}</dd>
              </div>
              <div className="flex items-start justify-between gap-3">
                <dt className="flex items-center gap-2 text-ink-500"><Users className="size-4" /> Cliente</dt>
                <dd className="font-medium text-ink-900">{isOwner ? "Tú" : shortId(project.clientId)}</dd>
              </div>
            </dl>
          </Card>

          <Card>
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink-500">Trazabilidad</h2>
            <p className="mt-3 text-xs text-ink-500">ID del proyecto</p>
            <p className="break-all font-mono text-xs text-ink-700">{project.id}</p>
            <p className="mt-3 text-xs text-ink-500">Creado {formatDateTime(project.createdAt)}</p>
            {isOwner && (
              <Link
                to={`/app/contratos`}
                className="mt-4 inline-flex items-center gap-1 text-sm font-semibold text-brand-700 hover:underline"
              >
                Ir a mis contratos <ExternalLink className="size-3.5" aria-hidden="true" />
              </Link>
            )}
          </Card>

          {isOwner && (
            <Card>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink-500">Mensajería</h2>
              <p className="mt-2 text-sm text-ink-600">
                La conversación se abre desde el contrato, una vez aceptada una propuesta.
              </p>
              <Link to="/app/mensajes" className="mt-3 inline-flex items-center gap-1.5 text-sm font-semibold text-brand-700 hover:underline">
                <MessageSquareText className="size-4" aria-hidden="true" /> Ir a conversaciones
              </Link>
            </Card>
          )}
        </aside>
      </div>
    </div>
  );
}
