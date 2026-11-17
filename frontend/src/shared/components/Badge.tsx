import type { ReactNode } from "react";
import { cn } from "@/shared/utils/cn";

export type BadgeTone = "neutral" | "info" | "success" | "warning" | "danger" | "brand" | "accent";

const tones: Record<BadgeTone, string> = {
  neutral:
    "bg-ink-100 text-ink-700 ring-ink-200 dark:bg-slate-800 dark:text-slate-200 dark:ring-slate-700",
  info: "bg-info-50 text-info-700 ring-info-200 dark:bg-info-600/15 dark:text-info-200 dark:ring-info-600/30",
  success:
    "bg-success-50 text-success-700 ring-success-200 dark:bg-success-600/15 dark:text-success-200 dark:ring-success-600/30",
  warning:
    "bg-warning-50 text-warning-700 ring-warning-200 dark:bg-warning-600/15 dark:text-warning-200 dark:ring-warning-600/30",
  danger:
    "bg-danger-50 text-danger-700 ring-danger-200 dark:bg-danger-600/15 dark:text-danger-200 dark:ring-danger-600/30",
  brand:
    "bg-brand-50 text-brand-800 ring-brand-200 dark:bg-brand-600/15 dark:text-brand-200 dark:ring-brand-600/30",
  accent:
    "bg-accent-50 text-accent-800 ring-accent-200 dark:bg-accent-600/15 dark:text-accent-200 dark:ring-accent-600/30",
};

const dotColors: Record<BadgeTone, string> = {
  neutral: "bg-ink-400",
  info: "bg-info-600",
  success: "bg-success-600",
  warning: "bg-warning-600",
  danger: "bg-danger-600",
  brand: "bg-brand-600",
  accent: "bg-accent-600",
};

export function Badge({
  children,
  tone = "neutral",
  dot = false,
  className,
}: {
  children: ReactNode;
  tone?: BadgeTone;
  dot?: boolean;
  className?: string;
}) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset",
        tones[tone],
        className,
      )}
    >
      {dot ? <span className={cn("size-1.5 rounded-full", dotColors[tone])} aria-hidden="true" /> : null}
      {children}
    </span>
  );
}

export function toneForStatus(status?: string | null): BadgeTone {
  switch (status) {
    case "PUBLISHED":
    case "ACTIVE":
    case "APPROVED":
    case "RELEASED":
    case "SENT":
    case "AVAILABLE":
    case "FINISHED":
      return "success";
    case "SUBMITTED":
    case "IN_PROGRESS":
    case "IN_REVIEW":
    case "PART_TIME":
      return "info";
    case "DRAFT":
    case "PENDING":
    case "PAUSED":
      return "warning";
    case "REJECTED":
    case "CANCELLED":
    case "FAILED":
    case "WITHDRAWN":
    case "UNAVAILABLE":
      return "danger";
    case "ACCEPTED":
      return "brand";
    case "ARCHIVED":
    case "SUSPENDED":
      return "neutral";
    default:
      return "neutral";
  }
}

export function StatusBadge({
  status,
  label,
  tone,
}: {
  status: string;
  label?: string;
  tone?: BadgeTone;
}) {
  return (
    <Badge tone={tone ?? toneForStatus(status)} dot>
      {label ?? status}
    </Badge>
  );
}
