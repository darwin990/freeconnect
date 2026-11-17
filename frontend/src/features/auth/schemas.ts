import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().min(1, "El correo es obligatorio").email("Introduce un correo válido"),
  password: z.string().min(1, "La contraseña es obligatoria"),
});

export type LoginValues = z.infer<typeof loginSchema>;

export const registerSchema = z
  .object({
    displayName: z
      .string()
      .min(2, "El nombre debe tener al menos 2 caracteres")
      .max(120, "Máximo 120 caracteres"),
    email: z.string().min(1, "El correo es obligatorio").email("Introduce un correo válido").max(180),
    password: z
      .string()
      .min(12, "La contraseña debe tener al menos 12 caracteres")
      .max(100, "Máximo 100 caracteres")
      .regex(/\d/, "La contraseña debe incluir al menos un número"),
    confirmPassword: z.string().min(1, "Confirma la contraseña"),
    role: z.enum(["CLIENT", "FREELANCER"]),
  })
  .refine((values) => values.password === values.confirmPassword, {
    path: ["confirmPassword"],
    message: "Las contraseñas no coinciden",
  });

export type RegisterValues = z.infer<typeof registerSchema>;
