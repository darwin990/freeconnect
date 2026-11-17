import type { ReactNode } from "react";
import { Inbox } from "lucide-react";
import { cn } from "@/shared/utils/cn";

/**
 * Estado vacío con ilustración decorativa propia (sin imágenes externas).
 * Mantiene el título y el CTA como contenido principal para accesibilidad.
 */
export function EmptyState({
  title,
  description,
  action,
  icon,
  className,
}: {
  title: string;
  description?: string;
  action?: ReactNode;
  icon?: ReactNode;
  className?: string;
}) {
  return (
    <div
      className={cn(
        "relative flex flex-col items-center justify-center overflow-hidden rounded-2xl border border-dashed border-ink-300 bg-white/70 px-6 py-14 text-center dark:border-slate-700 dark:bg-slate-900/60",
        className,
      )}
    >
      <div className="pointer-events-none absolute inset-0 bg-grid opacity-40" aria-hidden="true" />
      <div className="relative">
        <svg
          viewBox="0 0 160 96"
          className="mx-auto mb-4 h-24 w-40 text-brand-200 dark:text-brand-900"
          aria-hidden="true"
        >
          <ellipse cx="80" cy="82" rx="56" ry="8" fill="currentColor" opacity="0.35" />
          <rect x="30" y="20" width="100" height="56" rx="12" fill="white" stroke="currentColor" strokeWidth="2" />
          <rect x="42" y="34" width="46" height="6" rx="3" fill="currentColor" opacity="0.55" />
          <rect x="42" y="46" width="66" height="6" rx="3" fill="currentColor" opacity="0.35" />
          <rect x="42" y="58" width="34" height="6" rx="3" fill="currentColor" opacity="0.25" />
          <circle cx="118" cy="56" r="16" fill="var(--tw-gradient-from, #d8edff)" opacity="0.8" />
          <path d="M112 56h12M118 50v12" stroke="#0d6ef0" strokeWidth="2.5" strokeLinecap="round" />
        </svg>
        <div className="mx-auto mb-4 flex size-12 items-center justify-center rounded-2xl bg-brand-50 text-brand-700 ring-1 ring-inset ring-brand-100 dark:bg-brand-600/15 dark:text-brand-200 dark:ring-brand-600/30">
          {icon ?? <Inbox className="size-6" aria-hidden="true" />}
        </div>
        <h2 className="text-base font-semibold text-ink-900 dark:text-slate-100">{title}</h2>
        {description && (
          <p className="mx-auto mt-1 max-w-md text-sm leading-relaxed text-ink-500">{description}</p>
        )}
        {action && <div className="mt-5">{action}</div>}
      </div>
    </div>
  );
}
