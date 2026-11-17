import type { ReactNode } from "react";
import { cn } from "@/shared/utils/cn";

export type BadgeTone = "neutral" | "info" | "success" | "warning" | "danger" | "brand";

const tones: Record<BadgeTone, string> = {
  neutral: "bg-ink-100 text-ink-700 ring-ink-200",
  info: "bg-sky-50 text-sky-800 ring-sky-200",
  success: "bg-emerald-50 text-emerald-800 ring-emerald-200",
  warning: "bg-amber-50 text-amber-800 ring-amber-200",
  danger: "bg-red-50 text-red-800 ring-red-200",
  brand: "bg-brand-50 text-brand-800 ring-brand-200",
};

export function Badge({
  children,
  tone = "neutral",
  className,
}: {
  children: ReactNode;
  tone?: BadgeTone;
  className?: string;
}) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset",
        tones[tone],
        className,
      )}
    >
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
  return <Badge tone={tone ?? toneForStatus(status)}>{label ?? status}</Badge>;
}
