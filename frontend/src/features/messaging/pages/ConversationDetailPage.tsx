import { useEffect, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { Archive, ArrowLeft, Send } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import {
  useArchiveConversation,
  useConversation,
  useMarkConversationRead,
  useSendMessage,
} from "@/features/messaging/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { Field, Textarea } from "@/shared/components/FormControls";
import { ConfirmDialog } from "@/shared/components/Modal";
import { PageLoader } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { formatDateTime, shortId } from "@/shared/utils/format";
import { cn } from "@/shared/utils/cn";

export function ConversationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const query = useConversation(id);
  const markRead = useMarkConversationRead(id ?? "");
  const sendMessage = useSendMessage(id ?? "");
  const archive = useArchiveConversation(id ?? "");

  const [body, setBody] = useState("");
  const [sendError, setSendError] = useState<unknown>(null);
  const [archiveOpen, setArchiveOpen] = useState(false);
  const markedRef = useRef<string | null>(null);
  const endRef = useRef<HTMLDivElement>(null);

  const conversation = query.data;
  useDocumentTitle(conversation?.subject ?? "Conversación");

  useEffect(() => {
    markedRef.current = null;
  }, [id]);

  useEffect(() => {
    if (!conversation || conversation.unread <= 0 || conversation.status !== "ACTIVE") return;
    if (markedRef.current === conversation.id) return;
    markedRef.current = conversation.id;
    markRead.mutate();
  }, [conversation, markRead]);

  const messageCount = conversation?.messages?.length ?? 0;
  useEffect(() => {
    endRef.current?.scrollIntoView({ block: "end" });
  }, [messageCount]);

  if (query.isLoading) return <PageLoader label="Cargando conversación" />;
  if (query.isError) {
    return (
      <div className="space-y-4">
        <Link to="/app/mensajes" className="inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
          <ArrowLeft className="size-4" aria-hidden="true" /> Volver a mensajes
        </Link>
        <ErrorAlert error={query.error} title="No se pudo cargar la conversación" />
      </div>
    );
  }
  if (!conversation || !user) return null;

  const isOpen = conversation.status === "ACTIVE";

  const submit = async () => {
    const value = body.trim();
    if (!value) return;
    setSendError(null);
    try {
      await sendMessage.mutateAsync(value);
      setBody("");
    } catch (error) {
      setSendError(error);
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <Link to="/app/mensajes" className="inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
          <ArrowLeft className="size-4" aria-hidden="true" /> Volver a mensajes
        </Link>
        <div className="flex flex-wrap items-center gap-2">
          <Link
            to={`/app/contratos/${conversation.contractId}`}
            className="text-sm font-semibold text-brand-700 hover:underline"
          >
            Ver contrato
          </Link>
          {isOpen && (
            <Button variant="outline" size="sm" icon={<Archive className="size-4" />} onClick={() => setArchiveOpen(true)}>
              Archivar
            </Button>
          )}
        </div>
      </div>

      <div>
        <p className="text-xs font-semibold uppercase tracking-wider text-brand-700">
          Contrato {shortId(conversation.contractId)}
        </p>
        <h1 className="mt-1 text-2xl font-bold tracking-tight text-ink-950">
          {conversation.subject || "Conversación del contrato"}
        </h1>
        <div className="mt-2 flex flex-wrap items-center gap-2">
          <Badge tone={isOpen ? "success" : "neutral"}>{isOpen ? "Activa" : "Archivada"}</Badge>
          <span className="text-xs text-ink-500">Cliente {shortId(conversation.clientId)}</span>
          <span className="text-xs text-ink-500">Freelancer {shortId(conversation.freelancerId)}</span>
          {conversation.unread > 0 && <Badge tone="danger">{conversation.unread} sin leer</Badge>}
        </div>
      </div>

      {archive.isError ? <ErrorAlert error={archive.error} title="No se pudo archivar la conversación" /> : null}

      <Card className="p-0">
        <div
          className="max-h-[55vh] min-h-72 space-y-3 overflow-y-auto p-5"
          aria-live="polite"
          aria-label="Mensajes de la conversación"
        >
          {messageCount === 0 ? (
            <EmptyState
              title="Aún no hay mensajes"
              description="Escribe el primero para coordinar el trabajo del contrato."
            />
          ) : (
            conversation.messages?.map((message) => {
              const mine = message.senderId === user.id;
              return (
                <div key={message.id} className={cn("flex", mine ? "justify-end" : "justify-start")}>
                  <div
                    className={cn(
                      "max-w-[85%] rounded-2xl px-4 py-2.5 text-sm shadow-sm sm:max-w-[70%]",
                      mine ? "bg-brand-700 text-white" : "border border-ink-200 bg-ink-50 text-ink-900",
                    )}
                  >
                    <p className="whitespace-pre-line break-words">{message.body}</p>
                    <p className={cn("mt-1 text-[11px]", mine ? "text-brand-100" : "text-ink-500")}>
                      {mine ? "Tú" : shortId(message.senderId)} · {formatDateTime(message.sentAt)}
                      {message.readAt ? " · leído" : ""}
                    </p>
                  </div>
                </div>
              );
            })
          )}
          <div ref={endRef} />
        </div>

        <div className="border-t border-ink-200 p-5">
          {sendError ? <ErrorAlert error={sendError} title="No se pudo enviar el mensaje" className="mb-3" /> : null}
          {isOpen ? (
            <form
              className="space-y-3"
              onSubmit={(event) => {
                event.preventDefault();
                void submit();
              }}
            >
              <Field label="Nuevo mensaje" htmlFor="message-body">
                <Textarea
                  id="message-body"
                  rows={3}
                  maxLength={4000}
                  placeholder="Escribe un mensaje para la otra parte…"
                  value={body}
                  onChange={(event) => setBody(event.target.value)}
                />
              </Field>
              <div className="flex items-center justify-between gap-3">
                <span className="text-xs text-ink-500">{body.length}/4000</span>
                <Button
                  type="submit"
                  icon={<Send className="size-4" />}
                  loading={sendMessage.isPending}
                  disabled={!body.trim()}
                >
                  Enviar mensaje
                </Button>
              </div>
            </form>
          ) : (
            <p className="text-sm text-ink-500">
              Esta conversación está archivada y no admite mensajes nuevos.
            </p>
          )}
        </div>
      </Card>

      <ConfirmDialog
        open={archiveOpen}
        onClose={() => setArchiveOpen(false)}
        title="Archivar conversación"
        description="La conversación quedará en estado ARCHIVED y no se podrán enviar más mensajes."
        confirmLabel="Archivar"
        loading={archive.isPending}
        onConfirm={() => {
          archive.mutate(undefined, { onSuccess: () => setArchiveOpen(false) });
        }}
      >
        {archive.isError ? <ErrorAlert error={archive.error} /> : null}
      </ConfirmDialog>
    </div>
  );
}
