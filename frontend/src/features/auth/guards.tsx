import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { PageLoader } from "@/shared/components/Spinner";
import type { Role } from "@/types/models";
import { useAuth } from "./AuthProvider";

export function RequireAuth({ children }: { children: ReactNode }) {
  const { status } = useAuth();
  const location = useLocation();

  if (status === "loading") return <PageLoader label="Restaurando sesión" />;
  if (status !== "authenticated") {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  }
  return <>{children}</>;
}

export function RequireRole({ roles, children }: { roles: Role[]; children: ReactNode }) {
  const { user } = useAuth();
  if (!user || !roles.includes(user.role)) {
    return (
      <div className="mx-auto max-w-xl py-16 text-center">
        <p className="text-sm font-semibold uppercase tracking-wide text-red-700">Acceso restringido</p>
        <h1 className="mt-2 text-2xl font-bold text-ink-950">No tienes permiso para ver esta sección</h1>
        <p className="mt-2 text-ink-500">
          Tu rol actual es <strong>{user?.role ?? "desconocido"}</strong>. La navegación solo muestra las
          secciones permitidas.
        </p>
      </div>
    );
  }
  return <>{children}</>;
}

export function PublicOnlyRoute({ children }: { children: ReactNode }) {
  const { status } = useAuth();
  if (status === "loading") return <PageLoader label="Comprobando sesión" />;
  if (status === "authenticated") return <Navigate to="/app" replace />;
  return <>{children}</>;
}
