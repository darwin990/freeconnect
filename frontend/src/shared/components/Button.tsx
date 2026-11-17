import type { ButtonHTMLAttributes, ReactNode } from "react";
import { LoaderCircle } from "lucide-react";
import { cn } from "@/shared/utils/cn";

type Variant = "primary" | "secondary" | "outline" | "ghost" | "danger" | "subtle";
type Size = "sm" | "md" | "lg" | "icon";

const variants: Record<Variant, string> = {
  primary:
    "bg-brand-700 text-white shadow-sm hover:bg-brand-800 hover:shadow-glow focus-visible:outline-brand-700 disabled:bg-brand-300 dark:bg-brand-600 dark:hover:bg-brand-500",
  secondary:
    "bg-ink-900 text-white shadow-sm hover:bg-ink-800 focus-visible:outline-ink-900 disabled:bg-ink-400 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-white",
  outline:
    "border border-ink-300 bg-white text-ink-800 hover:border-ink-400 hover:bg-ink-50 focus-visible:outline-brand-600 disabled:text-ink-400 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800",
  ghost:
    "text-ink-700 hover:bg-ink-100 hover:text-ink-950 focus-visible:outline-ink-600 disabled:text-ink-400 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-white",
  danger:
    "bg-danger-600 text-white shadow-sm hover:bg-danger-700 focus-visible:outline-danger-600 disabled:bg-danger-200 disabled:text-danger-700",
  subtle:
    "bg-brand-50 text-brand-800 hover:bg-brand-100 focus-visible:outline-brand-600 disabled:text-brand-300 dark:bg-brand-600/15 dark:text-brand-200 dark:hover:bg-brand-600/25",
};

const sizes: Record<Size, string> = {
  sm: "h-9 gap-1.5 px-3 text-sm",
  md: "h-11 gap-2 px-4 text-sm",
  lg: "h-12 gap-2 px-6 text-base",
  icon: "size-10",
};

export function buttonClasses(variant: Variant = "primary", size: Size = "md", className?: string) {
  return cn(
    "inline-flex items-center justify-center rounded-xl font-semibold transition-all duration-200 ease-smooth focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 active:translate-y-px disabled:cursor-not-allowed disabled:active:translate-y-0",
    variants[variant],
    sizes[size],
    className,
  );
}

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  loading?: boolean;
  icon?: ReactNode;
}

export function Button({
  variant = "primary",
  size = "md",
  loading = false,
  icon,
  className,
  children,
  disabled,
  type = "button",
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      className={buttonClasses(variant, size, className)}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      {...props}
    >
      {loading ? <LoaderCircle className="size-4 animate-spin" aria-hidden="true" /> : icon}
      {children}
    </button>
  );
}
