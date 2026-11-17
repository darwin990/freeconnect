import { useEffect, useState } from "react";
import type { ReactNode } from "react";
import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  BarChart3,
  Bell,
  BriefcaseBusiness,
  ClipboardList,
  Compass,
  Handshake,
  LayoutDashboard,
  LogOut,
  Menu,
  MessageSquareText,
  Plus,
  ScrollText,
  ShieldCheck,
  Star,
  UserRound,
  Users,
  Wallet,
  X,
} from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { useNotifications } from "@/features/notifications/hooks";
import { Button } from "@/shared/components/Button";
import { Badge } from "@/shared/components/Badge";
import { cn } from "@/shared/utils/cn";
import { roleLabels } from "@/shared/utils/labels";

interface NavItem {
  to: string;
  label: string;
  icon: ReactNode;
  end?: boolean;
}

function navItemsForRole(role?: string): { main: NavItem[]; admin: NavItem[] } {
  if (role === "ADMIN") {
    return {
      main: [
        { to: "/app", label: "Panel", icon: <LayoutDashboard className="size-4" />, end: true },
        { to: "/app/notificaciones", label: "Notificaciones", icon: <Bell className="size-4" /> },
      ],
      admin: [
        { to: "/app/admin/reportes", label: "Reportes", icon: <BarChart3 className="size-4" /> },
        { to: "/app/admin/moderacion", label: "Moderación", icon: <ShieldCheck className="size-4" /> },
        { to: "/app/admin/usuarios", label: "Usuarios", icon: <Users className="size-4" /> },
        { to: "/app/admin/auditoria", label: "Auditoría", icon: <ScrollText className="size-4" /> },
      ],
    };
  }

  if (role === "FREELANCER") {
    return {
      main: [
        { to: "/app", label: "Panel", icon: <LayoutDashboard className="size-4" />, end: true },
        { to: "/proyectos", label: "Explorar proyectos", icon: <Compass className="size-4" /> },
        { to: "/app/contratos", label: "Contratos", icon: <Handshake className="size-4" /> },
        { to: "/app/pagos", label: "Pagos", icon: <Wallet className="size-4" /> },
        { to: "/app/resenas", label: "Reseñas", icon: <Star className="size-4" /> },
        { to: "/app/mensajes", label: "Mensajes", icon: <MessageSquareText className="size-4" /> },
        { to: "/app/notificaciones", label: "Notificaciones", icon: <Bell className="size-4" /> },
        { to: "/app/perfil", label: "Mi perfil", icon: <UserRound className="size-4" /> },
      ],
      admin: [],
    };
  }

  return {
    main: [
      { to: "/app", label: "Panel", icon: <LayoutDashboard className="size-4" />, end: true },
      { to: "/app/mis-proyectos", label: "Mis proyectos", icon: <BriefcaseBusiness className="size-4" /> },
      { to: "/app/proyectos/nuevo", label: "Nuevo proyecto", icon: <Plus className="size-4" /> },
      { to: "/app/propuestas", label: "Propuestas", icon: <ClipboardList className="size-4" /> },
      { to: "/app/contratos", label: "Contratos", icon: <Handshake className="size-4" /> },
      { to: "/app/pagos", label: "Pagos", icon: <Wallet className="size-4" /> },
      { to: "/app/resenas", label: "Reseñas", icon: <Star className="size-4" /> },
      { to: "/app/mensajes", label: "Mensajes", icon: <MessageSquareText className="size-4" /> },
      { to: "/app/notificaciones", label: "Notificaciones", icon: <Bell className="size-4" /> },
      { to: "/app/perfil", label: "Mi perfil", icon: <UserRound className="size-4" /> },
    ],
    admin: [],
  };
}

function navLinkClass(isActive: boolean) {
  return cn(
    "flex items-center gap-2.5 rounded-xl px-3 py-2.5 text-sm font-medium transition",
    isActive
      ? "bg-white/10 text-white ring-1 ring-inset ring-white/15"
      : "text-ink-300 hover:bg-white/5 hover:text-white",
  );
}

export function AppShell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const notificationsQuery = useNotifications();
  const notifications = notificationsQuery.data ?? [];
  const failed = notifications.filter((notification) => notification.status === "FAILED").length;
  const { main, admin } = navItemsForRole(user?.role);
  const initials = (user?.displayName ?? "FC")
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  const handleLogout = async () => {
    await logout();
    navigate("/login", { replace: true });
  };

  return (
    <div className="min-h-screen bg-ink-50 lg:grid lg:grid-cols-[260px_minmax(0,1fr)]">
      <a
        href="#contenido"
        className="sr-only z-50 rounded bg-white px-3 py-2 font-semibold text-brand-800 focus:not-sr-only focus:absolute focus:left-3 focus:top-3"
      >
        Saltar al contenido
      </a>

      {menuOpen && (
        <button
          type="button"
          aria-label="Cerrar menú"
          className="fixed inset-0 z-30 bg-ink-950/50 lg:hidden"
          onClick={() => setMenuOpen(false)}
        />
      )}

      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-40 flex w-72 flex-col bg-ink-950 transition-transform lg:static lg:z-auto lg:w-auto lg:translate-x-0",
          menuOpen ? "translate-x-0" : "-translate-x-full",
        )}
      >
        <div className="flex items-center justify-between px-5 py-4">
          <Link to="/app" className="inline-flex items-center gap-2 rounded-lg text-white">
            <span className="flex size-9 items-center justify-center rounded-xl bg-brand-600">
              <BriefcaseBusiness className="size-5" aria-hidden="true" />
            </span>
            <span className="text-lg font-bold tracking-tight">FreeConnect</span>
          </Link>
          <button
            type="button"
            className="rounded-lg p-2 text-ink-300 hover:bg-white/10 hover:text-white lg:hidden"
            aria-label="Cerrar menú"
            onClick={() => setMenuOpen(false)}
          >
            <X className="size-5" aria-hidden="true" />
          </button>
        </div>

        <div className="mx-5 rounded-xl bg-white/5 p-3 text-white ring-1 ring-inset ring-white/10">
          <p className="truncate text-sm font-semibold">{user?.displayName}</p>
          <p className="mt-0.5 truncate text-xs text-ink-300">{user?.email}</p>
          <p className="mt-2">
            <Badge tone="brand">{roleLabels[user?.role ?? "CLIENT"]}</Badge>
          </p>
        </div>

        <nav className="mt-5 flex-1 space-y-1 overflow-y-auto px-3 pb-6" aria-label="Navegación del panel">
          {main.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => navLinkClass(isActive)}
            >
              {item.icon}
              <span className="truncate">{item.label}</span>
              {item.to === "/app/notificaciones" && failed > 0 && (
                <span className="ml-auto rounded-full bg-red-500 px-1.5 py-0.5 text-[10px] font-bold text-white">
                  {failed}
                </span>
              )}
            </NavLink>
          ))}

          {admin.length > 0 && (
            <>
              <p className="px-3 pb-1 pt-4 text-[11px] font-semibold uppercase tracking-wider text-ink-500">
                Administración
              </p>
              {admin.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) => navLinkClass(isActive)}
                >
                  {item.icon}
                  <span className="truncate">{item.label}</span>
                </NavLink>
              ))}
            </>
          )}
        </nav>

        <div className="border-t border-white/10 p-3">
          <Link
            to="/proyectos"
            className="flex items-center gap-2 rounded-xl px-3 py-2.5 text-sm font-medium text-ink-300 hover:bg-white/5 hover:text-white"
          >
            <Compass className="size-4" aria-hidden="true" />
            Ver catálogo público
          </Link>
          <button
            type="button"
            onClick={() => void handleLogout()}
            className="mt-1 flex w-full items-center gap-2 rounded-xl px-3 py-2.5 text-sm font-medium text-ink-300 hover:bg-white/5 hover:text-white"
          >
            <LogOut className="size-4" aria-hidden="true" />
            Cerrar sesión
          </button>
        </div>
      </aside>

      <div className="flex min-w-0 flex-col">
        <header className="sticky top-0 z-20 flex items-center justify-between gap-3 border-b border-ink-200 bg-white/90 px-4 py-3 backdrop-blur sm:px-6">
          <div className="flex min-w-0 items-center gap-3">
            <Button
              variant="ghost"
              size="sm"
              className="lg:hidden"
              aria-label="Abrir menú"
              onClick={() => setMenuOpen(true)}
            >
              <Menu className="size-5" aria-hidden="true" />
            </Button>
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold text-ink-900">
                {user?.displayName ?? "FreeConnect"}
              </p>
              <p className="text-xs text-ink-500">{roleLabels[user?.role ?? "CLIENT"]}</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Link
              to="/app/notificaciones"
              className="relative rounded-xl p-2 text-ink-600 hover:bg-ink-100"
              aria-label={`Notificaciones${failed > 0 ? `, ${failed} con error` : ""}`}
            >
              <Bell className="size-5" aria-hidden="true" />
              {failed > 0 && (
                <span className="absolute right-1 top-1 size-2 rounded-full bg-red-500 ring-2 ring-white" />
              )}
            </Link>
            <div className="flex size-9 items-center justify-center rounded-full bg-brand-100 text-sm font-bold text-brand-800">
              {initials}
            </div>
          </div>
        </header>

        <main id="contenido" className="flex-1 px-4 py-6 sm:px-6 lg:px-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
