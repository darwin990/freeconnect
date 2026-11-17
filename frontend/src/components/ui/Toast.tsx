import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import type { ReactNode } from "react";
import { AlertCircle, CheckCircle2, Info, TriangleAlert, X } from "lucide-react";
import { cn } from "@/shared/utils/cn";

export type ToastTone = "success" | "error" | "info" | "warning";

export interface ToastOptions {
  title: string;
  description?: string;
  tone?: ToastTone;
  /** Duración en ms. Usa 0 para mantener el toast hasta cerrarlo manualmente. */
  duration?: number;
}

interface ToastItem extends ToastOptions {
  id: string;
}

interface ToastContextValue {
  toast: (options: ToastOptions) => string;
  dismiss: (id: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

const tones: Record<ToastTone, { box: string; icon: ReactNode }> = {
  success: {
    box: "border-success-200 bg-white text-ink-900 dark:border-success-600/40 dark:bg-slate-900 dark:text-slate-100",
    icon: <CheckCircle2 className="size-5 text-success-600" aria-hidden="true" />,
  },
  error: {
    box: "border-danger-200 bg-white text-ink-900 dark:border-danger-600/40 dark:bg-slate-900 dark:text-slate-100",
    icon: <AlertCircle className="size-5 text-danger-600" aria-hidden="true" />,
  },
  warning: {
    box: "border-warning-200 bg-white text-ink-900 dark:border-warning-600/40 dark:bg-slate-900 dark:text-slate-100",
    icon: <TriangleAlert className="size-5 text-warning-600" aria-hidden="true" />,
  },
  info: {
    box: "border-info-200 bg-white text-ink-900 dark:border-info-600/40 dark:bg-slate-900 dark:text-slate-100",
    icon: <Info className="size-5 text-info-600" aria-hidden="true" />,
  },
};

let counter = 0;

function nextId(): string {
  counter += 1;
  return `toast-${Date.now()}-${counter}`;
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);
  const timers = useRef(new Map<string, number>());

  const dismiss = useCallback((id: string) => {
    const timer = timers.current.get(id);
    if (timer) window.clearTimeout(timer);
    timers.current.delete(id);
    setToasts((current) => current.filter((item) => item.id !== id));
  }, []);

  const toast = useCallback(
    (options: ToastOptions) => {
      const id = nextId();
      const item: ToastItem = { ...options, id };
      // Máximo 4 toasts simultáneos para no tapar la interfaz.
      setToasts((current) => [...current.slice(-3), item]);
      const duration = options.duration ?? 4500;
      if (duration > 0) {
        timers.current.set(id, window.setTimeout(() => dismiss(id), duration));
      }
      return id;
    },
    [dismiss],
  );

  useEffect(() => {
    const activeTimers = timers.current;
    return () => {
      activeTimers.forEach((timer) => window.clearTimeout(timer));
      activeTimers.clear();
    };
  }, []);

  const value = useMemo(() => ({ toast, dismiss }), [toast, dismiss]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div
        className="pointer-events-none fixed inset-x-0 bottom-0 z-[60] flex flex-col items-center gap-2 p-4 sm:items-end"
        aria-label="Notificaciones de la aplicación"
      >
        {toasts.map((item) => (
          <div
            key={item.id}
            role="status"
            className={cn(
              "pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-2xl border p-4 shadow-float animate-slide-in-right",
              tones[item.tone ?? "info"].box,
            )}
          >
            {tones[item.tone ?? "info"].icon}
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold">{item.title}</p>
              {item.description ? (
                <p className="mt-0.5 text-sm text-ink-600 dark:text-slate-300">{item.description}</p>
              ) : null}
            </div>
            <button
              type="button"
              onClick={() => dismiss(item.id)}
              className="rounded-lg p-1 text-ink-400 transition hover:bg-ink-100 hover:text-ink-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-brand-600 dark:hover:bg-slate-800"
              aria-label="Cerrar notificación"
            >
              <X className="size-4" aria-hidden="true" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastContextValue {
  const value = useContext(ToastContext);
  if (!value) throw new Error("useToast debe usarse dentro de ToastProvider");
  return value;
}
