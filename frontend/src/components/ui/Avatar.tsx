import { cn } from "@/shared/utils/cn";

const sizes = {
  xs: "size-7 text-[10px]",
  sm: "size-9 text-xs",
  md: "size-11 text-sm",
  lg: "size-14 text-base",
} as const;

/*
 * Colores sólidos con texto blanco: funcionan igual en tema claro y oscuro
 * sin depender del remapeo de utilidades del tema.
 */
const palettes = [
  "bg-brand-600 text-white ring-brand-500/40",
  "bg-accent-600 text-white ring-accent-500/40",
  "bg-amber-500 text-amber-950 ring-amber-400/50",
  "bg-sky-600 text-white ring-sky-500/40",
  "bg-rose-600 text-white ring-rose-500/40",
];

function initialsFrom(name: string): string {
  return (
    name
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join("") || "FC"
  );
}

/** Avatar de iniciales con color estable derivado del nombre. */
export function Avatar({
  name,
  src,
  size = "sm",
  className,
}: {
  name: string;
  src?: string;
  size?: keyof typeof sizes;
  className?: string;
}) {
  const palette = palettes[name.charCodeAt(0) % palettes.length];

  if (src) {
    return (
      <img
        src={src}
        alt={name}
        className={cn("rounded-full object-cover ring-1 ring-ink-200", sizes[size], className)}
      />
    );
  }

  return (
    <span
      className={cn(
        "inline-flex shrink-0 items-center justify-center rounded-full font-bold ring-1 ring-inset",
        sizes[size],
        palette,
        className,
      )}
      aria-hidden="true"
      title={name}
    >
      {initialsFrom(name)}
    </span>
  );
}
