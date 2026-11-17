import { Bell, RotateCcw } from "lucide-react";
import { useNotifications, useRetryNotification } from "@/features/notifications/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge, toneForStatus } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { CardSkeleton } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { notificationTypeLabels } from "@/shared/utils/labels";
import { formatDateTime } from "@/shared/utils/format";

export function NotificationsPage() {
  useDocumentTitle("Notificaciones");
  const query = useNotifications();
  const retry = useRetryNotification();

  const notifications = query.data ?? [];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Notificaciones"
        description="Bandeja de notificaciones internas. Puedes reintentar la entrega de las que fallaron. La bandeja se refresca periódicamente."
        actions={
          <Button variant="outline" size="sm" onClick={() => void query.refetch()} loading={query.isFetching}>
            Actualizar
          </Button>
        }
      />

      {retry.isError ? <ErrorAlert error={retry.error} title="No se pudo reintentar la notificación" /> : null}

      {query.isError ? (
        <ErrorAlert error={query.error} title="No se pudieron cargar las notificaciones" />
      ) : query.isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, index) => (
            <CardSkeleton key={index} />
          ))}
        </div>
      ) : notifications.length === 0 ? (
        <EmptyState
          title="No tienes notificaciones"
          description="Las notificaciones se generan al recibir propuestas, mensajes, pagos o cambios en tus contratos."
          icon={<Bell className="size-6" aria-hidden="true" />}
        />
      ) : (
        <ul className="space-y-3">
          {notifications.map((notification) => (
            <li key={notification.id} className="rounded-2xl border border-ink-200 bg-white p-5 shadow-soft">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="min-w-0">
                  <p className="text-xs font-semibold uppercase tracking-wide text-brand-700">
                    {notificationTypeLabels[notification.type]}
                  </p>
                  <h2 className="mt-1 text-base font-semibold text-ink-950">{notification.title}</h2>
                  <p className="mt-1 text-sm text-ink-600">{notification.body}</p>
                </div>
                <Badge tone={toneForStatus(notification.status)}>
                  {notification.status === "SENT"
                    ? "Enviada"
                    : notification.status === "PENDING"
                      ? "Pendiente"
                      : "Fallida"}
                </Badge>
              </div>

              <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs text-ink-500">
                <span>Canal: {notification.channel}</span>
                <span>Intentos: {notification.attempts}</span>
                <span>Creada: {formatDateTime(notification.createdAt)}</span>
                {notification.sentAt && <span>Enviada: {formatDateTime(notification.sentAt)}</span>}
              </div>

              {notification.lastError && (
                <p className="mt-3 rounded-lg bg-red-50 p-3 text-xs text-red-800">
                  Último error: {notification.lastError}
                </p>
              )}

              {notification.status !== "SENT" && (
                <div className="mt-3">
                  <Button
                    size="sm"
                    variant="outline"
                    icon={<RotateCcw className="size-4" />}
                    loading={retry.isPending && retry.variables === notification.id}
                    onClick={() => retry.mutate(notification.id)}
                  >
                    Reintentar entrega
                  </Button>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
