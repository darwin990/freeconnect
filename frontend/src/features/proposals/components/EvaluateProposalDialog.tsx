import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import type { Proposal } from "@/types/models";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { Field, Select, Textarea } from "@/shared/components/FormControls";
import { Modal } from "@/shared/components/Modal";
import { useEvaluateProposal } from "@/features/proposals/hooks";
import { evaluateProposalSchema, type EvaluateProposalFormValues } from "@/features/proposals/schemas";

export function EvaluateProposalDialog({
  proposal,
  open,
  onClose,
}: {
  proposal: Proposal;
  open: boolean;
  onClose: () => void;
}) {
  const evaluate = useEvaluateProposal(proposal.projectId);
  const [error, setError] = useState<unknown>(null);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<EvaluateProposalFormValues>({
    resolver: zodResolver(evaluateProposalSchema),
    defaultValues: { score: String(proposal.evaluationScore ?? 3), note: proposal.evaluationNote ?? "" },
  });

  useEffect(() => {
    if (!open) return;
    reset({ score: String(proposal.evaluationScore ?? 3), note: proposal.evaluationNote ?? "" });
    setError(null);
  }, [open, proposal.evaluationScore, proposal.evaluationNote, reset]);

  const onSubmit = async (values: EvaluateProposalFormValues) => {
    setError(null);
    try {
      await evaluate.mutateAsync({
        proposalId: proposal.id,
        score: Number(values.score),
        note: values.note?.trim() || undefined,
      });
      onClose();
    } catch (cause) {
      setError(cause);
    }
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Evaluar propuesta"
      description="La puntuación y la nota son visibles para el cliente; el freelancer no ve la nota interna."
      size="sm"
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancelar
          </Button>
          <Button form="evaluate-form" type="submit" loading={isSubmitting || evaluate.isPending}>
            Guardar evaluación
          </Button>
        </>
      }
    >
      <form id="evaluate-form" className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
        {error ? <ErrorAlert error={error} /> : null}
        <Field label="Puntuación" htmlFor="score" error={errors.score?.message} required>
          <Select id="score" {...register("score")}>
            {[1, 2, 3, 4, 5].map((score) => (
              <option key={score} value={score}>
                {score} / 5
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Nota" htmlFor="note" error={errors.note?.message}>
          <Textarea id="note" placeholder="Fortalezas, dudas o siguientes pasos." {...register("note")} />
        </Field>
      </form>
    </Modal>
  );
}
