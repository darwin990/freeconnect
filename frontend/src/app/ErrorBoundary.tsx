import { Component } from "react";
import type { ErrorInfo, ReactNode } from "react";
import { AlertTriangle } from "lucide-react";
import { Button } from "@/shared/components/Button";

interface Props {
  children: ReactNode;
}

interface State {
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("Error no controlado en la interfaz", error, info.componentStack);
  }

  render() {
    if (this.state.error) {
      return (
        <main className="flex min-h-screen flex-col items-center justify-center bg-ink-50 px-4 text-center">
          <span className="flex size-12 items-center justify-center rounded-full bg-red-50 text-red-700">
            <AlertTriangle className="size-6" aria-hidden="true" />
          </span>
          <h1 className="mt-4 text-2xl font-bold text-ink-950">Algo salió mal</h1>
          <p className="mt-2 max-w-md text-sm text-ink-500">
            La interfaz encontró un error inesperado. Puedes recargar la página; si el problema continúa,
            revisa la consola del navegador.
          </p>
          <p className="mt-3 max-w-lg break-all rounded-lg bg-white p-3 font-mono text-xs text-ink-600 ring-1 ring-ink-200">
            {this.state.error.message}
          </p>
          <Button className="mt-5" onClick={() => window.location.reload()}>
            Recargar página
          </Button>
        </main>
      );
    }
    return this.props.children;
  }
}
