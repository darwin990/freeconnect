import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { AuthShell } from "@/features/auth/AuthShell";
import { loginSchema, type LoginValues } from "@/features/auth/schemas";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { Field, Input } from "@/shared/components/FormControls";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";

export function LoginPage() {
  useDocumentTitle("Iniciar sesión");
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<unknown>(null);
  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const requestedPath =
    typeof (location.state as { from?: unknown } | null)?.from === "string"
      ? ((location.state as { from: string }).from)
      : "/app";

  const onSubmit = async (values: LoginValues) => {
    setError(null);
    try {
      await login(values);
      navigate(requestedPath, { replace: true });
    } catch (cause) {
      setError(cause);
    }
  };

  return (
    <AuthShell
      title="Bienvenido de nuevo"
      subtitle="Accede con tu cuenta para gestionar proyectos, propuestas y contratos."
      footer={
        <p>
          ¿Aún no tienes cuenta?{" "}
          <Link to="/registro" className="font-semibold text-brand-700 hover:underline">
            Crear una cuenta
          </Link>
        </p>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
        {error ? <ErrorAlert error={error} title="No se pudo iniciar sesión" /> : null}
        <Field label="Correo electrónico" htmlFor="email" error={errors.email?.message} required>
          <Input
            id="email"
            type="email"
            autoComplete="email"
            placeholder="tu@correo.com"
            invalid={Boolean(errors.email)}
            aria-describedby={errors.email ? "email-error" : undefined}
            {...register("email")}
          />
        </Field>
        <Field label="Contraseña" htmlFor="password" error={errors.password?.message} required>
          <div className="relative">
            <Input
              id="password"
              type={showPassword ? "text" : "password"}
              autoComplete="current-password"
              invalid={Boolean(errors.password)}
              className="pr-11"
              {...register("password")}
            />
            <button
              type="button"
              onClick={() => setShowPassword((value) => !value)}
              className="absolute inset-y-0 right-0 flex w-11 items-center justify-center rounded-r-xl text-ink-500 hover:text-ink-800 focus-visible:outline focus-visible:outline-2 focus-visible:outline-brand-600"
              aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
            >
              {showPassword ? <EyeOff className="size-5" /> : <Eye className="size-5" />}
            </button>
          </div>
        </Field>
        <Button type="submit" className="w-full" loading={isSubmitting}>
          Entrar
        </Button>
        <p className="text-center text-xs text-ink-500">
          El access token se guarda solo en memoria. El refresh token se guarda en la sesión del
          navegador.
        </p>
      </form>
    </AuthShell>
  );
}
