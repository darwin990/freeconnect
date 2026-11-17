import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Send } from "lucide-react";
import { proposalSchema, type ProposalFormValues } from "@/features/projects/schemas";
import { useSubmitProposal } from "@/features/proposals/hooks";
import type { Project } from "@/types/models";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { Field, Input, Textarea } from "@/shared/components/FormControls";

export function SubmitProposalForm({ project }: { project: Project }) {
  const submit = useSubmitProposal(project.id);
  const [error, setError] = useState<unknown>(null);
  const [sent, setSent] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ProposalFormValues>({
    resolver: zodResolver(proposalSchema),
    defaultValues: {
      coverLetter: "",
      amount: String(project.budgetAmount),
      currency: project.currency,
      estimatedDays: "30",
    },
  });

  const onSubmit = async (values: ProposalFormValues) => {
    setError(null);
    setSent(false);
    try {
      await submit.mutateAsync({
        coverLetter: values.coverLetter.trim(),
        amount: Number(values.amount),
        currency: values.currency.toUpperCase(),
        estimatedDays: Number(values.estimatedDays),
      });
      reset({
        coverLetter: "",
        amount: String(project.budgetAmount),
        currency: project.currency,
        estimatedDays: "30",
      });
      setSent(true);
    } catch (cause) {
      setError(cause);
    }
  };

  return (
    <form className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
      {error ? <ErrorAlert error={error} title="No se pudo enviar la propuesta" /> : null}
      {sent ? (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-900" role="status">
          Propuesta enviada. El cliente puede evaluarla y aceptarla.
        </div>
      ) : null}
      <Field
        label="Carta de presentación"
        htmlFor="coverLetter"
        hint="Mínimo 30 caracteres. Explica tu enfoque, experiencia y disponibilidad."
        error={errors.coverLetter?.message}
        required
      >
        <Textarea
          id="coverLetter"
          rows={5}
          placeholder="He construido APIs REST con Spring Boot y PostgreSQL durante…"
          invalid={Boolean(errors.coverLetter)}
          {...register("coverLetter")}
        />
      </Field>
      <div className="grid gap-4 sm:grid-cols-3">
        <Field label="Monto" htmlFor="amount" error={errors.amount?.message} required>
          <Input id="amount" type="number" min="0.01" step="0.01" invalid={Boolean(errors.amount)} {...register("amount")} />
        </Field>
        <Field label="Moneda" htmlFor="proposal-currency" error={errors.currency?.message} required>
          <Input id="proposal-currency" maxLength={3} {...register("currency")} />
        </Field>
        <Field label="Días estimados" htmlFor="estimatedDays" error={errors.estimatedDays?.message} required>
          <Input id="estimatedDays" type="number" min="1" max="730" invalid={Boolean(errors.estimatedDays)} {...register("estimatedDays")} />
        </Field>
      </div>
      <div className="flex justify-end">
        <Button type="submit" loading={isSubmitting || submit.isPending} icon={<Send className="size-4" />}>
          Enviar propuesta
        </Button>
      </div>
    </form>
  );
}
