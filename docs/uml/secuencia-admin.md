# Secuencia de administración

Panel de solo lectura y moderación. **Todo** `/api/v1/admin/**` está protegido por
`hasRole('ADMIN')` en `SecurityConfig` y, además, por `@PreAuthorize("hasRole('ADMIN')")`
a nivel de clase en `AdminController` y `AuditController`. Esta nota sustituye a la
afirmación anterior de que los reportes eran públicos.

## Acceso y reportes

```mermaid
sequenceDiagram
    autonumber
    actor A as Administrador
    participant SPA as SPA React
    participant API as API Spring Boot
    participant Sec as Spring Security
    participant UC as AdminReportsUseCase
    participant DB as Base de datos

    A->>SPA: /login (admin@freeconnect.io)
    SPA->>API: POST /auth/login
    API-->>SPA: 200 AuthResponse (rol ADMIN)
    SPA->>SPA: Menú admin: Reportes, Moderación, Usuarios, Auditoría
    Note over SPA: No se muestran Mis proyectos, Propuestas ni Contratos

    A->>SPA: /app/admin/reportes
    SPA->>API: GET /api/v1/admin/reports (Bearer)
    API->>Sec: JwtAuthenticationFilter
    Sec->>Sec: SecurityConfig hasRole('ADMIN')
    Sec->>Sec: AdminController @PreAuthorize("hasRole('ADMIN')")
    alt Sin sesión o rol distinto
        Sec-->>SPA: 401 / 403
        SPA-->>A: Redirección o "Acceso restringido"
    else ADMIN
        Sec->>UC: execute()
        UC->>UC: currentUser.requireAdmin()
        UC->>DB: findAll projects / contracts + count users
        UC->>UC: Visitor: ProjectReport, ContractReport, UserReport
        UC-->>API: List<AdminReport>
        API-->>SPA: 200 JSON
        SPA-->>A: Tarjetas de métricas por reporte
    end
```

## Moderación de proyectos

```mermaid
sequenceDiagram
    autonumber
    actor A as Administrador
    participant SPA as SPA React
    participant API as AdminController
    participant UC as AdminModerationUseCase
    participant P as Project aggregate
    participant DB as Base de datos

    A->>SPA: /app/admin/moderacion
    SPA->>API: POST /api/v1/admin/projects/{id}/moderate {"status":"PAUSED"}
    Note over API: @PreAuthorize hasRole('ADMIN') a nivel de clase
    API->>UC: moderateProject(id, PAUSED)
    UC->>UC: currentUser.requireAdmin()
    UC->>DB: findById
    alt Proyecto inexistente
        DB-->>UC: empty
        UC-->>SPA: 404 PROJECT_NOT_FOUND
    else Estado no permitido
        UC->>P: pause/cancel
        P-->>UC: INVALID_PROJECT_TRANSITION
        UC-->>SPA: 409
    else Transición válida
        UC->>P: pause(adminId) o cancel(adminId)
        P->>P: transitionTo + evento de dominio
        UC->>DB: save project + audit
        UC-->>SPA: 200 ProjectView
        SPA-->>A: Estado actualizado
    end
```

> La moderación solo acepta `PAUSED` o `CANCELLED`; cualquier otro estado devuelve
> `ValidationException`. Las transiciones legales dependen del estado actual del proyecto.

## Suspensión de usuarios y auditoría

```mermaid
sequenceDiagram
    autonumber
    actor A as Administrador
    participant SPA as SPA React
    participant API as AdminController
    participant UC as AdminModerationUseCase
    participant AU as AuditController
    participant DB as Base de datos

    Note over A,SPA: No existe listado/búsqueda de usuarios en la API
    A->>SPA: /app/admin/usuarios (pega un UUID manualmente)
    SPA->>API: POST /api/v1/admin/users/{id}/suspend
    API->>UC: suspendUser(id)
    alt UUID inexistente
        UC-->>SPA: 404 USER_NOT_FOUND
    else Usuario encontrado
        UC->>DB: UPDATE user.status = SUSPENDED
        UC->>DB: INSERT audit log
        UC-->>SPA: 200 {id, status:"SUSPENDED"}
        SPA-->>A: Confirmación
    end

    A->>SPA: /app/admin/auditoria
    SPA->>AU: GET /api/v1/admin/audit
    AU->>AU: currentUser.requireAdmin()
    AU->>DB: últimos 100 eventos
    AU-->>SPA: 200 AuditLogView[]
    SPA-->>A: Tabla de eventos (actor, acción, entidad, fecha)
```

## Límites conocidos del panel

| Límite | Efecto | Mitigación actual |
|--------|--------|-------------------|
| No hay `GET /admin/users` | La suspensión exige pegar el UUID. | La UI advierte y valida el formato. |
| No hay reactivación | Un usuario suspendido no puede rehabilitarse por API. | Fuera de alcance del MVP. |
| El admin no es parte de contratos | No puede abrir el detalle de contrato ni el perfil público. | Navegación admin acotada a sus cuatro secciones. |
| Los reportes son agregados | No hay exportación ni series temporales. | Se muestran métricas y fecha de generación. |
| Auditoría limitada | Solo los últimos 100 eventos. | Suficiente para el prototipo académico. |
