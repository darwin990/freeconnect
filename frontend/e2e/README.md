# E2E smoke (Playwright)

Pruebas de humo contra el backend real de FreeConnect. No usan mocks: crean
cuentas, proyectos, contratos, pagos y reseñas en la base de datos H2 en memoria
del backend de desarrollo.

## Requisitos

1. Backend en `http://localhost:8080` con CORS para el origen de Vite:

   ```powershell
   $env:FREECONNECT_CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000,http://localhost:8080"
   java -jar target/freeconnect.jar
   ```

   Sin esa variable el navegador bloquea las peticiones desde `localhost:5173`.

2. Navegador de Playwright instalado (solo la primera vez):

   ```powershell
   npx playwright install chromium
   ```

## Ejecución

```powershell
npm run test:e2e
```

`playwright.config.ts` levanta `npm run dev` automáticamente si no hay un Vite
escuchando en `E2E_BASE_URL`. Para usar un servidor ya iniciado:

```powershell
$env:E2E_SKIP_WEBSERVER="1"
npm run test:e2e
```

Variables opcionales:

| Variable | Default | Uso |
| --- | --- | --- |
| `E2E_BASE_URL` | `http://localhost:5173` | Origen del frontend |
| `E2E_API_URL` | `http://localhost:8080/api/v1` | Metadato informativo del API |
| `E2E_SKIP_WEBSERVER` | vacío | No arrancar Vite |

## Cobertura

- `full-flow.spec.ts`: registro de cliente, creación y publicación de proyecto,
  registro de freelancer, propuesta, aceptación con contrato de hito único,
  conversación con mensajes de ambas partes, entregable, envío de hito a
  revisión, aprobación (contrato completado), pago simulado, reseña y bloqueo de
  rutas por rol.
- `admin-smoke.spec.ts`: login con la cuenta `admin@freeconnect.io`, acceso a
  reportes, moderación, usuarios y auditoría, y bloqueo de secciones de cliente.

Cada ejecución genera correos con sufijo temporal (`*@example.com`), por lo que
puede repetirse contra el mismo backend sin chocar con cuentas previas. El
proyecto y contrato creados quedan en la base en memoria hasta reiniciar el
backend.

## Limitaciones conocidas

- El backend usa H2 en memoria: al reiniciarlo se pierden los datos y la cuenta
  admin se vuelve a sembrar.
- Redis no es necesario para estos flujos, pero `/actuator/health` queda en 503
  si Redis no está disponible.
- Los flujos que el backend no expone (listado de “mis proyectos” propio,
  listado de “mis propuestas”, perfil por `userId`) no se cubren aquí; están
  documentados en `../README.md`.
