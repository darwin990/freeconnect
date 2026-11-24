import { useState } from "react";
import { ShieldX, UserRound } from "lucide-react";
import { useSuspendUser } from "@/features/admin/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { Field, Input } from "@/shared/components/FormControls";
import { ConfirmDialog } from "@/shared/components/Modal";
import { PageHeader } from "@/shared/components/PageHeader";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import type { SuspendResult } from "@/types/models";

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function AdminUsersPage() {
  useDocumentTitle("Usuarios");
  const suspend = useSuspendUser();
  const [userId, setUserId] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [result, setResult] = useState<SuspendResult | null>(null);

  const valid = UUID_PATTERN.test(userId.trim());

  const requestSuspend = () => {
    setError(null);
    if (!valid) {
      setError("Introduce un UUID de usuario válido (36 caracteres con guiones).");
      return;
    }
    setConfirmOpen(true);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Administración"
        title="Suspensión de usuarios"
        description="Suspende una cuenta por su identificador. Por seguridad, la búsqueda requiere el UUID exacto del usuario."
      />

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <Card>
          <CardHeader
            title="Suspender cuenta"
            description="Introduce el UUID del usuario. La operación cambia su estado a SUSPENDED y se audita."
            action={<UserRound className="size-5 text-ink-400" aria-hidden="true" />}
          />
          <div className="mt-5 space-y-4">
            {suspend.isError ? <ErrorAlert error={suspend.error} title="No se pudo suspender el usuario" /> : null}
            {error ? <ErrorAlert error={new Error(error)} title="Identificador inválido" /> : null}

            <Field
              label="UUID del usuario"
              htmlFor="user-id"
              hint="Formato esperado: 00000000-0000-0000-0000-000000000000"
              required
            >
              <Input
                id="user-id"
                value={userId}
                onChange={(event) => {
                  setUserId(event.target.value);
                  setResult(null);
                }}
                placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                className="font-mono text-xs"
                invalid={Boolean(userId) && !valid}
              />
            </Field>

            <div className="flex flex-wrap items-center gap-2">
              <Button
                variant="danger"
                icon={<ShieldX className="size-4" />}
                disabled={!userId.trim()}
                onClick={requestSuspend}
              >
                Suspender usuario
              </Button>
              <Button
                variant="ghost"
                onClick={() => {
                  setUserId("");
                  setResult(null);
                  setError(null);
                }}
              >
                Limpiar
              </Button>
            </div>

            {result && (
              <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-950" role="status">
                <p className="font-semibold">Usuario suspendido</p>
                <p className="mt-1 break-all font-mono text-xs">{result.id}</p>
                <p className="mt-1">
                  Estado resultante: <Badge tone="warning">{result.status}</Badge>
                </p>
              </div>
            )}
          </div>
        </Card>

        <Card>
          <CardHeader title="Cómo obtener el identificador" />
          <ul className="mt-4 space-y-3 text-sm text-ink-600">
            <li className="flex gap-2">
              <span aria-hidden="true">•</span>
              La búsqueda directa por correo no está disponible en esta versión.
            </li>
            <li className="flex gap-2">
              <span aria-hidden="true">•</span>
              Una cuenta suspendida no puede reactivarse desde esta pantalla.
            </li>
            <li className="flex gap-2">
              <span aria-hidden="true">•</span>
              El UUID aparece en el detalle de un proyecto, contrato o reseña visible para administración.
            </li>
            <li className="flex gap-2">
              <span aria-hidden="true">•</span>
              Se recomienda añadir un listado paginado de usuarios con filtros y acciones de
              suspensión/reactivación.
            </li>
          </ul>
        </Card>
      </div>

      <ConfirmDialog
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        title="Suspender usuario"
        description="La cuenta quedará suspendida. Confirma que el UUID corresponde al usuario correcto."
        confirmLabel="Suspender"
        dangerous
        loading={suspend.isPending}
        onConfirm={() => {
          suspend.mutate(userId.trim(), {
            onSuccess: (value) => {
              setResult(value);
              setConfirmOpen(false);
            },
          });
        }}
      >
        <p className="break-all font-mono text-xs text-ink-700">{userId.trim()}</p>
        {suspend.isError ? <ErrorAlert error={suspend.error} className="mt-3" /> : null}
      </ConfirmDialog>
    </div>
  );
}
