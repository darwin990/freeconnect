# FreeConnect

API REST para conectar clientes con freelancers: publicación de proyectos,
propuestas, contratos con hitos, entregables, mensajería, reseñas, pagos y
administración.

Monolito modular en **Java 21 + Spring Boot 3.5** con Clean Architecture,
seguridad JWT, migraciones Flyway y más de 20 patrones de diseño aplicados.

## Contenido

- [Stack](#stack)
- [Inicio rápido](#inicio-rápido)
- [Docker](#docker)
- [Pruebas](#pruebas)
- [API](#api)
- [Flujo curl](#flujo-curl)
- [Estructura](#estructura)
- [Documentación](#documentación)
- [Documentación UML](#documentación-uml)
- [Seguridad](#seguridad)
- [Límites conocidos](#límites-conocidos)

## Stack

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 21 (compilado con JDK 21+) |
| Framework | Spring Boot 3.5, Spring MVC, Spring Security 6 |
| Persistencia | Spring Data JPA, Hibernate 6, PostgreSQL 16, H2 (dev/test) |
| Migraciones | Flyway (`V1..V6`) |
| Caché | Caffeine + Spring Cache; Redis disponible en Docker |
| Seguridad | JWT HS256, refresh tokens opacos rotativos, BCrypt/Argon2 |
| Documentación | springdoc OpenAPI + Swagger UI |
| Observabilidad | Actuator + Micrometer/Prometheus |
| Pruebas | JUnit 5, MockMvc, H2 |
| Empaquetado | Maven, Docker multi-stage, Docker Compose |

## Inicio rápido

Requisitos: JDK 21 o superior y Maven 3.9+.

```bash
cd freeconnect
mvn spring-boot:run
```

El perfil por defecto es `dev`:

- API: <http://localhost:8080/api/v1> 
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI: <http://localhost:8080/v3/api-docs>
- H2 console: <http://localhost:8080/h2-console> (`jdbc:h2:mem:freeconnect`, usuario `sa`, sin contraseña)
- Health: <http://localhost:8080/actuator/health>

Cuenta administrativa inicial (sólo si la base está vacía):

```
admin@freeconnect.io
Admin#FreeConnect2026
```

> Cambiar `ADMIN_PASSWORD` y `JWT_SECRET` en cualquier entorno real.

## Docker

```bash
cp .env.example .env
# editar .env (JWT_SECRET y contraseñas)
docker compose up --build
```

Servicios: `app` (8080), `postgres` (5433 en el host → 5432 en el contenedor) y
`redis` (6379). El puerto 5433 evita conflictos con un PostgreSQL local. La
aplicación usa el perfil `docker`, Flyway crea el esquema y el volumen
`portfolio-data` persiste los archivos de portafolio.

## Pruebas

```bash
# Unitarias de dominio + integración MockMvc (H2)
mvn test

# Empaquetado sin tests
mvn -DskipTests package

# E2E de humo contra una instancia en ejecución (perfil dev)
powershell -ExecutionPolicy Bypass -File scripts/e2e.ps1
```

Cobertura actual: **20 pruebas** que incluyen State, Builder, Composite, Command,
Strategy, Interpreter, Visitor y el flujo completo RF-01→RF-06 más mensajería,
reseñas, pagos, notificaciones, reportes y moderación.

## API

Base: `/api/v1`. Todas las respuestas de error usan un contrato uniforme
(`application/problem+json`).

### Autenticación

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| POST | `/auth/register` | público | Registro `CLIENT` o `FREELANCER` |
| POST | `/auth/login` | público | Login; devuelve access + refresh |
| POST | `/auth/refresh` | público | Rotación del refresh token |
| POST | `/auth/logout` | autenticado | Revoca el refresh token |
| GET | `/auth/me` | autenticado | Usuario actual |

### Perfiles

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET | `/profiles/me` | autenticado | Mi perfil |
| PUT | `/profiles/me` | autenticado | Actualizar perfil/habilidades |
| GET | `/profiles/{id}` | autenticado | Perfil público por id de perfil |
| POST | `/profiles/me/portfolio` | FREELANCER | Agregar ítem de portafolio |
| POST | `/profiles/me/portfolio/{itemId}/attachment` | FREELANCER | Subir adjunto (multipart) |
| GET | `/profiles/me/portfolio/{itemId}/attachment` | FREELANCER | Descargar adjunto |

### Proyectos

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET | `/projects` | público | Búsqueda paginada y filtros |
| GET | `/projects/{id}` | público | Detalle |
| POST | `/projects` | CLIENT | Crear en `DRAFT` |
| PUT | `/projects/{id}` | CLIENT | Editar proyecto propio |
| PATCH | `/projects/{id}/status` | CLIENT | Transición de estado |
| GET | `/projects/templates` | público | Plantillas clonables |
| POST | `/projects/from-template/{templateId}` | CLIENT | Crear desde plantilla |
| GET | `/projects/mine/export` | CLIENT | Exportar mis proyectos (CSV) |

Parámetros de búsqueda: `q`, `category`, `modality`, `status`, `minBudget`,
`maxBudget`, `skill` (repetible o separado por comas), `filter` (DSL), `page`,
`size`. Ejemplo de DSL:

```
budget >= 1000 AND modality = REMOTE AND (category = SOFTWARE OR category = DATA)
```

### Propuestas

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| POST | `/projects/{projectId}/proposals` | FREELANCER | Enviar propuesta |
| GET | `/projects/{projectId}/proposals` | autenticado | Listar propuestas |
| DELETE | `/proposals/{id}` | FREELANCER | Retirar propuesta propia |
| POST | `/proposals/{id}/evaluate` | CLIENT | Marcar en evaluación |
| POST | `/proposals/{id}/reject` | CLIENT | Rechazar |
| POST | `/proposals/{id}/accept` | CLIENT | Aceptar y crear contrato |

### Contratos

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET | `/contracts` | autenticado | Mis contratos |
| GET | `/contracts/{id}` | autenticado | Detalle con hitos |
| POST | `/contracts/{id}/milestones/{milestoneId}/deliverables` | FREELANCER | Registrar entregable |
| POST | `/contracts/{id}/milestones/{milestoneId}/submit` | FREELANCER | Enviar hito a revisión |
| POST | `/contracts/{id}/milestones/{milestoneId}/approve` | CLIENT | Aprobar hito |
| POST | `/contracts/{id}/milestones/{milestoneId}/reject` | CLIENT | Rechazar hito |
| POST | `/contracts/{id}/cancel` | autenticado | Cancelar con motivo |
| POST | `/contracts/{id}/milestones/{milestoneId}/deliverables/{deliverableId}/tasks` | FREELANCER | Agregar tarea |
| PATCH | `/contracts/{id}/milestones/{milestoneId}/deliverables/{deliverableId}/tasks/{taskId}` | FREELANCER | Marcar tarea |
| POST | `/contracts/{id}/milestones/{milestoneId}/deliverables/{deliverableId}/comments` | autenticado | Comentar |

### Mensajería, reseñas y pagos

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| POST | `/contracts/{contractId}/conversations` | autenticado | Abrir/recuperar conversación |
| GET | `/conversations` | autenticado | Mis conversaciones |
| GET | `/conversations/{id}` | autenticado | Detalle con mensajes |
| POST | `/conversations/{id}/messages` | autenticado | Enviar mensaje |
| POST | `/conversations/{id}/read` | autenticado | Marcar como leído |
| POST | `/conversations/{id}/archive` | autenticado | Archivar |
| POST | `/contracts/{contractId}/reviews` | autenticado | Reseñar contraparte |
| GET | `/contracts/{contractId}/reviews` | autenticado | Reseñas del contrato |
| GET | `/users/{userId}/reviews` | autenticado | Reseñas recibidas |
| POST | `/contracts/{id}/payments` | CLIENT | Liberar pago |
| GET | `/contracts/{id}/payments` | autenticado | Historial del contrato |
| GET | `/payments` | autenticado | Mis pagos |

### Notificaciones y administración

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET | `/notifications` | autenticado | Mis notificaciones |
| POST | `/notifications/{id}/retry` | autenticado | Reintentar entrega |
| GET | `/admin/reports` | ADMIN | Reportes (Visitor) |
| POST | `/admin/projects/{id}/moderate` | ADMIN | Pausar/cancelar proyecto |
| POST | `/admin/users/{id}/suspend` | ADMIN | Suspender usuario |
| GET | `/admin/audit` | ADMIN | Últimos eventos auditados |

## Flujo curl

```bash
BASE=http://localhost:8080/api/v1

# 1. Registro
CLIENT=$(curl -s -X POST $BASE/auth/register -H "Content-Type: application/json" \
  -d '{"email":"cliente@demo.io","password":"FreeConnect#2026Aa","displayName":"Cliente Demo","role":"CLIENT"}')
FREELANCER=$(curl -s -X POST $BASE/auth/register -H "Content-Type: application/json" \
  -d '{"email":"freelancer@demo.io","password":"FreeConnect#2026Aa","displayName":"Freelancer Demo","role":"FREELANCER"}')

CT=$(echo $CLIENT | jq -r .accessToken)
FT=$(echo $FREELANCER | jq -r .accessToken)

# 2. Perfil del freelancer
curl -s -X PUT $BASE/profiles/me -H "Authorization: Bearer $FT" -H "Content-Type: application/json" \
  -d '{"headline":"Backend Java","bio":"Spring Boot","hourlyRate":45,"currency":"USD","availability":"AVAILABLE","skills":["Java","Spring Boot"]}'

# 3. Proyecto publicado
PROJECT=$(curl -s -X POST $BASE/projects -H "Authorization: Bearer $CT" -H "Content-Type: application/json" \
  -d '{"title":"API de reservas","description":"API REST con Spring Boot","category":"SOFTWARE","budget":1000,"currency":"USD","modality":"REMOTE","deadline":"2026-12-31","skills":["Java"]}')
PID=$(echo $PROJECT | jq -r .id)
curl -s -X PATCH $BASE/projects/$PID/status -H "Authorization: Bearer $CT" -H "Content-Type: application/json" \
  -d '{"status":"PUBLISHED"}'

# 4. Propuesta
PROPOSAL=$(curl -s -X POST $BASE/projects/$PID/proposals -H "Authorization: Bearer $FT" -H "Content-Type: application/json" \
  -d '{"coverLetter":"Tengo experiencia construyendo APIs REST con Spring Boot y PostgreSQL.","amount":1000,"currency":"USD","estimatedDays":20}')
PROPOSAL_ID=$(echo $PROPOSAL | jq -r .id)

# 5. Aceptar y crear contrato con 2 hitos
CONTRACT=$(curl -s -X POST $BASE/proposals/$PROPOSAL_ID/accept -H "Authorization: Bearer $CT" -H "Content-Type: application/json" \
  -d '{"commissionPlan":"PREMIUM","milestoneType":"EQUAL_SPLIT","parts":2}')
CID=$(echo $CONTRACT | jq -r .id)
MID=$(echo $CONTRACT | jq -r '.milestones[0].id')

# 6. Entregable, envío y aprobación
curl -s -X POST $BASE/contracts/$CID/milestones/$MID/deliverables -H "Authorization: Bearer $FT" -H "Content-Type: application/json" \
  -d '{"title":"Modulo de autenticacion","description":"JWT + refresh tokens","url":"https://example.com/repo"}'
curl -s -X POST $BASE/contracts/$CID/milestones/$MID/submit -H "Authorization: Bearer $FT"
curl -s -X POST $BASE/contracts/$CID/milestones/$MID/approve -H "Authorization: Bearer $CT"

# 7. Mensajería, reseña y pago
CONV=$(curl -s -X POST $BASE/contracts/$CID/conversations -H "Authorization: Bearer $CT")
CONV_ID=$(echo $CONV | jq -r .id)
curl -s -X POST $BASE/conversations/$CONV_ID/messages -H "Authorization: Bearer $CT" -H "Content-Type: application/json" \
  -d '{"body":"Buen trabajo"}'
curl -s -X POST $BASE/contracts/$CID/reviews -H "Authorization: Bearer $CT" -H "Content-Type: application/json" \
  -d '{"rating":5,"comment":"Excelente entrega"}'
curl -s -X POST $BASE/contracts/$CID/payments -H "Authorization: Bearer $CT" -H "Content-Type: application/json" \
  -d '{"amount":1000}'
```

El script `scripts/e2e.ps1` ejecuta este flujo completo (y los módulos
adicionales) con aserciones, sin depender de `jq`.

## Estructura

```
freeconnect/
├── src/main/java/com/freeconnect/
│   ├── identity/        # auth, usuarios, JWT, refresh tokens
│   ├── profiles/        # perfiles, habilidades, portafolio, almacenamiento
│   ├── projects/        # proyectos, filtros, plantillas, exportación
│   ├── proposals/       # propuestas y validación encadenada
│   ├── contracts/       # contratos, hitos, entregables, comandos
│   ├── messaging/       # conversaciones y mensajes
│   ├── reviews/         # reseñas y reputación
│   ├── payments/        # pagos y pasarelas
│   ├── notifications/   # outbox multicanal
│   ├── admin/           # reportes y moderación
│   ├── audit/           # auditoría de eventos
│   └── shared/          # cross-cutting
├── src/main/resources/db/migration/   # V1..V6
├── src/test/java/                      # unitarias + integración
├── scripts/e2e.ps1                     # humo E2E
├── docs/                               # arquitectura, patrones, UML
├── Dockerfile
└── docker-compose.yml
```

## Documentación

- [Arquitectura](docs/ARQUITECTURA.md)
- [Patrones de diseño](docs/PATRONES.md)
- [Frontend: alcance, rutas y limitaciones](frontend/README.md)
- [Diagramas UML](docs/uml/README.md)
- [Skill UML Generator](.opencode/skills/uml-generator/SKILL.md)
- [Guía para agentes](AGENT.md)

## Documentación UML

Los diagramas de sistema están en [`docs/uml/`](docs/uml/README.md) en formato **Mermaid**,
renderizable directamente en GitHub/GitLab y versionable como texto:

| Diagrama | Contenido |
|----------|-----------|
| [Casos de uso](docs/uml/casos-de-uso.md) | Actores CLIENT, FREELANCER, ADMIN y protección por rol. |
| [Clases de dominio](docs/uml/clases-dominio.md) | Entidades, campos, relaciones y cardinalidades. |
| [Secuencia del flujo principal](docs/uml/secuencia-flujo-principal.md) | Ciclo E2E: registro → proyecto → propuesta → contrato → entrega → pago → reseña. |
| [Secuencia de autenticación](docs/uml/secuencia-autenticacion.md) | Registro, login, refresh single-flight, logout y `hasRole`. |
| [Secuencia de administración](docs/uml/secuencia-admin.md) | Reportes, moderación, usuarios y auditoría. |
| [Componentes](docs/uml/componentes.md) | SPA, API, H2/PostgreSQL, Redis ausente, nginx y Docker. |
| [Despliegue](docs/uml/despliegue.md) | Entorno local verificado y objetivo Docker no verificado. |
| [Estados del hito](docs/uml/estados-hito.md) | `PENDING → IN_PROGRESS → SUBMITTED → APPROVED/REJECTED`. |

> Los diagramas PlantUML previos se conservan en la misma carpeta y se indexan en
> [`docs/uml/README.md`](docs/uml/README.md).

## Seguridad

- Los access tokens viven 15 minutos; los refresh tokens son opacos, se guardan
  como digest SHA-256 y rotan en cada uso.
- Las contraseñas usan BCrypt (12 rondas) o Argon2id según configuración.
- Los casos de uso verifican propiedad y participación; los controladores sólo
  declaran roles.
- El rate limit es configurable y se desactiva en tests.
- Nunca se registran contraseñas, tokens ni cuerpos sensibles.

## Límites conocidos

- El frontend SPA vive en `frontend/` (React 19 + Vite) y consume esta API; sus
  limitaciones y supuestos se documentan en [`frontend/README.md`](frontend/README.md).
- Email/push y pasarela de pagos son simulados (patrones y puertos reales).
- El almacenamiento incluido es de sistema de archivos; `ObjectStore` está listo
  para S3/MinIO.
- Redis está disponible en Compose, pero el rate limit del MVP es en memoria.
- La validación E2E principal se ejecutó con H2 en modo PostgreSQL; PostgreSQL
  real se usa en el perfil `docker`.
