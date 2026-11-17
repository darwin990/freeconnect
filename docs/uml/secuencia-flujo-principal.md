# Secuencia del flujo principal

Ciclo de negocio completo (RF-01 a RF-06). Se divide en tres tramos para que cada
diagrama sea legible; los IDs de mensaje continúan entre tramos.

## Tramo A — Registro, proyecto, propuesta y contrato

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    actor F as Freelancer
    participant SPA as SPA React
    participant API as API Spring Boot
    participant DB as PostgreSQL / H2

    Note over C,SPA: Registro de cliente
    C->>SPA: /registro (rol CLIENT)
    SPA->>API: POST /auth/register
    API->>DB: INSERT user + profile
    API-->>SPA: 201 AuthResponse (access + refresh)
    SPA->>SPA: session.setAuth (access en memoria, refresh en sessionStorage)
    SPA-->>C: Redirige a /app

    Note over C,API: Publicación del proyecto
    C->>SPA: /app/proyectos/nuevo (título, descripción, presupuesto)
    SPA->>API: POST /projects
    API->>DB: INSERT project (DRAFT)
    API-->>SPA: 201 ProjectView
    SPA-->>C: Detalle del proyecto en borrador
    C->>SPA: Botón "Publicar"
    SPA->>API: PATCH /projects/{id}/status {"status":"PUBLISHED"}
    API->>API: Project.transitionTo(PUBLISHED)
    API->>DB: UPDATE project + INSERT audit/outbox
    API-->>SPA: 200 ProjectView PUBLISHED
    SPA-->>C: Badge "Publicado"

    Note over F,API: Perfil y propuesta del freelancer
    F->>SPA: /registro (rol FREELANCER)
    SPA->>API: POST /auth/register
    API-->>SPA: 201 AuthResponse
    F->>SPA: /app/perfil (titular, disponibilidad)
    SPA->>API: PUT /profiles/me
    API->>DB: UPDATE profile
    API-->>SPA: 200 ProfileView
    SPA-->>F: "Perfil guardado correctamente."

    F->>SPA: /proyectos/{id}
    SPA->>API: GET /projects/{id}
    API-->>SPA: 200 ProjectView
    F->>SPA: Carta de presentación + envío
    SPA->>API: POST /projects/{id}/proposals
    alt Perfil profesional incompleto
        API-->>SPA: 422 FREELANCER_PROFILE_REQUIRED
        SPA-->>F: Error uniforme (ErrorAlert)
    else Propuesta válida
        API->>DB: INSERT proposal (SUBMITTED)
        API-->>SPA: 201 ProposalView
        SPA->>SPA: Invalida queries del proyecto
        SPA-->>F: Tarjeta "Enviada" + "Retirar propuesta"
    end

    Note over C,API: Aceptación y creación del contrato
    C->>SPA: /app/propuestas?project={id}
    SPA->>API: GET /projects/{id}/proposals
    API-->>SPA: 200 ProposalView[]
    C->>SPA: "Aceptar y contratar" (plan, tipo de hitos, partes)
    SPA->>API: POST /proposals/{proposalId}/accept
    API->>API: Proposal.accept + ContractFactory
    API->>DB: INSERT contract (ACTIVE) + milestones + audit/outbox
    API-->>SPA: 201 ContractView
    SPA-->>C: Redirige a /app/contratos/{id}
    opt Notificación al freelancer
        API-->>DB: Outbox PROPOSAL_ACCEPTED / CONTRACT_CREATED
    end
```

## Tramo B — Mensajería, entregables y revisión

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    actor F as Freelancer
    participant SPA as SPA React
    participant API as API Spring Boot
    participant DB as PostgreSQL / H2

    C->>SPA: "Abrir conversación" en el contrato
    SPA->>API: POST /contracts/{contractId}/conversations
    API->>DB: SELECT/INSERT conversation
    API-->>SPA: 200 ConversationView
    SPA-->>C: /app/mensajes/{conversationId}

    C->>SPA: Nuevo mensaje
    SPA->>API: POST /conversations/{id}/messages
    API->>DB: INSERT conversation_message
    API-->>SPA: 201 MessageView
    SPA-->>C: Mensaje visible (polling cada VITE_MESSAGING_POLL_MS)

    F->>SPA: /app/mensajes/{id}
    SPA->>API: GET /conversations/{id}
    API-->>SPA: 200 ConversationView con mensajes
    F->>SPA: Responde
    SPA->>API: POST /conversations/{id}/messages
    API-->>SPA: 201 MessageView
    SPA-->>F: Mensaje visible

    F->>SPA: /app/contratos/{contractId}
    SPA->>API: GET /contracts/{id}
    API-->>SPA: 200 ContractView con hitos
    F->>SPA: "Agregar entregable"
    SPA->>API: POST /contracts/{id}/milestones/{mid}/deliverables
    API->>API: Milestone.addDeliverable
    alt Primer entregable y hito PENDING
        API->>API: PENDING -> IN_PROGRESS
    end
    API->>DB: INSERT deliverable + UPDATE milestone
    API-->>SPA: 201 DeliverableView
    SPA-->>F: Entregable en la tarjeta del hito

    opt Tareas y comentarios
        F->>SPA: Agregar tarea
        SPA->>API: POST .../deliverables/{did}/tasks
        API-->>SPA: 201 TaskView
        C->>SPA: Comentar entregable
        SPA->>API: POST .../deliverables/{did}/comments
        API-->>SPA: 201 CommentView
    end

    F->>SPA: "Enviar a revisión"
    SPA->>API: POST /contracts/{id}/milestones/{mid}/submit
    alt Sin entregables
        API-->>SPA: 409 MILESTONE_WITHOUT_DELIVERABLES
        SPA-->>F: Error uniforme
    else Con entregables
        API->>API: IN_PROGRESS -> SUBMITTED
        API->>DB: UPDATE milestone + audit/outbox
        API-->>SPA: 200 ContractView
        SPA-->>F: Badge "En revisión"
    end

    Note over F,SPA: Protección de rutas
    F->>SPA: /app/mis-proyectos
    SPA->>SPA: RequireRole CLIENT
    SPA-->>F: "No tienes permiso para ver esta sección"
```

## Tramo C — Aprobación, pago y reseña

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant SPA as SPA React
    participant API as API Spring Boot
    participant DB as PostgreSQL / H2
    participant GW as PaymentGateway (MOCK)

    C->>SPA: /app/contratos/{id}
    SPA->>API: GET /contracts/{id}
    API-->>SPA: 200 ContractView (hito SUBMITTED)
    C->>SPA: "Aprobar hito"
    SPA->>API: POST /contracts/{id}/milestones/{mid}/approve
    API->>API: Milestone.approve (SUBMITTED -> APPROVED)
    alt Todos los hitos aprobados
        API->>API: Contract.complete (ACTIVE -> COMPLETED)
        API->>DB: UPDATE contract + completedAt
        API-->>DB: Outbox CONTRACT_COMPLETED
    end
    API-->>SPA: 200 ContractView COMPLETED
    SPA-->>C: Badge "Completado"

    C->>SPA: "Liberar pago" (monto)
    SPA->>API: POST /contracts/{id}/payments {"amount": n}
    alt No es el cliente dueño o contrato no ACTIVE/COMPLETED
        API-->>SPA: 403/409
        SPA-->>C: Error uniforme
    else Pago válido
        API->>API: Payment.create (monto, comisión, neto)
        API->>GW: authorize/release
        GW-->>API: referencia simulada
        API->>DB: INSERT payment (RELEASED) + audit
        API-->>SPA: 201 PaymentView
        SPA-->>C: "Pago registrado correctamente." + Badge "Liberado"
    end

    opt Reseña de la contraparte
        C->>SPA: Calificación + comentario
        SPA->>API: POST /contracts/{id}/reviews
        alt Contrato no COMPLETED o ya existe reseña del autor
            API-->>SPA: 409 REVIEW_NOT_ALLOWED
        else Reseña válida
            API->>DB: INSERT review + actualiza reputación del perfil
            API-->>SPA: 201 ReviewView
            SPA-->>C: "Ya enviaste tu reseña para este contrato."
        end
    end
```

## Notas de implementación

- El cliente HTTP reintenta **una sola vez** tras un `401` mediante `refreshAccessToken` (single-flight).
- No hay WebSockets: mensajes y notificaciones se actualizan por polling configurable.
- Los eventos de dominio alimentan notificaciones (outbox) y auditoría sin bloquear la respuesta.
- `%% TODO: confirmar` la política exacta de reintentos del `PaymentGateway`; en el MVP es un mock síncrono.
