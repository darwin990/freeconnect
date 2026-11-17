import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import {
  ArrowLeft,
  BriefcaseBusiness,
  CheckCircle2,
  ShieldCheck,
  Sparkles,
  Star,
} from "lucide-react";
import { ThemeToggle } from "@/components/ui/ThemeToggle";

export function BrandMark({ compact = false }: { compact?: boolean }) {
  return (
    <Link
      to="/"
      className="inline-flex items-center gap-2 rounded-lg focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600"
    >
      <span className="flex size-9 items-center justify-center rounded-xl bg-brand-700 text-white shadow-sm">
        <BriefcaseBusiness className="size-5" aria-hidden="true" />
      </span>
      {!compact && (
        <span className="text-lg font-bold tracking-tight text-ink-950 dark:text-white">
          FreeConnect
        </span>
      )}
    </Link>
  );
}

const trustPoints = [
  "Propuestas con evaluación y trazabilidad",
  "Contratos con hitos y aprobación por entregables",
  "Pagos simulados y reseñas verificadas",
  "Roles claros para clientes, freelancers y administración",
];

export function AuthShell({
  title,
  subtitle,
  children,
  footer,
}: {
  title: string;
  subtitle: string;
  children: ReactNode;
  footer?: ReactNode;
}) {
  return (
    <main className="min-h-screen bg-ink-50 dark:bg-slate-950 lg:grid lg:grid-cols-[1.05fr_1fr]">
      <section className="relative hidden overflow-hidden bg-ink-950 bg-ink-gradient p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div
          className="absolute -right-24 -top-24 size-96 rounded-full bg-brand-500/30 blur-3xl"
          aria-hidden="true"
        />
        <div
          className="absolute -bottom-32 -left-20 size-96 rounded-full bg-accent-500/20 blur-3xl"
          aria-hidden="true"
        />
        <div className="absolute inset-0 bg-grid opacity-[0.18]" aria-hidden="true" />

        <div className="relative">
          <Link to="/" className="inline-flex items-center gap-2 text-white">
            <span className="flex size-10 items-center justify-center rounded-xl bg-white/10 ring-1 ring-inset ring-white/15">
              <BriefcaseBusiness className="size-5" aria-hidden="true" />
            </span>
            <span className="text-xl font-bold">FreeConnect</span>
          </Link>

          <span className="mt-14 inline-flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1 text-xs font-semibold text-brand-100 ring-1 ring-inset ring-white/15">
            <Sparkles className="size-3.5" aria-hidden="true" />
            Prototipo académico funcional
          </span>
          <h1 className="mt-4 max-w-lg text-4xl font-bold leading-tight">
            Donde el talento y los proyectos se encuentran.
          </h1>
          <p className="mt-4 max-w-md text-ink-200">
            Publica proyectos, recibe propuestas y gestiona contratos con hitos, entregables y
            mensajería en un mismo lugar.
          </p>

          <ul className="mt-10 space-y-3 text-sm text-ink-100">
            {trustPoints.map((item) => (
              <li key={item} className="flex items-start gap-3">
                <CheckCircle2 className="mt-0.5 size-5 shrink-0 text-accent-300" aria-hidden="true" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </div>

        <div className="relative space-y-4">
          <figure className="max-w-md rounded-2xl bg-white/5 p-4 ring-1 ring-inset ring-white/10">
            <div className="flex items-center gap-1 text-amber-300" aria-hidden="true">
              {Array.from({ length: 5 }).map((_, index) => (
                <Star key={index} className="size-4 fill-amber-300" />
              ))}
            </div>
            <blockquote className="mt-2 text-sm leading-relaxed text-ink-100">
              “El flujo de propuesta, contrato por hitos y pago simulado se entiende sin
              explicaciones: ideal para demostrar arquitectura.”
            </blockquote>
            <figcaption className="mt-2 text-xs text-ink-300">
              Guion de demostración del prototipo
            </figcaption>
          </figure>

          <div className="flex items-center gap-2 text-xs text-ink-300">
            <ShieldCheck className="size-4" aria-hidden="true" />
            JWT de corta duración · refresh rotation · sin pagos reales
          </div>
        </div>
      </section>

      <section className="flex min-h-screen items-center justify-center p-4 sm:p-8">
        <div className="w-full max-w-md">
          <div className="mb-5 flex items-center justify-between lg:hidden">
            <BrandMark />
            <div className="flex items-center gap-1.5">
              <ThemeToggle />
              <Link
                to="/"
                className="inline-flex items-center gap-1 rounded-lg px-2 py-1.5 text-sm font-medium text-brand-700 hover:underline"
              >
                <ArrowLeft className="size-4" aria-hidden="true" />
                Inicio
              </Link>
            </div>
          </div>

          <div className="rounded-3xl border border-ink-200 bg-white p-6 shadow-card sm:p-8 dark:border-slate-800 dark:bg-slate-900">
            <div className="mb-6">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-brand-50 px-2.5 py-1 text-xs font-semibold text-brand-800 ring-1 ring-inset ring-brand-100 dark:bg-brand-600/15 dark:text-brand-200 dark:ring-brand-600/30">
                <Sparkles className="size-3.5" aria-hidden="true" />
                FreeConnect MVP
              </span>
              <h2 className="mt-3 text-2xl font-bold tracking-tight text-ink-950 dark:text-white">
                {title}
              </h2>
              <p className="mt-1 text-sm text-ink-500">{subtitle}</p>
            </div>

            {children}

            {footer && (
              <div className="mt-6 border-t border-ink-200 pt-5 text-sm text-ink-500 dark:border-slate-800">
                {footer}
              </div>
            )}
          </div>

          <p className="mt-5 text-center text-xs text-ink-500">
            <Link to="/acerca" className="font-medium hover:text-brand-700">
              Acerca del prototipo
            </Link>{" "}
            · Pagos simulados, sin cobros reales.
          </p>
        </div>
      </section>
    </main>
  );
}
