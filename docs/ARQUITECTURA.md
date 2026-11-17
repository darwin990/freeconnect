# Arquitectura de FreeConnect

## 1. Visión general

FreeConnect es un **monolito modular** con **Clean Architecture**. Cada módulo
de negocio se divide en cuatro capas y se comunica con los demás mediante
interfaces de dominio, casos de uso y eventos. El resultado es un único
despliegue, pero con límites explícitos que permiten extraer módulos a servicios
si el negocio lo exige.

```
┌──────────────────────────────────────────────────────────────────────┐
│                            API REST (/api/v1)                        │
│  Auth  Profiles  Projects  Proposals  Contracts  Messaging  Reviews  │
│  Payments  Notifications  Admin  Audit                               │
└───────────────────────────────┬──────────────────────────────────────┘
                                │ DTOs + UseCase
┌───────────────────────────────▼──────────────────────────────────────┐
│                        CAPA DE APLICACIÓN                            │
│  Casos de uso · validadores · fábricas · puertos de repositorio      │
└───────────────────────────────┬──────────────────────────────────────┘
                                │ modelos de dominio
┌───────────────────────────────▼──────────────────────────────────────┐
│                        CAPA DE DOMINIO                               │
│  Agregados · Value Objects · State · eventos · reglas de negocio     │
└───────────────────────────────▲──────────────────────────────────────┘
                                │ implementa puertos
┌───────────────────────────────┴──────────────────────────────────────┐
│                     INFRAESTRUCTURA (adaptadores)                    │
│  JPA · Flyway · JWT · almacenamiento · pasarela · listeners · cache   │
└──────────────────────────────────────────────────────────────────────┘
```

La regla de dependencia es estricta: **dominio no conoce Spring, JPA ni HTTP**.
Los controladores no contienen reglas de negocio ni repositorios; sólo traducen
HTTP a comandos/consultas y devuelven vistas.

## 2. Módulos

| Módulo | Responsabilidad | Puertos principales |
|--------|-----------------|---------------------|
| `identity` | Registro, login, refresh rotativo, logout, roles, suspensión | `UserRepository`, `RefreshTokenRepository`, `TokenService`, `PasswordHasherStrategy` |
| `profiles` | Perfil de freelancer/cliente, habilidades, portafolio, adjuntos | `ProfileRepository`, `SkillRepository`, `ObjectStore` |
| `projects` | Ciclo de vida, búsqueda, filtros DSL, plantillas, exportación | `ProjectRepository` |
| `proposals` | Envío, retiro, evaluación, rechazo, aceptación | `ProposalRepository` |
| `contracts` | Contrato, hitos, entregables, tareas, comentarios, comisiones, pagos | `ContractRepository`, `PaymentGateway` |
| `messaging` | Conversaciones por contrato, mensajes, lectura, archivo | `ConversationRepository` |
| `reviews` | Reseñas de contratos completados y reputación | `ReviewRepository` |
| `payments` | Liberación de pagos, comisión, historial | `PaymentRepository`, `PaymentGateway` |
| `notifications` | Outbox in-app/email/push con reintentos | `NotificationRepository` |
| `admin` | Reportes (Visitor) y moderación | Repositorios de proyectos/contratos/usuarios |
| `audit` | Trazabilidad de eventos de dominio | `AuditRecorder` |
| `shared` | Result, paginación, Money, eventos, errores, seguridad, decoradores | `DomainEventPublisher`, `CurrentUserProvider`, `RateLimiter` |

## 3. Estructura de un módulo

```
com.freeconnect.<modulo>
├── domain/            # agregados, VOs, estados, eventos, puertos
├── application/       # casos de uso, comandos/consultas, vistas, fábricas
├── api/               # controladores + DTOs de entrada/salida
└── infrastructure/    # JPA, adaptadores, configuración, listeners
```

Ejemplo: en `contracts` el dominio no importa nada de `infrastructure`; el
adaptador `ContractRepositoryAdapter` implementa `ContractRepository` y el
mapeador `ContractJpaMapper` reconstruye el agregado desde las entidades.

## 4. Flujo de una petición

```
HTTP → Controller (validación Jakarta) → UseCase → Agregado (reglas + eventos)
     → Repository (puerto) → Adaptador JPA → PostgreSQL
     → DomainEventPublisher → Listeners (notificaciones, auditoría)
     → Vista/DTO → JSON
```

- **Errores uniformes:** `GlobalExceptionHandler` traduce excepciones de dominio
  a `ProblemDetail` con `type`, `title`, `status`, `detail` y `code`.
- **Paginación:** `PageQuery` y `PageResponse<T>` con metadatos estables.
- **Autorización:** `@PreAuthorize` en controladores y guardas de propiedad en
  los casos de uso (`CurrentUserProvider.requireOwner/requireParticipant`).

## 5. Seguridad

| Aspecto | Decisión |
|---------|----------|
| Autenticación | JWT **HS256** de vida corta (15 min) emitido por `JwtTokenService` |
| Refresh | Token **opaco** de 7 días, guardado como digest **SHA-256** y rotado en cada uso |
| Contraseñas | BCrypt (12 rondas) por defecto; Argon2id disponible por estrategia |
| Autorización | Roles `CLIENT`, `FREELANCER`, `ADMIN` + verificación de propiedad |
| Sesiones | Stateless, sin cookies; `SecurityContext` por petición |
| Rate limit | Ventana fija en memoria (240 req/min configurable) |
| Secretos | Variables de entorno; nunca se registran tokens ni contraseñas |
| CORS | Orígenes configurables |
| Bootstrap admin | `AdminBootstrapRunner` sólo crea la cuenta si la base está vacía |

La contraseña del administrador se toma de `ADMIN_PASSWORD`; el valor por
defecto es **sólo para desarrollo**.

## 6. Persistencia

- **PostgreSQL** en Docker; **H2 en modo PostgreSQL** para `dev` y tests.
- **Flyway** aplica migraciones versionadas `V1..V6`:

| Versión | Contenido |
|---------|-----------|
| V1 | Usuarios, refresh tokens, perfiles, habilidades, portafolio, proyectos, propuestas, contratos, hitos, entregables, tareas, comentarios |
| V2 | Conversaciones y mensajes |
| V3 | Notificaciones (outbox) |
| V4 | Reseñas |
| V5 | Pagos |
| V6 | Auditoría |

Las migraciones usan SQL portable (`UUID`, `TIMESTAMP WITH TIME ZONE`, sin
JSONB) para que el mismo esquema corra en H2 y PostgreSQL.

## 7. Eventos de dominio

Los agregados registran eventos y el publicador los entrega a los listeners.

| Evento | Emisor | Consumidores |
|--------|--------|--------------|
| `UserRegisteredEvent`, `UserRoleChangedEvent` | `User` | auditoría |
| `ProjectPublishedEvent`, `ProjectStatusChangedEvent` | `Project` | auditoría / notificaciones |
| `ProposalSubmittedEvent`, `ProposalAcceptedEvent`, `ProposalRejectedEvent`, `ProposalWithdrawnEvent`, `ProposalEvaluatedEvent` | `Proposal` | notificaciones, auditoría |
| `ContractCreatedEvent`, `ContractStatusChangedEvent`, `ContractCompletedEvent` | `Contract` | notificaciones, auditoría |
| `MilestoneStatusChangedEvent`, `DeliverableSubmittedEvent` | `Milestone` | notificaciones, auditoría |
| `ConversationOpenedEvent`, `MessageSentEvent`, `ConversationArchivedEvent` | `Conversation` | notificaciones, auditoría |
| `ReviewCreatedEvent` | `Review` | notificaciones, auditoría |
| `PaymentReleasedEvent` | `Payment` | notificaciones, auditoría |

Los listeners de notificación capturan sus errores: una notificación fallida no
revierte la operación de negocio. El outbox reintenta con backoff configurable.

## 8. Almacenamiento de archivos

`ObjectStore` es el puerto. `StorageProxy` valida que el actor sea dueño de la
clave `portfolio/{userId}/...` (o ADMIN), cachea lecturas con Caffeine y delega
en `StorageBackend`. La implementación incluida es de sistema de archivos; el
puerto permite conectar S3/MinIO sin tocar perfiles ni casos de uso.

## 9. Cobertura de requisitos

| RF | Implementación | Verificación |
|----|----------------|--------------|
| RF-01 Registro/login/refresh | `identity` | `FreeConnectFlowIntegrationTest`, `scripts/e2e.ps1` |
| RF-02 Perfiles y habilidades | `profiles` | integración + E2E |
| RF-03 Proyectos, filtros y búsqueda | `projects` | tests del intérprete + E2E |
| RF-04 Propuestas | `proposals` | integración + E2E |
| RF-05 Aceptar propuesta y contrato con hitos | `contracts` | integración + E2E |
| RF-06 Entregables, hitos, mensajería, reseñas, pagos | `contracts`, `messaging`, `reviews`, `payments` | integración + E2E |

## 10. Decisiones y límites conocidos

1. **Monolito modular, no microservicios.** Un solo proceso reduce la complejidad
   operativa del MVP; los límites por módulo permiten extraer servicios después.
2. **H2 para desarrollo local.** Docker no está disponible en el entorno de
   validación; el perfil `docker` usa PostgreSQL real. Flyway es portable.
3. **Redis incluido en el stack pero no crítico.** El rate limit y los refresh
   tokens del MVP funcionan en memoria/base de datos. Redis queda listo para
   escalar a varias instancias.
4. **Canales email/push simulados.** El patrón Bridge y el outbox son reales; la
   entrega externa (SES/SMTP/FCM) es trabajo de producción.
5. **Pasarela de pagos MOCK/LEGACY.** El adaptador legado demuestra el patrón
   Adapter; no se realizan cobros reales.
6. **Almacenamiento local.** `ObjectStore` abstrae S3-compatible; el backend
   incluido es de sistema de archivos.
7. **Sin frontend.** La entrega es la API REST documentada con OpenAPI/Swagger.
