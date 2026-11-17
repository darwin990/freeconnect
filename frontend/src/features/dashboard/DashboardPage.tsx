import { Link } from "react-router-dom";
import type { ReactNode } from "react";
import {
  ArrowRight,
  Bell,
  BriefcaseBusiness,
  ClipboardList,
  FileText,
  MessageSquareText,
  Plus,
  Search,
  ShieldCheck,
  Star,
  Wallet,
} from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { useAdminAudit, useAdminReports } from "@/features/admin/hooks";
import { useContracts } from "@/features/contracts/hooks";
import { useConversations } from "@/features/messaging/hooks";
import { useNotifications } from "@/features/notifications/hooks";
import { useMyPayments } from "@/features/payments/hooks";
import { ProjectCard } from "@/features/projects/components/ProjectCard";
import { useMyProjects, useProjects } from "@/features/projects/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge, StatusBadge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { ProgressBar } from "@/shared/components/ProgressBar";
import { ListSkeleton, Skeleton } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { contractStatusLabels, humanizeKey, roleLabels } from "@/shared/utils/labels";
import { formatDateTime, formatMoney, shortId } from "@/shared/utils/format";
import { cn } from "@/shared/utils/cn";

export function DashboardPage() {
  useDocumentTitle("Panel");
  const { user } = useAuth();
  const role = user?.role;
  const isClient = role === "CLIENT";
  const isFreelancer = role === "FREELANCER";
  const isAdmin = role === "ADMIN";

  const myProjectsQuery = useMyProjects(isClient ? user?.id : undefined);
  const contractsQuery = useContracts(!isAdmin);
  const paymentsQuery = useMyPayments(!isAdmin);
  const notificationsQuery = useNotifications();
  const conversationsQuery = useConversations(!isAdmin);
  const suggestionsQuery = useProjects({ status: "PUBLISHED", size: 3, page: 0 }, isFreelancer);
  const reportsQuery = useAdminReports(isAdmin);
  const auditQuery = useAdminAudit(isAdmin);

  if (!user) return null;

  const contracts = contractsQuery.data ?? [];
  const payments = paymentsQuery.data ?? [];
  const notifications = notificationsQuery.data ?? [];
  const conversations = conversationsQuery.data ?? [];
  const unread = conversations.reduce((total, conversation) => total + conversation.unread, 0);
  const activeContracts = contracts.filter((contract) => contract.status === "ACTIVE").length;
  const completedContracts = contracts.filter((contract) => contract.status === "COMPLETED").length;
  const netReleased = payments
    .filter((payment) => payment.status === "RELEASED")
    .reduce((total, payment) => total + payment.netAmount.amount, 0);
  const clientReleased = payments
    .filter((payment) => payment.status === "RELEASED")
    .reduce((total, payment) => total + payment.amount.amount, 0);
  const currency = payments[0]?.amount.currency ?? "USD";
  const reports = reportsQuery.data ?? [];
  const audit = auditQuery.data ?? [];

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow={`Panel · ${roleLabels[user.role]}`}
        title={`Hola, ${user.displayName}`}
        description={
          isAdmin
            ? "Resumen operativo de la plataforma: reportes agregados y últimos eventos auditados."
            : "Tu actividad reciente, accesos rápidos y el estado de tus contratos y pagos."
        }
        actions={
          isClient ? (
            <Link to="/app/proyectos/nuevo">
              <Button icon={<Plus className="size-4" />}>Publicar proyecto</Button>
            </Link>
          ) : isFreelancer ? (
            <Link to="/proyectos">
              <Button icon={<Search className="size-4" />}>Explorar proyectos</Button>
            </Link>
          ) : (
            <Link to="/app/admin/reportes">
              <Button icon={<ShieldCheck className="size-4" />}>Ver reportes</Button>
            </Link>
          )
        }
      />

      {isAdmin ? (
        <>
          {reportsQuery.isError ? (
            <ErrorAlert error={reportsQuery.error} title="No se pudieron cargar los reportes" />
          ) : null}
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {reportsQuery.isLoading ? (
              Array.from({ length: 4 }).map((_, index) => (
                <Metric key={index} label="Cargando métrica" value="" loading />
              ))
            ) : reports.length === 0 ? (
              <Card className="sm:col-span-2 xl:col-span-4">
                <p className="text-sm text-ink-500">
                  El backend no devolvió métricas agregadas.
                </p>
              </Card>
            ) : (
              Object.entries(reports[0].metrics ?? {})
                .slice(0, 4)
                .map(([key, value], index) => (
                  <Metric
                    key={key}
                    label={humanizeKey(key)}
                    value={typeof value === "object" ? "—" : String(value)}
                    icon={<ClipboardList className="size-4" />}
                    tone={["brand", "accent", "info", "success"][index % 4] as MetricTone}
                  />
                ))
            )}
          </div>

          <div className="grid gap-6 lg:grid-cols-2">
            <Card>
              <CardHeader
                title="Reportes disponibles"
                description="Generados por el backend."
                action={
                  <Link to="/app/admin/reportes" className="text-sm font-semibold text-brand-700 hover:underline">
                    Ver todos
                  </Link>
                }
              />
              <div className="mt-4 flex flex-wrap gap-2">
                {reports.map((report) => (
                  <Badge key={`${report.report}-${report.generatedAt}`} tone="brand">
                    {humanizeKey(report.report)}
                  </Badge>
                ))}
                {reports.length === 0 && <p className="text-sm text-ink-500">Sin reportes.</p>}
              </div>
            </Card>

            <Card>
              <CardHeader
                title="Últimos eventos de auditoría"
                description="Máximo 100 eventos según el backend."
                action={
                  <Link to="/app/admin/auditoria" className="text-sm font-semibold text-brand-700 hover:underline">
                    Ver auditoría
                  </Link>
                }
              />
              {auditQuery.isError ? (
                <ErrorAlert error={auditQuery.error} className="mt-4" />
              ) : audit.length === 0 ? (
                <p className="mt-4 text-sm text-ink-500">Sin eventos.</p>
              ) : (
                <ul className="mt-4 divide-y divide-ink-100 text-sm">
                  {audit.slice(0, 5).map((entry) => (
                    <li key={entry.id} className="flex flex-wrap items-start justify-between gap-2 py-2.5">
                      <div>
                        <p className="font-mono text-xs font-semibold text-ink-900">{entry.action}</p>
                        <p className="text-xs text-ink-500">
                          {entry.entityType} {shortId(entry.entityId)}
                        </p>
                      </div>
                      <span className="text-xs text-ink-400">{formatDateTime(entry.occurredAt)}</span>
                    </li>
                  ))}
                </ul>
              )}
            </Card>
          </div>
        </>
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {isClient ? (
              <>
                <Metric
                  label="Mis proyectos"
                  value={String(myProjectsQuery.data?.length ?? 0)}
                  icon={<BriefcaseBusiness className="size-4" />}
                  loading={myProjectsQuery.isLoading}
                  hint="Borradores y publicados"
                />
                <Metric
                  label="Contratos activos"
                  value={String(activeContracts)}
                  icon={<FileText className="size-4" />}
                  tone="info"
                  loading={contractsQuery.isLoading}
                  hint={`${completedContracts} completados`}
                />
                <Metric
                  label="Total pagado"
                  value={formatMoney(clientReleased, currency)}
                  icon={<Wallet className="size-4" />}
                  tone="success"
                  loading={paymentsQuery.isLoading}
                  hint="Pagos simulados liberados"
                />
                <Metric
                  label="Mensajes sin leer"
                  value={String(unread)}
                  icon={<MessageSquareText className="size-4" />}
                  tone="accent"
                  loading={conversationsQuery.isLoading}
                  hint={`${conversations.length} conversaciones`}
                />
              </>
            ) : (
              <>
                <Metric
                  label="Contratos activos"
                  value={String(activeContracts)}
                  icon={<FileText className="size-4" />}
                  loading={contractsQuery.isLoading}
                  hint="En ejecución ahora"
                />
                <Metric
                  label="Completados"
                  value={String(completedContracts)}
                  icon={<Star className="size-4" />}
                  tone="info"
                  loading={contractsQuery.isLoading}
                  hint="Histórico de contratos"
                />
                <Metric
                  label="Neto liberado"
                  value={formatMoney(netReleased, currency)}
                  icon={<Wallet className="size-4" />}
                  tone="success"
                  loading={paymentsQuery.isLoading}
                  hint="Después de comisión"
                />
                <Metric
                  label="Mensajes sin leer"
                  value={String(unread)}
                  icon={<MessageSquareText className="size-4" />}
                  tone="accent"
                  loading={conversationsQuery.isLoading}
                  hint={`${conversations.length} conversaciones`}
                />
              </>
            )}
          </div>

          <Card>
            <CardHeader title="Accesos rápidos" description="Atajos según tu rol." />
            <div className="mt-4 flex flex-wrap gap-2">
              {isClient && (
                <>
                  <Link to="/app/proyectos/nuevo">
                    <Button size="sm" variant="outline" icon={<Plus className="size-4" />}>Nuevo proyecto</Button>
                  </Link>
                  <Link to="/app/propuestas">
                    <Button size="sm" variant="outline" icon={<ClipboardList className="size-4" />}>Propuestas</Button>
                  </Link>
                  <Link to="/app/mis-proyectos">
                    <Button size="sm" variant="outline" icon={<BriefcaseBusiness className="size-4" />}>Mis proyectos</Button>
                  </Link>
                </>
              )}
              {isFreelancer && (
                <Link to="/proyectos">
                  <Button size="sm" variant="outline" icon={<Search className="size-4" />}>Buscar proyectos</Button>
                </Link>
              )}
              <Link to="/app/contratos">
                <Button size="sm" variant="outline" icon={<FileText className="size-4" />}>Contratos</Button>
              </Link>
              <Link to="/app/mensajes">
                <Button size="sm" variant="outline" icon={<MessageSquareText className="size-4" />}>Mensajes</Button>
              </Link>
              <Link to="/app/pagos">
                <Button size="sm" variant="outline" icon={<Wallet className="size-4" />}>Pagos</Button>
              </Link>
              <Link to="/app/resenas">
                <Button size="sm" variant="outline" icon={<Star className="size-4" />}>Reseñas</Button>
              </Link>
            </div>
          </Card>

          <div className="grid gap-6 xl:grid-cols-[1fr_380px]">
            <div className="space-y-6">
              <Card>
                <CardHeader
                  title="Contratos recientes"
                  description="Progreso y estado de tus últimos contratos."
                  action={
                    <Link to="/app/contratos" className="inline-flex items-center gap-1 text-sm font-semibold text-brand-700 hover:underline">
                      Ver todos <ArrowRight className="size-3.5" aria-hidden="true" />
                    </Link>
                  }
                />
                {contractsQuery.isError ? (
                  <ErrorAlert error={contractsQuery.error} className="mt-4" />
                ) : contractsQuery.isLoading ? (
                  <div className="mt-4">
                    <ListSkeleton rows={2} />
                  </div>
                ) : contracts.length === 0 ? (
                  <p className="mt-4 text-sm text-ink-500">Aún no tienes contratos.</p>
                ) : (
                  <ul className="mt-4 space-y-3">
                    {contracts.slice(0, 3).map((contract) => (
                      <li key={contract.id} className="rounded-xl border border-ink-200 p-4">
                        <div className="flex flex-wrap items-start justify-between gap-2">
                          <Link to={`/app/contratos/${contract.id}`} className="font-semibold text-ink-950 hover:text-brand-700">
                            {contract.title}
                          </Link>
                          <StatusBadge status={contract.status} label={contractStatusLabels[contract.status]} />
                        </div>
                        <p className="mt-1 text-xs text-ink-500">
                          {formatMoney(contract.amount.amount, contract.amount.currency)} ·{" "}
                          {shortId(isClient ? contract.freelancerId : contract.clientId)}
                        </p>
                        <ProgressBar value={contract.progress} className="mt-2" label="Progreso" />
                      </li>
                    ))}
                  </ul>
                )}
              </Card>

              <Card>
                <CardHeader
                  title={isClient ? "Mis proyectos recientes" : "Proyectos sugeridos"}
                  description={isClient ? "Últimos proyectos creados por ti." : "Proyectos publicados recientemente."}
                  action={
                    <Link
                      to={isClient ? "/app/mis-proyectos" : "/proyectos"}
                      className="text-sm font-semibold text-brand-700 hover:underline"
                    >
                      Ver más
                    </Link>
                  }
                />
                {(isClient ? myProjectsQuery : suggestionsQuery).isError ? (
                  <ErrorAlert error={(isClient ? myProjectsQuery : suggestionsQuery).error} className="mt-4" />
                ) : isClient ? (
                  (myProjectsQuery.data ?? []).length === 0 ? (
                    <EmptyState
                      title="Sin proyectos"
                      description="Publica tu primer proyecto para recibir propuestas."
                      action={
                        <Link to="/app/proyectos/nuevo">
                          <Button size="sm">Crear proyecto</Button>
                        </Link>
                      }
                    />
                  ) : (
                    <div className="mt-4 grid gap-4 md:grid-cols-2">
                      {(myProjectsQuery.data ?? []).slice(0, 2).map((project) => (
                        <ProjectCard key={project.id} project={project} />
                      ))}
                    </div>
                  )
                ) : (suggestionsQuery.data?.content ?? []).length === 0 ? (
                  <p className="mt-4 text-sm text-ink-500">
                    {suggestionsQuery.isLoading ? "Cargando proyectos…" : "No hay proyectos publicados ahora mismo."}
                  </p>
                ) : (
                  <div className="mt-4 grid gap-4 md:grid-cols-2">
                    {suggestionsQuery.data?.content.slice(0, 2).map((project) => (
                      <ProjectCard key={project.id} project={project} />
                    ))}
                  </div>
                )}
              </Card>
            </div>

            <div className="space-y-6">
              <Card>
                <CardHeader
                  title="Notificaciones"
                  description="Actividad reciente de la plataforma."
                  action={<Bell className="size-5 text-ink-400" aria-hidden="true" />}
                />
                {notificationsQuery.isError ? (
                  <ErrorAlert error={notificationsQuery.error} className="mt-4" />
                ) : notifications.length === 0 ? (
                  <p className="mt-4 text-sm text-ink-500">Sin notificaciones.</p>
                ) : (
                  <ul className="mt-4 space-y-3">
                    {notifications.slice(0, 4).map((notification) => (
                      <li key={notification.id} className="rounded-xl border border-ink-200 p-3 text-sm">
                        <div className="flex items-start justify-between gap-2">
                          <p className="font-medium text-ink-900">{notification.title}</p>
                          <Badge tone={notification.status === "SENT" ? "success" : notification.status === "FAILED" ? "danger" : "warning"}>
                            {notification.status}
                          </Badge>
                        </div>
                        <p className="mt-1 line-clamp-2 text-xs text-ink-500">{notification.body}</p>
                        <p className="mt-1 text-[11px] text-ink-400">{formatDateTime(notification.createdAt)}</p>
                      </li>
                    ))}
                  </ul>
                )}
                <Link to="/app/notificaciones" className="mt-4 inline-flex items-center gap-1 text-sm font-semibold text-brand-700 hover:underline">
                  Ver todas <ArrowRight className="size-3.5" aria-hidden="true" />
                </Link>
              </Card>

              <Card>
                <CardHeader title="Resumen de pagos" description="Pasarela simulada, sin cobros reales." />
                <p className="mt-3 text-2xl font-bold text-ink-950">
                  {formatMoney(isClient ? clientReleased : netReleased, currency)}
                </p>
                <p className="mt-1 text-xs text-ink-500">
                  {isClient ? "Total liberado en contratos" : "Neto recibido tras comisión"}
                </p>
                <Link to="/app/pagos" className="mt-4 inline-flex items-center gap-1 text-sm font-semibold text-brand-700 hover:underline">
                  Ver historial <ArrowRight className="size-3.5" aria-hidden="true" />
                </Link>
              </Card>

              <Card>
                <CardHeader title="Rol y alcance" />
                <p className="mt-3 text-sm text-ink-600">
                  Estás operando como <strong>{roleLabels[user.role]}</strong>. La navegación y las acciones
                  visibles se adaptan a los permisos del backend.
                </p>
                <div className="mt-3 flex flex-wrap gap-2">
                  <Badge tone="brand">{roleLabels[user.role]}</Badge>
                  {isFreelancer && <Badge tone="info">Freelancer</Badge>}
                  {isClient && <Badge tone="success">Cliente</Badge>}
                </div>
              </Card>
            </div>
          </div>
        </>
      )}

      {isClient && myProjectsQuery.isError ? (
        <ErrorAlert error={myProjectsQuery.error} title="No se pudo cargar el resumen de proyectos" />
      ) : null}
      {isClient && (myProjectsQuery.data ?? []).length === 0 && !myProjectsQuery.isLoading ? (
        <p className="text-xs text-ink-500">
          Consejo: publica un proyecto y espera propuestas; cuando aceptes una se creará el contrato y podrás
          abrir la conversación.
        </p>
      ) : null}
      {isFreelancer && suggestionsQuery.isError ? (
        <ErrorAlert error={suggestionsQuery.error} title="No se pudieron cargar los proyectos sugeridos" />
      ) : null}
    </div>
  );
}

type MetricTone = "brand" | "accent" | "info" | "success";

const metricTones: Record<MetricTone, string> = {
  brand:
    "bg-brand-50 text-brand-700 ring-brand-100 dark:bg-brand-600/15 dark:text-brand-200 dark:ring-brand-600/30",
  accent:
    "bg-accent-50 text-accent-700 ring-accent-100 dark:bg-accent-600/15 dark:text-accent-200 dark:ring-accent-600/30",
  info: "bg-info-50 text-info-700 ring-info-100 dark:bg-info-600/15 dark:text-info-200 dark:ring-info-600/30",
  success:
    "bg-success-50 text-success-700 ring-success-100 dark:bg-success-600/15 dark:text-success-200 dark:ring-success-600/30",
};

function Metric({
  label,
  value,
  icon,
  hint,
  loading,
  tone = "brand",
}: {
  label: string;
  value: string;
  icon?: ReactNode;
  hint?: string;
  loading?: boolean;
  tone?: MetricTone;
}) {
  return (
    <div className="rounded-2xl border border-ink-200 bg-white p-4 shadow-card transition hover:shadow-float dark:border-slate-800 dark:bg-slate-900">
      <div className="flex items-start justify-between gap-2">
        <p className="text-xs font-medium text-ink-500">{label}</p>
        <span
          className={cn(
            "flex size-8 shrink-0 items-center justify-center rounded-lg ring-1 ring-inset",
            metricTones[tone],
          )}
        >
          {icon}
        </span>
      </div>
      {loading ? (
        <Skeleton className="mt-3 h-7 w-24" />
      ) : (
        <p className="mt-2 text-2xl font-bold tracking-tight text-ink-950 dark:text-white">{value}</p>
      )}
      {hint && <p className="mt-1 text-xs text-ink-500">{hint}</p>}
    </div>
  );
}
