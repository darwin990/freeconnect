import { z } from "zod";

export const acceptProposalSchema = z
  .object({
    title: z.string().max(180, "Máximo 180 caracteres").optional(),
    scope: z.string().max(4000, "Máximo 4000 caracteres").optional(),
    startDate: z.string().optional(),
    endDate: z.string().optional(),
    commissionPlan: z.enum(["STANDARD", "PREMIUM"]),
    milestoneType: z.enum(["SINGLE", "EQUAL_SPLIT", "CUSTOM"]),
    parts: z.string().optional(),
    milestones: z
      .array(
        z.object({
          title: z.string().min(1, "Título obligatorio").max(180, "Máximo 180 caracteres"),
          description: z.string().max(4000, "Máximo 4000 caracteres").optional(),
          amount: z
            .string()
            .min(1, "Monto obligatorio")
            .refine((value) => Number(value) > 0, "Monto mayor a cero"),
          dueDate: z.string().optional(),
        }),
      )
      .optional(),
  })
  .superRefine((values, ctx) => {
    if (values.startDate && values.endDate && values.endDate < values.startDate) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["endDate"], message: "La fecha fin no puede ser anterior al inicio" });
    }
    if (values.milestoneType === "EQUAL_SPLIT") {
      const parts = Number(values.parts ?? "1");
      if (!Number.isInteger(parts) || parts < 1 || parts > 12) {
        ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["parts"], message: "Entre 1 y 12 hitos" });
      }
    }
    if (values.milestoneType === "CUSTOM" && (!values.milestones || values.milestones.length === 0)) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["milestones"], message: "Define al menos un hito" });
    }
  });

export type AcceptProposalFormValues = z.infer<typeof acceptProposalSchema>;

export const evaluateProposalSchema = z.object({
  score: z
    .string()
    .min(1, "Selecciona una puntuación")
    .refine((value) => Number(value) >= 1 && Number(value) <= 5, "Entre 1 y 5"),
  note: z.string().max(1000, "Máximo 1000 caracteres").optional(),
});

export type EvaluateProposalFormValues = z.infer<typeof evaluateProposalSchema>;
