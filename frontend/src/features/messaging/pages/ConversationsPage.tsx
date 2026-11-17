import { Link } from "react-router-dom";
import { MessagesSquare, Users } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { useConversations } from "@/features/messaging/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { CardSkeleton } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { formatDateTime, shortId } from "@/shared/utils/format";
import type { Conversation } from "@/types/models";

function counterpartLabel(conversation: Conversation, userId?: string) {
  const other = conversation.clientId === userId ? conversation.freelancerId : conversation.clientId;
  return shortId(other);
}

export function ConversationsPage() {
  useDocumentTitle("Mensajes");
  const { user } = useAuth();
  const query = useConversations();

  const conversations = [...(query.data ?? [])].sort((a, b) =>
    (b.lastMessageAt ?? b.updatedAt).localeCompare(a.lastMessageAt ?? a.updatedAt),
  );

  return (
    <div className="space-y-6">
      <PageHeader
        title="Mensajes"
        description="Conversaciones vinculadas a contratos. La lista se actualiza automáticamente cada pocos segundos."
      />

      {query.isError ? (
        <ErrorAlert error={query.error} title="No se pudieron cargar las conversaciones" />
      ) : query.isLoading ? (
        <div className="grid gap-4 md:grid-cols-2">
          {Array.from({ length: 4 }).map((_, index) => (
            <CardSkeleton key={index} />
          ))}
        </div>
      ) : conversations.length === 0 ? (
        <EmptyState
          title="Todavía no tienes conversaciones"
          description="Las conversaciones se abren desde un contrato activo con la contraparte."
          icon={<MessagesSquare className="size-6" aria-hidden="true" />}
          action={
            <Link to="/app/contratos">
              <Button variant="outline">Ir a mis contratos</Button>
            </Link>
          }
        />
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {conversations.map((conversation) => (
            <article
              key={conversation.id}
              className="flex h-full flex-col rounded-2xl border border-ink-200 bg-white p-5 shadow-soft"
            >
              <div className="flex flex-wrap items-start justify-between gap-2">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-ink-400">
                    Contrato {shortId(conversation.contractId)}
                  </p>
                  <h2 className="mt-1 text-lg font-semibold text-ink-950">
                    <Link to={`/app/mensajes/${conversation.id}`} className="hover:text-brand-700">
                      {conversation.subject || "Conversación del contrato"}
                    </Link>
                  </h2>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  {conversation.unread > 0 && <Badge tone="danger">{conversation.unread} sin leer</Badge>}
                  <Badge tone={conversation.status === "ACTIVE" ? "success" : "neutral"}>
                    {conversation.status === "ACTIVE" ? "Activa" : "Archivada"}
                  </Badge>
                </div>
              </div>

              <p className="mt-3 flex items-center gap-2 text-sm text-ink-600">
                <Users className="size-4 text-ink-400" aria-hidden="true" />
                Contraparte: {counterpartLabel(conversation, user?.id)}
              </p>
              <p className="mt-1 text-xs text-ink-500">
                Última actividad: {formatDateTime(conversation.lastMessageAt ?? conversation.updatedAt)}
              </p>

              <div className="mt-auto pt-5">
                <Link
                  to={`/app/mensajes/${conversation.id}`}
                  className="text-sm font-semibold text-brand-700 hover:underline"
                >
                  Abrir conversación
                </Link>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
