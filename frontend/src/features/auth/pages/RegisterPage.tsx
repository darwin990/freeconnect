import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate } from "react-router-dom";
import { BriefcaseBusiness, UserRound } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { AuthShell } from "@/features/auth/AuthShell";
import { registerSchema, type RegisterValues } from "@/features/auth/schemas";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { Field, Input } from "@/shared/components/FormControls";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { cn } from "@/shared/utils/cn";

export function RegisterPage() {
  useDocumentTitle("Crear cuenta");
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<unknown>(null);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      displayName: "",
      email: "",
      password: "",
      confirmPassword: "",
      role: "CLIENT",
    },
  });

  const role = watch("role");

  const onSubmit = async (values: RegisterValues) => {
    setError(null);
    try {
      await registerUser({
        displayName: values.displayName,
        email: values.email,
        password: values.password,
        role: values.role,
      });
      navigate("/app", { replace: true });
    } catch (cause) {
      setError(cause);
    }
  };

  return (
    <AuthShell
      title="Crea tu cuenta"
      subtitle="Elige tu rol. Podrás completar tu perfil después de registrarte."
      footer={
        <p>
          ¿Ya tienes cuenta?{" "}
          <Link to="/login" className="font-semibold text-brand-700 hover:underline">
            Iniciar sesión
          </Link>
        </p>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
        {error ? <ErrorAlert error={error} title="No se pudo crear la cuenta" /> : null}

        <fieldset>
          <legend className="mb-2 text-sm font-medium text-ink-800">Quiero usar FreeConnect como</legend>
          <div className="grid grid-cols-2 gap-3">
            {[
              {
                value: "CLIENT" as const,
                label: "Cliente",
                description: "Publico proyectos",
                icon: BriefcaseBusiness,
              },
              {
                value: "FREELANCER" as const,
                label: "Freelancer",
                description: "Envío propuestas",
                icon: UserRound,
              },
            ].map((option) => {
              const Icon = option.icon;
              const selected = role === option.value;
              return (
                <label
                  key={option.value}
                  className={cn(
                    "flex cursor-pointer flex-col gap-1 rounded-xl border p-3 text-sm transition focus-within:ring-2 focus-within:ring-brand-200",
                    selected
                      ? "border-brand-600 bg-brand-50 text-brand-900"
                      : "border-ink-300 bg-white text-ink-700 hover:border-ink-400",
                  )}
                >
                  <input type="radio" value={option.value} className="sr-only" {...register("role")} />
                  <Icon className="size-5" aria-hidden="true" />
                  <span className="font-semibold">{option.label}</span>
                  <span className="text-xs text-ink-500">{option.description}</span>
                </label>
              );
            })}
          </div>
          {errors.role ? <p className="mt-1 text-sm text-red-700">{errors.role.message}</p> : null}
        </fieldset>

        <Field label="Nombre visible" htmlFor="displayName" error={errors.displayName?.message} required>
          <Input
            id="displayName"
            autoComplete="name"
            placeholder="Ana Pérez"
            invalid={Boolean(errors.displayName)}
            {...register("displayName")}
          />
        </Field>
        <Field label="Correo electrónico" htmlFor="register-email" error={errors.email?.message} required>
          <Input
            id="register-email"
            type="email"
            autoComplete="email"
            placeholder="tu@correo.com"
            invalid={Boolean(errors.email)}
            {...register("email")}
          />
        </Field>
        <Field
          label="Contraseña"
          htmlFor="register-password"
          error={errors.password?.message}
          hint="Mínimo 12 caracteres e incluye al menos un número."
          required
        >
          <Input
            id="register-password"
            type="password"
            autoComplete="new-password"
            invalid={Boolean(errors.password)}
            {...register("password")}
          />
        </Field>
        <Field
          label="Confirmar contraseña"
          htmlFor="confirm-password"
          error={errors.confirmPassword?.message}
          required
        >
          <Input
            id="confirm-password"
            type="password"
            autoComplete="new-password"
            invalid={Boolean(errors.confirmPassword)}
            {...register("confirmPassword")}
          />
        </Field>

        <Button type="submit" className="w-full" loading={isSubmitting}>
          Crear cuenta
        </Button>
        <p className="text-center text-xs text-ink-500">
          Al registrarte aceptas el uso de esta demo. No se realizan cobros reales.
        </p>
      </form>
    </AuthShell>
  );
}
