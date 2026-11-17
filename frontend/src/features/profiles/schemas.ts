import { z } from "zod";

export function createProfileSchema(role: "CLIENT" | "FREELANCER" | "ADMIN") {
  return z
    .object({
      headline: z.string().max(160, "Máximo 160 caracteres").optional(),
      bio: z.string().max(2000, "Máximo 2000 caracteres").optional(),
      hourlyRate: z
        .string()
        .optional()
        .refine(
          (value) => !value || (!Number.isNaN(Number(value)) && Number(value) >= 0),
          "Introduce un monto válido",
        ),
      currency: z.string().length(3, "Usa un código ISO de 3 letras").optional(),
      availability: z.enum(["AVAILABLE", "PART_TIME", "UNAVAILABLE"]).optional(),
      skills: z.string().max(1000, "Máximo 1000 caracteres").optional(),
      companyName: z.string().max(160, "Máximo 160 caracteres").optional(),
      description: z.string().max(2000, "Máximo 2000 caracteres").optional(),
    })
    .superRefine((values, ctx) => {
      if (role === "FREELANCER") {
        if (!values.headline?.trim()) {
          ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["headline"], message: "El titular es obligatorio" });
        }
        if (!values.availability) {
          ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["availability"], message: "Selecciona tu disponibilidad" });
        }
      }
      if (role === "CLIENT" && !values.companyName?.trim()) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["companyName"],
          message: "El nombre de la empresa es obligatorio",
        });
      }
    });
}

export type ProfileFormValues = z.infer<ReturnType<typeof createProfileSchema>>;
