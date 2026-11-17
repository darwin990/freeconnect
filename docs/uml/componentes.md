# Componentes de alto nivel

Vista de contenedores y dependencias externas. El estado operativo refleja lo
verificado en este entorno de desarrollo (H2 en memoria, sin Redis ni Docker).

```mermaid
flowchart TB
    subgraph Cliente["Cliente"]
        Browser["Navegador<br/>React 19 + TypeScript + Vite"]
    end

    subgraph Frontend["Frontend SPA"]
        Router["React Router 6<br/>RequireAuth / RequireRole"]
        Query["TanStack Query 5<br/>caché + polling"]
        HTTP["api client<br/>fetch + refresh single-flight"]
        UI["Design system<br/>Tailwind + componentes accesibles"]
        Session["session.ts<br/>access en memoria<br/>refresh en sessionStorage"]
    end

    subgraph Backend["Backend monolito modular · Spring Boot 3.5"]
        Security["Spring Security 6<br/>JWT HS256 + @PreAuthorize"]
        API["Controllers REST /api/v1"]
        Modules["Módulos<br/>identity · profiles · projects · proposals<br/>contracts · messaging · reviews<br/>payments · notifications · admin · audit"]
        Events["Eventos de dominio<br/>outbox + auditoría"]
    end

    subgraph Datos["Persistencia"]
        H2[("H2 en memoria<br/>perfil dev/test<br/>OPERATIVO")]
        PG[("PostgreSQL 16<br/>perfil docker<br/>NO VERIFICADO")]
        Files[("Sistema de archivos<br/>portafolio<br/>OPERATIVO")]
    end

    subgraph Infra["Infraestructura"]
        Redis[("Redis 7<br/>Rate limit / caché<br/>AUSENTE · health 503")]
        Nginx["nginx<br/>SPA + proxy /api<br/>NO VERIFICADO"]
        Docker["Docker Compose<br/>app + postgres + redis + frontend<br/>NO DISPONIBLE EN ESTE HOST"]
        GW["PaymentGateway MOCK<br/>SIMULADO"]
    end

    Browser --> Router
    Router --> Query
    Query --> HTTP
    Router --> UI
    HTTP --> Session
    HTTP -->|"HTTPS/HTTP + Bearer"| Security
    Security --> API
    API --> Modules
    Modules --> Events
    Modules --> H2
    Modules -.->|"perfil docker"| PG
    Modules --> Files
    Modules -.->|"rate limit"| Redis
    Modules -->|"pagos simulados"| GW
    Nginx -.->|"sirve dist y proxy /api"| Frontend
    Docker -.->|"orquesta"| Backend
    Docker -.->|"orquesta"| PG
    Docker -.->|"orquesta"| Redis
    Docker -.->|"orquesta"| Nginx
```

## Estado operativo verificado

| Componente | Estado | Evidencia / nota |
|------------|--------|------------------|
| SPA React + Vite | ✅ Operativo | `npm run build`, `npm run test` y Playwright E2E en local. |
| API Spring Boot (perfil `dev`) | ✅ Operativo | Flujo E2E completo contra `http://localhost:8080/api/v1`. |
| H2 en memoria | ✅ Operativo | Datos efímeros; se reinicia al reiniciar el backend. |
| PostgreSQL 16 | ⚠️ No verificado | Definido en `docker-compose.yml`; sin Docker local. |
| Redis 7 | ❌ Ausente | `/actuator/health` degradado (503); el rate limit del MVP es en memoria. |
| nginx (frontend) | ⚠️ No verificado | `frontend/nginx.conf` y `Dockerfile` existen; no se construyó la imagen. |
| Docker Compose | ❌ No disponible | `docker` no está instalado en este host. |
| Pasarela de pagos | 🟡 Simulada | `PAYMENT_PROVIDER=MOCK`; sin cobros reales. |
| Almacenamiento de portafolio | ✅ Operativo | Sistema de archivos; `ObjectStore` listo para S3/MinIO. |
| WebSockets | ❌ No implementado | Mensajería y notificaciones usan polling configurable. |

## Decisiones de arquitectura relevantes

- **Monolito modular** con Clean Architecture por módulo (`domain`, `application`, `infrastructure`, `api`).
- **Estado y reglas** en el dominio (`Project`, `Proposal`, `Contract`, `Milestone`), no en controladores.
- **Seguridad stateless**: el servidor no guarda sesión; el JWT se valida en cada petición.
- **Errores uniformes**: `{timestamp,status,code,message,path,details}`; el cliente también acepta RFC 7807.
- **Outbox de notificaciones** y auditoría desacopladas del request principal.

> `%% TODO: confirmar` la topología definitiva de caché si se activa Redis en producción;
> hoy la caché del MVP usa Caffeine en proceso.
