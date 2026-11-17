import { cn } from "@/shared/utils/cn";
import { formatPercent } from "@/shared/utils/format";

export function ProgressBar({
  value,
  label,
  className,
}: {
  value: number;
  label?: string;
  className?: string;
}) {
  const clamped = Math.max(0, Math.min(100, value));
  return (
    <div className={className}>
      <div className="mb-1 flex justify-between text-xs text-ink-500">
        <span>{label ?? "Progreso"}</span>
        <span className="font-semibold text-ink-700">{formatPercent(clamped)}</span>
      </div>
      <div
        className="h-2 overflow-hidden rounded-full bg-ink-200"
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={clamped}
        aria-label={label ?? "Progreso"}
      >
        <div
          className={cn("h-full rounded-full bg-brand-600 transition-all")}
          style={{ width: `${clamped}%` }}
        />
      </div>
    </div>
  );
}
