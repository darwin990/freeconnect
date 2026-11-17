import type { ReactNode } from "react";
import { AlertCircle, CheckCircle2, Info } from "lucide-react";
import { errorMessage, isApiError } from "@/shared/api/errors";
import { cn } from "@/shared/utils/cn";

type Tone = "error" | "success" | "info";

const styles: Record<Tone, { box: string; icon: ReactNode }> = {
  error: {
    box: "border-red-200 bg-red-50 text-red-900",
    icon: <AlertCircle className="mt-0.5 size-5 shrink-0" aria-hidden="true" />,
  },
  success: {
    box: "border-emerald-200 bg-emerald-50 text-emerald-900",
    icon: <CheckCircle2 className="mt-0.5 size-5 shrink-0" aria-hidden="true" />,
  },
  info: {
    box: "border-sky-200 bg-sky-50 text-sky-900",
    icon: <Info className="mt-0.5 size-5 shrink-0" aria-hidden="true" />,
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
      className={cn("flex gap-3 rounded-xl border p-4 text-sm", styles[tone].box, className)}
      role={tone === "error" ? "alert" : "status"}
    >
      {styles[tone].icon}
      <div className="min-w-0 space-y-1">
        {title && <p className="font-semibold">{title}</p>}
        <div>{children}</div>
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
