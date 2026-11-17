import { Link } from "react-router-dom";
import type { ReactNode } from "react";
import { CreditCard, Wallet } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { useMyPayments } from "@/features/payments/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge } from "@/shared/components/Badge";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { CardSkeleton } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { paymentStatusLabels } from "@/shared/utils/labels";
import { formatDateTime, formatMoney, shortId } from "@/shared/utils/format";

export function PaymentsPage() {
  useDocumentTitle("Pagos");
  const { user } = useAuth();
  const query = useMyPayments();

  const payments = query.data ?? [];
  const released = payments
    .filter((payment) => payment.status === "RELEASED")
    .reduce((total, payment) => total + payment.netAmount.amount, 0);
  const pending = payments
    .filter((payment) => payment.status === "PENDING")
    .reduce((total, payment) => total + payment.amount.amount, 0);
  const currency = payments[0]?.amount.currency ?? "USD";

  return (
    <div className="space-y-6">
      <PageHeader
        title="Pagos"
        description="Pagos simulados del MVP: no se realizan cobros reales. Solo el cliente propietario puede liberar pagos de un contrato."
      />

      {query.isError ? (
        <ErrorAlert error={query.error} title="No se pudieron cargar los pagos" />
      ) : query.isLoading ? (
        <div className="grid gap-4 md:grid-cols-2">
          {Array.from({ length: 4 }).map((_, index) => (
            <CardSkeleton key={index} />
          ))}
        </div>
      ) : payments.length === 0 ? (
        <EmptyState
          title="Todavía no hay pagos"
          description="Los pagos se registran cuando el cliente libera fondos de un contrato."
          icon={<CreditCard className="size-6" aria-hidden="true" />}
          action={
            <Link to="/app/contratos" className="text-sm font-semibold text-brand-700 hover:underline">
              Ver mis contratos
            </Link>
          }
        />
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <Metric label="Pagos registrados" value={String(payments.length)} icon={<CreditCard className="size-4" />} />
            <Metric
              label={user?.role === "FREELANCER" ? "Neto liberado" : "Total liberado"}
              value={formatMoney(released, currency)}
              icon={<Wallet className="size-4" />}
            />
            <Metric label="Pendiente" value={formatMoney(pending, currency)} icon={<Wallet className="size-4" />} />
          </div>

          <div className="overflow-x-auto rounded-2xl border border-ink-200 bg-white shadow-soft">
            <table className="min-w-full divide-y divide-ink-200 text-sm">
              <caption className="sr-only">Historial de pagos</caption>
              <thead className="bg-ink-50 text-left text-xs uppercase tracking-wide text-ink-500">
                <tr>
                  <th scope="col" className="px-4 py-3 font-semibold">Fecha</th>
                  <th scope="col" className="px-4 py-3 font-semibold">Contrato</th>
                  <th scope="col" className="px-4 py-3 font-semibold">Monto</th>
                  <th scope="col" className="px-4 py-3 font-semibold">Comisión</th>
                  <th scope="col" className="px-4 py-3 font-semibold">Neto</th>
                  <th scope="col" className="px-4 py-3 font-semibold">Estado</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {payments.map((payment) => (
                  <tr key={payment.id} className="align-top">
                    <td className="whitespace-nowrap px-4 py-3 text-ink-600">{formatDateTime(payment.createdAt)}</td>
                    <td className="px-4 py-3">
                      <Link
                        to={`/app/contratos/${payment.contractId}`}
                        className="font-medium text-brand-700 hover:underline"
                      >
                        {shortId(payment.contractId)}
                      </Link>
                      <p className="text-xs text-ink-400">{payment.provider ?? "Pasarela simulada"}</p>
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 font-semibold text-ink-900">
                      {formatMoney(payment.amount.amount, payment.amount.currency)}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-ink-600">
                      {formatMoney(payment.commission.amount, payment.commission.currency)}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 font-semibold text-emerald-800">
                      {formatMoney(payment.netAmount.amount, payment.netAmount.currency)}
                    </td>
                    <td className="px-4 py-3">
                      <Badge
                        tone={
                          payment.status === "RELEASED"
                            ? "success"
                            : payment.status === "FAILED"
                              ? "danger"
                              : payment.status === "REFUNDED"
                                ? "neutral"
                                : "warning"
                        }
                      >
                        {paymentStatusLabels[payment.status]}
                      </Badge>
                      {payment.failureReason && (
                        <p className="mt-1 max-w-xs text-xs text-red-700">{payment.failureReason}</p>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}

function Metric({ label, value, icon }: { label: string; value: string; icon: ReactNode }) {
  return (
    <div className="rounded-2xl border border-ink-200 bg-white p-4 shadow-soft">
      <p className="flex items-center gap-1.5 text-xs font-medium text-ink-500">
        {icon}
        {label}
      </p>
      <p className="mt-1 text-lg font-bold text-ink-950">{value}</p>
    </div>
  );
}
