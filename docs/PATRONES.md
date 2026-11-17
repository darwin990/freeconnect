# Patrones de diseño aplicados en FreeConnect

Este documento justifica cada patrón con el problema real que resuelve, dónde
está implementado y su costo. No se listan patrones "de catálogo": si una
solución es sólo una consecuencia del framework, se marca como **nominal**.

## Resumen

| # | Patrón | Dónde | Estado |
|---|--------|-------|--------|
| 1 | State | Proyecto, contrato, hito, conversación | Real |
| 2 | Strategy | Comisiones, hashing, rate limiting | Real |
| 3 | Builder | Agregados con invariantes | Real |
| 4 | Factory Method | Creación de hitos y contratos | Real |
| 5 | Abstract Factory | Familias de notificaciones | Real |
| 6 | Bridge | Notificación ↔ canal de entrega | Real |
| 7 | Observer | Eventos de dominio → notificaciones/auditoría | Real |
| 8 | Composite | Hito → entregable → tarea | Real |
| 9 | Interpreter | DSL de filtros de proyectos | Real |
| 10 | Command | Aprobar/cancelar contrato con undo | Real |
| 11 | Mediator | Conversaciones por contrato | Real |
| 12 | Visitor | Reportes administrativos | Real |
| 13 | Adapter | Pasarela legada, persistencia JPA | Real |
| 14 | Proxy | Almacenamiento, contexto de seguridad | Real |
| 15 | Decorator | Pipeline de casos de uso | Real |
| 16 | Chain of Responsibility | Validación de propuestas | Real |
| 17 | Flyweight | Catálogo de habilidades | Real |
| 18 | Prototype | Plantillas de proyecto | Real |
| 19 | Iterator | Paginación y exportación CSV | Real |
| 20 | Template Method | `AbstractUseCase` | Real |
| 21 | Singleton | Beans de Spring | Nominal |

---

## 1. State

**Problema.** Los estados de proyecto, contrato, hito y conversación tienen
transiciones distintas y no deben poder saltarse. Un `if/switch` disperso en
servicios termina duplicando reglas y permitiendo transiciones inválidas.

**Solución.** Cada estado es un objeto que conoce sus transiciones válidas; el
agregado delega la validación antes de cambiar de estado.

- Proyecto: `projects/domain/state/ProjectState.java`, `ProjectStates.java`.
- Contrato: `contracts/domain/state/ContractState.java`, `ContractStates.java`.
- Hito: `contracts/domain/state/MilestoneStates.java`.
- Conversación: `messaging/domain/state/ConversationStates.java`.

**Costo.** Un objeto y un registro por familia de estados. A cambio, las reglas
quedan en un único lugar y los tests de transición son triviales.

## 2. Strategy

**Problema.** El porcentaje de comisión, el algoritmo de hash y la política de
rate limit deben poder variar sin tocar el agregado ni los controladores.

**Solución.** Una interfaz por familia y una implementación por variante.

- `StandardCommissionStrategy` (10 %) y `PremiumCommissionStrategy` (5 %).
- `PasswordHasherStrategy` con `BcryptPasswordHasher` y `Argon2PasswordHasher`,
  seleccionados por `DelegatingPasswordHasher`.
- `RateLimiter` con `InMemoryRateLimiter`.

**Costo.** Más clases pequeñas; el beneficio es que agregar un plan de comisión
no requiere modificar `Contract`.

## 3. Builder

**Problema.** `Project`, `Contract` y `FreelancerProfile` tienen muchos campos y
reglas cruzadas (suma de hitos ≤ monto, moneda ISO, fechas). Un constructor
telescópico vuelve fácil crear objetos inválidos.

**Solución.** Builders con métodos fluidos y validación en `build()`.

- `projects/domain/Project.java`
- `contracts/domain/Contract.java`
- `profiles/domain/FreelancerProfile.java`

`Proposal` y `ClientProfile` usan fábricas estáticas (`Proposal.submit(...)`,
`ClientProfile.create(...)`) porque su conjunto de datos es más pequeño; no se
fuerza Builder donde no aporta.

**Costo.** Más código que un constructor; se compensa con invariantes
garantizadas y mensajes de error claros.

## 4. Factory Method

**Problema.** Al aceptar una propuesta hay que decidir cuántos hitos crear y con
qué montos (`SINGLE`, `EQUAL_SPLIT`, `CUSTOM`) sin que el caso de uso conozca
cada variante.

**Solución.** `MilestoneFactory` encapsula la creación de la lista de hitos y
`ContractFactory` ensambla el agregado completo.

- `contracts/domain/milestone/MilestoneFactory.java`
- `contracts/application/ContractFactory.java`

## 5. Abstract Factory y 6. Bridge (notificaciones)

**Problema.** Una notificación tiene dos dimensiones independientes: la familia
(funcional: propuesta, contrato, pago) y el canal (in-app, email, push). Mezclar
ambas produce una explosión de clases `EmailProposalNotification`,
`PushPaymentNotification`, etc.

**Solución.**

- **Abstract Factory**: `NotificationFactory` y sus familias crean el título,
  el cuerpo y el tipo de notificación (`notifications/domain/factory`).
- **Bridge**: `NotificationChannel` separa la abstracción de la entrega
  (`InAppNotificationChannel`, `EmailNotificationChannel`,
  `PushNotificationChannel`).

**Nota honesta.** Los canales de email y push son **simulados** (registran en
log). El patrón y el punto de extensión son reales; la integración con SES/SMTP
o FCM queda como trabajo de producción.

## 7. Observer (eventos de dominio)

**Problema.** Aprobar un hito debe notificar al freelancer, registrar auditoría y
actualizar el outbox sin acoplar el agregado a esos módulos.

**Solución.** Los agregados registran eventos (`DomainEvent`); un publicador los
emite y varios listeners reaccionan.

- Emisión: `shared/event/DomainEventPublisher.java`.
- Consumo: `notifications/application/NotificationEventListener.java`,
  `audit/application/AuditEventListener.java`.
- Reintentos: `NotificationOutboxProcessor` (outbox con estado y `attempts`).

**Costo.** El flujo deja de ser explícito; se mitiga con eventos tipados y
auditoría persistida. Los listeners corren en la misma transacción, por lo que
un fallo de notificación no debe romper la operación de negocio (se captura y se
registra).

## 8. Composite

**Problema.** El progreso de un contrato debe calcularse igual para hitos,
entregables y tareas, sin `instanceof` ni recorridos ad hoc.

**Solución.** `WorkComponent` define `children()`, `ownProgress()`, `progress()`
y `leafCount()`. `Milestone` y `Deliverable` son compuestos; `TaskItem` es hoja.

- `contracts/domain/composite/WorkComponent.java`
- `contracts/domain/Milestone.java`, `Deliverable.java`,
  `contracts/domain/composite/TaskItem.java`

## 9. Interpreter

**Problema.** RF-03 pide filtros combinables por categoría, modalidad,
presupuesto, habilidad y texto. Agregar un parámetro por combinación no escala.

**Solución.** Un DSL con gramática propia (`budget >= 1000 AND modality =
REMOTE`) se convierte en un AST; cada nodo interpreta el proyecto. La misma
expresión se traduce a `Specification` de JPA para filtrar en SQL.

- `projects/domain/filter/ProjectFilterParser.java`
- `projects/domain/filter/ProjectFilterExpression.java`
- `projects/infrastructure/persistence/ProjectSpecifications.java`

**Costo.** Hay que mantener parser, AST y traducción SQL en sincronía; los tests
del intérprete cubren la semántica.

## 10. Command

**Problema.** Aprobar un hito puede completar el contrato; si una operación
posterior falla, hace falta revertir el cambio de estado.

**Solución.** `ContractCommand` encapsula ejecución y deshacer;
`ContractCommandInvoker` mantiene el historial por unidad de trabajo.

- `contracts/domain/command/ContractCommand.java`
- `CompleteMilestoneCommand`, `CancelContractCommand`
- `ContractCommandInvoker`

**Nota honesta.** El undo se usa como compensación de dominio y está cubierto por
tests; no sustituye una transacción ni un patrón Saga entre servicios.

## 11. Mediator

**Problema.** Abrir una conversación requiere verificar que el contrato exista,
que el actor sea participante y reutilizar la conversación si ya existe. Los
casos de uso no deberían conocer esos detalles.

**Solución.** `ConversationMediator` centraliza la resolución/creación y los
casos de uso sólo piden `resolveOrOpen(contractId, actorId)`.

- `messaging/application/ConversationMediator.java`

## 12. Visitor

**Problema.** Los reportes administrativos tienen formas distintas (proyectos,
contratos, usuarios) y se quieren varias salidas (métricas, CSV, PDF) sin tocar
los elementos cada vez.

**Solución.** `ReportElement.accept(ReportVisitor)` hace doble despacho;
`StatisticsReportVisitor` produce el `AdminReport` con métricas.

- `admin/domain/report/ReportElement.java`, `ReportVisitor.java`
- `admin/application/StatisticsReportVisitor.java`

## 13. Adapter

**Problema.** El SDK legado de pagos trabaja en centavos y devuelve un código de
autorización; el dominio trabaja con `Money` y `PaymentResult`.

**Solución.** `LegacyPaymentGatewayAdapter` implementa el puerto
`PaymentGateway` y traduce en ambos sentidos. Los adaptadores JPA cumplen el
mismo rol entre el dominio y la base de datos.

- `payments/domain/PaymentGateway.java`
- `payments/infrastructure/gateway/LegacyPaymentGatewayAdapter.java`
- `*_/infrastructure/persistence/*RepositoryAdapter.java`

## 14. Proxy

**Problema.** El acceso a archivos de portafolio debe validar propiedad y evitar
lecturas repetidas; el contexto de seguridad debe exponer al usuario actual sin
filtrar detalles de Spring Security.

**Solución.** `StorageProxy` implementa `ObjectStore`, valida la clave
`portfolio/{userId}/...`, aplica caché Caffeine y delega en el backend.
`CurrentUserProvider` es un proxy de seguridad para los casos de uso.

**Nota honesta.** El backend incluido es de sistema de archivos; el puerto
`ObjectStore` está preparado para un backend S3-compatible sin cambiar los casos
de uso.

## 15. Decorator

**Problema.** Logging, autorización y auditoría son preocupaciones transversales
de los casos de uso; heredarlas o repetirlas contamina la lógica.

**Solución.** `UseCasePipeline` envuelve un `UseCase` con
`LoggingUseCaseDecorator`, `AuthorizationDecorator` y `AuditUseCaseDecorator`.

- `shared/decorator/UseCasePipeline.java`

## 16. Chain of Responsibility

**Problema.** Una propuesta debe cumplir varias reglas (proyecto publicado,
presupuesto, plazo, propuesta duplicada). Un método con muchos `if` es difícil
de probar y extender.

**Solución.** `ProposalValidator` encadena reglas; cada eslabón decide pasar o
cortar con una excepción de dominio.

- `proposals/domain/validation/ProposalValidator.java`
- `proposals/application/validation/*`

## 17. Flyweight

**Problema.** Las habilidades se repiten en perfiles y proyectos; duplicar
instancias mutables invita a inconsistencias.

**Solución.** `Skill` es inmutable y `SkillCatalog` comparte instancias; el
estado extrínseco vive en las relaciones perfil/proyecto.

- `profiles/domain/Skill.java`
- `profiles/application/SkillCatalog.java`

## 18. Prototype

**Problema.** Crear un proyecto desde una plantilla debe producir una copia
independiente; modificar el proyecto no puede alterar la plantilla.

**Solución.** `ProjectTemplate.copy()` hace copia profunda y
`ProjectTemplateRegistry` entrega clones.

- `projects/domain/ProjectTemplate.java`
- `projects/application/ProjectTemplateRegistry.java`

## 19. Iterator

**Problema.** Exportar todos los proyectos de un cliente no debe cargar la tabla
completa en memoria.

**Solución.** `PagedIterator` recorre páginas de a una y `ExportMyProjectsUseCase`
las convierte a CSV.

- `shared/pagination/PagedIterator.java`
- `projects/application/ExportMyProjectsUseCase.java`

## 20. Template Method

**Problema.** Todos los casos de uso comparten el contrato `execute(input)` y
validaciones comunes.

**Solución.** `AbstractUseCase` fija el esqueleto y delega el paso variable.

- `shared/application/AbstractUseCase.java`

**Nota honesta.** En la mayoría de los casos se implementa directamente la
interfaz `UseCase`; `AbstractUseCase` se usa donde hay comportamiento común.

## 21. Singleton (nominal)

Spring registra una instancia por bean (`@Service`, `@Component`, `@Bean`) y
gestiona su ciclo de vida. No se implementó un Singleton manual con
`getInstance()` porque complicaría los tests y el wiring. Se documenta como
**nominal**: el contenedor es quien garantiza la unicidad.
