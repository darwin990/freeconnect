import type { HTMLAttributes, ReactNode } from "react";
import { cn } from "@/shared/utils/cn";

type CardVariant = "default" | "muted" | "interactive";

const variants: Record<CardVariant, string> = {
  default: "border-ink-200 bg-white shadow-card dark:border-slate-800 dark:bg-slate-900",
  muted: "border-ink-200 bg-ink-50/70 shadow-none dark:border-slate-800 dark:bg-slate-900/60",
  interactive:
    "border-ink-200 bg-white shadow-card transition duration-200 ease-smooth hover:-translate-y-0.5 hover:border-brand-200 hover:shadow-float dark:border-slate-800 dark:bg-slate-900 dark:hover:border-brand-700",
};

export function Card({
  className,
  variant = "default",
  ...props
}: HTMLAttributes<HTMLDivElement> & { variant?: CardVariant }) {
  return <div className={cn("rounded-2xl border p-5", variants[variant], className)} {...props} />;
}

export function CardHeader({
  title,
  description,
  action,
  className,
}: {
  title: ReactNode;
  description?: ReactNode;
  action?: ReactNode;
  className?: string;
}) {
  return (
    <div className={cn("flex flex-wrap items-start justify-between gap-3", className)}>
      <div className="min-w-0">
        <h2 className="text-lg font-semibold text-ink-900 dark:text-slate-100">{title}</h2>
        {description && (
          <p className="mt-1 max-w-2xl text-sm leading-relaxed text-ink-500">{description}</p>
        )}
      </div>
      {action}
    </div>
  );
}
