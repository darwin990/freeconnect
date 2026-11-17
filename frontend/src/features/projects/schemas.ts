import { z } from "zod";

export const projectSchema = z.object({
  title: z.string().min(1, "El título es obligatorio").max(160, "Máximo 160 caracteres"),
  description: z.string().max(4000, "Máximo 4000 caracteres").optional(),
  category: z.enum(["SOFTWARE", "DESIGN", "MARKETING", "WRITING", "DATA", "OTHER"]),
  budget: z
    .string()
    .min(1, "El presupuesto es obligatorio")
    .refine((value) => Number(value) > 0, "El presupuesto debe ser mayor a cero"),
  currency: z.string().length(3, "Usa un código ISO de 3 letras"),
  modality: z.enum(["REMOTE", "ONSITE", "HYBRID"]),
  deadline: z.string().optional(),
  skills: z.string().max(1000, "Máximo 1000 caracteres").optional(),
});

export type ProjectFormValues = z.infer<typeof projectSchema>;

export const proposalSchema = z.object({
  coverLetter: z
    .string()
    .min(30, "La carta debe tener al menos 30 caracteres")
    .max(3000, "Máximo 3000 caracteres"),
  amount: z
    .string()
    .min(1, "El monto es obligatorio")
    .refine((value) => Number(value) > 0, "El monto debe ser mayor a cero"),
  currency: z.string().length(3, "Usa un código ISO de 3 letras"),
  estimatedDays: z
    .string()
    .min(1, "Indica los días estimados")
    .refine((value) => Number(value) >= 1 && Number(value) <= 730, "Entre 1 y 730 días"),
});

export type ProposalFormValues = z.infer<typeof proposalSchema>;
