# Secuencia de autenticación

Registro, login, restauración de sesión, refresh single-flight y logout. El access
token vive solo en memoria; el refresh token se persiste en `sessionStorage` (riesgo
XSS documentado) y rota en cada uso.

## Registro

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant SPA as SPA React
    participant API as API Spring Boot
    participant DB as Base de datos

    U->>SPA: /registro (rol CLIENT o FREELANCER)
    SPA->>SPA: Validación Zod + React Hook Form
    SPA->>API: POST /auth/register
    API->>DB: Verifica email único
    alt Email duplicado
        API-->>SPA: 409 EMAIL_ALREADY_EXISTS
        SPA-->>U: ErrorAlert uniforme
    else Datos válidos
        API->>DB: INSERT user (ACTIVE) + profile
        API-->>SPA: 201 AuthResponse {accessToken, refreshToken, user}
        SPA->>SPA: session.setAuth(auth)
        SPA-->>U: Redirige a /app
    end
```

## Login y bootstrap

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant SPA as SPA React
    participant Auth as AuthProvider
    participant Sess as session (memoria + sessionStorage)
    participant API as API Spring Boot

    U->>SPA: /login (email + contraseña)
    SPA->>API: POST /auth/login
    alt Credenciales inválidas
        API-->>SPA: 401 INVALID_CREDENTIALS
        SPA-->>U: "No se pudo iniciar sesión"
    else Credenciales válidas
        API-->>SPA: 200 AuthResponse
        SPA->>Auth: login()
        Auth->>Sess: setAuth: access en memoria, refresh en sessionStorage
        Auth-->>SPA: user + status "authenticated"
        SPA-->>U: Redirige a /app (o a location.state.from)
    end

    Note over SPA,API: Recarga de pestaña
    SPA->>Auth: Bootstrap al montar
    Auth->>Sess: hasStoredSession()
    alt No hay refresh
        Auth-->>SPA: status "anonymous"
    else Hay refresh
        Auth->>API: POST /auth/refresh {refreshToken}
        alt Refresh válido
            API-->>Auth: 200 AuthResponse (refresh rotado)
            Auth->>Sess: setAuth(nuevo par)
            Auth-->>SPA: status "authenticated"
        else Refresh inválido/revocado
            API-->>Auth: 401
            Auth->>Sess: clear()
            Auth-->>SPA: status "anonymous"
        end
    end
```

## Refresh automático ante 401

```mermaid
sequenceDiagram
    autonumber
    participant Caller as Hook/Query
    participant Client as api client
    participant Sess as session
    participant API as API Spring Boot

    Caller->>Client: GET /contracts (Authorization: Bearer access)
    API-->>Client: 401 Unauthorized
    Client->>Sess: getRefreshToken()
    alt No hay refresh o ya es reintento
        Client->>Client: notifySessionExpired()
        Client-->>Caller: ApiError 401
    else Hay refresh y es el primer intento
        Client->>API: POST /auth/refresh (single-flight)
        alt Refresh válido
            API-->>Client: 200 AuthResponse
            Client->>Sess: setAuth(nuevo access + refresh)
            Client->>API: GET /contracts (reintento único)
            API-->>Client: 200 datos
            Client-->>Caller: datos
        else Refresh inválido
            API-->>Client: 401
            Client->>Sess: clear()
            Client->>Client: notifySessionExpired()
            Client-->>Caller: ApiError 401
            Note over Client: AuthProvider escucha "freeconnect:session-expired" y pasa a anonymous
        end
    end
```

## Logout

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant SPA as SPA React
    participant Auth as AuthProvider
    participant API as API Spring Boot
    participant Sess as session

    U->>SPA: "Cerrar sesión"
    SPA->>Auth: logout()
    Auth->>Sess: getRefreshToken()
    Auth->>API: POST /auth/logout {refreshToken}
    alt Revocación correcta
        API-->>Auth: 204
    else Fallo de red o token ya revocado
        API-->>Auth: error
        Note over Auth: Se ignora: la sesión local se limpia igualmente
    end
    Auth->>Sess: clear()
    Auth-->>SPA: user=null, status="anonymous"
    SPA-->>U: Redirige a /login
```

## Rol y autorización

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant SPA as SPA React
    participant API as API Spring Boot
    participant Sec as Spring Security

    U->>SPA: Navega a /app/admin/reportes
    SPA->>SPA: RequireRole roles=["ADMIN"]
    alt Usuario no ADMIN
        SPA-->>U: "No tienes permiso para ver esta sección"
    else Usuario ADMIN
        SPA->>API: GET /api/v1/admin/reports (Bearer)
        API->>Sec: JwtAuthenticationFilter valida firma y expiración
        Sec->>Sec: SecurityConfig: /api/v1/admin/** hasRole('ADMIN')
        Sec->>Sec: AdminController @PreAuthorize("hasRole('ADMIN')")
        alt Sin rol ADMIN
            Sec-->>SPA: 403 ACCESS_DENIED
        else Autorizado
            Sec-->>API: proceed
            API-->>SPA: 200 reportes
        end
    end
```

## Propiedades de seguridad

| Aspecto | Implementación |
|---------|----------------|
| Access token | Variable de módulo en `shared/api/session.ts`; no se escribe en `localStorage`. |
| Refresh token | `sessionStorage`; se envía solo a `/auth/refresh` y `/auth/logout`. |
| Rotación | Cada refresh devuelve un refresh nuevo; el anterior queda revocado. |
| Reintento 401 | Una sola vez por petición, con promesa single-flight compartida. |
| Logout | Revoca en backend y limpia estado local aunque falle la red. |
| Autorización real | Backend (`SecurityConfig` + `@PreAuthorize`); el frontend solo evita mostrar UI. |
| Riesgo XSS | Documentado en `frontend/README.md`; se recomienda cookie `HttpOnly` en producción. |
