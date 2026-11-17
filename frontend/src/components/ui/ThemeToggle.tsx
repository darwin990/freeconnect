import { Moon, Sun } from "lucide-react";
import { useTheme } from "@/app/ThemeProvider";
import { cn } from "@/shared/utils/cn";

/**
 * Toggle de tema claro/oscuro. El tema claro es el predeterminado; la
 * preferencia se guarda en localStorage y se aplica con la clase `dark`.
 */
export function ThemeToggle({ className }: { className?: string }) {
  const { theme, toggleTheme } = useTheme();
  const dark = theme === "dark";

  return (
    <button
      type="button"
      onClick={toggleTheme}
      aria-label={dark ? "Cambiar a tema claro" : "Cambiar a tema oscuro"}
      aria-pressed={dark}
      title={dark ? "Tema claro" : "Tema oscuro"}
      className={cn(
        "inline-flex size-9 items-center justify-center rounded-xl border border-ink-200 bg-white text-ink-600 transition hover:bg-ink-50 hover:text-ink-900 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:bg-slate-800",
        className,
      )}
    >
      {dark ? <Sun className="size-4" aria-hidden="true" /> : <Moon className="size-4" aria-hidden="true" />}
    </button>
  );
}
