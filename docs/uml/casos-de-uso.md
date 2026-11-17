# Casos de uso

Actores y funcionalidades expuestas por la API `v1`. La autorización real vive en
el backend (`SecurityConfig` + `@PreAuthorize`); el frontend replica la restricción
con `RequireAuth` y `RequireRole` para no mostrar acciones que el servidor rechazaría.

```mermaid
flowchart LR
    Visitante((Visitante))
    Cliente((Cliente))
    Freelancer((Freelancer))
    Admin((Administrador))

    subgraph Publico["Acceso público"]
        UC01[Explorar proyectos publicados]
        UC02[Consultar detalle de proyecto]
        UC03[Registrarse como CLIENT o FREELANCER]
        UC04[Iniciar sesión]
    end

    subgraph Cuenta["Cuenta y perfil"]
        UC05[Consultar /auth/me]
        UC06[Editar perfil propio]
        UC07[Gestionar portafolio y adjuntos]
        UC08[Cerrar sesión y revocar refresh token]
    end

    subgraph ClienteUC["Cliente"]
        UC10[Crear proyecto en DRAFT]
        UC11[Editar proyecto propio]
        UC12[Publicar, pausar, reanudar, cancelar o finalizar]
        UC13[Crear proyecto desde plantilla]
        UC14[Exportar mis proyectos a CSV]
        UC15[Listar propuestas del proyecto]
        UC16[Evaluar, rechazar o aceptar propuesta]
        UC17[Aprobar o rechazar hitos]
        UC18[Liberar pago simulado]
        UC19[Escribir reseña tras contrato completado]
    end

    subgraph FreelancerUC["Freelancer"]
        UC20[Buscar proyectos por filtros]
        UC21[Enviar propuesta]
        UC22[Retirar propuesta propia]
        UC23[Registrar entregable]
        UC24[Gestionar tareas y comentarios]
        UC25[Enviar hito a revisión]
        UC26[Consultar pagos netos]
    end

    subgraph Compartido["Colaboración"]
        UC30[Abrir conversación del contrato]
        UC31[Enviar y leer mensajes]
        UC32[Archivar conversación]
        UC33[Consultar notificaciones]
        UC34[Reintentar entrega de notificación]
        UC35[Ver contratos e hitos]
    end

    subgraph AdminUC["Administración"]
        UC40[Ver reportes agregados]
        UC41[Moderar proyecto: pausar o cancelar]
        UC42[Suspender usuario por UUID]
        UC43[Consultar auditoría]
    end

    Visitante --> UC01
    Visitante --> UC02
    Visitante --> UC03
    Visitante --> UC04

    Cliente --> UC05
    Cliente --> UC06
    Cliente --> UC10
    Cliente --> UC11
    Cliente --> UC12
    Cliente --> UC13
    Cliente --> UC14
    Cliente --> UC15
    Cliente --> UC16
    Cliente --> UC17
    Cliente --> UC18
    Cliente --> UC19
    Cliente --> UC30
    Cliente --> UC31
    Cliente --> UC33
    Cliente --> UC34
    Cliente --> UC35

    Freelancer --> UC05
    Freelancer --> UC06
    Freelancer --> UC07
    Freelancer --> UC08
    Freelancer --> UC20
    Freelancer --> UC21
    Freelancer --> UC22
    Freelancer --> UC23
    Freelancer --> UC24
    Freelancer --> UC25
    Freelancer --> UC26
    Freelancer --> UC30
    Freelancer --> UC31
    Freelancer --> UC32
    Freelancer --> UC33
    Freelancer --> UC34
    Freelancer --> UC35

    Admin --> UC05
    Admin --> UC08
    Admin --> UC33
    Admin --> UC40
    Admin --> UC41
    Admin --> UC42
    Admin --> UC43

    UC16 -.->|incluye| UC35
    UC17 -.->|incluye| UC35
    UC18 -.->|requiere contrato ACTIVE o COMPLETED| UC35
    UC19 -.->|requiere contrato COMPLETED| UC35
    UC23 -.->|requiere contrato ACTIVE| UC35
    UC25 -.->|requiere al menos un entregable| UC23

    Admin -.->|"RequireRole: sin acceso a /app/mis-proyectos, /app/propuestas, /app/contratos"| ClienteUC
    Admin -.->|"SecurityConfig: /api/v1/admin/** hasRole('ADMIN')"| AdminUC
```

## Restricciones de acceso

| Ámbito | Regla de backend | Regla de frontend |
|--------|------------------|-------------------|
| `/api/v1/admin/**` | `hasRole('ADMIN')` en `SecurityConfig` y `@PreAuthorize` a nivel de clase (`AdminController`, `AuditController`). | Rutas bajo `RequireRole roles={["ADMIN"]}`. |
| Crear/editar/publicar proyectos | Propiedad del proyecto (`clientId`) validada en los casos de uso. | `RequireRole roles={["CLIENT"]}`. |
| Propuestas | Solo `FREELANCER` con perfil profesional; retiro solo del autor. | El formulario solo se muestra al freelancer. |
| Contratos, pagos y reseñas | Solo las dos partes del contrato; el pago solo lo libera el cliente dueño. | `RequireRole roles={["CLIENT","FREELANCER"]}` y comprobación de participación. |
| Administración | El admin **no** es parte de contratos ni tiene perfil público. | La navegación admin se limita a reportes, moderación, usuarios y auditoría. |

> El catálogo público (`GET /projects`) no requiere autenticación. La UI consulta
> por defecto `status=PUBLISHED` para no exponer borradores.

## Referencias

- Contratos de endpoints: [`docs/uml/README.md`](./README.md) y `frontend/schema/openapi.json`.
- Reglas de negocio y limitaciones: [`frontend/README.md`](../../frontend/README.md).
- Seguridad: `src/main/java/com/freeconnect/identity/infrastructure/security/SecurityConfig.java`.
