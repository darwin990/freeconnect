import type { ReactNode } from "react";
import { AlertCircle, CheckCircle2, Info } from "lucide-react";
import { errorMessage, isApiError } from "@/shared/api/errors";
import { cn } from "@/shared/utils/cn";

type Tone = "error" | "success" | "info";

const styles: Record<Tone, { box: string; icon: ReactNode }> = {
  error: {
    box: "border-danger-200 bg-danger-50 text-danger-900 dark:border-danger-600/40 dark:bg-danger-600/10 dark:text-danger-100",
    icon: <AlertCircle className="mt-0.5 size-5 shrink-0 text-danger-600" aria-hidden="true" />,
  },
  success: {
    box: "border-success-200 bg-success-50 text-success-900 dark:border-success-600/40 dark:bg-success-600/10 dark:text-success-100",
    icon: <CheckCircle2 className="mt-0.5 size-5 shrink-0 text-success-600" aria-hidden="true" />,
  },
  info: {
    box: "border-info-200 bg-info-50 text-info-900 dark:border-info-600/40 dark:bg-info-600/10 dark:text-info-100",
    icon: <Info className="mt-0.5 size-5 shrink-0 text-info-600" aria-hidden="true" />,
  },
};

export function Alert({
  tone = "info",
  title,
  children,
  className,
}: {
  tone?: Tone;
  title?: string;
  children: ReactNode;
  className?: string;
}) {
  return (
    <div
      className={cn("flex gap-3 rounded-xl border p-4 text-sm shadow-sm animate-fade-in", styles[tone].box, className)}
      role={tone === "error" ? "alert" : "status"}
    >
      {styles[tone].icon}
      <div className="min-w-0 space-y-1">
        {title && <p className="font-semibold">{title}</p>}
        <div className="leading-relaxed">{children}</div>
      </div>
    </div>
  );
}

export function ErrorAlert({
  error,
  title = "No se pudo completar la operación",
  className,
}: {
  error: unknown;
  title?: string;
  className?: string;
}) {
  const details = isApiError(error) ? error.details : [];
  const code = isApiError(error) ? error.code : undefined;
  return (
    <Alert tone="error" title={title} className={className}>
      <p>{errorMessage(error)}</p>
      {code && <p className="mt-1 text-xs opacity-80">Código: {code}</p>}
      {details.length > 0 && (
        <ul className="mt-2 list-inside list-disc text-xs">
          {details.map((detail, index) => (
            <li key={`${detail.field}-${index}`}>
              {detail.field ? <strong>{detail.field}: </strong> : null}
              {detail.message}
            </li>
          ))}
        </ul>
      )}
    </Alert>
  );
}
