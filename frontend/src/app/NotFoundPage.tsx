import { Link } from "react-router-dom";
import { Compass, Home } from "lucide-react";
import { BrandMark } from "@/features/auth/AuthShell";
import { Button } from "@/shared/components/Button";

export function NotFoundPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-ink-50 px-4 py-16 text-center">
      <BrandMark />
      <p className="mt-10 text-sm font-semibold uppercase tracking-wider text-brand-700">Error 404</p>
      <h1 className="mt-2 text-3xl font-bold tracking-tight text-ink-950">
        No encontramos esta página
      </h1>
      <p className="mt-2 max-w-md text-sm text-ink-500">
        La ruta no existe o fue movida. Puedes volver al inicio o explorar el catálogo de proyectos.
      </p>
      <div className="mt-6 flex flex-wrap justify-center gap-2">
        <Link to="/">
          <Button icon={<Home className="size-4" />}>Ir al inicio</Button>
        </Link>
        <Link to="/proyectos">
          <Button variant="outline" icon={<Compass className="size-4" />}>Explorar proyectos</Button>
        </Link>
      </div>
    </main>
  );
}
