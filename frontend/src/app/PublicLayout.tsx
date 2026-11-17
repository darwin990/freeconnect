import type { ReactNode } from "react";
import { Link, NavLink, Outlet } from "react-router-dom";
import { LogIn, Sparkles } from "lucide-react";
import { BrandMark } from "@/features/auth/AuthShell";
import { useAuth } from "@/features/auth/AuthProvider";
import { Button } from "@/shared/components/Button";
import { roleLabels } from "@/shared/utils/labels";

export function PublicLayout() {
  const { status, user } = useAuth();

  return (
    <div className="flex min-h-screen flex-col bg-ink-50">
      <a
        href="#contenido"
        className="sr-only z-50 rounded bg-white px-3 py-2 font-semibold text-brand-800 focus:not-sr-only focus:absolute focus:left-3 focus:top-3"
      >
        Saltar al contenido
      </a>
      <header className="sticky top-0 z-30 border-b border-ink-200 bg-white/90 backdrop-blur">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-3 px-4 py-3 sm:px-6">
          <BrandMark />
          <nav className="order-3 flex w-full items-center gap-4 text-sm font-medium text-ink-600 sm:order-2 sm:w-auto" aria-label="Navegación principal">
            <NavLink
              to="/proyectos"
              className={({ isActive }) =>
                `rounded-lg px-2 py-1 transition hover:text-brand-700 ${isActive ? "text-brand-700" : ""}`
              }
            >
              Proyectos
            </NavLink>
            <NavLink
              to="/"
              end
              className={({ isActive }) =>
                `rounded-lg px-2 py-1 transition hover:text-brand-700 ${isActive ? "text-brand-700" : ""}`
              }
            >
              Inicio
            </NavLink>
          </nav>
          <div className="order-2 flex items-center gap-2 sm:order-3">
            {status === "authenticated" && user ? (
              <>
                <span className="hidden text-xs text-ink-500 sm:inline">{roleLabels[user.role]}</span>
                <Link to="/app">
                  <Button size="sm" icon={<Sparkles className="size-4" />}>Mi panel</Button>
                </Link>
              </>
            ) : status === "loading" ? (
              <span className="text-xs text-ink-500">Comprobando sesión…</span>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="ghost" size="sm" icon={<LogIn className="size-4" />}>Ingresar</Button>
                </Link>
                <Link to="/registro">
                  <Button size="sm">Crear cuenta</Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main id="contenido" className="mx-auto w-full max-w-7xl flex-1 px-4 py-8 sm:px-6 lg:py-10">
        <Outlet />
      </main>

      <footer className="border-t border-ink-200 bg-white">
        <div className="mx-auto flex max-w-7xl flex-col gap-2 px-4 py-6 text-xs text-ink-500 sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <p>FreeConnect MVP · Proyecto académico de arquitectura de software.</p>
          <p>Pagos simulados. Sin cobros reales. JWT con refresh rotation.</p>
        </div>
      </footer>
    </div>
  );
}

export function PublicSection({ children }: { children: ReactNode }) {
  return <section className="space-y-6">{children}</section>;
}
