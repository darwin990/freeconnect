# Inventario de patrones detectados

> Documento de **trazabilidad**. Cada fila se puede verificar en el código real.
> No se listan patrones "de catálogo": si una solución sólo existe porque la
> aporta el framework, se marca como **framework** y se separa de los patrones
> implementados a mano.
>
> Rutas relativas a la raíz del repositorio `freeconnect/`.
> Diagramas por módulo: [`docs/uml/clases/`](./clases/README.md).

## Leyenda de estado

| Estado | Significado |
|--------|-------------|
| `confirmado` | Hay una clase y un uso real en el flujo de la aplicación. |
| `confirmado (no cableado)` | La implementación existe, pero no se invoca desde el código de producción. |
| `por confirmar` | La estructura sugiere el patrón, pero no hay evidencia suficiente para afirmarlo. |
| `framework` | Lo aporta Spring, React o Playwright; se documenta por trazabilidad, no como diseño manual. |
| `ausente` | Se buscó explícitamente y no existe. |

## 1. Patrones de arquitectura (no GoF)

| Patrón | Módulo | Implementación (archivo:clase) | Evidencia (fragmento o nota) | ¿Se usa en otros módulos? |
|--------|--------|-------------------------------|------------------------------|---------------------------|
| Monolito modular por capas | Backend completo | `src/main/java/com/freeconnect/*/api`, `application`, `domain`, `infrastructure` | Cada módulo repite `api → application → domain ← infrastructure`. `ContractController` sólo depende de casos de uso. | Todos los módulos de negocio. |
| Puertos y adaptadores (Hexagonal) | Persistencia, pagos, storage, auditoría | `src/main/java/com/freeconnect/payments/domain/PaymentGateway.java:9`, `.../payments/infrastructure/gateway/LegacyPaymentGatewayAdapter.java:19` | El dominio declara el puerto; infraestructura implementa el adaptador. | `projects`, `contracts`, `profiles`, `identity`, `notifications`, `reviews`, `payments`. |
| MVC + Front Controller | API REST | `src/main/java/com/freeconnect/identity/infrastructure/security/SecurityConfig.java:26` (config), controladores `@RestController` | Spring MVC enruta por `DispatcherServlet`; los controladores no manejan el ciclo HTTP completo. | Todos los `*Controller`. |
| DTO + Mapper | API y aplicación | `src/main/java/com/freeconnect/profiles/application/ProfileViewMapper.java:17`; `src/main/java/com/freeconnect/contracts/application/ContractView.java` | `ProfileViewMapper.toView(...)` traduce agregados a vistas; `ContractView.from(...)` hace lo propio. | `ContractView`, `ProjectView`, `ProposalView`, `PaymentView`, `NotificationView`, `ReviewView`, `UserView`, `ConversationView`. |
| Facade de aplicación | Servicios de aplicación | `src/main/java/com/freeconnect/payments/application/PaymentUseCases.java:28`; `.../messaging/application/MessagingUseCases.java:17`; `.../notifications/application/NotificationService.java:20` | Una clase agrupa varios casos de uso y oculta repositorios, puertos y eventos al controlador. | `AdminReportsUseCase`, `NotificationService`, `PaymentUseCases`, `MessagingUseCases`, `ReviewUseCases`. |
| Repository | Persistencia | `src/main/java/com/freeconnect/projects/domain/ProjectRepository.java:11`; `.../projects/infrastructure/persistence/ProjectJpaRepository.java:9` | Puerto de dominio + `JpaRepository<ProjectJpaEntity, UUID>` de Spring Data. | Todos los agregados. |
| Outbox | Notificaciones | `src/main/java/com/freeconnect/notifications/application/NotificationOutboxProcessor.java:17` | `@Scheduled` reintenta notificaciones con `attempts < maxAttempts`. | No se usa en pagos ni auditoría (auditoría es síncrona). |
| Specification | Proyectos | `src/main/java/com/freeconnect/projects/infrastructure/persistence/ProjectSpecifications.java:27` | Traduce criterios y el AST del filtro a `Specification<ProjectJpaEntity>`. | Sólo `projects`. |
| Inyección de dependencias / IoC | Backend completo | `src/main/java/com/freeconnect/shared/config/InfrastructureConfig.java:26`; constructores de todos los `@Service`/`@Component` | Spring construye el grafo; no hay `new` de colaboradores en producción salvo value objects y comandos. | Todos los módulos. |

## 2. Patrones GoF y enterprise implementados a mano

| Patrón | Módulo | Implementación (archivo:clase) | Evidencia (fragmento o nota) | ¿Se usa en otros módulos? |
|--------|--------|-------------------------------|------------------------------|---------------------------|
| **State** | Proyectos, contratos, mensajería | `src/main/java/com/freeconnect/projects/domain/state/ProjectStates.java:13`; `.../contracts/domain/state/ContractStates.java:10`; `.../contracts/domain/state/MilestoneStates.java`; `.../messaging/domain/state/ConversationStates.java:10` | Registro inmutable `Map<Status, State>`; el agregado llama `canTransitionTo(target)` antes de cambiar de estado. | `Contract.transitionTo` (`contracts/domain/Contract.java:142`), `Conversation.archive` (`messaging/domain/Conversation.java:104`). |
| **Strategy** | Contratos, identidad, rate limit | `src/main/java/com/freeconnect/contracts/domain/commission/CommissionStrategy.java:12`; `.../contracts/application/CommissionStrategyRegistry.java:17`; `.../identity/domain/PasswordHasherStrategy.java:9`; `.../shared/ratelimit/RateLimiter.java` | `CommissionStrategyRegistry` selecciona por `CommissionPlan`; `DelegatingPasswordHasher` selecciona BCrypt/Argon2; `RateLimitFilter` depende de `RateLimiter`. | `payments` reutiliza la comisión calculada del contrato; no vuelve a elegir estrategia. |
| **Template Method** | Servicios | `src/main/java/com/freeconnect/shared/application/AbstractUseCase.java:10` | `execute()` fija `validate → handle → afterSuccess`; `handle` es abstracto. | `SubmitProposalUseCase` y otros casos que extienden `AbstractUseCase`. |
| **Decorator** | Servicios transversales | `src/main/java/com/freeconnect/shared/decorator/UseCasePipeline.java:13`; `LoggingUseCaseDecorator.java:14`; `AuthorizationDecorator.java:14`; `AuditUseCaseDecorator.java:16` | Cada decorador implementa `UseCase<I,O>` y envuelve a otro. | **No cableado**: no hay llamadas a `UseCasePipeline` en `src/main` ni en tests. Ver "Pendientes". |
| **Observer** | Eventos de dominio | `src/main/java/com/freeconnect/shared/event/DomainEventPublisher.java:18`; `.../audit/application/AuditEventListener.java:18`; `.../notifications/application/NotificationEventListener.java:27` | `DomainEventPublisher.publish(...)` delega en `ApplicationEventPublisher`; los listeners usan `@EventListener`. | `contracts`, `proposals`, `payments`, `messaging`, `reviews`, `identity`. |
| **Factory Method** | Contratos | `src/main/java/com/freeconnect/contracts/application/ContractFactory.java:23`; `.../contracts/domain/milestone/MilestoneFactory.java:21` | `ContractFactory` crea el agregado completo; `MilestoneFactory.create(type, ...)` decide `SINGLE/EQUAL_SPLIT/CUSTOM`. | `TaskItem.create` (`contracts/domain/composite/TaskItem.java:21`) es fábrica estática simple. |
| **Abstract Factory** | Notificaciones | `src/main/java/com/freeconnect/notifications/domain/factory/NotificationFactory.java:15`; `NotificationFactoryRegistry.java:14` | Cada familia (`Email`, `InApp`, `Push`) crea `Notification` + `NotificationFormatter`. | Sólo notificaciones. |
| **Bridge** | Notificaciones | `src/main/java/com/freeconnect/notifications/domain/channel/NotificationChannel.java:12`; `.../notifications/application/NotificationDispatcher.java:15` | La abstracción de notificación se enruta a un implementador de canal por `NotificationChannelType`. | Canales `Email`, `InApp`, `Push`. |
| **Composite** | Contratos | `src/main/java/com/freeconnect/contracts/domain/composite/WorkComponent.java:12`; `.../contracts/domain/Milestone.java:23`; `.../contracts/domain/Deliverable.java:18`; `.../contracts/domain/composite/TaskItem.java:9` | `WorkComponent.progress()` recorre hijos; `Milestone`/`Deliverable` son compuestos y `TaskItem` hoja. | `Contract.progress()` agrega hitos (`contracts/domain/Contract.java:178`). |
| **Interpreter** | Proyectos | `src/main/java/com/freeconnect/projects/domain/filter/ProjectFilterExpression.java:15`; `.../projects/domain/filter/ProjectFilterParser.java:28`; `.../projects/infrastructure/persistence/ProjectSpecifications.java:64` | AST `sealed interface` con `And`, `Or`, `Comparison`, `Always`; el parser construye el árbol y JPA lo traduce a predicados. | Sólo filtros de proyectos. |
| **Command** | Contratos | `src/main/java/com/freeconnect/contracts/domain/command/ContractCommand.java:10`; `.../ContractCommandInvoker.java:13`; `.../CancelContractCommand.java:10`; `.../CompleteMilestoneCommand.java` | `execute()`/`undo()`; el invocador guarda historial en `ArrayDeque`. | `CancelContractUseCase` (`:39`) crea el invocador por unidad de trabajo. |
| **Mediator** | Mensajería | `src/main/java/com/freeconnect/messaging/application/ConversationMediator.java:21` | `resolveOrOpen(contractId, actorId)` centraliza contrato, participantes y reutilización de conversación. | Sólo mensajería. |
| **Visitor** | Administración | `src/main/java/com/freeconnect/admin/domain/report/ReportElement.java:10`; `.../ReportVisitor.java:6`; `.../admin/application/StatisticsReportVisitor.java:16` | Doble despacho `accept(visitor)`; el visitante produce `AdminReport`. | `AdminReportsUseCase` arma la lista de elementos visitables (`:78-80`). |
| **Adapter** | Pagos, persistencia, auditoría | `src/main/java/com/freeconnect/payments/domain/PaymentGateway.java:9`; `.../payments/infrastructure/gateway/LegacyPaymentGatewayAdapter.java:19`; `.../projects/infrastructure/persistence/ProjectRepositoryAdapter.java:28`; `.../audit/application/JpaAuditRecorder.java:19` | Traduce entre el modelo de dominio y APIs externas/JPA. | Todos los `*RepositoryAdapter`. |
| **Proxy** | Perfiles y seguridad | `src/main/java/com/freeconnect/profiles/infrastructure/storage/StorageProxy.java:26`; `.../shared/security/CurrentUserProvider.java:17` | `StorageProxy` valida propiedad de la clave y cachea con Caffeine; `CurrentUserProvider` encapsula `SecurityContextHolder`. | `StorageProxy` es `@Primary` sobre `ObjectStore`; `CurrentUserProvider` lo usan casi todos los casos de uso. |
| **Chain of Responsibility** | Propuestas | `src/main/java/com/freeconnect/proposals/domain/validation/ProposalValidator.java:9`; `.../AvailabilityValidator.java:9`; `.../BudgetValidator.java:8`; `.../proposals/application/validation/DuplicateProposalValidator.java:11` | `setNext(...)` + `validate()`; la cadena se arma en `SubmitProposalUseCase:65-67`. | Sólo propuestas. |
| **Prototype** | Proyectos | `src/main/java/com/freeconnect/projects/domain/Prototype.java:9`; `.../projects/domain/ProjectTemplate.java:18`; `.../projects/application/ProjectTemplateRegistry.java:25` | `copy()` crea un id nuevo y copia profunda de habilidades; el registro entrega clones (`:65`). | `ProjectTemplate.instantiate(...)` materializa un `Project` en `DRAFT`. |
| **Iterator** | Transversal | `src/main/java/com/freeconnect/shared/pagination/PagedIterator.java:17`; `.../shared/pagination/PageLoader.java:7` | `hasNext()` carga páginas bajo demanda; `ExportMyProjectsUseCase` lo usa para CSV (`:41`). | Exportación de proyectos; no se usa en listados REST (esos devuelven `PagedResult`). |
| **Flyweight** | Perfiles | `src/main/java/com/freeconnect/profiles/application/SkillCatalog.java:23` | Caché `ConcurrentHashMap<String, Skill>` comparte instancias canónicas; el estado extrínseco vive en tablas puente. | `ProjectTemplateRegistry` y `ProjectRepositoryAdapter` resuelven habilidades por el catálogo. |
| **Builder** | Proyectos, contratos | `src/main/java/com/freeconnect/projects/domain/Project.java:193`; `.../contracts/domain/Contract.java:260` | `Contract.Builder.build()` valida título, alcance, monto, comisión, hitos y suma ≤ total. | `ProjectTemplate.instantiate` usa `Project.builder(...)`. |
| **Singleton** | Backend | `src/main/java/com/freeconnect/shared/config/InfrastructureConfig.java:26`; cualquier `@Service`/`@Component`/`@Bean` | No hay `getInstance()` manual: la unicidad la garantiza el contenedor Spring. | Todos los beans. Estado: **framework / nominal**. |

## 3. Patrones del frontend

| Patrón | Módulo | Implementación (archivo:clase) | Evidencia (fragmento o nota) | ¿Se usa en otros módulos? |
|--------|--------|-------------------------------|------------------------------|---------------------------|
| Provider + Context | Estado global | `frontend/src/app/ThemeProvider.tsx:14`; `frontend/src/features/auth/AuthProvider.tsx:19`; `frontend/src/components/ui/Toast.tsx:25` | `createContext<T \| null>(null)` + `<XProvider>` + hook que falla fuera del provider. | `frontend/src/app/providers.tsx:20` compone `Theme`, `QueryClient`, `Toast` y `Auth`. |
| Custom Hook | Estado y datos | `frontend/src/shared/hooks/useDebouncedValue.ts:3`; `frontend/src/features/contracts/hooks.ts:14`; `frontend/src/features/admin/hooks.ts:6` | Hooks por dominio que envuelven React Query y comparten invalidación (`useContractMutation`, `:30`). | `auth`, `projects`, `proposals`, `contracts`, `payments`, `reviews`, `messaging`, `notifications`, `profiles`, `admin`. |
| Route Guard | Rutas | `frontend/src/features/auth/guards.tsx:7` (`RequireAuth`), `:18` (`RequireRole`), `:35` (`PublicOnlyRoute`) | Render condicional con `<Navigate>` o mensaje "No tienes permiso para ver esta sección". | `frontend/src/app/AppRoutes.tsx:12` importa las tres guardas. |
| HTTP Adapter + Facade | Servicios API | `frontend/src/shared/api/client.ts:152` (`api`), `:48` (`perform`), `:96` (`refreshAccessToken`) | Envuelve `fetch`, añade bearer, timeout, single-flight de refresh, parseo de errores y reintento único ante 401. | Todos los `features/*/api.ts`, p. ej. `features/contracts/api.ts:4`. |
| Observer (store + eventos) | Sesión y caché | `frontend/src/shared/api/session.ts:7` (`listeners`), `:50` (`subscribe`); `:57` (`notifySessionExpired`) | Set de listeners + `CustomEvent`; React Query invalida/refresca por claves. | `AuthProvider` escucha `freeconnect:session-expired` (`:61`). |
| Factory de claves de consulta | Servicios API | `frontend/src/shared/api/queryKeys.ts:1` | Objeto con claves estáticas y funciones `project(id)`, `contract(id)`, `contractPayments(id)`. | Todos los hooks de React Query. |
| Error Boundary | Transversal UI | `frontend/src/app/ErrorBoundary.tsx:14` | `componentDidCatch` + `getDerivedStateFromError`; pantalla de recarga. | `frontend/src/main.tsx` (composición raíz). Patrón **framework**. |
| Composición de componentes | Design system | `frontend/src/shared/components/Card.tsx:13` (`Card`) y `:21` (`CardHeader`) | Se componen por children/props; no hay Compound Component con contexto propio. | `ContractDetailPage`, `PaymentsPage`, etc. |
| `forwardRef` | Componentes de formulario | `frontend/src/shared/components/FormControls.tsx:51`, `:73`, `:97` | `Input`, `Textarea` y `Select` reenvían la ref al elemento nativo. Es un mecanismo de React, no un patrón GoF. | Formularios de autenticación, proyectos, propuestas y contrato. |
| Estrategia por tabla de datos | Rutas de proyecto | `frontend/src/features/projects/transitions.ts:3` | `Record<ProjectStatus, ProjectStatus[]>`; el componente consulta la tabla. **No es GoF Strategy estricto**, es configuración de datos. | `ProjectStatusActions`. Estado: `por confirmar` como patrón. |
| Test Data Factory | E2E | `frontend/e2e/helpers.ts:11` (`uniqueEmail`), `:16` (`register`), `:38` (`login`) | Genera correos únicos y encapsula el alta/login; los datos del flujo se construyen inline en el spec. | `full-flow.spec.ts`, `admin-smoke.spec.ts`. |
| Page Object Model | E2E | — | **Ausente.** No hay clases `*Page` ni `pages/`; se usan funciones helper y locators inline. | — |
| Fixture | E2E | — | **Ausente.** No hay `test.extend(...)`, carpeta `fixtures/` ni datos precargados; cada test crea sus cuentas. | — |
| HOC / Render Props / `forwardRef` / Compound Component | Componentes | — | **Ausentes.** Búsqueda sin resultados en `frontend/src`; los componentes reciben props y children. | — |
| Store tipo Redux/Zustand/Jotai/`useReducer` | Estado | — | **Ausente.** El estado servidor lo gestiona React Query y el estado local `useState`. | — |

## 4. Patrones por framework (no implementados a mano)

| Patrón | Dónde lo aporta el framework | Por qué no cuenta como diseño manual |
|--------|------------------------------|--------------------------------------|
| Singleton de beans | Spring `@Service`, `@Component`, `@Bean` | El contenedor garantiza una instancia; no hay `getInstance()`. |
| Front Controller | Spring MVC `DispatcherServlet` | El enrutamiento y el ciclo HTTP los aporta el framework. |
| Filter Chain | Spring Security `SecurityFilterChain` | `SecurityConfig` declara la cadena, pero el patrón lo implementa Spring Security. |
| Repository de Spring Data | `JpaRepository<T, ID>` | La interfaz base y la implementación dinámica son del framework; el puerto de dominio sí es manual. |
| Observer de eventos | `ApplicationEventPublisher` + `@EventListener` | La infraestructura de publicación/suscripción es de Spring; el patrón de dominio (eventos) es manual. |
| Provider / Context | React `createContext` + `<Context.Provider>` | React implementa la propagación; el diseño de qué se comparte es manual. |
| Observer de estado | React Query (`useQuery`, invalidate) y `useState` | La suscripción y el cache los aporta la librería. |
| Error Boundary | React `componentDidCatch` | El ciclo de captura lo aporta React. |
| Fixtures de Playwright | `test.extend` (no usado) | No se usa; se documenta la ausencia. |
| Flag de build | Vite `import.meta.env` | `demoFlag.ts` usa el mecanismo del bundler. |

## 5. Convenciones de patrones del proyecto

FreeConnect es un **monolito modular** con capas `Controller → UseCase → Domain/Repository` y puertos/adaptadores en infraestructura: los controladores sólo conocen casos de uso, los casos de uso orquestan agregados y puertos, y la persistencia se adapta con `*RepositoryAdapter` + `JpaRepository`. Los DTO se construyen con mappers (`ContractView.from`, `ProfileViewMapper`). El cálculo de comisiones es **Strategy** (`CommissionStrategy` + `CommissionStrategyRegistry`), el ciclo de vida se protege con **State** (`ProjectStates`, `ContractStates`, `MilestoneStates`, `ConversationStates`), la creación compleja con **Builder/Factory** (`Contract.Builder`, `ContractFactory`, `MilestoneFactory`) y la comunicación entre módulos con **Observer** vía eventos de dominio (`DomainEventPublisher` + `@EventListener`). En el frontend se usa **Provider + Context + hooks** para sesión y tema, **React Query** para estado servidor, **guardas de ruta** por rol (`RequireAuth`, `RequireRole`) y un **adaptador HTTP** único (`shared/api/client.ts`).

Archivos canónicos: [`AbstractUseCase`](../../src/main/java/com/freeconnect/shared/application/AbstractUseCase.java), [`CommissionStrategy`](../../src/main/java/com/freeconnect/contracts/domain/commission/CommissionStrategy.java), [`DomainEventPublisher`](../../src/main/java/com/freeconnect/shared/event/DomainEventPublisher.java), [`SecurityConfig`](../../src/main/java/com/freeconnect/identity/infrastructure/security/SecurityConfig.java), [`client.ts`](../../frontend/src/shared/api/client.ts), [`AuthProvider`](../../frontend/src/features/auth/AuthProvider.tsx), [`guards.tsx`](../../frontend/src/features/auth/guards.tsx).

## 6. Patrones buscados y no encontrados

| Patrón | Resultado | Nota |
|--------|-----------|------|
| Page Object Model | Ausente | `frontend/e2e/` sólo tiene `helpers.ts` y dos specs. |
| Fixture de Playwright | Ausente | No hay `test.extend` ni carpeta `fixtures/`. |
| HOC / Render Props | Ausente | No hay funciones `withX` que devuelvan componentes ni props de render. |
| Compound Component con contexto | Ausente | `Card`/`CardHeader` y `Tabs` se componen por props, sin contexto interno. |
| Redux / Zustand / Jotai / `useReducer` | Ausente | Estado local con `useState`; estado servidor con React Query. |
| Saga / Circuit Breaker | Ausente | No hay orquestación distribuida ni cortocircuito. |
| CQRS / Event Sourcing | Ausente | Los eventos se publican, pero el estado se persiste en tablas. |
| Flyweight en frontend | Ausente | No hay caché de instancias de UI. |

## 7. Pendientes y matices de este inventario

1. **Decorator no cableado.** `UseCasePipeline`, `LoggingUseCaseDecorator`, `AuthorizationDecorator` y `AuditUseCaseDecorator` existen, pero no se invocan en `src/main` ni en `src/test`. Se documenta como implementación disponible, no como patrón activo.
2. **Facade de aplicación vs GoF.** `PaymentUseCases`, `MessagingUseCases` y `NotificationService` actúan como fachada de casos de uso; no hay una clase `Facade` con subsistemas clásicos.
3. **`NotificationOutboxProcessor`** se etiqueta como *outbox* (patrón enterprise). El `Javadoc` dice "Observer + reintentos"; el Observer real es el listener de eventos.
4. **`ProjectFilterExpression` + `ProjectSpecifications`** duplican la semántica en memoria y en SQL. Es una decisión consciente, pero exige mantener ambas en sincronía.
5. **`transitions.ts`** es una tabla de transiciones, no una Strategy GoF.
6. **Redis ausente en runtime.** `/actuator/health` devuelve 503; el rate limit es en memoria y la caché es Caffeine. No se puede afirmar un patrón distribuido.
7. **Pasarela legada** sólo se activa con `freeconnect.payments.provider=LEGACY`; por defecto se usa `MockPaymentGateway`.
8. **Cobertura de líneas**: en clases grandes (p. ej. `ContractJpaMapper`) se cita `archivo:clase` sin línea; no se inventan números.
9. **`SkillCatalog`** declara Flyweight con instancias inmutables compartidas; es un Flyweight de objetos, no de estado intrínseco complejo.
10. **Ausencias del frontend** (POM, fixtures, HOC, render props, store externo) están verificadas por búsqueda y se listan para revisión humana. `forwardRef` **sí** se usa en `FormControls`; se documenta como mecanismo de React, no como patrón de diseño.
