import { useState } from "react";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import {
  ArrowRight,
  Bell,
  BriefcaseBusiness,
  CheckCircle2,
  CreditCard,
  Palette,
  Sparkles,
  Star,
  Type,
  Wand2,
} from "lucide-react";
import { demoEnabled } from "@/app/demoFlag";
import { Avatar } from "@/components/ui/Avatar";
import { Tabs } from "@/components/ui/Tabs";
import { useToast } from "@/components/ui/Toast";
import { ThemeToggle } from "@/components/ui/ThemeToggle";
import { Alert } from "@/shared/components/Alert";
import { Badge, StatusBadge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { Field, Input, Select, Textarea } from "@/shared/components/FormControls";
import { Modal } from "@/shared/components/Modal";
import { PageHeader } from "@/shared/components/PageHeader";
import { ProgressBar } from "@/shared/components/ProgressBar";
import { CardSkeleton, ListSkeleton } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";

const swatches = [
  ["brand", "bg-brand-600", "Primario"],
  ["accent", "bg-accent-500", "Secundario"],
  ["ink", "bg-ink-800", "Neutro"],
  ["success", "bg-success-600", "Éxito"],
  ["warning", "bg-warning-600", "Aviso"],
  ["danger", "bg-danger-600", "Error"],
  ["info", "bg-info-600", "Información"],
];

export function DemoPage() {
  useDocumentTitle("Demo visual");
  const { toast } = useToast();
  const [tab, setTab] = useState("resumen");
  const [modalOpen, setModalOpen] = useState(false);

  if (!demoEnabled) {
    return (
      <div className="mx-auto max-w-xl py-16 text-center">
        <h1 className="text-2xl font-bold text-ink-950">Demo visual deshabilitada</h1>
        <p className="mt-2 text-sm text-ink-500">
          Esta galería solo está disponible en desarrollo o cuando el build se genera con{" "}
          <code className="rounded bg-ink-100 px-1.5 py-0.5 text-xs">VITE_ENABLE_DEMO=true</code>.
        </p>
        <Link to="/" className="mt-6 inline-block">
          <Button variant="outline">Volver al inicio</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-10">
      <PageHeader
        eyebrow="Design system"
        title="Galería del prototipo"
        description="Una vista rápida de tokens, componentes y estados de la interfaz. No consume datos reales ni modifica el backend."
        actions={
          <>
            <ThemeToggle />
            <Link to="/acerca">
              <Button variant="outline" size="sm" icon={<ArrowRight className="size-4" />}>
                Acerca del prototipo
              </Button>
            </Link>
          </>
        }
      />

      <Alert tone="info" title="Página solo para la demostración">
        La demo está habilitada con la bandera de entorno. En una entrega de producción se excluye de
        la navegación salvo que se active explícitamente.
      </Alert>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <DemoMetric icon={<BriefcaseBusiness className="size-4" />} label="Componentes" value="25+" />
        <DemoMetric icon={<Palette className="size-4" />} label="Tokens de color" value="7" />
        <DemoMetric icon={<Type className="size-4" />} label="Tipografías" value="Inter" />
        <DemoMetric icon={<Wand2 className="size-4" />} label="Animaciones" value="5" />
      </section>

      <section className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader title="Colores" description="Escalas semánticas definidas en Tailwind." />
          <ul className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
            {swatches.map(([name, className, label]) => (
              <li key={name}>
                <span
                  className={`block h-14 rounded-xl ring-1 ring-inset ring-black/5 ${className}`}
                  aria-hidden="true"
                />
                <p className="mt-1.5 text-xs font-semibold text-ink-800">{label}</p>
                <p className="text-[11px] text-ink-500">{name}</p>
              </li>
            ))}
          </ul>
        </Card>

        <Card>
          <CardHeader title="Tipografía y jerarquía" description="Títulos balanceados y texto legible." />
          <div className="mt-4 space-y-2">
            <p className="text-3xl font-bold tracking-tight text-ink-950">Título de página</p>
            <p className="text-xl font-semibold text-ink-900">Título de sección</p>
            <p className="text-sm text-ink-600">
              Texto de apoyo con medidas cómodas de lectura. Los importes se formatean con Intl y la
              interfaz evita jerga innecesaria.
            </p>
            <p className="eyebrow">Etiqueta superior</p>
          </div>
        </Card>
      </section>

      <section className="space-y-4">
        <SectionTitle title="Botones" description="Variantes, tamaños y estado de carga." />
        <Card className="flex flex-wrap items-center gap-3">
          <Button>Primario</Button>
          <Button variant="secondary">Secundario</Button>
          <Button variant="outline">Contorno</Button>
          <Button variant="ghost">Fantasma</Button>
          <Button variant="danger">Peligro</Button>
          <Button variant="subtle">Sutil</Button>
          <Button size="sm">Pequeño</Button>
          <Button size="lg">Grande</Button>
          <Button loading>Cargando</Button>
          <Button size="icon" aria-label="Notificaciones">
            <Bell className="size-4" aria-hidden="true" />
          </Button>
        </Card>
      </section>

      <section className="space-y-4">
        <SectionTitle title="Estados y etiquetas" description="Semáforo de estado con punto de color." />
        <Card className="flex flex-wrap items-center gap-2">
          <Badge tone="brand" dot>Aceptada</Badge>
          <Badge tone="success" dot>Activo</Badge>
          <Badge tone="info" dot>En revisión</Badge>
          <Badge tone="warning" dot>Pendiente</Badge>
          <Badge tone="danger" dot>Rechazado</Badge>
          <Badge tone="neutral">Archivado</Badge>
          <StatusBadge status="PUBLISHED" />
          <StatusBadge status="SUBMITTED" />
          <StatusBadge status="REJECTED" />
        </Card>
      </section>

      <section className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader title="Formularios" description="Foco visible, ayudas y errores accesibles." />
          <div className="mt-4 space-y-4">
            <Field label="Correo electrónico" htmlFor="demo-email" hint="Ejemplo de ayuda contextual." required>
              <Input id="demo-email" type="email" placeholder="tu@correo.com" />
            </Field>
            <Field label="Categoría" htmlFor="demo-category">
              <Select id="demo-category" defaultValue="SOFTWARE">
                <option value="SOFTWARE">Software</option>
                <option value="DESIGN">Diseño</option>
                <option value="MARKETING">Marketing</option>
              </Select>
            </Field>
            <Field label="Descripción" htmlFor="demo-description">
              <Textarea id="demo-description" rows={3} placeholder="Describe el alcance del proyecto" />
            </Field>
            <Field
              label="Campo con error"
              htmlFor="demo-error"
              hint="Ejemplo visual de estado inválido; no dispara mensajes live."
            >
              <Input id="demo-error" invalid defaultValue="valor inválido" />
            </Field>
          </div>
        </Card>

        <Card>
          <CardHeader title="Avatares, progreso y pestañas" description="Componentes de apoyo." />
          <div className="mt-4 flex items-center gap-3">
            <Avatar name="Ana Pérez" size="lg" />
            <Avatar name="Cliente E2E" size="md" />
            <Avatar name="Freelancer" size="sm" />
          </div>
          <ProgressBar value={68} label="Progreso del contrato" className="mt-5" />
          <Tabs
            className="mt-5"
            ariaLabel="Secciones de la demo"
            value={tab}
            onChange={setTab}
            items={[
              { id: "resumen", label: "Resumen", icon: <CheckCircle2 className="size-4" aria-hidden="true" /> },
              { id: "pagos", label: "Pagos", icon: <CreditCard className="size-4" aria-hidden="true" /> },
              { id: "resenas", label: "Reseñas", icon: <Star className="size-4" aria-hidden="true" /> },
            ]}
          />
          <p className="mt-3 text-sm text-ink-600">
            Pestaña activa: <strong>{tab}</strong>. Navega con las flechas del teclado.
          </p>
        </Card>
      </section>

      <section className="space-y-4">
        <SectionTitle title="Feedback" description="Toasts, modales y estados vacíos." />
        <Card className="flex flex-wrap items-center gap-2">
          <Button
            variant="outline"
            onClick={() =>
              toast({ tone: "success", title: "Perfil guardado", description: "Cambios aplicados." })
            }
          >
            Toast de éxito
          </Button>
          <Button
            variant="outline"
            onClick={() =>
              toast({ tone: "error", title: "No se pudo guardar", description: "Revisa los datos." })
            }
          >
            Toast de error
          </Button>
          <Button
            variant="outline"
            onClick={() =>
              toast({ tone: "info", title: "Sesión restaurada", description: "Token renovado." })
            }
          >
            Toast informativo
          </Button>
          <Button onClick={() => setModalOpen(true)}>Abrir modal</Button>
        </Card>

        <div className="grid gap-4 lg:grid-cols-2">
          <EmptyState
            title="Sin propuestas todavía"
            description="Cuando un freelancer envíe una propuesta aparecerá aquí."
            action={<Button size="sm" variant="outline">Invitar freelancer</Button>}
          />
          <div className="space-y-4">
            <CardSkeleton />
            <ListSkeleton rows={2} />
          </div>
        </div>
      </section>

      <section className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-brand-200 bg-brand-50 p-5 dark:border-brand-800 dark:bg-brand-950/40">
        <div>
          <p className="text-sm font-semibold text-ink-950 dark:text-white">
            ¿Listo para probar el flujo real?
          </p>
          <p className="text-sm text-ink-600 dark:text-slate-300">
            La galería es estática; los datos reales viven en el panel autenticado.
          </p>
        </div>
        <Link to="/proyectos">
          <Button icon={<Sparkles className="size-4" />}>Explorar proyectos</Button>
        </Link>
      </section>

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Diálogo de ejemplo"
        description="Trampa de foco, cierre con Escape y animación respetuosa con reduced motion."
        footer={
          <>
            <Button variant="outline" onClick={() => setModalOpen(false)}>
              Cancelar
            </Button>
            <Button
              onClick={() => {
                setModalOpen(false);
                toast({ tone: "success", title: "Acción confirmada" });
              }}
            >
              Confirmar
            </Button>
          </>
        }
      >
        <p className="text-sm text-ink-600">
          Este diálogo no ejecuta ninguna acción real. Sirve para revisar jerarquía, espaciado y
          accesibilidad del componente.
        </p>
      </Modal>
    </div>
  );
}

function DemoMetric({
  icon,
  label,
  value,
}: {
  icon: ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-center gap-3 rounded-2xl border border-ink-200 bg-white p-4 shadow-card dark:border-slate-800 dark:bg-slate-900">
      <span className="flex size-9 items-center justify-center rounded-xl bg-brand-50 text-brand-700 ring-1 ring-inset ring-brand-100 dark:bg-brand-600/15 dark:text-brand-200 dark:ring-brand-600/30">
        {icon}
      </span>
      <div>
        <p className="text-xs text-ink-500">{label}</p>
        <p className="text-lg font-bold text-ink-950 dark:text-white">{value}</p>
      </div>
    </div>
  );
}

function SectionTitle({ title, description }: { title: string; description: string }) {
  return (
    <div>
      <h2 className="text-lg font-semibold text-ink-950 dark:text-white">{title}</h2>
      <p className="text-sm text-ink-500">{description}</p>
    </div>
  );
}
