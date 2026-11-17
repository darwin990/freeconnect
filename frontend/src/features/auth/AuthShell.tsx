import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { BriefcaseBusiness, CheckCircle2, ShieldCheck, Sparkles } from "lucide-react";

export function BrandMark({ compact = false }: { compact?: boolean }) {
  return (
    <Link to="/" className="inline-flex items-center gap-2 rounded-lg focus-visible:outline focus-visible:outline-2 focus-visible:outline-brand-600">
      <span className="flex size-9 items-center justify-center rounded-xl bg-brand-700 text-white">
        <BriefcaseBusiness className="size-5" aria-hidden="true" />
      </span>
      {!compact && <span className="text-lg font-bold tracking-tight text-ink-950">FreeConnect</span>}
    </Link>
  );
}

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
    <main className="min-h-screen bg-ink-50 lg:grid lg:grid-cols-[1.1fr_1fr]">
      <section className="relative hidden overflow-hidden bg-ink-950 p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="absolute -right-24 -top-24 size-96 rounded-full bg-brand-600/30 blur-3xl" aria-hidden="true" />
        <div className="absolute -bottom-32 -left-20 size-96 rounded-full bg-brand-500/20 blur-3xl" aria-hidden="true" />
        <div className="relative">
          <Link to="/" className="inline-flex items-center gap-2 text-white">
            <span className="flex size-10 items-center justify-center rounded-xl bg-white/10">
              <BriefcaseBusiness className="size-5" aria-hidden="true" />
            </span>
            <span className="text-xl font-bold">FreeConnect</span>
          </Link>
          <h1 className="mt-16 max-w-lg text-4xl font-bold leading-tight">
            Donde el talento y los proyectos se encuentran.
          </h1>
          <p className="mt-4 max-w-md text-ink-200">
            Publica proyectos, recibe propuestas y gestiona contratos con hitos, entregables y
            mensajería en un mismo lugar.
          </p>
          <ul className="mt-10 space-y-4 text-sm text-ink-100">
            {[
              "Propuestas con evaluación y trazabilidad",
              "Contratos con hitos y aprobación por entregables",
              "Pagos simulados y reseñas verificadas",
              "Roles claros para clientes, freelancers y administración",
            ].map((item) => (
              <li key={item} className="flex items-start gap-3">
                <CheckCircle2 className="mt-0.5 size-5 shrink-0 text-emerald-300" aria-hidden="true" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </div>
        <div className="relative flex items-center gap-2 text-xs text-ink-300">
          <ShieldCheck className="size-4" aria-hidden="true" />
          JWT de corta duración · refresh rotation · sin pagos reales
        </div>
      </section>

      <section className="flex min-h-screen items-center justify-center p-4 sm:p-8">
        <div className="w-full max-w-md">
          <div className="mb-6 flex items-center justify-between lg:hidden">
            <BrandMark />
            <Link to="/" className="text-sm font-medium text-brand-700 hover:underline">
              Volver al inicio
            </Link>
          </div>
          <div className="rounded-2xl border border-ink-200 bg-white p-6 shadow-soft sm:p-8">
            <div className="mb-6">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-brand-50 px-2.5 py-1 text-xs font-semibold text-brand-800">
                <Sparkles className="size-3.5" aria-hidden="true" />
                FreeConnect MVP
              </span>
              <h2 className="mt-3 text-2xl font-bold tracking-tight text-ink-950">{title}</h2>
              <p className="mt-1 text-sm text-ink-500">{subtitle}</p>
            </div>
            {children}
            {footer && <div className="mt-6 border-t border-ink-200 pt-5 text-sm text-ink-500">{footer}</div>}
          </div>
        </div>
      </section>
    </main>
  );
}
