import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "./Button";

export function Pagination({
  page,
  totalPages,
  totalElements,
  onPageChange,
  label = "elementos",
}: {
  page: number;
  totalPages: number;
  totalElements: number;
  onPageChange: (page: number) => void;
  label?: string;
}) {
  if (totalPages <= 1) {
    return (
      <p className="text-sm text-ink-500">
        {totalElements} {label}
      </p>
    );
  }
  return (
    <nav
      className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-ink-200 bg-white px-4 py-3 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      aria-label="Paginación"
    >
      <p className="text-sm text-ink-500">
        Página <strong className="text-ink-800 dark:text-slate-200">{page + 1}</strong> de{" "}
        <strong className="text-ink-800 dark:text-slate-200">{totalPages}</strong> · {totalElements} {label}
      </p>
      <div className="flex gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(page - 1)}
          disabled={page <= 0}
          aria-label="Página anterior"
        >
          <ChevronLeft className="size-4" aria-hidden="true" />
          Anterior
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(page + 1)}
          disabled={page + 1 >= totalPages}
          aria-label="Página siguiente"
        >
          Siguiente
          <ChevronRight className="size-4" aria-hidden="true" />
        </Button>
      </div>
    </nav>
  );
}
