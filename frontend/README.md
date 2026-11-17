# FreeConnect · Frontend

Aplicación web SPA en **React 19 + TypeScript + Vite** para el MVP de FreeConnect. Consume la API Spring Boot existente en `/api/v1` y cubre los flujos de **CLIENT**, **FREELANCER** y **ADMIN**: autenticación, perfiles, proyectos, propuestas, contratos por hitos, entregables, mensajería, notificaciones, pagos simulados, reseñas y administración.

> No se modificó el backend para construir esta interfaz. Todas las rutas y contratos provienen del OpenAPI real en `schema/openapi.json` (generado desde `http://localhost:8080/v3/api-docs`).

---

## Stack

| Capa | Tecnología |
|------|------------|
| UI | React 19, React DOM |
| Lenguaje | TypeScript 5.8 (strict) |
| Build | Vite 6 |
| Router | React Router 6 |
| Datos | TanStack Query 5 |
| Formularios | React Hook Form + Zod |
| Estilos | Tailwind CSS 3 |
| Iconos | lucide-react |
| Tests unitarios | Vitest + Testing Library |
| E2E | Playwright |
| Tipos API | openapi-typescript |

---

## Requisitos

- Node.js 20+ (recomendado 22).
- npm 10+.
- Backend FreeConnect disponible en `http://localhost:8080` (perfil `dev`).
- Navegador moderno para Playwright (`npx playwright install chromium`).

---

## Inicio rápido

```bash
# 1. Backend (en otra terminal)
cd freeconnect
mvn spring-boot:run

# 2. Frontend
cd freeconnect/frontend
npm install
cp .env.example .env
npm run dev
```

Aplicación: <http://localhost:5173>

El backend de desarrollo usa H2 en memoria. La cuenta administrativa inicial es:

```
admin@freeconnect.io
Admin#FreeConnect2026
```

Los usuarios cliente y freelancer se crean desde `/registro`.

---

## Variables de entorno

Archivo `.env` (basado en `.env.example`):

| Variable | Por defecto | Descripción |
|----------|-------------|-------------|
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1` | Base de la API. En Docker se usa `/api/v1` con proxy de nginx. |
| `VITE_NOTIFICATIONS_POLL_MS` | `30000` | Polling de notificaciones. `0` lo desactiva. |
| `VITE_MESSAGING_POLL_MS` | `5000` | Polling de conversaciones y mensajes. `0` lo desactiva. |

No hay secretos en el frontend. Las variables `VITE_*` son públicas por diseño.

---

## Scripts

```bash
npm run dev             # Servidor de desarrollo
npm run build           # tsc -b + build de producción
npm run preview         # Previsualizar dist
npm run lint            # ESLint 9 (flat config)
npm run format          # Prettier
npm run test            # Vitest (una pasada)
npm run test:watch      # Vitest en watch
npm run test:e2e        # Playwright
npm run gen:api         # Regenerar tipos desde schema/openapi.json
npm run gen:api:live    # Regenerar tipos desde el backend en ejecución
```

El archivo `src/types/api.ts` es **generado**. No editarlo a mano: usa `npm run gen:api` o `npm run gen:api:live`.

---

## Estructura

```
src/
├── app/                 # Layouts, router, providers, error boundary
├── features/
│   ├── admin/           # Reportes, moderación, usuarios, auditoría
│   ├── auth/            # Login, registro, guards, AuthProvider
│   ├── contracts/       # Contratos, hitos, entregables, tareas
│   ├── dashboard/       # Panel adaptado por rol
│   ├── landing/         # Página pública de inicio
│   ├── messaging/       # Conversaciones y mensajes
│   ├── notifications/   # Bandeja y reintentos
│   ├── payments/        # Pagos simulados
│   ├── profiles/        # Perfil y portafolio
│   ├── projects/        # Proyectos, filtros, plantillas, mis proyectos
│   ├── proposals/       # Propuestas y aceptación
│   └── reviews/         # Reseñas
├── shared/
│   ├── api/             # Cliente HTTP, errores, sesión, query keys
│   ├── components/      # UI reutilizable y accesible
│   ├── hooks/           # Hooks compartidos
│   └── utils/           # Formato, etiquetas, clases
├── types/
│   ├── api.ts           # GENERADO desde OpenAPI
│   └── models.ts        # Tipos normalizados de la app
└── main.tsx
```

---

## Rutas

### Públicas

| Ruta | Pantalla |
|------|----------|
| `/` | Landing con proyectos publicados recientes |
| `/proyectos` | Catálogo con filtros y paginación en URL |
| `/proyectos/:id` | Detalle del proyecto y postulación |
| `/login`, `/registro` | Autenticación |

### Panel autenticado (`/app`)

| Ruta | Roles |
|------|-------|
| `/app` | Todos |
| `/app/perfil` | CLIENT, FREELANCER |
| `/app/notificaciones` | Todos |
| `/app/mis-proyectos` | CLIENT |
| `/app/proyectos/nuevo`, `/app/proyectos/:id/editar` | CLIENT |
| `/app/propuestas` | CLIENT |
| `/app/contratos`, `/app/contratos/:id` | CLIENT, FREELANCER |
| `/app/pagos` | CLIENT, FREELANCER |
| `/app/resenas` | CLIENT, FREELANCER |
| `/app/mensajes`, `/app/mensajes/:id` | CLIENT, FREELANCER |
| `/app/admin/reportes` | ADMIN |
| `/app/admin/moderacion` | ADMIN |
| `/app/admin/usuarios` | ADMIN |
| `/app/admin/auditoria` | ADMIN |

Las rutas están protegidas con `RequireAuth` y `RequireRole`. Además, la navegación lateral se construye según el rol y las acciones de cada pantalla respetan las reglas del backend.

---

## Flujos cubiertos

### Cliente

1. Registro/login y creación de perfil de empresa.
2. Creación de proyecto en estado `DRAFT` y publicación.
3. Edición y transiciones de estado (`DRAFT → PUBLISHED → PAUSED/IN_PROGRESS → FINISHED/CANCELLED`).
4. Recepción, evaluación, rechazo y aceptación de propuestas.
5. Creación de contrato al aceptar: hitos únicos, reparto igual o personalizados.
6. Aprobación/rechazo de hitos, liberación de pagos simulados y reseña.
7. Conversaciones y notificaciones.
8. Exportación CSV de “mis proyectos” y clonado de plantillas.

### Freelancer

1. Registro/login y perfil profesional con habilidades y portafolio.
2. Búsqueda de proyectos publicados y envío/retiro de propuestas.
3. Gestión de contratos: entregables, tareas, comentarios y envío de hitos.
4. Consulta de pagos netos, reseñas recibidas y mensajería.

### Administración

1. Reportes agregados del backend.
2. Moderación de proyectos (pausar/cancelar).
3. Suspensión de usuarios por UUID.
4. Auditoría de los últimos 100 eventos.

---

## Autenticación y sesión

- El **access token** se mantiene solo en memoria (variable del módulo `shared/api/session.ts`).
- El **refresh token** se guarda en `sessionStorage` para sobrevivir a recargas de pestaña.
- Ante un `401`, el cliente intenta **un único refresh** (single-flight) y reintenta la petición original una vez.
- Si el refresh falla, se limpia la sesión y se notifica a la app para volver a `/login`.
- El logout revoca el refresh token en `POST /auth/logout` y limpia el estado local incluso si la revocación remota falla.

### Riesgo de seguridad documentado

Guardar el refresh token en `sessionStorage` lo expone a **XSS**: cualquier script inyectado podría leerlo. Para producción se recomienda que el backend lo emita en una cookie `HttpOnly; Secure; SameSite=Lax/Strict` y que el refresh se haga contra esa cookie. Esta mejora requiere cambios coordinados en el backend y no se implementó para no alterar su contrato.

---

## Manejo de errores

`shared/api/errors.ts` implementa `ApiError` y `parseApiError`, que soportan:

- El contrato real del backend: `application/json` con `{ timestamp, status, code, message, path, details }`.
- Respuestas tipo RFC 7807 (`application/problem+json`) con `detail`, `title`, `type`, `instance`, `errors`.
- Respuestas de texto plano o sin cuerpo.

La UI muestra siempre un `ErrorAlert` uniforme con mensaje, código y detalles por campo cuando existen. Los errores de red y timeout se traducen a mensajes en español.

---

## Paginación, filtros y URL

El catálogo sincroniza en la URL: `q`, `category`, `modality`, `status`, `minBudget`, `maxBudget`, `skill`, `filter`, `page` y `size`. Esto permite compartir enlaces y usar atrás/adelante del navegador.

Por defecto se consulta `status=PUBLISHED` para no exponer borradores. El parámetro `status=` (vacío) significa “todos los estados”.

---

## Archivos y descargas

- Portafolio: subida `multipart/form-data` a `POST /profiles/me/portfolio/{itemId}/attachment` y descarga autenticada por blob.
- Exportación CSV: `GET /projects/mine/export` con descarga autenticada y nombre de archivo del `Content-Disposition`.

---

## Docker

### Solo frontend

```bash
cd freeconnect/frontend
docker build -t freeconnect-frontend --build-arg VITE_API_BASE_URL=/api/v1 .
docker run --rm -p 3000:80 --network freeconnect_default freeconnect-frontend
```

El contenedor sirve la SPA con nginx y hace proxy de `/api/` y `/assets/` hacia el servicio `app` del compose.

### Stack completo

Desde `freeconnect/`:

```bash
docker compose up --build
```

Servicios:

- Frontend: <http://localhost:3000>
- API: <http://localhost:8080/api/v1>
- Swagger: <http://localhost:8080/swagger-ui.html>

El frontend se construye con `VITE_API_BASE_URL=/api/v1`; nginx evita CORS al servir API y SPA desde el mismo origen.

---

## Pruebas

### Unitarias

```bash
npm run test
```

Cubren:

- Parseo del contrato de errores propio y RFC 7807.
- Sesión en memoria + `sessionStorage` y notificaciones de cambio.
- Refresh automático ante 401 con reintento único y rotación.
- Sincronización y normalización de filtros de proyectos en URL.
- Componentes reutilizables (`Pagination`, `EmptyState`).

### E2E (Playwright)

Con el backend en `http://localhost:8080` y CORS habilitado para el origen de Vite
(`FREECONNECT_CORS_ALLOWED_ORIGINS=http://localhost:5173,...`):

```bash
npx playwright install chromium
npm run test:e2e
```

Variables opcionales:

| Variable | Por defecto |
|----------|-------------|
| `E2E_BASE_URL` | `http://localhost:5173` |
| `E2E_API_URL` | `http://localhost:8080/api/v1` |
| `E2E_SKIP_WEBSERVER` | vacío (levanta Vite automáticamente) |

El smoke test genera usuarios únicos, publica un proyecto, envía una propuesta, acepta el contrato, intercambia mensajes, gestiona un entregable/hito, aprueba, libera un pago y deja una reseña. Un segundo spec verifica el panel de administración y el bloqueo de rutas por rol. Consulta `e2e/README.md` para el detalle y para el requisito de CORS en el backend.

---

## Decisiones y supuestos

- **No se inventaron endpoints.** Todo consumo está en `schema/openapi.json`.
- Los roles se verificaron en las anotaciones `@PreAuthorize` del backend, porque el OpenAPI no publica metadatos de seguridad.
- El backend devuelve errores con un formato propio, no RFC 7807 estándar; el cliente soporta ambos.
- Los pagos son simulados (`PAYMENT_PROVIDER=MOCK`). No hay integración real ni WebSockets.
- El catálogo público excluye borradores por defecto.
- La sesión no persiste el access token entre recargas; se recupera con refresh.

---

## Limitaciones y brechas del backend

Estas limitaciones se manejan en la UI y se documentan aquí para que el equipo decida si conviene ampliar la API:

1. **No existe `GET /projects/mine`.** “Mis proyectos” consulta hasta 10 páginas de 100 proyectos públicos y filtra por `clientId` en el navegador. Es costoso y no escala. Se propone añadir `GET /projects/mine` paginado.
2. **No existe un listado de propuestas propias.** El freelancer ve sus propuestas dentro del detalle de cada proyecto. Se propone `GET /proposals/mine`.
3. **No existe listado ni búsqueda de usuarios.** La suspensión administrativa requiere pegar el UUID manualmente. Se propone `GET /admin/users?query=` y reactivación.
4. **No hay consulta de perfil por `userId`.** Las contrapartes se muestran con IDs cortos. Se propone `GET /profiles/by-user/{userId}` o incluir `displayName` en los DTO.
5. **Un hito rechazado no puede reenviarse.** Añadir un entregable deja el hito en `REJECTED`, y la máquina de estados solo permite `REJECTED → IN_PROGRESS` (sin endpoint para esa transición). La UI lo advierte; se propone permitir reenvío o una transición explícita.
6. **El administrador no puede abrir contratos ni perfil público.** La navegación admin se limita a sus secciones. Los reportes (`GET /admin/reports`), la moderación y la suspensión sí están protegidos por `hasRole('ADMIN')` en `SecurityConfig` y `@PreAuthorize`.
7. **No hay WebSockets.** Mensajería y notificaciones usan polling configurable.
8. **No existen endpoints de edición/borrado de mensajes** ni marcado de notificaciones como leídas.

---

## Seguridad

- No se usan `dangerouslySetInnerHTML` ni se interpreta HTML del backend.
- Los enlaces externos del portafolio usan `rel="noreferrer noopener"`.
- Las rutas y acciones se adaptan al rol, pero **la autorización real es del backend**.
- El token de acceso no se escribe en `localStorage`.
- Se recomienda migrar el refresh token a cookie `HttpOnly` y añadir CSP en el servidor de producción.

---

## Troubleshooting

| Síntoma | Causa probable | Solución |
|---------|----------------|----------|
| “No se pudo contactar con el servidor” | Backend apagado o URL incorrecta | Verifica `http://localhost:8080/actuator/health` y `VITE_API_BASE_URL`. |
| 401 en bucle | Refresh inválido o revocado | Cierra sesión y vuelve a entrar. |
| CORS | Origen distinto sin permiso | Usa el proxy de nginx o añade el origen a `CORS_ALLOWED_ORIGINS`. |
| E2E no encuentra Chromium | Navegador no instalado | `npx playwright install chromium`. |
| Tipos desactualizados | OpenAPI cambió | `npm run gen:api` o `npm run gen:api:live`. |
