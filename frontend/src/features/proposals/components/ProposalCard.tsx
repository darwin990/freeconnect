import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { CalendarClock, UserRound, Wallet } from "lucide-react";
import type { Proposal } from "@/types/models";
import { Badge, StatusBadge } from "@/shared/components/Badge";
import { proposalStatusLabels } from "@/shared/utils/labels";
import { formatDateTime, formatMoney, shortId } from "@/shared/utils/format";

export function ProposalCard({
  proposal,
  projectTitle,
  actions,
}: {
  proposal: Proposal;
  projectTitle?: string;
  actions?: ReactNode;
}) {
  return (
    <article className="rounded-2xl border border-ink-200 bg-white p-5 shadow-soft">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          {projectTitle && <p className="text-xs font-semibold uppercase tracking-wide text-brand-700">{projectTitle}</p>}
          <p className="mt-1 flex items-center gap-2 text-sm font-medium text-ink-700">
            <UserRound className="size-4 text-ink-400" aria-hidden="true" />
            Freelancer {shortId(proposal.freelancerId)}
          </p>
          <p className="mt-1 text-xs text-ink-500">Enviada {formatDateTime(proposal.createdAt)}</p>
        </div>
        <StatusBadge status={proposal.status} label={proposalStatusLabels[proposal.status]} />
      </div>

      <p className="mt-4 whitespace-pre-line text-sm leading-relaxed text-ink-700">{proposal.coverLetter}</p>

      <dl className="mt-4 grid gap-3 text-sm sm:grid-cols-3">
        <div className="rounded-xl bg-ink-50 p-3">
          <dt className="flex items-center gap-1.5 text-xs text-ink-500">
            <Wallet className="size-3.5" aria-hidden="true" />
            Monto propuesto
          </dt>
          <dd className="mt-1 font-semibold text-ink-900">{formatMoney(proposal.amount, proposal.currency)}</dd>
        </div>
        <div className="rounded-xl bg-ink-50 p-3">
          <dt className="flex items-center gap-1.5 text-xs text-ink-500">
            <CalendarClock className="size-3.5" aria-hidden="true" />
            Plazo
          </dt>
          <dd className="mt-1 font-semibold text-ink-900">{proposal.estimatedDays} días</dd>
        </div>
        <div className="rounded-xl bg-ink-50 p-3">
          <dt className="text-xs text-ink-500">Evaluación</dt>
          <dd className="mt-1 font-semibold text-ink-900">
            {proposal.evaluationScore ? `${proposal.evaluationScore} / 5` : "Sin evaluar"}
          </dd>
        </div>
      </dl>

      {proposal.evaluationNote && (
        <p className="mt-3 rounded-lg bg-amber-50 p-3 text-sm text-amber-900">
          <strong>Nota interna:</strong> {proposal.evaluationNote}
        </p>
      )}

      <div className="mt-4 flex flex-wrap items-center gap-2">
        {actions}
        <Link to={`/proyectos/${proposal.projectId}`} className="text-sm font-semibold text-brand-700 hover:underline">
          Ver proyecto
        </Link>
        <Badge tone="neutral">Propuesta {shortId(proposal.id)}</Badge>
      </div>
    </article>
  );
}
