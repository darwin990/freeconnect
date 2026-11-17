import { useEffect, useState } from "react";
import { useFieldArray, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Trash2 } from "lucide-react";
import type { Proposal } from "@/types/models";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { Field, Input, Select } from "@/shared/components/FormControls";
import { Modal } from "@/shared/components/Modal";
import { commissionOptions, milestoneTypeOptions } from "@/shared/utils/labels";
import { todayISO } from "@/shared/utils/format";
import { useAcceptProposal } from "@/features/proposals/hooks";
import { acceptProposalSchema, type AcceptProposalFormValues } from "@/features/proposals/schemas";

export function AcceptProposalDialog({
  proposal,
  projectTitle,
  open,
  onClose,
  onAccepted,
}: {
  proposal: Proposal;
  projectTitle: string;
  open: boolean;
  onClose: () => void;
  onAccepted: (contractId: string) => void;
}) {
  const accept = useAcceptProposal();
  const [error, setError] = useState<unknown>(null);

  const {
    register,
    control,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<AcceptProposalFormValues>({
    resolver: zodResolver(acceptProposalSchema),
    defaultValues: {
      title: projectTitle,
      scope: proposal.coverLetter,
      startDate: todayISO(),
      endDate: todayISO(proposal.estimatedDays || 30),
      commissionPlan: "STANDARD",
      milestoneType: "SINGLE",
      parts: "1",
      milestones: [],
    },
  });

  const { fields, append, remove } = useFieldArray({ control, name: "milestones" });
  const milestoneType = watch("milestoneType");

  useEffect(() => {
    if (!open) return;
    reset({
      title: projectTitle,
      scope: proposal.coverLetter,
      startDate: todayISO(),
      endDate: todayISO(proposal.estimatedDays || 30),
      commissionPlan: "STANDARD",
      milestoneType: "SINGLE",
      parts: "1",
      milestones: [],
    });
    setError(null);
  }, [open, projectTitle, proposal.coverLetter, proposal.estimatedDays, reset]);

  const onSubmit = async (values: AcceptProposalFormValues) => {
    setError(null);
    try {
      const contract = await accept.mutateAsync({
        proposalId: proposal.id,
        payload: {
          title: values.title || undefined,
          scope: values.scope || undefined,
          startDate: values.startDate || undefined,
          endDate: values.endDate || undefined,
          commissionPlan: values.commissionPlan,
          milestoneType: values.milestoneType,
          parts: values.milestoneType === "EQUAL_SPLIT" ? Number(values.parts || 1) : 1,
          milestones:
            values.milestoneType === "CUSTOM"
              ? (values.milestones ?? []).map((milestone) => ({
                  title: milestone.title,
                  description: milestone.description || undefined,
                  amount: Number(milestone.amount),
                  dueDate: milestone.dueDate || undefined,
                }))
              : undefined,
        },
      });
      onAccepted(contract.id);
      onClose();
    } catch (cause) {
      setError(cause);
    }
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Aceptar propuesta y crear contrato"
      description="El contrato se crea en estado activo y el proyecto pasa a en progreso. Los hitos no pueden superar el monto propuesto."
      size="lg"
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancelar
          </Button>
          <Button form="accept-proposal-form" type="submit" loading={isSubmitting || accept.isPending}>
            Aceptar propuesta
          </Button>
        </>
      }
    >
      <form id="accept-proposal-form" className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
        {error ? <ErrorAlert error={error} title="No se pudo crear el contrato" /> : null}

        <div className="rounded-xl bg-ink-50 p-3 text-sm text-ink-700">
          Monto propuesto: <strong>{proposal.amount} {proposal.currency}</strong> · Plazo:{" "}
          <strong>{proposal.estimatedDays} días</strong>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Título del contrato" htmlFor="contract-title" error={errors.title?.message}>
            <Input id="contract-title" {...register("title")} />
          </Field>
          <Field label="Alcance" htmlFor="contract-scope" error={errors.scope?.message}>
            <Input id="contract-scope" {...register("scope")} />
          </Field>
          <Field label="Fecha de inicio" htmlFor="contract-start" error={errors.startDate?.message}>
            <Input id="contract-start" type="date" {...register("startDate")} />
          </Field>
          <Field label="Fecha de fin" htmlFor="contract-end" error={errors.endDate?.message}>
            <Input id="contract-end" type="date" {...register("endDate")} />
          </Field>
          <Field label="Plan de comisión" htmlFor="commission-plan" error={errors.commissionPlan?.message}>
            <Select id="commission-plan" {...register("commissionPlan")}>
              {commissionOptions.map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </Field>
          <Field label="Tipo de hitos" htmlFor="milestone-type" error={errors.milestoneType?.message}>
            <Select id="milestone-type" {...register("milestoneType")}>
              {milestoneTypeOptions.map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </Field>
        </div>

        {milestoneType === "EQUAL_SPLIT" && (
          <Field label="Cantidad de hitos" htmlFor="milestone-parts" error={errors.parts?.message} required>
            <Input id="milestone-parts" type="number" min="1" max="12" {...register("parts")} />
          </Field>
        )}

        {milestoneType === "CUSTOM" && (
          <fieldset className="space-y-3">
            <legend className="text-sm font-medium text-ink-800">Hitos personalizados</legend>
            {fields.map((field, index) => (
              <div key={field.id} className="rounded-xl border border-ink-200 p-3">
                <div className="grid gap-3 sm:grid-cols-2">
                  <Field label={`Título hito ${index + 1}`} htmlFor={`milestone-title-${index}`}>
                    <Input id={`milestone-title-${index}`} {...register(`milestones.${index}.title`)} />
                  </Field>
                  <Field label="Monto" htmlFor={`milestone-amount-${index}`}>
                    <Input
                      id={`milestone-amount-${index}`}
                      type="number"
                      min="0.01"
                      step="0.01"
                      {...register(`milestones.${index}.amount`)}
                    />
                  </Field>
                  <Field label="Fecha límite" htmlFor={`milestone-date-${index}`}>
                    <Input id={`milestone-date-${index}`} type="date" {...register(`milestones.${index}.dueDate`)} />
                  </Field>
                  <Field label="Descripción" htmlFor={`milestone-description-${index}`}>
                    <Input id={`milestone-description-${index}`} {...register(`milestones.${index}.description`)} />
                  </Field>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  className="mt-2 text-red-700"
                  icon={<Trash2 className="size-4" />}
                  onClick={() => remove(index)}
                >
                  Quitar hito
                </Button>
              </div>
            ))}
            <p className="text-xs text-ink-500">
              La suma de los hitos debe ser menor o igual al monto del contrato.
            </p>
            <Button
              variant="outline"
              size="sm"
              icon={<Plus className="size-4" />}
              onClick={() => append({ title: "", amount: "", description: "", dueDate: "" })}
            >
              Agregar hito
            </Button>
          </fieldset>
        )}
      </form>
    </Modal>
  );
}
