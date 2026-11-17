import { Link } from "react-router-dom";
import type { ReactNode } from "react";
import {
  ArrowRight,
  BadgeCheck,
  BriefcaseBusiness,
  CheckCircle2,
  FileSignature,
  MessageSquareText,
  Search,
  ShieldCheck,
  Sparkles,
  Star,
  Users,
  Wallet,
} from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { ProjectCard } from "@/features/projects/components/ProjectCard";
import { useProjects } from "@/features/projects/hooks";
import { demoEnabled } from "@/app/demoFlag";
import { Badge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card } from "@/shared/components/Card";
import { ProgressBar } from "@/shared/components/ProgressBar";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";

const features = [
  {
    icon: <BadgeCheck className="size-5" aria-hidden="true" />,
    title: "Propuestas con evaluación",
    description:
      "Los freelancers se postulan y el cliente evalúa, rechaza o acepta para generar el contrato.",
  },
  {
    icon: <FileSignature className="size-5" aria-hidden="true" />,
    title: "Contratos por hitos",
    description:
      "Hitos únicos, reparto igual o personalizados, con entregables, tareas y comentarios.",
  },
  {
    icon: <MessageSquareText className="size-5" aria-hidden="true" />,
    title: "Mensajería y notificaciones",
    description:
      "Conversaciones vinculadas al contrato y bandeja de notificaciones con reintentos.",
  },
  {
    icon: <Wallet className="size-5" aria-hidden="true" />,
    title: "Pagos simulados",
    description:
      "El cliente libera pagos y el freelancer ve el neto tras comisión. Sin cobros reales.",
  },
  {
    icon: <Star className="size-5" aria-hidden="true" />,
    title: "Reseñas verificadas",
    description: "Solo las partes de un contrato completado pueden dejar reseña.",
  },
  {
    icon: <ShieldCheck className="size-5" aria-hidden="true" />,
    title: "Roles y permisos",
    description:
      "Cliente, freelancer y administración con rutas protegidas y acciones adaptadas.",
  },
];

const roles = [
  {
    icon: <BriefcaseBusiness className="size-5" aria-hidden="true" />,
    name: "Cliente",
    tone: "brand" as const,
    description: "Publica proyectos y contrata con hitos claros.",
    bullets: ["Crear y publicar proyectos", "Evaluar propuestas", "Aprobar hitos y liberar pagos"],
  },
  {
    icon: <Users className="size-5" aria-hidden="true" />,
    name: "Freelancer",
    tone: "accent" as const,
    description: "Encuentra proyectos y entrega por hitos.",
    bullets: ["Explorar el catálogo", "Enviar propuestas", "Registrar entregables y cobrar"],
  },
  {
    icon: <ShieldCheck className="size-5" aria-hidden="true" />,
    name: "Administración",
    tone: "info" as const,
    description: "Supervisa la plataforma sin mezclarse con las partes.",
    bullets: ["Reportes agregados", "Moderar proyectos", "Suspender usuarios y auditar"],
  },
];

export function LandingPage() {
  useDocumentTitle("Inicio");
  const { status, user } = useAuth();
  const projectsQuery = useProjects({ status: "PUBLISHED", page: 0, size: 3 });
  const featured = projectsQuery.data?.content ?? [];
  const authenticated = status === "authenticated" && user;

  return (
    <div className="space-y-16 lg:space-y-24">
      <section className="relative overflow-hidden rounded-3xl bg-ink-950 bg-ink-gradient px-6 py-14 text-white shadow-float sm:px-10 lg:px-14 lg:py-20">
        <div className="absolute inset-0 bg-grid opacity-[0.15]" aria-hidden="true" />
        <div
          className="absolute -right-24 -top-24 size-96 rounded-full bg-brand-500/30 blur-3xl"
          aria-hidden="true"
        />
        <div
          className="absolute -bottom-32 -left-16 size-96 rounded-full bg-accent-500/20 blur-3xl"
          aria-hidden="true"
        />

        <div className="relative grid gap-12 lg:grid-cols-[1.15fr_0.85fr] lg:items-center">
          <div>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1 text-xs font-semibold text-brand-100 ring-1 ring-inset ring-white/15">
              <Sparkles className="size-3.5" aria-hidden="true" />
              MVP de FreeConnect · React 19 + Spring Boot
            </span>
            <h1 className="mt-5 max-w-2xl text-4xl font-bold leading-[1.1] tracking-tight sm:text-5xl lg:text-[3.4rem]">
              Publica, propone, contrata y entrega.{" "}
              <span className="text-gradient">Todo en un mismo flujo.</span>
            </h1>
            <p className="mt-5 max-w-xl text-base leading-relaxed text-ink-200">
              FreeConnect conecta clientes y freelancers con propuestas evaluables, contratos por
              hitos, mensajería, notificaciones y pagos simulados. Una experiencia completa sobre
              una API Spring Boot.
            </p>

            <div className="mt-7 flex flex-wrap gap-3">
              <Link to="/proyectos">
                <Button size="lg" icon={<Search className="size-4" />}>
                  Explorar proyectos
                </Button>
              </Link>
              {authenticated ? (
                <Link to="/app">
                  <Button
                    size="lg"
                    variant="outline"
                    className="border-white/30 bg-white/10 text-white hover:bg-white/20"
                  >
                    Ir a mi panel
                  </Button>
                </Link>
              ) : (
                <Link to="/registro">
                  <Button
                    size="lg"
                    variant="outline"
                    className="border-white/30 bg-white/10 text-white hover:bg-white/20"
                  >
                    Crear cuenta gratis
                  </Button>
                </Link>
              )}
            </div>

            <div className="mt-6 flex flex-wrap items-center gap-x-5 gap-y-2 text-xs text-ink-300">
              <span className="inline-flex items-center gap-1.5">
                <CheckCircle2 className="size-4 text-accent-300" aria-hidden="true" />
                Sin pagos reales
              </span>
              <span className="inline-flex items-center gap-1.5">
                <CheckCircle2 className="size-4 text-accent-300" aria-hidden="true" />
                JWT de corta duración
              </span>
              <span className="inline-flex items-center gap-1.5">
                <CheckCircle2 className="size-4 text-accent-300" aria-hidden="true" />
                Rutas protegidas por rol
              </span>
            </div>
          </div>

          <div className="relative">
            <div className="rounded-3xl border border-white/15 bg-white/10 p-4 shadow-float backdrop-blur">
              <div className="rounded-2xl bg-white p-5 text-ink-900 shadow-card">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-xs font-semibold uppercase tracking-wider text-ink-500">
                      Contrato de ejemplo
                    </p>
                    <p className="mt-1 font-semibold text-ink-950">API de reservas · 3 hitos</p>
                  </div>
                  <Badge tone="success" dot>
                    Activo
                  </Badge>
                </div>
                <ProgressBar value={66} label="Progreso del contrato" className="mt-4" />
                <ul className="mt-4 space-y-2 text-sm">
                  {[
                    ["Hito 1", "Aprobado", "success"],
                    ["Hito 2", "Aprobado", "success"],
                    ["Hito 3", "En revisión", "info"],
                  ].map(([name, state, tone]) => (
                    <li
                      key={name}
                      className="flex items-center justify-between gap-3 rounded-xl bg-ink-50 px-3 py-2"
                    >
                      <span className="font-medium text-ink-700">{name}</span>
                      <Badge tone={tone as "success" | "info"}>{state}</Badge>
                    </li>
                  ))}
                </ul>
                <p className="mt-4 text-xs text-ink-500">
                  Pago simulado liberado: <strong className="text-ink-800">USD 1.500</strong>
                </p>
              </div>
            </div>

            <div className="mt-3 grid gap-3 sm:grid-cols-3">
              <HeroStat
                label="Proyectos publicados"
                value={String(projectsQuery.data?.totalElements ?? "—")}
                icon={<BriefcaseBusiness className="size-4" />}
              />
              <HeroStat label="Roles" value="3" icon={<Users className="size-4" />} />
              <HeroStat label="Estado" value="Funcional" icon={<CheckCircle2 className="size-4" />} />
            </div>
          </div>
        </div>
      </section>

      <section className="space-y-6">
        <div className="max-w-2xl">
          <p className="eyebrow">Funcionalidades</p>
          <h2 className="mt-1 text-2xl font-bold tracking-tight text-ink-950 sm:text-3xl dark:text-white">
            Un flujo completo, de la propuesta al pago
          </h2>
          <p className="mt-2 text-sm leading-relaxed text-ink-500">
            Cada pantalla consume endpoints reales del backend y respeta los permisos por rol.
          </p>
        </div>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {features.map((feature) => (
            <Card key={feature.title} variant="interactive" className="h-full">
              <div className="flex size-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700 ring-1 ring-inset ring-brand-100 dark:bg-brand-600/15 dark:text-brand-200 dark:ring-brand-600/30">
                {feature.icon}
              </div>
              <h3 className="mt-4 font-semibold text-ink-950 dark:text-white">{feature.title}</h3>
              <p className="mt-1 text-sm leading-relaxed text-ink-500">{feature.description}</p>
            </Card>
          ))}
        </div>
      </section>

      <section className="space-y-6">
        <div className="max-w-2xl">
          <p className="eyebrow">Tres roles, un mismo sistema</p>
          <h2 className="mt-1 text-2xl font-bold tracking-tight text-ink-950 sm:text-3xl dark:text-white">
            La interfaz se adapta a lo que puedes hacer
          </h2>
        </div>
        <div className="grid gap-4 md:grid-cols-3">
          {roles.map((role) => (
            <Card key={role.name} className="h-full">
              <Badge tone={role.tone} dot>
                {role.name}
              </Badge>
              <h3 className="mt-3 font-semibold text-ink-950 dark:text-white">{role.description}</h3>
              <ul className="mt-3 space-y-2 text-sm text-ink-600 dark:text-slate-300">
                {role.bullets.map((bullet) => (
                  <li key={bullet} className="flex items-start gap-2">
                    <CheckCircle2
                      className="mt-0.5 size-4 shrink-0 text-success-600"
                      aria-hidden="true"
                    />
                    {bullet}
                  </li>
                ))}
              </ul>
            </Card>
          ))}
        </div>
      </section>

      <section className="grid gap-8 lg:grid-cols-[0.85fr_1.15fr] lg:items-center">
        <div>
          <p className="eyebrow">Cómo funciona</p>
          <h2 className="mt-1 text-2xl font-bold tracking-tight text-ink-950 sm:text-3xl dark:text-white">
            Tres pasos para empezar
          </h2>
          <p className="mt-2 text-sm leading-relaxed text-ink-500">
            Clientes y freelancers comparten el mismo flujo, con acciones distintas según su rol.
          </p>
          {demoEnabled && (
            <Link
              to="/demo"
              className="mt-4 inline-flex items-center gap-1 text-sm font-semibold text-brand-700 hover:underline"
            >
              Ver la galería de componentes <ArrowRight className="size-4" aria-hidden="true" />
            </Link>
          )}
        </div>
        <ol className="grid gap-3 sm:grid-cols-3">
          {[
            ["01", "Crea tu cuenta", "Elige rol de cliente o freelancer y completa tu perfil."],
            ["02", "Conecta", "Publica un proyecto o envía una propuesta con monto y plazo."],
            ["03", "Ejecuta", "Gestiona hitos, mensajes, pagos simulados y reseñas."],
          ].map(([step, title, description]) => (
            <li
              key={step}
              className="rounded-2xl border border-ink-200 bg-white p-5 shadow-card transition hover:-translate-y-0.5 hover:shadow-float dark:border-slate-800 dark:bg-slate-900"
            >
              <span className="inline-flex size-8 items-center justify-center rounded-lg bg-brand-50 text-xs font-bold text-brand-700 dark:bg-brand-600/15 dark:text-brand-200">
                {step}
              </span>
              <h3 className="mt-3 font-semibold text-ink-950 dark:text-white">{title}</h3>
              <p className="mt-1 text-sm leading-relaxed text-ink-500">{description}</p>
            </li>
          ))}
        </ol>
      </section>

      {featured.length > 0 && (
        <section className="space-y-5">
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div>
              <p className="eyebrow">Catálogo</p>
              <h2 className="mt-1 text-2xl font-bold tracking-tight text-ink-950 sm:text-3xl dark:text-white">
                Proyectos publicados recientemente
              </h2>
            </div>
            <Link
              to="/proyectos"
              className="inline-flex items-center gap-1 text-sm font-semibold text-brand-700 hover:underline"
            >
              Ver todos <ArrowRight className="size-4" aria-hidden="true" />
            </Link>
          </div>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {featured.map((project) => (
              <ProjectCard key={project.id} project={project} />
            ))}
          </div>
        </section>
      )}

      <section className="relative overflow-hidden rounded-3xl border border-brand-200 bg-brand-50 p-6 sm:p-10 dark:border-brand-800 dark:bg-brand-950/40">
        <div className="absolute inset-0 bg-grid opacity-40" aria-hidden="true" />
        <div className="relative flex flex-wrap items-center justify-between gap-6">
          <div className="max-w-2xl">
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-brand-800 dark:text-brand-200">
              Empieza hoy
            </p>
            <h2 className="mt-1 text-2xl font-bold tracking-tight text-ink-950 sm:text-3xl dark:text-white">
              Tu próximo proyecto está a una propuesta de distancia
            </h2>
            <p className="mt-2 text-sm leading-relaxed text-ink-600 dark:text-slate-300">
              Regístrate como cliente para publicar o como freelancer para postularte. La
              administración supervisa reportes, moderación y auditoría.
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            {authenticated ? (
              <Link to="/app">
                <Button size="lg">Ir a mi panel</Button>
              </Link>
            ) : (
              <>
                <Link to="/registro">
                  <Button size="lg">Crear cuenta</Button>
                </Link>
                <Link to="/login">
                  <Button size="lg" variant="outline">
                    Iniciar sesión
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}

function HeroStat({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div className="rounded-2xl bg-white/10 p-3 ring-1 ring-inset ring-white/15 backdrop-blur">
      <p className="flex items-center gap-1.5 text-[11px] font-medium text-ink-200">
        {icon}
        {label}
      </p>
      <p className="mt-1 text-lg font-bold text-white">{value}</p>
    </div>
  );
}
