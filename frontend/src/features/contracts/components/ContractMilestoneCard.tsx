import { useState } from "react";
import { Check, FilePlus2, MessageSquare, Paperclip, Send, X } from "lucide-react";
import type { Contract, Deliverable, Milestone, Task, Role } from "@/types/models";
import {
  useAddComment,
  useAddDeliverable,
  useAddTask,
  useApproveMilestone,
  useRejectMilestone,
  useSubmitMilestone,
  useToggleTask,
} from "@/features/contracts/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { StatusBadge } from "@/shared/components/Badge";import { Button } from "@/shared/components/Button";
import { Field, Input, Textarea } from "@/shared/components/FormControls";
import { Modal } from "@/shared/components/Modal";
import { ProgressBar } from "@/shared/components/ProgressBar";
import { deliverableStatusLabels, milestoneStatusLabels } from "@/shared/utils/labels";
import { formatDate, formatDateTime, formatMoney, shortId } from "@/shared/utils/format";

export function ContractMilestoneCard({
  contract,
  milestone,
  role,
}: {
  contract: Contract;
  milestone: Milestone;
  role: Role;
}) {
  const [addOpen, setAddOpen] = useState(false);
  const [rejectOpen, setRejectOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState("");
  const [deliverableForm, setDeliverableForm] = useState({ title: "", description: "", url: "" });
  const [error, setError] = useState<unknown>(null);

  const addDeliverable = useAddDeliverable(contract.id);
  const submitMilestone = useSubmitMilestone(contract.id);
  const approveMilestone = useApproveMilestone(contract.id);
  const rejectMilestone = useRejectMilestone(contract.id);

  const isFreelancer = role === "FREELANCER";
  const isClient = role === "CLIENT";
  const contractActive = contract.status === "ACTIVE";

  const handleAddDeliverable = async () => {
    setError(null);
    if (!deliverableForm.title.trim()) {
      setError(new Error("El título del entregable es obligatorio."));
      return;
    }
    try {
      await addDeliverable.mutateAsync({
        milestoneId: milestone.id,
        payload: {
          title: deliverableForm.title.trim(),
          description: deliverableForm.description.trim() || undefined,
          url: deliverableForm.url.trim() || undefined,
        },
      });
      setDeliverableForm({ title: "", description: "", url: "" });
      setAddOpen(false);
    } catch (cause) {
      setError(cause);
    }
  };

  return (
    <section className="rounded-2xl border border-ink-200 bg-white p-5 shadow-soft">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-ink-400">
            Hito {milestone.position + 1}
          </p>
          <h3 className="mt-1 text-lg font-semibold text-ink-950">{milestone.title}</h3>
          {milestone.description && <p className="mt-1 text-sm text-ink-500">{milestone.description}</p>}
        </div>
        <div className="flex flex-col items-end gap-2">
          <StatusBadge status={milestone.status} label={milestoneStatusLabels[milestone.status]} />
          <span className="text-sm font-bold text-ink-900">
            {formatMoney(milestone.amount.amount, milestone.amount.currency)}
          </span>
          <span className="text-xs text-ink-500">Vence {formatDate(milestone.dueDate)}</span>
        </div>
      </div>

      <ProgressBar value={milestone.progress} label="Avance del hito" className="mt-4" />

      {milestone.status === "REJECTED" && (
        <div className="mt-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
          El hito fue rechazado. El backend actual exige una transición adicional que la API no expone para
          reenviarlo; revisa los comentarios o contacta al cliente. Está documentado como limitación.
        </div>
      )}

      <div className="mt-5 space-y-4">
        {(milestone.deliverables ?? []).length === 0 ? (
          <p className="rounded-xl border border-dashed border-ink-300 p-4 text-sm text-ink-500">
            Aún no hay entregables en este hito.
          </p>
        ) : (
          (milestone.deliverables ?? []).map((deliverable) => (
            <DeliverableItem
              key={deliverable.id}
              contractId={contract.id}
              milestoneId={milestone.id}
              deliverable={deliverable}
              role={role}
              contractActive={contractActive}
            />
          ))
        )}
      </div>

      {error ? <ErrorAlert error={error} className="mt-4" /> : null}
      {addDeliverable.isError ? <ErrorAlert error={addDeliverable.error} className="mt-4" /> : null}
      {submitMilestone.isError ? <ErrorAlert error={submitMilestone.error} className="mt-4" /> : null}
      {approveMilestone.isError ? <ErrorAlert error={approveMilestone.error} className="mt-4" /> : null}

      <div className="mt-5 flex flex-wrap gap-2 border-t border-ink-200 pt-4">
        {isFreelancer && contractActive && milestone.status !== "APPROVED" && (
          <Button size="sm" variant="outline" icon={<FilePlus2 className="size-4" />} onClick={() => setAddOpen(true)}>
            Agregar entregable
          </Button>
        )}
        {isFreelancer && contractActive && milestone.status === "IN_PROGRESS" && (milestone.deliverables ?? []).length > 0 && (
          <Button
            size="sm"
            icon={<Send className="size-4" />}
            loading={submitMilestone.isPending}
            onClick={() => submitMilestone.mutate(milestone.id)}
          >
            Enviar a revisión
          </Button>
        )}
        {isClient && milestone.status === "SUBMITTED" && (
          <>
            <Button
              size="sm"
              icon={<Check className="size-4" />}
              loading={approveMilestone.isPending}
              onClick={() => approveMilestone.mutate(milestone.id)}
            >
              Aprobar hito
            </Button>
            <Button size="sm" variant="danger" icon={<X className="size-4" />} onClick={() => setRejectOpen(true)}>
              Rechazar
            </Button>
          </>
        )}
      </div>

      <Modal
        open={addOpen}
        onClose={() => setAddOpen(false)}
        title="Registrar entregable"
        description="Describe la evidencia y enlaza el repositorio o demo si aplica."
        footer={
          <>
            <Button variant="outline" onClick={() => setAddOpen(false)} disabled={addDeliverable.isPending}>
              Cancelar
            </Button>
            <Button onClick={handleAddDeliverable} loading={addDeliverable.isPending}>
              Guardar entregable
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <Field label="Título" htmlFor={`deliverable-title-${milestone.id}`} required>
            <Input
              id={`deliverable-title-${milestone.id}`}
              value={deliverableForm.title}
              onChange={(event) => setDeliverableForm((value) => ({ ...value, title: event.target.value }))}
            />
          </Field>
          <Field label="Descripción" htmlFor={`deliverable-description-${milestone.id}`}>
            <Textarea
              id={`deliverable-description-${milestone.id}`}
              value={deliverableForm.description}
              onChange={(event) => setDeliverableForm((value) => ({ ...value, description: event.target.value }))}
            />
          </Field>
          <Field label="URL" htmlFor={`deliverable-url-${milestone.id}`}>
            <Input
              id={`deliverable-url-${milestone.id}`}
              type="url"
              placeholder="https://…"
              value={deliverableForm.url}
              onChange={(event) => setDeliverableForm((value) => ({ ...value, url: event.target.value }))}
            />
          </Field>
        </div>
      </Modal>

      <Modal
        open={rejectOpen}
        onClose={() => setRejectOpen(false)}
        title="Rechazar hito"
        description="Indica el motivo. El freelancer podrá verlo en el historial del contrato."
        size="sm"
        footer={
          <>
            <Button variant="outline" onClick={() => setRejectOpen(false)} disabled={rejectMilestone.isPending}>
              Cancelar
            </Button>
            <Button
              variant="danger"
              loading={rejectMilestone.isPending}
              disabled={rejectReason.trim().length === 0}
              onClick={() => {
                if (!rejectReason.trim()) return;
                rejectMilestone.mutate(
                  { milestoneId: milestone.id, reason: rejectReason.trim() },
                  { onSuccess: () => { setRejectOpen(false); setRejectReason(""); } },
                );
              }}
            >
              Rechazar hito
            </Button>
          </>
        }
      >
        <Field label="Motivo" htmlFor={`reject-reason-${milestone.id}`} required>
          <Textarea
            id={`reject-reason-${milestone.id}`}
            value={rejectReason}
            maxLength={500}
            onChange={(event) => setRejectReason(event.target.value)}
            placeholder="Falta documentación, el alcance no coincide…"
          />
        </Field>
        {rejectMilestone.isError ? <ErrorAlert error={rejectMilestone.error} className="mt-3" /> : null}
      </Modal>
    </section>
  );
}

function DeliverableItem({
  contractId,
  milestoneId,
  deliverable,
  role,
  contractActive,
}: {
  contractId: string;
  milestoneId: string;
  deliverable: Deliverable;
  role: Role;
  contractActive: boolean;
}) {
  const [taskTitle, setTaskTitle] = useState("");
  const [comment, setComment] = useState("");
  const [error, setError] = useState<unknown>(null);
  const addTask = useAddTask(contractId);
  const toggleTask = useToggleTask(contractId);
  const addComment = useAddComment(contractId);

  const handleTask = async () => {
    setError(null);
    if (!taskTitle.trim()) return;
    try {
      await addTask.mutateAsync({ milestoneId, deliverableId: deliverable.id, title: taskTitle.trim() });
      setTaskTitle("");
    } catch (cause) {
      setError(cause);
    }
  };

  const handleComment = async () => {
    setError(null);
    if (!comment.trim()) return;
    try {
      await addComment.mutateAsync({ milestoneId, deliverableId: deliverable.id, body: comment.trim() });
      setComment("");
    } catch (cause) {
      setError(cause);
    }
  };

  return (
    <article className="rounded-xl border border-ink-200 bg-ink-50/50 p-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div>
          <h4 className="font-semibold text-ink-900">{deliverable.title}</h4>
          {deliverable.description && <p className="mt-1 text-sm text-ink-500">{deliverable.description}</p>}
          {deliverable.url && (
            <a
              href={deliverable.url}
              target="_blank"
              rel="noreferrer noopener"
              className="mt-1 inline-flex items-center gap-1 text-sm font-medium text-brand-700 hover:underline"
            >
              <Paperclip className="size-3.5" aria-hidden="true" /> Ver evidencia
            </a>
          )}
          <p className="mt-1 text-xs text-ink-400">Registrado {formatDateTime(deliverable.submittedAt)}</p>
        </div>
        <StatusBadge status={deliverable.status} label={deliverableStatusLabels[deliverable.status]} />
      </div>

      <div className="mt-3">
        <p className="mb-1 text-xs font-semibold uppercase tracking-wide text-ink-500">Tareas</p>
        {(deliverable.tasks ?? []).length === 0 ? (
          <p className="text-sm text-ink-500">Sin tareas.</p>
        ) : (
          <ul className="space-y-1.5">
            {(deliverable.tasks ?? []).map((task: Task) => (
              <li key={task.id}>
                <label className="flex items-center gap-2 text-sm text-ink-700">
                  <input
                    type="checkbox"
                    checked={task.done}
                    disabled={role !== "FREELANCER" || !contractActive || toggleTask.isPending}
                    onChange={(event) =>
                      toggleTask.mutate({
                        milestoneId,
                        deliverableId: deliverable.id,
                        taskId: task.id,
                        done: event.target.checked,
                      })
                    }
                    className="size-4 rounded border-ink-300 text-brand-700 focus:ring-brand-500"
                  />
                  <span className={task.done ? "line-through text-ink-400" : ""}>{task.title}</span>
                </label>
              </li>
            ))}
          </ul>
        )}
        {role === "FREELANCER" && contractActive && (
          <form
            className="mt-2 flex gap-2"
            onSubmit={(event) => {
              event.preventDefault();
              void handleTask();
            }}
          >
            <Input
              aria-label="Título de la nueva tarea"
              placeholder="Nueva tarea"
              value={taskTitle}
              maxLength={180}
              onChange={(event) => setTaskTitle(event.target.value)}
              className="h-9"
            />
            <Button type="submit" size="sm" variant="outline" disabled={!taskTitle.trim()} loading={addTask.isPending}>
              Agregar
            </Button>
          </form>
        )}
      </div>

      <div className="mt-3">
        <p className="mb-1 text-xs font-semibold uppercase tracking-wide text-ink-500">Comentarios</p>
        {(deliverable.comments ?? []).length === 0 ? (
          <p className="text-sm text-ink-500">Sin comentarios.</p>
        ) : (
          <ul className="space-y-2">
            {(deliverable.comments ?? []).map((entry) => (
              <li key={entry.id} className="rounded-lg bg-white p-2 text-sm ring-1 ring-ink-200">
                <p className="text-ink-700">{entry.body}</p>
                <p className="mt-1 text-xs text-ink-400">
                  {shortId(entry.authorId)} · {formatDateTime(entry.createdAt)}
                </p>
              </li>
            ))}
          </ul>
        )}
        <form
          className="mt-2 flex gap-2"
          onSubmit={(event) => {
            event.preventDefault();
            void handleComment();
          }}
        >
          <Textarea
            aria-label="Nuevo comentario"
            rows={1}
            placeholder="Escribe un comentario…"
            value={comment}
            maxLength={2000}
            onChange={(event) => setComment(event.target.value)}
            className="min-h-9 text-sm"
          />
          <Button type="submit" size="sm" variant="outline" icon={<MessageSquare className="size-4" />} disabled={!comment.trim()} loading={addComment.isPending}>
            Comentar
          </Button>
        </form>
      </div>

      {error ? <ErrorAlert error={error} className="mt-3" /> : null}
      {toggleTask.isError ? <ErrorAlert error={toggleTask.error} className="mt-3" /> : null}
    </article>
  );
}
