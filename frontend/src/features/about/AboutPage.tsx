import { Link } from "react-router-dom";
import type { ReactNode } from "react";
import {
  BookOpen,
  CircleAlert,
  Code2,
  Database,
  FileCode2,
  GitBranch,
  KeyRound,
  Layers,
  LockKeyhole,
  Server,
  ShieldCheck,
  Sparkles,
  Users,
} from "lucide-react";
import { demoEnabled } from "@/app/demoFlag";
import { Badge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { PageHeader } from "@/shared/components/PageHeader";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";

const stack = [
  ["UI", "React 19 + TypeScript 5.8 (strict)"],
  ["Build", "Vite 6"],
  ["Rutas", "React Router 6 con guards por rol"],
  ["Datos", "TanStack Query 5 (caché y polling)"],
  ["Formularios", "React Hook Form + Zod"],
  ["Estilos", "Tailwind CSS 3 + design system propio"],
  ["Pruebas", "Vitest + Testing Library y Playwright E2E"],
  ["API", "Spring Boot 3.5 + Java 21, OpenAPI generado"],
];

const roles = [
  {
    role: "CLIENT",
    label: "Cliente",
    can: "Publica proyectos, evalúa propuestas, aprueba hitos, libera pagos simulados y deja reseñas.",
    cannot: "No puede enviar propuestas ni acceder al panel de administración.",
  },
  {
    role: "FREELANCER",
    label: "Freelancer",
    can: "Explora el catálogo, envía propuestas, registra entregables, envía hitos a revisión y consulta su neto.",
    cannot: "No puede publicar proyectos, aprobar hitos ni liberar pagos.",
  },
  {
    role: "ADMIN",
    label: "Administrador",
    can: "Consulta reportes, modera proyectos, suspende usuarios por UUID y revisa la auditoría.",
    cannot: "No es parte de contratos, no tiene perfil público y no ve secciones de cliente o freelancer.",
  },
];

const limitations = [
  "Sin WebSockets: mensajes y notificaciones se actualizan por polling configurable.",
  "Pagos simulados con proveedor MOCK; no existen cobros reales.",
  "No hay endpoint de listado de usuarios: la suspensión pide un UUID manual.",
  "No hay búsqueda de perfil por usuario ni endpoint de «mis proyectos» o «mis propuestas»; el frontend filtra hasta 10 páginas de 100 proyectos públicos.",
  "Un hito rechazado no puede reenviarse: el dominio define la transición, pero no existe endpoint.",
  "No hay recuperación de contraseña ni verificación de correo.",
  "El correo y las notificaciones push son simulados.",
];

const securityPoints = [
  {
    icon: <KeyRound className="size-4" aria-hidden="true" />,
    title: "Access token en memoria",
    text: "No se persiste en localStorage; una recarga lo reconstruye con el refresh token.",
  },
  {
    icon: <LockKeyhole className="size-4" aria-hidden="true" />,
    title: "Refresh token en sessionStorage",
    text: "Se borra al cerrar la pestaña. Mitiga recargas, pero sigue expuesto a XSS; se recomienda cookie HttpOnly en producción.",
  },
  {
    icon: <ShieldCheck className="size-4" aria-hidden="true" />,
    title: "Logout con revocación",
    text: "El backend revoca el refresh token y la sesión local se limpia aunque falle la red.",
  },
  {
    icon: <GitBranch className="size-4" aria-hidden="true" />,
    title: "Refresh single-flight",
    text: "Ante un 401 se refresca una sola vez y se reintenta la petición original una única vez.",
  },
];

const diagrams = [
  ["casos-de-uso", "Actores y funcionalidades por rol"],
  ["clases-dominio", "Entidades, campos y relaciones"],
  ["secuencia-flujo-principal", "Ciclo completo de negocio"],
  ["secuencia-autenticacion", "Registro, login, refresh y logout"],
  ["secuencia-admin", "Reportes, moderación y auditoría"],
  ["componentes", "Contenedores y dependencias"],
  ["despliegue", "Local verificado y Docker objetivo"],
  ["estados-hito", "Máquina de estados del hito"],
];

export function AboutPage() {
  useDocumentTitle("Acerca del prototipo");

  return (
    <div className="space-y-10">
      <PageHeader
        eyebrow="Proyecto académico"
        title="Acerca del prototipo"
        description="FreeConnect es un MVP funcional de arquitectura de software: una SPA React sobre una API Spring Boot, con roles, contratos por hitos y pagos simulados."
        actions={
          <>
            <Link to="/proyectos">
              <Button variant="outline">Ver catálogo</Button>
            </Link>
            {demoEnabled && (
              <Link to="/demo">
                <Button icon={<Sparkles className="size-4" />}>Galería visual</Button>
              </Link>
            )}
          </>
        }
      />

      <section className="grid gap-4 md:grid-cols-3">
        <SummaryCard
          icon={<Code2 className="size-5" aria-hidden="true" />}
          title="Frontend"
          text="SPA con React 19, TypeScript estricto, Vite y TanStack Query."
        />
        <SummaryCard
          icon={<Server className="size-5" aria-hidden="true" />}
          title="Backend sin cambios"
          text="La SPA consume los endpoints reales documentados en OpenAPI."
        />
        <SummaryCard
          icon={<Database className="size-5" aria-hidden="true" />}
          title="Datos efímeros"
          text="H2 en memoria en desarrollo; PostgreSQL vía Docker no verificado."
        />
      </section>

      <section className="grid gap-6 lg:grid-cols-[1.05fr_0.95fr]">
        <Card>
          <CardHeader
            title="Alcance funcional"
            description="Flujo cubierto de extremo a extremo y verificado con Playwright."
          />
          <ol className="mt-4 space-y-2.5 text-sm text-ink-600 dark:text-slate-300">
            {[
              "Registro de cliente y freelancer con perfiles separados.",
              "Publicación de proyectos y exploración pública.",
              "Propuesta del freelancer, evaluación y aceptación del cliente.",
              "Contrato con hitos, entregables, tareas y comentarios.",
              "Mensajería por contrato y notificaciones con reintentos.",
              "Aprobación de hitos, pago simulado y reseña de la contraparte.",
              "Panel de administración con reportes, moderación y auditoría.",
            ].map((item, index) => (
              <li key={item} className="flex gap-2.5">
                <span className="flex size-6 shrink-0 items-center justify-center rounded-full bg-brand-50 text-xs font-bold text-brand-700 dark:bg-brand-600/15 dark:text-brand-200">
                  {index + 1}
                </span>
                <span>{item}</span>
              </li>
            ))}
          </ol>
        </Card>

        <Card>
          <CardHeader title="Stack técnico" description="Herramientas reales del repositorio." />
          <dl className="mt-4 divide-y divide-ink-100 text-sm dark:divide-slate-800">
            {stack.map(([label, value]) => (
              <div key={label} className="flex flex-wrap items-center justify-between gap-2 py-2.5">
                <dt className="font-medium text-ink-600 dark:text-slate-300">{label}</dt>
                <dd className="text-right text-ink-900 dark:text-slate-100">{value}</dd>
              </div>
            ))}
          </dl>
        </Card>
      </section>

      <section className="space-y-4">
        <div className="flex items-center gap-2">
          <Users className="size-5 text-brand-700" aria-hidden="true" />
          <h2 className="text-xl font-bold text-ink-950 dark:text-white">Roles y permisos</h2>
        </div>
        <div className="grid gap-4 md:grid-cols-3">
          {roles.map((item) => (
            <Card key={item.role} className="h-full">
              <Badge tone={item.role === "ADMIN" ? "info" : item.role === "CLIENT" ? "brand" : "accent"} dot>
                {item.label}
              </Badge>
              <p className="mt-3 text-sm text-ink-600 dark:text-slate-300">{item.can}</p>
              <p className="mt-2 flex items-start gap-1.5 text-xs text-ink-500">
                <CircleAlert className="mt-0.5 size-3.5 shrink-0" aria-hidden="true" />
                {item.cannot}
              </p>
            </Card>
          ))}
        </div>
      </section>

      <section className="space-y-4">
        <div className="flex items-center gap-2">
          <ShieldCheck className="size-5 text-brand-700" aria-hidden="true" />
          <h2 className="text-xl font-bold text-ink-950 dark:text-white">Seguridad y sesión</h2>
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          {securityPoints.map((point) => (
            <Card key={point.title}>
              <div className="flex items-start gap-3">
                <span className="flex size-9 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-700 ring-1 ring-inset ring-brand-100 dark:bg-brand-600/15 dark:text-brand-200 dark:ring-brand-600/30">
                  {point.icon}
                </span>
                <div>
                  <h3 className="text-sm font-semibold text-ink-950 dark:text-white">{point.title}</h3>
                  <p className="mt-1 text-sm leading-relaxed text-ink-600 dark:text-slate-300">
                    {point.text}
                  </p>
                </div>
              </div>
            </Card>
          ))}
        </div>
        <Card className="border-warning-200 bg-warning-50/70 dark:border-warning-700 dark:bg-warning-900/20">
          <p className="text-sm leading-relaxed text-ink-700 dark:text-slate-200">
            <strong>Riesgo documentado:</strong> guardar el refresh token en sessionStorage lo expone
            a XSS. En producción se recomienda moverlo a una cookie <code>HttpOnly</code> +{" "}
            <code>Secure</code> + <code>SameSite</code> y añadir protección CSRF.
          </p>
        </Card>
      </section>

      <section className="space-y-4">
        <div className="flex items-center gap-2">
          <CircleAlert className="size-5 text-warning-600" aria-hidden="true" />
          <h2 className="text-xl font-bold text-ink-950 dark:text-white">Límites conocidos</h2>
        </div>
        <Card>
          <ul className="grid gap-2.5 text-sm text-ink-600 sm:grid-cols-2 dark:text-slate-300">
            {limitations.map((item) => (
              <li key={item} className="flex items-start gap-2">
                <CircleAlert className="mt-0.5 size-4 shrink-0 text-warning-600" aria-hidden="true" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </Card>
      </section>

      <section className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader
            title="Documentación UML"
            description="Ocho diagramas Mermaid versionados en docs/uml/."
          />
          <ul className="mt-4 space-y-2 text-sm">
            {diagrams.map(([file, description]) => (
              <li key={file} className="flex flex-wrap items-baseline gap-x-2">
                <code className="rounded bg-ink-100 px-1.5 py-0.5 text-xs text-ink-800 dark:bg-slate-800 dark:text-slate-200">
                  {file}.md
                </code>
                <span className="text-ink-500">{description}</span>
              </li>
            ))}
          </ul>
          <p className="mt-4 text-xs text-ink-500">
            Índice: <code>docs/uml/README.md</code>. Contrato de API:{" "}
            <code>frontend/schema/openapi.json</code>.
          </p>
        </Card>

        <Card>
          <CardHeader title="Estado de verificación" description="Lo comprobado y lo pendiente." />
          <ul className="mt-4 space-y-2.5 text-sm">
            {[
              ["build + lint + tests unitarios", "verificado", "success"],
              ["E2E Playwright del ciclo completo", "verificado", "success"],
              ["Docker Compose + nginx", "no verificado (sin Docker)", "warning"],
              ["Redis", "ausente (health 503)", "warning"],
              ["Pasarela de pagos", "simulada (MOCK)", "info"],
            ].map(([label, state, tone]) => (
              <li key={label} className="flex flex-wrap items-center justify-between gap-2">
                <span className="text-ink-600 dark:text-slate-300">{label}</span>
                <Badge tone={tone as "success" | "warning" | "info"} dot>
                  {state}
                </Badge>
              </li>
            ))}
          </ul>
        </Card>
      </section>

      <section className="rounded-3xl border border-brand-200 bg-brand-50 p-6 sm:p-8 dark:border-brand-800 dark:bg-brand-950/40">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="max-w-2xl">
            <div className="flex items-center gap-2">
              <BookOpen className="size-5 text-brand-700" aria-hidden="true" />
              <h2 className="text-xl font-bold text-ink-950 dark:text-white">
                Cómo explorar el prototipo
              </h2>
            </div>
            <p className="mt-2 text-sm leading-relaxed text-ink-600 dark:text-slate-300">
              Crea una cuenta de cliente y otra de freelancer para recorrer el flujo completo, o entra
              con la cuenta de administración sembrada por el backend. Los datos viven en H2 y se
              reinician con el servidor.
            </p>
            <p className="mt-3 flex items-center gap-1.5 text-xs text-ink-500">
              <FileCode2 className="size-4" aria-hidden="true" /> Guía completa en{" "}
              <code>frontend/README.md</code>
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <Link to="/registro">
              <Button size="lg">Crear cuenta</Button>
            </Link>
            <Link to="/login">
              <Button size="lg" variant="outline">
                Iniciar sesión
              </Button>
            </Link>
          </div>
        </div>
      </section>

      <section className="flex flex-wrap items-center gap-4 text-xs text-ink-500">
        <span className="inline-flex items-center gap-1.5">
          <Layers className="size-4" aria-hidden="true" /> Monolito modular
        </span>
        <span className="inline-flex items-center gap-1.5">
          <Server className="size-4" aria-hidden="true" /> API REST versionada
        </span>
        <span className="inline-flex items-center gap-1.5">
          <ShieldCheck className="size-4" aria-hidden="true" /> Autorización en backend
        </span>
      </section>
    </div>
  );
}

function SummaryCard({
  icon,
  title,
  text,
}: {
  icon: ReactNode;
  title: string;
  text: string;
}) {
  return (
    <Card>
      <span className="flex size-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700 ring-1 ring-inset ring-brand-100 dark:bg-brand-600/15 dark:text-brand-200 dark:ring-brand-600/30">
        {icon}
      </span>
      <h2 className="mt-3 font-semibold text-ink-950 dark:text-white">{title}</h2>
      <p className="mt-1 text-sm leading-relaxed text-ink-500">{text}</p>
    </Card>
  );
}
