import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { CalendarDays, UserRound, Wallet } from "lucide-react";
import { useContracts } from "@/features/contracts/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { StatusBadge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { CardSkeleton } from "@/shared/components/Spinner";
import { ProgressBar } from "@/shared/components/ProgressBar";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { contractStatusLabels } from "@/shared/utils/labels";
import { formatDate, formatMoney, shortId } from "@/shared/utils/format";
import { cn } from "@/shared/utils/cn";

type Filter = "ALL" | "ACTIVE" | "COMPLETED" | "CANCELLED";

export function ContractsPage() {
  useDocumentTitle("Contratos");
  const contractsQuery = useContracts();
  const [filter, setFilter] = useState<Filter>("ALL");

  const contracts = useMemo(() => {
    const list = contractsQuery.data ?? [];
    return filter === "ALL" ? list : list.filter((contract) => contract.status === filter);
  }, [contractsQuery.data, filter]);

  const counts = useMemo(() => {
    const list = contractsQuery.data ?? [];
    return {
      ALL: list.length,
      ACTIVE: list.filter((contract) => contract.status === "ACTIVE").length,
      COMPLETED: list.filter((contract) => contract.status === "COMPLETED").length,
      CANCELLED: list.filter((contract) => contract.status === "CANCELLED").length,
    };
  }, [contractsQuery.data]);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Mis contratos"
        description="Consulta hitos, entregables, pagos y reseñas de cada contrato."
      />

      <div className="flex flex-wrap gap-2" role="tablist" aria-label="Filtrar contratos por estado">
        {(
          [
            ["ALL", "Todos"],
            ["ACTIVE", "Activos"],
            ["COMPLETED", "Completados"],
            ["CANCELLED", "Cancelados"],
          ] as [Filter, string][]
        ).map(([value, label]) => (
          <Button
            key={value}
            size="sm"
            variant={filter === value ? "primary" : "outline"}
            role="tab"
            aria-selected={filter === value}
            onClick={() => setFilter(value)}
          >
            {label} ({counts[value]})
          </Button>
        ))}
      </div>

      {contractsQuery.isError ? (
        <ErrorAlert error={contractsQuery.error} title="No se pudieron cargar los contratos" />
      ) : contractsQuery.isLoading ? (
        <div className="grid gap-4 md:grid-cols-2">
          {Array.from({ length: 4 }).map((_, index) => (
            <CardSkeleton key={index} />
          ))}
        </div>
      ) : contracts.length === 0 ? (
        <EmptyState
          title="No hay contratos en esta vista"
          description="Los contratos se crean cuando un cliente acepta una propuesta."
          action={
            <Link to="/proyectos">
              <Button variant="outline">Explorar proyectos</Button>
            </Link>
          }
        />
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {contracts.map((contract) => (
            <article
              key={contract.id}
              className={cn(
                "flex h-full flex-col rounded-2xl border bg-white p-5 shadow-soft",
                contract.status === "ACTIVE" ? "border-brand-200" : "border-ink-200",
              )}
            >
              <div className="flex flex-wrap items-start justify-between gap-2">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-ink-400">
                    Contrato {shortId(contract.id)}
                  </p>
                  <h2 className="mt-1 text-lg font-semibold text-ink-950">
                    <Link to={`/app/contratos/${contract.id}`} className="hover:text-brand-700">
                      {contract.title}
                    </Link>
                  </h2>
                </div>
                <StatusBadge status={contract.status} label={contractStatusLabels[contract.status]} />
              </div>

              <div className="mt-4 flex flex-wrap gap-x-4 gap-y-2 text-sm text-ink-600">
                <span className="inline-flex items-center gap-1.5">
                  <Wallet className="size-4 text-ink-400" aria-hidden="true" />
                  {formatMoney(contract.amount.amount, contract.amount.currency)}
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <CalendarDays className="size-4 text-ink-400" aria-hidden="true" />
                  {formatDate(contract.startDate)} → {formatDate(contract.endDate)}
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <UserRound className="size-4 text-ink-400" aria-hidden="true" />
                  {shortId(contract.freelancerId)}
                </span>
              </div>

              <ProgressBar value={contract.progress} label={`Hitos aprobados: ${contract.approvedMilestones}`} className="mt-4" />

              <div className="mt-auto pt-5">
                <Link to={`/app/contratos/${contract.id}`} className="text-sm font-semibold text-brand-700 hover:underline">
                  Abrir contrato
                </Link>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
