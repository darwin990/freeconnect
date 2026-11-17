import { useEffect, useState } from "react";
import type { ReactNode } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import {
  ArrowRight,
  BriefcaseBusiness,
  FileText,
  Info,
  LayoutDashboard,
  LogIn,
  Menu,
  ShieldCheck,
  Sparkles,
  X,
} from "lucide-react";
import { BrandMark } from "@/features/auth/AuthShell";
import { useAuth } from "@/features/auth/AuthProvider";
import { ThemeToggle } from "@/components/ui/ThemeToggle";
import { Button } from "@/shared/components/Button";
import { demoEnabled } from "@/app/demoFlag";
import { cn } from "@/shared/utils/cn";
import { roleLabels } from "@/shared/utils/labels";

const publicLinks = [
  { to: "/", label: "Inicio", end: true },
  { to: "/proyectos", label: "Proyectos", end: false },
  { to: "/acerca", label: "Acerca del prototipo", end: false },
];

function publicLinkClass(isActive: boolean) {
  return cn(
    "rounded-lg px-3 py-2 text-sm font-medium transition-colors",
    isActive
      ? "bg-brand-50 text-brand-800 dark:bg-brand-600/15 dark:text-brand-200"
      : "text-ink-600 hover:bg-ink-100 hover:text-ink-950 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-white",
  );
}

export function PublicLayout() {
  const { status, user } = useAuth();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  const authenticated = status === "authenticated" && user;

  return (
    <div className="flex min-h-screen flex-col bg-ink-50 dark:bg-slate-950">
      <a
        href="#contenido"
        className="sr-only z-50 rounded-xl bg-white px-3 py-2 font-semibold text-brand-800 focus:not-sr-only focus:absolute focus:left-3 focus:top-3"
      >
        Saltar al contenido
      </a>

      <header className="sticky top-0 z-40 border-b border-ink-200/80 bg-white/85 backdrop-blur dark:border-slate-800 dark:bg-slate-950/85">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-3 px-4 py-3 sm:px-6">
          <BrandMark />

          <nav
            className="hidden items-center gap-1 lg:flex"
            aria-label="Navegación principal"
          >
            {publicLinks.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.end}
                className={({ isActive }) => publicLinkClass(isActive)}
              >
                {link.label}
              </NavLink>
            ))}
            {demoEnabled && (
              <NavLink to="/demo" className={({ isActive }) => publicLinkClass(isActive)}>
                Demo visual
              </NavLink>
            )}
          </nav>

          <div className="flex items-center gap-1.5">
            <ThemeToggle />
            <div className="hidden items-center gap-2 sm:flex">
              {authenticated ? (
                <>
                  <span className="hidden text-xs font-medium text-ink-500 md:inline">
                    {roleLabels[authenticated.role]}
                  </span>
                  <Link to="/app">
                    <Button size="sm" icon={<LayoutDashboard className="size-4" />}>
                      Mi panel
                    </Button>
                  </Link>
                </>
              ) : status === "loading" ? (
                <span className="text-xs text-ink-500" role="status">
                  Comprobando sesión…
                </span>
              ) : (
                <>
                  <Link to="/login">
                    <Button variant="ghost" size="sm" icon={<LogIn className="size-4" />}>
                      Ingresar
                    </Button>
                  </Link>
                  <Link to="/registro">
                    <Button size="sm">Crear cuenta</Button>
                  </Link>
                </>
              )}
            </div>
            <Button
              variant="ghost"
              size="icon"
              className="lg:hidden"
              aria-label={menuOpen ? "Cerrar menú" : "Abrir menú"}
              aria-expanded={menuOpen}
              aria-controls="menu-publico"
              onClick={() => setMenuOpen((open) => !open)}
            >
              {menuOpen ? (
                <X className="size-5" aria-hidden="true" />
              ) : (
                <Menu className="size-5" aria-hidden="true" />
              )}
            </Button>
          </div>
        </div>

        {menuOpen && (
          <div
            id="menu-publico"
            className="border-t border-ink-200 bg-white px-4 py-3 lg:hidden dark:border-slate-800 dark:bg-slate-950"
          >
            <nav className="flex flex-col gap-1" aria-label="Navegación principal móvil">
              {publicLinks.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  end={link.end}
                  className={({ isActive }) => publicLinkClass(isActive)}
                >
                  {link.label}
                </NavLink>
              ))}
              {demoEnabled && (
                <NavLink to="/demo" className={({ isActive }) => publicLinkClass(isActive)}>
                  Demo visual
                </NavLink>
              )}
            </nav>
            <div className="mt-3 flex flex-wrap gap-2 border-t border-ink-200 pt-3 dark:border-slate-800">
              {authenticated ? (
                <Link to="/app" onClick={() => setMenuOpen(false)}>
                  <Button size="sm" icon={<LayoutDashboard className="size-4" />}>
                    Mi panel
                  </Button>
                </Link>
              ) : (
                <>
                  <Link to="/login" onClick={() => setMenuOpen(false)}>
                    <Button variant="outline" size="sm">
                      Ingresar
                    </Button>
                  </Link>
                  <Link to="/registro" onClick={() => setMenuOpen(false)}>
                    <Button size="sm">Crear cuenta</Button>
                  </Link>
                </>
              )}
            </div>
          </div>
        )}
      </header>

      <main id="contenido" className="mx-auto w-full max-w-7xl flex-1 px-4 py-8 sm:px-6 lg:py-12">
        <div key={location.pathname} className="animate-fade-up">
          <Outlet />
        </div>
      </main>

      <footer className="border-t border-ink-200 bg-white dark:border-slate-800 dark:bg-slate-900">
        <div className="mx-auto grid max-w-7xl gap-8 px-4 py-10 sm:px-6 md:grid-cols-[1.4fr_1fr_1fr]">
          <div>
            <BrandMark />
            <p className="mt-3 max-w-sm text-sm leading-relaxed text-ink-500">
              Prototipo académico de arquitectura de software: clientes, freelancers y
              administración sobre una API Spring Boot con pagos simulados.
            </p>
            <p className="mt-3 inline-flex items-center gap-1.5 rounded-full bg-success-50 px-2.5 py-1 text-xs font-semibold text-success-700 ring-1 ring-inset ring-success-200">
              <ShieldCheck className="size-3.5" aria-hidden="true" />
              Sin cobros reales
            </p>
          </div>

          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.14em] text-ink-500">Producto</p>
            <ul className="mt-3 space-y-2 text-sm">
              <li>
                <Link to="/proyectos" className="text-ink-600 hover:text-brand-700">
                  Catálogo de proyectos
                </Link>
              </li>
              <li>
                <Link to="/acerca" className="text-ink-600 hover:text-brand-700">
                  Acerca del prototipo
                </Link>
              </li>
              <li>
                <Link to="/login" className="text-ink-600 hover:text-brand-700">
                  Iniciar sesión
                </Link>
              </li>
              <li>
                <Link to="/registro" className="text-ink-600 hover:text-brand-700">
                  Crear cuenta
                </Link>
              </li>
              {demoEnabled && (
                <li>
                  <Link to="/demo" className="inline-flex items-center gap-1 text-ink-600 hover:text-brand-700">
                    Demo visual <ArrowRight className="size-3.5" aria-hidden="true" />
                  </Link>
                </li>
              )}
            </ul>
          </div>

          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.14em] text-ink-500">Prototipo</p>
            <ul className="mt-3 space-y-2 text-sm text-ink-600">
              <li className="flex items-center gap-2">
                <BriefcaseBusiness className="size-4 text-brand-600" aria-hidden="true" />
                React 19 + Vite + Tailwind
              </li>
              <li className="flex items-center gap-2">
                <Sparkles className="size-4 text-accent-600" aria-hidden="true" />
                Design system propio
              </li>
              <li className="flex items-center gap-2">
                <Info className="size-4 text-info-600" aria-hidden="true" />
                Documentación UML en Mermaid
              </li>
              <li className="flex items-center gap-2">
                <FileText className="size-4 text-ink-400" aria-hidden="true" />
                Versionado en git
              </li>
            </ul>
          </div>
        </div>
        <div className="border-t border-ink-200 dark:border-slate-800">
          <div className="mx-auto flex max-w-7xl flex-col gap-2 px-4 py-5 text-xs text-ink-500 sm:flex-row sm:items-center sm:justify-between sm:px-6">
            <p>FreeConnect MVP · Proyecto académico de arquitectura de software.</p>
            <p>Pagos simulados · JWT con refresh rotation · sin WebSockets.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}

export function PublicSection({ children }: { children: ReactNode }) {
  return <section className="space-y-6">{children}</section>;
}
