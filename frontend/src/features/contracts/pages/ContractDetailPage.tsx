import { useState, type ReactNode } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import {
  ArrowLeft,
  ArrowRight,
  CalendarDays,
  CreditCard,
  MessageSquareText,
  Star,
  XCircle,
} from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { ContractMilestoneCard } from "@/features/contracts/components/ContractMilestoneCard";
import { contractsApi } from "@/features/contracts/api";
import { useCancelContract, useContract } from "@/features/contracts/hooks";
import { useContractPayments, useReleasePayment } from "@/features/payments/hooks";
import { useContractReviews, useCreateReview } from "@/features/reviews/hooks";
import { useToast } from "@/components/ui/Toast";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge, StatusBadge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { Field, Input, Select, Textarea } from "@/shared/components/FormControls";
import { Modal } from "@/shared/components/Modal";
import { PageHeader } from "@/shared/components/PageHeader";
import { DetailSkeleton } from "@/shared/components/Spinner";
import { ProgressBar } from "@/shared/components/ProgressBar";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { contractStatusLabels, paymentStatusLabels } from "@/shared/utils/labels";
import { formatDate, formatDateTime, formatMoney, shortId } from "@/shared/utils/format";
import { cn } from "@/shared/utils/cn";

export function ContractDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const contractQuery = useContract(id);
  const paymentsQuery = useContractPayments(id);
  const reviewsQuery = useContractReviews(id);
  const cancelContract = useCancelContract(id ?? "");
  const releasePayment = useReleasePayment(id ?? "");
  const createReview = useCreateReview(id ?? "");
  const { toast } = useToast();

  const [cancelOpen, setCancelOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState("");
  const [paymentAmount, setPaymentAmount] = useState("");
  const [paymentError, setPaymentError] = useState<unknown>(null);
  const [reviewRating, setReviewRating] = useState("5");
  const [reviewComment, setReviewComment] = useState("");
  const [reviewError, setReviewError] = useState<unknown>(null);

  const contract = contractQuery.data;
  useDocumentTitle(contract?.title ?? "Contrato");

  const openConversation = useMutation({
    mutationFn: () => contractsApi.openConversation(id as string),
    onSuccess: (conversation) => navigate(`/app/mensajes/${conversation.id}`),
  });

  if (contractQuery.isLoading) return <DetailSkeleton />;
  if (contractQuery.isError) {
    return (
      <div className="space-y-4">
        <Link
          to="/app/contratos"
          className="inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800"
        >
          <ArrowLeft className="size-4" /> Volver a contratos
        </Link>
        <ErrorAlert error={contractQuery.error} title="No se pudo cargar el contrato" />
      </div>
    );
  }
  if (!contract || !user) return null;

  const isClient = contract.clientId === user.id;
  const isFreelancer = contract.freelancerId === user.id;
  if (!isClient && !isFreelancer) {
    return (
      <ErrorAlert
        error={new Error("Solo las partes del contrato pueden consultarlo.")}
        title="Acceso restringido"
      />
    );
  }

  const canReleasePayment =
    isClient && (contract.status === "ACTIVE" || contract.status === "COMPLETED");
  const myReview = reviewsQuery.data?.find((review) => review.authorId === user.id);
  const canReview = contract.status === "COMPLETED" && (isClient || isFreelancer) && !myReview;

  const submitPayment = async () => {
    setPaymentError(null);
    // El campo muestra el total del contrato cuando aún no se ha editado.
    const amount = Number(paymentAmount || contract.amount.amount);
    if (!Number.isFinite(amount) || amount <= 0) {
      setPaymentError(new Error("Introduce un monto mayor a cero."));
      return;
    }
    if (amount > contract.amount.amount) {
      setPaymentError(new Error("El monto no puede superar el total del contrato."));
      return;
    }
    try {
      await releasePayment.mutateAsync(amount);
      setPaymentAmount(String(contract.amount.amount));
      toast({
        tone: "success",
        title: "Pago liberado",
        description: "El pago simulado quedó registrado en el contrato.",
      });
    } catch (error) {
      setPaymentError(error);
    }
  };

  const submitReview = async () => {
    setReviewError(null);
    try {
      await createReview.mutateAsync({
        rating: Number(reviewRating),
        comment: reviewComment.trim() || undefined,
      });
      setReviewComment("");
      toast({
        tone: "success",
        title: "Reseña publicada",
        description: "La otra parte ya puede ver tu calificación.",
      });
    } catch (error) {
      setReviewError(error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <Link
          to="/app/contratos"
          className="inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800"
        >
          <ArrowLeft className="size-4" aria-hidden="true" /> Volver a contratos
        </Link>
        <div className="flex flex-wrap gap-2">
          <Button
            variant="outline"
            size="sm"
            icon={<MessageSquareText className="size-4" />}
            loading={openConversation.isPending}
            onClick={() => openConversation.mutate()}
          >
            Abrir conversación
          </Button>
          {(contract.status === "ACTIVE" || contract.status === "DRAFT") && (
            <Button
              variant="danger"
              size="sm"
              icon={<XCircle className="size-4" />}
              onClick={() => setCancelOpen(true)}
            >
              Cancelar contrato
            </Button>
          )}
        </div>
      </div>

      {openConversation.isError && (
        <ErrorAlert error={openConversation.error} title="No se pudo abrir la conversación" />
      )}

      <PageHeader
        eyebrow={`Contrato ${shortId(contract.id)}`}
        title={contract.title}
        description={contract.scope || "Sin alcance detallado."}
      />

      <Card className="overflow-hidden">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-2">
            <StatusBadge status={contract.status} label={contractStatusLabels[contract.status]} />
            <Badge tone="info">Progreso {contract.progress}%</Badge>
            <Badge tone="neutral">Hitos aprobados: {contract.approvedMilestones}</Badge>
            <Badge tone="brand">{isClient ? "Eres el cliente" : "Eres el freelancer"}</Badge>
          </div>
          <Link
            to={`/proyectos/${contract.projectId}`}
            className="inline-flex items-center gap-1 text-sm font-semibold text-brand-700 hover:underline"
          >
            Ver proyecto <ArrowRight className="size-4" aria-hidden="true" />
          </Link>
        </div>

        <div className="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <Metric
            label="Monto del contrato"
            value={formatMoney(contract.amount.amount, contract.amount.currency)}
            tone="brand"
          />
          <Metric
            label="Comisión"
            value={formatMoney(contract.commission.amount, contract.commission.currency)}
            tone="neutral"
          />
          <Metric
            label="Inicio"
            value={formatDate(contract.startDate)}
            icon={<CalendarDays className="size-4" />}
            tone="info"
          />
          <Metric
            label="Fin"
            value={formatDate(contract.endDate)}
            icon={<CalendarDays className="size-4" />}
            tone="accent"
          />
        </div>

        <ProgressBar value={contract.progress} label="Progreso global del contrato" className="mt-5" />
      </Card>

      <div className="grid gap-6 xl:grid-cols-[1fr_380px]">
        <div className="space-y-5">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-ink-950 dark:text-white">Hitos y entregables</h2>
            <span className="text-sm text-ink-500">{contract.milestones.length} hitos</span>
          </div>
          {contract.milestones.length === 0 ? (
            <EmptyState
              title="El contrato no tiene hitos"
              description="Esto no debería ocurrir; contacta con soporte."
            />
          ) : (
            contract.milestones.map((milestone) => (
              <ContractMilestoneCard
                key={milestone.id}
                contract={contract}
                milestone={milestone}
                role={user.role}
              />
            ))
          )}
        </div>

        <aside className="space-y-6 xl:sticky xl:top-20 xl:self-start">
          <Card>
            <CardHeader
              title="Pagos"
              description="Pasarela simulada; no se realizan cobros reales."
            />
            {canReleasePayment && (
              <div className="mt-4 space-y-3 rounded-xl border border-ink-200 bg-ink-50 p-3 dark:border-slate-700 dark:bg-slate-950/60">
                {paymentError ? <ErrorAlert error={paymentError} /> : null}
                {releasePayment.isSuccess && !paymentError ? (
                  <div className="flex items-start gap-2 rounded-lg bg-success-50 p-2.5 text-sm text-success-800 ring-1 ring-inset ring-success-200" role="status">
                    <CreditCard className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
                    <p>Pago registrado correctamente.</p>
                  </div>
                ) : null}
                <Field label="Monto a liberar" htmlFor="payment-amount">
                  <Input
                    id="payment-amount"
                    type="number"
                    min="0.01"
                    max={contract.amount.amount}
                    step="0.01"
                    value={paymentAmount || String(contract.amount.amount)}
                    onChange={(event) => setPaymentAmount(event.target.value)}
                  />
                </Field>
                <Button
                  className="w-full"
                  icon={<CreditCard className="size-4" />}
                  loading={releasePayment.isPending}
                  onClick={submitPayment}
                >
                  Liberar pago
                </Button>
              </div>
            )}
            {paymentsQuery.isLoading ? (
              <p className="mt-4 text-sm text-ink-500">Cargando pagos…</p>
            ) : paymentsQuery.isError ? (
              <ErrorAlert error={paymentsQuery.error} className="mt-4" />
            ) : (paymentsQuery.data ?? []).length === 0 ? (
              <p className="mt-4 text-sm text-ink-500">Aún no hay pagos registrados.</p>
            ) : (
              <ul className="mt-4 space-y-3">
                {(paymentsQuery.data ?? []).map((payment) => (
                  <li key={payment.id} className="rounded-xl border border-ink-200 p-3 text-sm">
                    <div className="flex items-start justify-between gap-2">
                      <span className="font-semibold text-ink-900">
                        {formatMoney(payment.amount.amount, payment.amount.currency)}
                      </span>
                      <Badge
                        tone={
                          payment.status === "RELEASED"
                            ? "success"
                            : payment.status === "FAILED"
                              ? "danger"
                              : "warning"
                        }
                      >
                        {paymentStatusLabels[payment.status]}
                      </Badge>
                    </div>
                    <p className="mt-1 text-xs text-ink-500">
                      Neto {formatMoney(payment.netAmount.amount, payment.netAmount.currency)} ·
                      Comisión {formatMoney(payment.commission.amount, payment.commission.currency)}
                    </p>
                    <p className="mt-1 text-xs text-ink-400">{formatDateTime(payment.createdAt)}</p>
                  </li>
                ))}
              </ul>
            )}
          </Card>

          <Card>
            <CardHeader
              title="Reseñas"
              description="Solo disponible cuando el contrato está completado."
            />
            {reviewsQuery.isLoading ? (
              <p className="mt-4 text-sm text-ink-500">Cargando reseñas…</p>
            ) : reviewsQuery.isError ? (
              <ErrorAlert error={reviewsQuery.error} className="mt-4" />
            ) : (
              <>
                {(reviewsQuery.data ?? []).length === 0 ? (
                  <p className="mt-4 text-sm text-ink-500">Sin reseñas.</p>
                ) : (
                  <ul className="mt-4 space-y-3">
                    {(reviewsQuery.data ?? []).map((review) => (
                      <li key={review.id} className="rounded-xl border border-ink-200 p-3 text-sm">
                        <div
                          className="flex items-center gap-1 text-amber-500"
                          aria-label={`${review.rating} de 5 estrellas`}
                        >
                          {Array.from({ length: 5 }).map((_, index) => (
                            <Star
                              key={index}
                              className={`size-4 ${index < review.rating ? "fill-amber-400" : "text-ink-300"}`}
                              aria-hidden="true"
                            />
                          ))}
                        </div>
                        {review.comment && <p className="mt-1 text-ink-700">{review.comment}</p>}
                        <p className="mt-1 text-xs text-ink-400">
                          {shortId(review.authorId)} · {formatDateTime(review.createdAt)}
                        </p>
                      </li>
                    ))}
                  </ul>
                )}
                {canReview && (
                  <div className="mt-4 space-y-3 border-t border-ink-200 pt-4">
                    {reviewError ? <ErrorAlert error={reviewError} /> : null}
                    <Field label="Calificación" htmlFor="review-rating">
                      <Select
                        id="review-rating"
                        value={reviewRating}
                        onChange={(event) => setReviewRating(event.target.value)}
                      >
                        {[5, 4, 3, 2, 1].map((rating) => (
                          <option key={rating} value={rating}>
                            {rating} de 5
                          </option>
                        ))}
                      </Select>
                    </Field>
                    <Field label="Comentario" htmlFor="review-comment">
                      <Textarea
                        id="review-comment"
                        rows={3}
                        maxLength={2000}
                        value={reviewComment}
                        onChange={(event) => setReviewComment(event.target.value)}
                      />
                    </Field>
                    <Button
                      className="w-full"
                      loading={createReview.isPending}
                      onClick={submitReview}
                    >
                      Enviar reseña
                    </Button>
                  </div>
                )}
                {myReview && (
                  <p className="mt-4 text-xs text-ink-500">
                    Ya enviaste tu reseña para este contrato.
                  </p>
                )}
              </>
            )}
          </Card>

          <Card>
            <CardHeader title="Participantes" />
            <dl className="mt-3 space-y-2 text-sm">
              <div>
                <dt className="text-ink-500">Cliente</dt>
                <dd className="font-mono text-xs text-ink-800">{contract.clientId}</dd>
              </div>
              <div>
                <dt className="text-ink-500">Freelancer</dt>
                <dd className="font-mono text-xs text-ink-800">{contract.freelancerId}</dd>
              </div>
            </dl>
          </Card>
        </aside>
      </div>

      <Modal
        open={cancelOpen}
        onClose={() => setCancelOpen(false)}
        title="Cancelar contrato"
        description="El contrato pasará a estado CANCELLED. El motivo es obligatorio."
        size="sm"
        footer={
          <>
            <Button
              variant="outline"
              onClick={() => setCancelOpen(false)}
              disabled={cancelContract.isPending}
            >
              Volver
            </Button>
            <Button
              variant="danger"
              loading={cancelContract.isPending}
              disabled={!cancelReason.trim()}
              onClick={() => {
                if (!cancelReason.trim()) return;
                cancelContract.mutate(cancelReason.trim(), {
                  onSuccess: () => {
                    setCancelOpen(false);
                    setCancelReason("");
                    toast({
                      tone: "success",
                      title: "Contrato cancelado",
                      description: "El contrato pasó a estado CANCELLED.",
                    });
                  },
                });
              }}
            >
              Cancelar contrato
            </Button>
          </>
        }
      >
        <Field label="Motivo" htmlFor="cancel-reason" required>
          <Textarea
            id="cancel-reason"
            maxLength={500}
            value={cancelReason}
            onChange={(event) => setCancelReason(event.target.value)}
          />
        </Field>
        {cancelContract.isError ? (
          <ErrorAlert error={cancelContract.error} className="mt-3" />
        ) : null}
      </Modal>
    </div>
  );
}

type MetricTone = "brand" | "accent" | "info" | "neutral";

const metricTones: Record<MetricTone, string> = {
  brand: "border-brand-100 bg-brand-50/70 dark:border-brand-800 dark:bg-brand-950/30",
  accent: "border-accent-100 bg-accent-50/70 dark:border-accent-800 dark:bg-accent-900/20",
  info: "border-info-100 bg-info-50/70 dark:border-info-700 dark:bg-info-900/20",
  neutral: "border-ink-200 bg-ink-50/70 dark:border-slate-800 dark:bg-slate-950/40",
};

function Metric({
  label,
  value,
  icon,
  tone = "neutral",
}: {
  label: string;
  value: string;
  icon?: ReactNode;
  tone?: MetricTone;
}) {
  return (
    <div className={cn("rounded-xl border p-3", metricTones[tone])}>
      <p className="flex items-center gap-1.5 text-xs font-medium text-ink-500">
        {icon}
        {label}
      </p>
      <p className="mt-1 text-lg font-bold tracking-tight text-ink-950 dark:text-white">{value}</p>
    </div>
  );
}
