import { LoaderCircle } from "lucide-react";
import { cn } from "@/shared/utils/cn";

export function Spinner({ className, label = "Cargando" }: { className?: string; label?: string }) {
  return (
    <span className={cn("inline-flex items-center gap-2 text-sm text-ink-500", className)} role="status">
      <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
      <span>{label}…</span>
    </span>
  );
}

export function PageLoader({ label = "Cargando contenido" }: { label?: string }) {
  return (
    <div className="flex min-h-60 items-center justify-center" role="status">
      <Spinner label={label} />
    </div>
  );
}

export function Skeleton({ className }: { className?: string }) {
  return <div className={cn("animate-pulse rounded-lg bg-ink-200/70", className)} aria-hidden="true" />;
}

export function CardSkeleton() {
  return (
    <div className="space-y-3 rounded-2xl border border-ink-200 bg-white p-5">
      <Skeleton className="h-5 w-2/3" />
      <Skeleton className="h-4 w-full" />
      <Skeleton className="h-4 w-4/5" />
      <div className="flex gap-2 pt-2">
        <Skeleton className="h-6 w-20 rounded-full" />
        <Skeleton className="h-6 w-24 rounded-full" />
      </div>
    </div>
  );
}
