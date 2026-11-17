# Despliegue

Dos escenarios: el **local** realmente utilizado para desarrollar y ejecutar los
E2E, y el **objetivo con Docker**, que está declarado pero **no se verificó en este
entorno** porque Docker no está instalado.

## Local (verificado)

```mermaid
flowchart LR
    Dev["Desarrollador<br/>navegador"] -->|"http://localhost:5173"| Vite["Vite dev server<br/>React SPA<br/>:5173"]
    Vite -->|"fetch http://localhost:8080/api/v1"| API["Spring Boot<br/>perfil dev<br/>:8080"]
    API --> H2[("H2 en memoria<br/>jdbc:h2:mem:freeconnect")]
    API --> FS[("Sistema de archivos<br/>portafolio")]
    API -.->|"health 503"| Redis[("Redis<br/>no disponible")]
    API -->|"Swagger UI / OpenAPI"| Docs["/swagger-ui.html<br/>/v3/api-docs"]
    API --> Act["/actuator/health"]

    Vite -.->|"CORS permitido"| CORS["FREECONNECT_CORS_ALLOWED_ORIGINS=<br/>http://localhost:5173,<br/>http://localhost:3000,<br/>http://localhost:8080"]
```

### Arranque local

```powershell
# Terminal 1 — backend con CORS para Vite y E2E
$env:FREECONNECT_CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000,http://localhost:8080"
mvn spring-boot:run

# Terminal 2 — frontend
cd frontend
npm run dev
```

- API: <http://localhost:8080/api/v1>
- Swagger: <http://localhost:8080/swagger-ui.html>
- H2 console: <http://localhost:8080/h2-console>
- Frontend: <http://localhost:5173>
- Health: <http://localhost:8080/actuator/health>

> El perfil `dev` usa H2 en memoria: cada reinicio borra datos y vuelve a sembrar
> la cuenta `admin@freeconnect.io`.

## Docker (objetivo, no verificado)

```mermaid
flowchart TB
    User["Usuario<br/>navegador"] -->|"http://localhost:3000"| FE["frontend<br/>nginx :80<br/>SPA + proxy /api y /assets"]
    FE -->|"proxy_pass http://app:8080"| APP["app<br/>Spring Boot<br/>perfil docker · :8080"]
    APP --> PG[("postgres:16-alpine<br/>host :5433 -> :5432<br/>volumen postgres-data")]
    APP --> REDIS[("redis:7-alpine<br/>:6379<br/>volumen redis-data")]
    APP --> VOL[("volumen portfolio-data<br/>archivos adjuntos")]
    APP --> GW["PaymentGateway MOCK"]
    APP --> HEALTH["healthcheck /actuator/health"]
    PG -.->|"depends_on healthy"| APP
    REDIS -.->|"depends_on healthy"| APP

    subgraph Compose["docker-compose.yml"]
        FE
        APP
        PG
        REDIS
    end
```

### Componentes declarados

| Servicio | Imagen / build | Puerto host | Estado |
|----------|----------------|-------------|--------|
| `frontend` | `frontend/Dockerfile` (multi-stage Node + nginx) | `3000 -> 80` | ⚠️ No verificado |
| `app` | `Dockerfile` multi-stage Maven + JRE | `8080` | ⚠️ No verificado |
| `postgres` | `postgres:16-alpine` | `5433 -> 5432` | ⚠️ No verificado |
| `redis` | `redis:7-alpine` | `6379` | ⚠️ No verificado |

### Variables y proxy

- El frontend se construye con `VITE_API_BASE_URL=/api/v1` para evitar CORS.
- `nginx.conf` sirve `index.html` para rutas SPA y hace proxy de `/api/` y `/assets/` a `app:8080`.
- El backend en Docker usa `SPRING_PROFILES_ACTIVE=docker`, `DATABASE_URL` y Flyway.
- **No se modificaron** `docker-compose.yml`, `Dockerfile` ni `nginx.conf` en esta fase.

### Limitación de verificación

```text
> docker version
docker: The term 'docker' is not recognized...
```

Por tanto, el build de contenedores, las migraciones Flyway sobre PostgreSQL real y
el proxy de nginx quedan como **pendientes de verificación**. El despliegue local sí
está validado con build, tests unitarios y E2E Playwright.

## Matriz de entornos

| Aspecto | Local | Docker objetivo |
|---------|-------|-----------------|
| Base de datos | H2 en memoria | PostgreSQL 16 |
| Migraciones | Hibernate / H2 | Flyway (`V1..V6`) |
| CORS | Necesario (5173 → 8080) | Innecesario (mismo origen vía nginx) |
| Refresh token | `sessionStorage` | `sessionStorage` (misma SPA) |
| Redis | Ausente (health 503) | Declarado con healthcheck |
| Pagos | Mock | Mock |
| Verificado | ✅ | ❌ |
