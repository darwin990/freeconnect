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
import { Button } from "@/shared/components/Button";
import { Card } from "@/shared/components/Card";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";

const features = [
  {
    icon: <BadgeCheck className="size-5" aria-hidden="true" />,
    title: "Propuestas con evaluación",
    description: "Los freelancers se postulan y el cliente evalúa, rechaza o acepta para generar el contrato.",
  },
  {
    icon: <FileSignature className="size-5" aria-hidden="true" />,
    title: "Contratos por hitos",
    description: "Hitos únicos, reparto igual o personalizados, con entregables, tareas y comentarios.",
  },
  {
    icon: <MessageSquareText className="size-5" aria-hidden="true" />,
    title: "Mensajería y notificaciones",
    description: "Conversaciones vinculadas al contrato y bandeja de notificaciones con reintentos.",
  },
  {
    icon: <Wallet className="size-5" aria-hidden="true" />,
    title: "Pagos simulados",
    description: "El cliente libera pagos y el freelancer ve el neto tras comisión. Sin cobros reales.",
  },
  {
    icon: <Star className="size-5" aria-hidden="true" />,
    title: "Reseñas verificadas",
    description: "Solo las partes de un contrato completado pueden dejar reseña.",
  },
  {
    icon: <ShieldCheck className="size-5" aria-hidden="true" />,
    title: "Roles y permisos",
    description: "Cliente, freelancer y administración con rutas protegidas y acciones adaptadas.",
  },
];

export function LandingPage() {
  useDocumentTitle("Inicio");
  const { status, user } = useAuth();
  const projectsQuery = useProjects({ status: "PUBLISHED", page: 0, size: 3 });
  const featured = projectsQuery.data?.content ?? [];
  const authenticated = status === "authenticated" && user;

  return (
    <div className="space-y-16">
      <section className="relative overflow-hidden rounded-3xl bg-ink-950 px-6 py-14 text-white shadow-soft sm:px-10 lg:px-14 lg:py-20">
        <div className="absolute -right-24 -top-24 size-80 rounded-full bg-brand-600/30 blur-3xl" aria-hidden="true" />
        <div className="absolute -bottom-32 -left-16 size-80 rounded-full bg-brand-500/20 blur-3xl" aria-hidden="true" />
        <div className="relative grid gap-10 lg:grid-cols-[1.2fr_0.8fr] lg:items-center">
          <div>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1 text-xs font-semibold text-brand-100 ring-1 ring-inset ring-white/15">
              <Sparkles className="size-3.5" aria-hidden="true" />
              MVP de FreeConnect
            </span>
            <h1 className="mt-5 max-w-2xl text-4xl font-bold leading-tight tracking-tight sm:text-5xl">
              Publica, propone, contrata y entrega. Todo en un mismo flujo.
            </h1>
            <p className="mt-4 max-w-xl text-base leading-relaxed text-ink-200">
              FreeConnect conecta clientes y freelancers con propuestas evaluables, contratos por hitos,
              mensajería, notificaciones y pagos simulados. Una experiencia completa sobre una API Spring Boot.
            </p>
            <div className="mt-7 flex flex-wrap gap-3">
              <Link to="/proyectos">
                <Button size="lg" icon={<Search className="size-4" />}>Explorar proyectos</Button>
              </Link>
              {authenticated ? (
                <Link to="/app">
                  <Button size="lg" variant="outline" className="border-white/30 bg-white/10 text-white hover:bg-white/20">
                    Ir a mi panel
                  </Button>
                </Link>
              ) : (
                <Link to="/registro">
                  <Button size="lg" variant="outline" className="border-white/30 bg-white/10 text-white hover:bg-white/20">
                    Crear cuenta gratis
                  </Button>
                </Link>
              )}
            </div>
            <p className="mt-5 flex items-center gap-2 text-xs text-ink-300">
              <CheckCircle2 className="size-4 text-emerald-300" aria-hidden="true" />
              Sin pagos reales · JWT de corta duración · refresh rotation
            </p>
          </div>

          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
            <HeroStat icon={<BriefcaseBusiness className="size-4" />} label="Proyectos publicados" value={String(projectsQuery.data?.totalElements ?? "—")} />
            <HeroStat icon={<Users className="size-4" />} label="Roles integrados" value="3" />
            <HeroStat icon={<CheckCircle2 className="size-4" />} label="Estado del MVP" value="Funcional" />
          </div>
        </div>
      </section>

      <section className="space-y-6">
        <div className="max-w-2xl">
          <p className="text-xs font-semibold uppercase tracking-wider text-brand-700">Funcionalidades</p>
          <h2 className="mt-1 text-2xl font-bold tracking-tight text-ink-950 sm:text-3xl">
            Un flujo completo, de la propuesta al pago
          </h2>
          <p className="mt-2 text-sm text-ink-500">
            Cada pantalla consume endpoints reales del backend y respeta los permisos por rol.
          </p>
        </div>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {features.map((feature) => (
            <Card key={feature.title} className="h-full">
              <div className="flex size-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
                {feature.icon}
              </div>
              <h3 className="mt-4 font-semibold text-ink-950">{feature.title}</h3>
              <p className="mt-1 text-sm leading-relaxed text-ink-500">{feature.description}</p>
            </Card>
          ))}
        </div>
      </section>

      <section className="grid gap-6 lg:grid-cols-[0.9fr_1.1fr] lg:items-center">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-brand-700">Cómo funciona</p>
          <h2 className="mt-1 text-2xl font-bold tracking-tight text-ink-950 sm:text-3xl">
            Tres pasos para empezar
          </h2>
          <p className="mt-2 text-sm text-ink-500">
            Clientes y freelancers comparten el mismo flujo, con acciones distintas según su rol.
          </p>
        </div>
        <ol className="grid gap-3 sm:grid-cols-3">
          {[
            ["01", "Crea tu cuenta", "Elige rol de cliente o freelancer y completa tu perfil."],
            ["02", "Conecta", "Publica un proyecto o envía una propuesta con monto y plazo."],
            ["03", "Ejecuta", "Gestiona hitos, mensajes, pagos simulados y reseñas."],
          ].map(([step, title, description]) => (
            <li key={step} className="rounded-2xl border border-ink-200 bg-white p-5 shadow-soft">
              <span className="text-xs font-bold text-brand-700">{step}</span>
              <h3 className="mt-2 font-semibold text-ink-950">{title}</h3>
              <p className="mt-1 text-sm text-ink-500">{description}</p>
            </li>
          ))}
        </ol>
      </section>

      {featured.length > 0 && (
        <section className="space-y-5">
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-brand-700">Catálogo</p>
              <h2 className="mt-1 text-2xl font-bold tracking-tight text-ink-950 sm:text-3xl">
                Proyectos publicados recientemente
              </h2>
            </div>
            <Link to="/proyectos" className="inline-flex items-center gap-1 text-sm font-semibold text-brand-700 hover:underline">
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

      <section className="rounded-3xl border border-brand-200 bg-brand-50 p-6 sm:p-10">
        <div className="flex flex-wrap items-center justify-between gap-6">
          <div className="max-w-2xl">
            <p className="text-xs font-semibold uppercase tracking-wider text-brand-800">Empieza hoy</p>
            <h2 className="mt-1 text-2xl font-bold tracking-tight text-ink-950 sm:text-3xl">
              Tu próximo proyecto está a una propuesta de distancia
            </h2>
            <p className="mt-2 text-sm text-ink-600">
              Regístrate como cliente para publicar o como freelancer para postularte. La administración
              supervisa reportes, moderación y auditoría.
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
                  <Button size="lg" variant="outline">Iniciar sesión</Button>
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
    <div className="rounded-2xl bg-white/5 p-4 ring-1 ring-inset ring-white/10">
      <p className="flex items-center gap-2 text-xs text-ink-300">
        {icon}
        {label}
      </p>
      <p className="mt-1 text-2xl font-bold text-white">{value}</p>
    </div>
  );
}
