# AGENT.md — Guía de trabajo en FreeConnect

Documento para agentes (humanos o IA) que continúen este repositorio. Describe
las reglas que no deben romperse, cómo verificar cambios y el estado real de la
entrega.

## 1. Contexto

FreeConnect es una API REST de contratación freelance construida como **monolito
modular** con Clean Architecture. El objetivo es demostrar diseño de software
(defensa en capas, patrones, tests, Docker, documentación), no un producto en
producción.

- Java 21, Spring Boot 3.5, Spring Security 6, JPA/Hibernate, PostgreSQL/H2.
- Flyway con migraciones portables `V1..V6`.
- Seguridad JWT + refresh tokens opacos rotativos.
- 315 archivos Java principales, 6 archivos de test, ~12.6k líneas.

## 2. Comandos

```bash
# Compilar
mvn -q -DskipTests compile

# Empaquetar
mvn -q -DskipTests package

# Tests (unitarios + integración MockMvc, perfil test/H2)
mvn test

# Ejecutar en desarrollo (H2 en memoria, perfil dev por defecto)
mvn spring-boot:run

# E2E contra la app en ejecución
powershell -ExecutionPolicy Bypass -File scripts/e2e.ps1

# Docker
docker compose up --build
```

En Windows/PowerShell, `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
puede interpretarse mal; el perfil `dev` ya es el default, así que basta
`mvn spring-boot:run`.

## 3. Reglas de arquitectura

1. **El dominio no depende de frameworks.** Nada de `@Entity`, `@Service`,
   `@RestController` ni imports de Spring/JPA en `domain`.
2. **Los controladores no tienen lógica de negocio ni repositorios.** Sólo
   validan DTOs, invocan casos de uso y devuelven vistas.
3. **Las entidades JPA viven en `infrastructure/persistence`.** Nunca se
   serializan en respuestas; los DTOs son `record`s.
4. **Todo cambio de estado pasa por el dominio** (State + eventos). No se
   actualizan estados con setters desde servicios.
5. **Los puertos se declaran en dominio/aplicación** y se implementan en
   infraestructura (`Repository`, `PaymentGateway`, `ObjectStore`,
   `AuditRecorder`, `RateLimiter`).
6. **Los eventos se publican después de persistir** y los listeners no deben
   romper la transacción de negocio (capturar y registrar).
7. **La autorización se hace dos veces:** rol en el controlador
   (`@PreAuthorize`) y propiedad/participación en el caso de uso
   (`CurrentUserProvider`).
8. **Errores uniformes** vía `GlobalExceptionHandler`; no devolver stacks ni
   mensajes internos.
9. **Migraciones aditivas.** Para cambiar el esquema, crear `V7__...sql`; no
   editar migraciones ya aplicadas.
10. **Sin Lombok.** El proyecto usa Java moderno (records, switch expressions,
    pattern matching).

## 4. Dónde agregar código

| Necesidad | Ubicación |
|-----------|-----------|
| Nueva regla/agregado | `com.freeconnect.<modulo>/domain` |
| Nuevo caso de uso | `<modulo>/application` con `@Service` y `@Transactional` |
| Nuevo endpoint | `<modulo>/api` + DTO `record` |
| Nueva persistencia | `<modulo>/infrastructure/persistence` (entidad + mapper + adapter) |
| Nuevo evento | `domain/...Event.java` + publicación en el agregado + listener |
| Nueva migración | `src/main/resources/db/migration/V<n>__descripcion.sql` |
| Patrón transversal | `shared/` (decoradores, paginación, error handling) |

## 5. Pruebas

- Unitarias de dominio: sin Spring, rápidas, enfocadas en invariantes y estados.
- Integración: `@SpringBootTest` + `@AutoConfigureMockMvc` + perfil `test`
  (H2, Flyway desactivado, `ddl-auto: create-drop`).
- E2E: `scripts/e2e.ps1` cubre RF-01→RF-06 y módulos adicionales con
  aserciones.
- Cualquier cambio de comportamiento debe agregar o ajustar una prueba.
- Estado actual: **20 tests, 0 fallos**; build `mvn -q -DskipTests package`
  exitoso; E2E completo con contrato `COMPLETED` y progreso `100%`.

## 6. Patrones implementados

Reales: State, Strategy, Builder, Factory Method, Abstract Factory, Bridge,
Observer, Composite, Interpreter, Command, Mediator, Visitor, Adapter, Proxy,
Decorator, Chain of Responsibility, Flyweight, Prototype, Iterator, Template
Method.

**Nominal:** Singleton. No hay `getInstance()` manual; Spring garantiza una
instancia por bean. Se documenta así para no exagerar la implementación.

Detalle y ubicación exacta en [docs/PATRONES.md](docs/PATRONES.md).

La skill reutilizable para regenerar los diagramas está en
`.opencode/skills/uml-generator/` (ver `SKILL.md`); su script
`scripts/validate-uml.ps1` valida sintaxis y, si `PLANTUML_JAR` está definido,
renderiza SVG.

## 7. Estado final de la entrega

### Completado y verificado

| Área | Estado |
|------|--------|
| Build | `mvn -q -DskipTests package` → OK |
| Tests | `mvn test` → 20 tests, 0 fallos |
| Flyway | V1–V6 aplicadas en H2 y esquema portable para PostgreSQL |
| RF-01 | Registro, login, refresh, logout, `/auth/me` |
| RF-02 | Perfiles, habilidades, portafolio, adjuntos |
| RF-03 | Proyectos, estados, búsqueda, filtros DSL, plantillas, CSV |
| RF-04 | Propuestas, validación encadenada, evaluación, rechazo |
| RF-05 | Aceptación, contrato, hitos, comisión |
| RF-06 | Entregables, tareas, comentarios, aprobación, completado, mensajería, reseñas, pagos |
| Notificaciones | Outbox con Observer, canales, reintentos |
| Admin | Reportes Visitor, moderación, suspensión |
| Auditoría | Registro de eventos y consulta ADMIN |
| Docker | `Dockerfile` multi-stage + `docker-compose.yml` con Postgres/Redis |
| Docs | README, arquitectura, patrones, UML y esta guía |

### Verificación E2E (perfil dev/H2)

`scripts/e2e.ps1` valida, en orden:

1. Registro y login de cliente/freelancer.
2. Perfil y habilidades.
3. Proyecto publicado y búsqueda.
4. Propuesta.
5. Contrato activo con 2 hitos.
6. Entregable, tarea, comentario, envío y aprobación hasta `COMPLETED` (100%).
7. Conversación, mensajes, lectura y archivo.
8. Reseña y actualización de reputación.
9. Pago con comisión PREMIUM (5%) y neto.
10. Notificaciones generadas por eventos.
11. Login admin, reportes, auditoría, moderación y suspensión.

Resultado: **OK**.

## 8. Límites conocidos (honestos)

- **Docker no se ejecutó en el entorno de validación** (no había Docker
  disponible). El perfil `docker` y el compose están preparados, pero la
  validación runtime se hizo con H2 en modo PostgreSQL.
- **Email/push simulados** (log). Los patrones Bridge/Abstract Factory y el
  outbox son reales; falta integrar SES/SMTP/FCM.
- **Pagos MOCK/LEGACY**. No se realizan cobros reales.
- **Almacenamiento local**. `ObjectStore` permite enchufar S3/MinIO; el backend
  incluido usa el sistema de archivos.
- **Redis no es crítico**. Está en Compose y en dependencias, pero el rate limit
  y los refresh tokens del MVP no lo requieren.
- **Sin frontend**.
- **Sin repositorio git inicializado** en el entorno de trabajo.

## 9. Próximos pasos recomendados

1. Ejecutar `docker compose up --build` en una máquina con Docker y validar
   Flyway sobre PostgreSQL real.
2. Implementar `S3StorageBackend` detrás de `StorageBackend` y probar con MinIO.
3. Integrar un proveedor real de email y una pasarela real (Stripe/Adyen)
   implementando `NotificationChannel`/`PaymentGateway`.
4. Mover rate limiting y refresh tokens a Redis para despliegue multi-instancia.
5. Añadir tests de contrato OpenAPI y pruebas de carga.
6. Configurar CI (build + tests + análisis estático) y firmar imágenes.

## 10. Convenciones de estilo

- Código y Javadoc en **español**; nombres de clases/paquetes en inglés.
- DTOs y vistas como `record`.
- Métodos de agregado con nombres de negocio (`approveMilestone`,
  `postMessage`), no `setStatus`.
- Fechas `Instant`/`LocalDate`; montos `Money` (BigDecimal + ISO 4217).
- IDs `UUID`.
- No registrar datos sensibles.
- Mantener los límites de módulo: si un módulo necesita datos de otro, usar un
  puerto o un evento, no importar su infraestructura.
