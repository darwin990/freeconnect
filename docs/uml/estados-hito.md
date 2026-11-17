# Estados del hito

Máquina de estados implementada en `MilestoneStates` y aplicada por
`Milestone.transitionTo`. El estado del entregable y del contrato se incluyen como
contexto porque condicionan las acciones disponibles.

## Ciclo de vida del hito

```mermaid
stateDiagram-v2
    [*] --> PENDING: ContractFactory crea el hito
    PENDING --> IN_PROGRESS: Primer entregable registrado
    IN_PROGRESS --> SUBMITTED: Enviar a revisión (requiere ≥ 1 entregable)
    SUBMITTED --> APPROVED: Cliente aprueba
    SUBMITTED --> REJECTED: Cliente rechaza (motivo obligatorio)
    REJECTED --> IN_PROGRESS: Transición definida en el dominio
    APPROVED --> [*]

    note right of APPROVED
        Terminal. No admite nuevos entregables
        ni cambios de estado.
    end note

    note right of REJECTED
        La transición REJECTED -> IN_PROGRESS
        existe en MilestoneStates, pero
        NO hay endpoint que la ejecute.
        Añadir un entregable deja el hito
        en REJECTED, por lo que no puede
        reenviarse. Limitación conocida.
    end note

    note left of PENDING
        Se crea al aceptar la propuesta.
        No se puede enviar a revisión
        sin entregables.
    end note
```

## Reglas de transición

| Desde | Hacia permitidos | Disparador |
|-------|------------------|------------|
| `PENDING` | `IN_PROGRESS` | `POST .../deliverables` (primer entregable) |
| `IN_PROGRESS` | `SUBMITTED` | `POST .../submit` con al menos un entregable |
| `SUBMITTED` | `APPROVED` | `POST .../approve` (solo cliente) |
| `SUBMITTED` | `REJECTED` | `POST .../reject` con motivo (solo cliente) |
| `REJECTED` | `IN_PROGRESS` | Definido en el dominio, **sin endpoint** |
| `APPROVED` | — | Terminal |

Errores de dominio esperados:

- `MILESTONE_WITHOUT_DELIVERABLES` (409) al enviar sin entregables.
- `INVALID_MILESTONE_TRANSITION` (409) al aprobar/rechazar fuera de `SUBMITTED`.
- `MILESTONE_CLOSED` (409) al añadir entregables a un hito `APPROVED`.

## Entregable (contexto)

```mermaid
stateDiagram-v2
    [*] --> SUBMITTED: Deliverable.submit
    SUBMITTED --> APPROVED: Al aprobar el hito
    SUBMITTED --> REJECTED: Al rechazar el hito
    APPROVED --> [*]
    REJECTED --> [*]
```

> El entregable nace directamente en `SUBMITTED`; el enum conserva `DRAFT` para
> compatibilidad, pero el flujo actual no lo usa.

## Contrato (contexto)

```mermaid
stateDiagram-v2
    [*] --> DRAFT: ContractFactory
    DRAFT --> ACTIVE: Al aceptar la propuesta
    ACTIVE --> COMPLETED: Todos los hitos APPROVED
    ACTIVE --> CANCELLED: Cancelación con motivo
    DRAFT --> CANCELLED: Cancelación con motivo
    COMPLETED --> [*]
    CANCELLED --> [*]
```

## Limitación conocida: reenvío de hitos rechazados

El dominio permite `REJECTED → IN_PROGRESS`, pero no existe un endpoint que ejecute
esa transición. En la práctica:

1. El cliente rechaza el hito (`SUBMITTED → REJECTED`).
2. El freelancer añade un nuevo entregable; `addDeliverable` **no** cambia el estado
   porque solo promueve desde `PENDING`.
3. El hito permanece `REJECTED` y `submit` no es válido desde ese estado.

La UI lo advierte. Se propone añadir `POST .../milestones/{id}/reopen` o permitir
`submit` desde `REJECTED`. Esta limitación también está documentada en
[`frontend/README.md`](../../frontend/README.md).
