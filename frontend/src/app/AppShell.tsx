import { useCallback, useEffect, useRef, useState } from "react";
import type { ReactNode, RefObject } from "react";
import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  BarChart3,
  Bell,
  BriefcaseBusiness,
  ClipboardList,
  Compass,
  Handshake,
  Info,
  LayoutDashboard,
  LogOut,
  Menu,
  MessageSquareText,
  Plus,
  ScrollText,
  ShieldCheck,
  Sparkles,
  Star,
  UserRound,
  Users,
  Wallet,
  X,
} from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { useNotifications } from "@/features/notifications/hooks";
import { Avatar } from "@/components/ui/Avatar";
import { ThemeToggle } from "@/components/ui/ThemeToggle";
import { Button } from "@/shared/components/Button";
import { cn } from "@/shared/utils/cn";
import { roleLabels } from "@/shared/utils/labels";
import type { UserSummary } from "@/types/models";

interface NavItem {
  to: string;
  label: string;
  icon: ReactNode;
  end?: boolean;
}

interface NavGroup {
  label: string;
  items: NavItem[];
}

function navGroupsForRole(role?: string): NavGroup[] {
  if (role === "ADMIN") {
    return [
      {
        label: "Administración",
        items: [
          { to: "/app", label: "Panel", icon: <LayoutDashboard className="size-4" />, end: true },
          { to: "/app/admin/reportes", label: "Reportes", icon: <BarChart3 className="size-4" /> },
          { to: "/app/admin/moderacion", label: "Moderación", icon: <ShieldCheck className="size-4" /> },
          { to: "/app/admin/usuarios", label: "Usuarios", icon: <Users className="size-4" /> },
          { to: "/app/admin/auditoria", label: "Auditoría", icon: <ScrollText className="size-4" /> },
        ],
      },
      {
        label: "Cuenta",
        items: [
          { to: "/app/notificaciones", label: "Notificaciones", icon: <Bell className="size-4" /> },
        ],
      },
    ];
  }

  if (role === "FREELANCER") {
    return [
      {
        label: "Principal",
        items: [
          { to: "/app", label: "Panel", icon: <LayoutDashboard className="size-4" />, end: true },
          { to: "/proyectos", label: "Explorar proyectos", icon: <Compass className="size-4" /> },
        ],
      },
      {
        label: "Colaboración",
        items: [
          { to: "/app/contratos", label: "Contratos", icon: <Handshake className="size-4" /> },
          { to: "/app/mensajes", label: "Mensajes", icon: <MessageSquareText className="size-4" /> },
          { to: "/app/pagos", label: "Pagos", icon: <Wallet className="size-4" /> },
          { to: "/app/resenas", label: "Reseñas", icon: <Star className="size-4" /> },
        ],
      },
      {
        label: "Cuenta",
        items: [
          { to: "/app/notificaciones", label: "Notificaciones", icon: <Bell className="size-4" /> },
          { to: "/app/perfil", label: "Mi perfil", icon: <UserRound className="size-4" /> },
        ],
      },
    ];
  }

  return [
    {
      label: "Principal",
      items: [
        { to: "/app", label: "Panel", icon: <LayoutDashboard className="size-4" />, end: true },
        { to: "/app/mis-proyectos", label: "Mis proyectos", icon: <BriefcaseBusiness className="size-4" /> },
        { to: "/app/proyectos/nuevo", label: "Nuevo proyecto", icon: <Plus className="size-4" /> },
        { to: "/app/propuestas", label: "Propuestas", icon: <ClipboardList className="size-4" /> },
      ],
    },
    {
      label: "Colaboración",
      items: [
        { to: "/app/contratos", label: "Contratos", icon: <Handshake className="size-4" /> },
        { to: "/app/mensajes", label: "Mensajes", icon: <MessageSquareText className="size-4" /> },
        { to: "/app/pagos", label: "Pagos", icon: <Wallet className="size-4" /> },
        { to: "/app/resenas", label: "Reseñas", icon: <Star className="size-4" /> },
      ],
    },
    {
      label: "Cuenta",
      items: [
        { to: "/app/notificaciones", label: "Notificaciones", icon: <Bell className="size-4" /> },
        { to: "/app/perfil", label: "Mi perfil", icon: <UserRound className="size-4" /> },
      ],
    },
  ];
}

function navLinkClass(isActive: boolean) {
  return cn(
    "group flex items-center gap-2.5 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors duration-150",
    isActive
      ? "bg-white text-ink-950 shadow-sm ring-1 ring-inset ring-white/60"
      : "text-ink-300 hover:bg-white/10 hover:text-white",
  );
}

export function AppShell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const notificationsQuery = useNotifications();
  const notifications = notificationsQuery.data ?? [];
  const failed = notifications.filter((notification) => notification.status === "FAILED").length;
  const groups = navGroupsForRole(user?.role);
  const displayName = user?.displayName ?? "FreeConnect";

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!menuOpen) return;
    const previous = document.activeElement as HTMLElement | null;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    closeButtonRef.current?.focus();

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") setMenuOpen(false);
    };
    document.addEventListener("keydown", onKeyDown);

    return () => {
      document.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = previousOverflow;
      previous?.focus();
    };
  }, [menuOpen]);

  const handleLogout = useCallback(async () => {
    await logout();
    navigate("/login", { replace: true });
  }, [logout, navigate]);

  const sidebar = (
    <SidebarContent
      user={user}
      groups={groups}
      failed={failed}
      onNavigate={() => setMenuOpen(false)}
      onLogout={() => void handleLogout()}
      closeButtonRef={closeButtonRef}
    />
  );

  return (
    <div className="min-h-screen bg-ink-50 lg:grid lg:grid-cols-[280px_minmax(0,1fr)]">
      <a
        href="#contenido"
        className="sr-only z-[60] rounded-xl bg-white px-3 py-2 font-semibold text-brand-800 focus:not-sr-only focus:absolute focus:left-3 focus:top-3"
      >
        Saltar al contenido
      </a>

      <aside className="sticky top-0 hidden h-screen bg-ink-950 bg-ink-gradient lg:flex lg:flex-col">
        {sidebar}
      </aside>

      {menuOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div
            className="absolute inset-0 bg-ink-950/60 backdrop-blur-sm animate-fade-in"
            aria-hidden="true"
            onClick={() => setMenuOpen(false)}
          />
          <div
            id="menu-movil"
            role="dialog"
            aria-modal="true"
            aria-label="Menú de navegación"
            className="absolute inset-y-0 left-0 flex w-80 max-w-[85vw] flex-col bg-ink-950 bg-ink-gradient shadow-float animate-slide-in-right"
          >
            {sidebar}
          </div>
        </div>
      )}

      <div className="flex min-w-0 flex-col">
        <header className="sticky top-0 z-30 flex items-center justify-between gap-3 border-b border-ink-200 bg-white/85 px-4 py-2.5 backdrop-blur sm:px-6 dark:border-slate-800 dark:bg-slate-900/85">
          <div className="flex min-w-0 items-center gap-2.5">
            <Button
              variant="ghost"
              size="icon"
              className="lg:hidden"
              aria-label="Abrir menú"
              aria-expanded={menuOpen}
              aria-controls="menu-movil"
              onClick={() => setMenuOpen(true)}
            >
              <Menu className="size-5" aria-hidden="true" />
            </Button>
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold text-ink-900 dark:text-slate-100">
                {displayName}
              </p>
              <p className="text-xs text-ink-500">{roleLabels[user?.role ?? "CLIENT"]}</p>
            </div>
          </div>

          <div className="flex items-center gap-1.5">
            <ThemeToggle />
            <Link
              to="/app/notificaciones"
              className="relative rounded-xl p-2 text-ink-600 transition hover:bg-ink-100 hover:text-ink-900 dark:text-slate-300 dark:hover:bg-slate-800"
              aria-label={`Notificaciones${failed > 0 ? `, ${failed} con error` : ""}`}
            >
              <Bell className="size-5" aria-hidden="true" />
              {failed > 0 && (
                <span className="absolute right-1 top-1 size-2 rounded-full bg-danger-600 ring-2 ring-white dark:ring-slate-900" />
              )}
            </Link>
            <Link
              to="/app/perfil"
              className="rounded-full focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600"
              aria-label="Mi perfil"
            >
              <Avatar name={displayName} size="sm" />
            </Link>
          </div>
        </header>

        <main id="contenido" className="flex-1 px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
          <div className="mx-auto w-full max-w-6xl animate-fade-up">
            <Outlet />
          </div>
        </main>

        <footer className="border-t border-ink-200 px-4 py-4 text-xs text-ink-500 sm:px-6 lg:px-8 dark:border-slate-800">
          <div className="mx-auto flex max-w-6xl flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
            <p>FreeConnect · prototipo académico con pagos simulados.</p>
            <div className="flex flex-wrap items-center gap-4">
              <Link to="/acerca" className="font-medium hover:text-brand-700">
                Acerca del prototipo
              </Link>
              <Link to="/proyectos" className="font-medium hover:text-brand-700">
                Catálogo público
              </Link>
              <span className="inline-flex items-center gap-1">
                <Sparkles className="size-3.5" aria-hidden="true" /> MVP
              </span>
            </div>
          </div>
        </footer>
      </div>
    </div>
  );
}

function SidebarContent({
  user,
  groups,
  failed,
  onNavigate,
  onLogout,
  closeButtonRef,
}: {
  user: UserSummary | null;
  groups: NavGroup[];
  failed: number;
  onNavigate: () => void;
  onLogout: () => void;
  closeButtonRef: RefObject<HTMLButtonElement | null>;
}) {
  return (
    <>
      <div className="flex items-center justify-between px-4 py-4">
        <Link
          to="/app"
          onClick={onNavigate}
          className="inline-flex items-center gap-2 rounded-lg text-white"
        >
          <span className="flex size-9 items-center justify-center rounded-xl bg-brand-600 shadow-glow">
            <BriefcaseBusiness className="size-5" aria-hidden="true" />
          </span>
          <span className="text-lg font-bold tracking-tight">FreeConnect</span>
        </Link>
        <button
          ref={closeButtonRef}
          type="button"
          className="rounded-lg p-2 text-ink-300 transition hover:bg-white/10 hover:text-white lg:hidden"
          aria-label="Cerrar menú"
          onClick={onNavigate}
        >
          <X className="size-5" aria-hidden="true" />
        </button>
      </div>

      <div className="mx-4 flex items-center gap-3 rounded-2xl bg-white/5 p-3 text-white ring-1 ring-inset ring-white/10">
        <Avatar name={user?.displayName ?? "FreeConnect"} size="md" />
        <div className="min-w-0">
          <p className="truncate text-sm font-semibold">{user?.displayName}</p>
          <p className="truncate text-xs text-ink-300">{user?.email}</p>
          <p className="mt-1.5">
            <span className="inline-flex items-center rounded-full bg-white/10 px-2.5 py-0.5 text-xs font-semibold text-brand-100 ring-1 ring-inset ring-white/15">
              {roleLabels[user?.role ?? "CLIENT"]}
            </span>
          </p>
        </div>
      </div>

      <nav
        className="mt-5 flex-1 space-y-5 overflow-y-auto px-3 pb-6"
        aria-label="Navegación del panel"
      >
        {groups.map((group) => (
          <div key={group.label}>
            <p className="px-3 pb-1.5 text-[11px] font-semibold uppercase tracking-[0.14em] text-ink-500">
              {group.label}
            </p>
            <div className="space-y-1">
              {group.items.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.end}
                  onClick={onNavigate}
                  className={({ isActive }) => navLinkClass(isActive)}
                >
                  {item.icon}
                  <span className="truncate">{item.label}</span>
                  {item.to === "/app/notificaciones" && failed > 0 && (
                    <span className="ml-auto rounded-full bg-danger-600 px-1.5 py-0.5 text-[10px] font-bold text-white">
                      {failed}
                    </span>
                  )}
                </NavLink>
              ))}
            </div>
          </div>
        ))}
      </nav>

      <div className="space-y-1 border-t border-white/10 p-3">
        <Link
          to="/proyectos"
          onClick={onNavigate}
          className="flex items-center gap-2 rounded-xl px-3 py-2.5 text-sm font-medium text-ink-300 transition hover:bg-white/5 hover:text-white"
        >
          <Compass className="size-4" aria-hidden="true" />
          Ver catálogo público
        </Link>
        <Link
          to="/acerca"
          onClick={onNavigate}
          className="flex items-center gap-2 rounded-xl px-3 py-2.5 text-sm font-medium text-ink-300 transition hover:bg-white/5 hover:text-white"
        >
          <Info className="size-4" aria-hidden="true" />
          Acerca del prototipo
        </Link>
        <button
          type="button"
          onClick={onLogout}
          className="flex w-full items-center gap-2 rounded-xl px-3 py-2.5 text-sm font-medium text-ink-300 transition hover:bg-white/5 hover:text-white"
        >
          <LogOut className="size-4" aria-hidden="true" />
          Cerrar sesión
        </button>
      </div>
    </>
  );
}
