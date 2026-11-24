# Backend · Dominio

| Campo | Valor |
|-------|-------|
| **Módulo** | Dominio compartido y agregados de negocio |
| **Código fuente** | `src/main/java/com/freeconnect/{shared/domain,identity/domain,projects/domain,proposals/domain,contracts/domain,messaging/domain,payments/domain,reviews/domain,notifications/domain,admin/domain/report}` |
| **Propósito** | Modelar las entidades, value objects, enums y reglas invariantes del negocio sin depender de Spring, JPA ni HTTP. |

> Vista de referencia: [`docs/uml/clases-dominio.md`](../clases-dominio.md) (Fase 1). Este archivo
> añade estereotipos, patrones y trazabilidad; no reemplaza el diagrama de Fase 1.

## 1. Agregados principales

```mermaid
classDiagram
    direction LR

    class AggregateRoot {
        <<Entity>>
        <<Aggregate>>
        +pullDomainEvents() List
        +peekDomainEvents() List
    }

    class User {
        <<Entity>>
        <<AggregateRoot>>
        +UUID id
        +Email email
        +PasswordHash passwordHash
        +Role role
        +UserStatus status
        +String displayName
        +changeRole(Role, UUID)
        +suspend()
    }

    class Email {
        <<ValueObject>>
        +String value
        +of(String) Email
    }

    class PasswordHash {
        <<ValueObject>>
        +String value
    }

    class Project {
        <<Entity>>
        <<AggregateRoot>>
        +UUID id
        +UUID clientId
        +String title
        +ProjectStatus status
        +Money budget
        +publish(UUID)
        +changeStatus(ProjectStatus, UUID)
    }

    class ProjectTemplate {
        <<Prototype>>
        +UUID id
        +String name
        +copy() ProjectTemplate
        +instantiate(UUID) Project
    }

    class Proposal {
        <<Entity>>
        <<AggregateRoot>>
        +UUID id
        +UUID projectId
        +UUID freelancerId
        +Money amount
        +ProposalStatus status
        +submit(...) Proposal
        +accept(UUID)
    }

    class Contract {
        <<Entity>>
        <<AggregateRoot>>
        +UUID id
        +UUID projectId
        +UUID proposalId
        +Money amount
        +Money commission
        +ContractStatus status
        +progress() int
        +transitionTo(ContractStatus, UUID)
    }

    class Milestone {
        <<Entity>>
        <<AggregateRoot>>
        <<Composite>>
        +UUID id
        +Money amount
        +MilestoneStatus status
        +progress() int
    }

    class Deliverable {
        <<Entity>>
        <<Composite>>
        +UUID id
        +DeliverableStatus status
        +progress() int
    }

    class TaskItem {
        <<ValueObject>>
        <<Leaf>>
        +UUID id
        +String title
        +boolean done
        +toggle(boolean)
    }

    class Conversation {
        <<Entity>>
        <<AggregateRoot>>
        +UUID id
        +UUID contractId
        +ConversationStatus status
        +postMessage(UUID, String) Message
        +archive(UUID)
    }

    class Message {
        <<Entity>>
        +UUID id
        +UUID senderId
        +String body
        +Instant sentAt
        +markRead()
    }

    class Payment {
        <<Entity>>
        <<AggregateRoot>>
        +UUID id
        +UUID contractId
        +Money amount
        +Money commission
        +Money netAmount
        +PaymentStatus status
        +markReleased(String)
        +markFailed(String)
    }

    class Review {
        <<Entity>>
        <<AggregateRoot>>
        +UUID id
        +UUID contractId
        +int rating
        +create(...) Review
    }

    class Notification {
        <<Entity>>
        <<AggregateRoot>>
        +UUID id
        +UUID recipientId
        +NotificationChannelType channel
        +NotificationStatus status
        +markSent()
        +markFailed(String)
        +canRetry(int) boolean
    }

    AggregateRoot <|-- User
    AggregateRoot <|-- Project
    AggregateRoot <|-- Proposal
    AggregateRoot <|-- Contract
    AggregateRoot <|-- Milestone
    AggregateRoot <|-- Conversation
    AggregateRoot <|-- Payment
    AggregateRoot <|-- Review
    AggregateRoot <|-- Notification

    User "1" --> "1" Email : identifica
    User "1" --> "1" PasswordHash : credencial
    User "1" --> "0..*" Project : publica como cliente
    Project "1" --> "0..*" Proposal : recibe
    User "1" --> "0..*" Proposal : envia como freelancer
    Project "1" --> "0..1" Contract : origina
    Proposal "1" --> "0..1" Contract : aceptada genera
    Contract "1" *-- "1..*" Milestone : contiene
    Milestone "1" *-- "0..*" Deliverable : agrupa
    Deliverable "1" *-- "0..*" TaskItem : desglosa
    Contract "1" --> "0..*" Conversation : habilita
    Conversation "1" *-- "0..*" Message : contiene
    Contract "1" --> "0..*" Payment : registra
    Contract "1" --> "0..*" Review : recibe
    User "1" --> "0..*" Notification : recibe
    ProjectTemplate ..> Project : materializa
```

## 2. Value objects, enums y contrato de eventos

```mermaid
classDiagram
    direction LR

    class Money {
        <<ValueObject>>
        +BigDecimal amount
        +String currency
        +of(BigDecimal, String) Money
        +plus(Money) Money
        +minus(Money) Money
        +percentage(BigDecimal) Money
        +isGreaterThan(Money) boolean
    }

    class Email {
        <<ValueObject>>
        +String value
    }

    class MilestoneSpec {
        <<ValueObject>>
        +String title
        +String description
        +BigDecimal amount
        +LocalDate dueDate
    }

    class Skill {
        <<ValueObject>>
        +UUID id
        +String name
        +String category
    }

    class DomainEvent {
        <<interface>>
        +UUID aggregateId()
        +String eventType()
        +UUID actorId()
    }

    class Role {
        <<enumeration>>
        FREELANCER
        CLIENT
        ADMIN
    }

    class ProjectStatus {
        <<enumeration>>
        DRAFT
        PUBLISHED
        PAUSED
        IN_PROGRESS
        FINISHED
        CANCELLED
    }

    class ContractStatus {
        <<enumeration>>
        DRAFT
        ACTIVE
        COMPLETED
        CANCELLED
    }

    class MilestoneStatus {
        <<enumeration>>
        PENDING
        IN_PROGRESS
        SUBMITTED
        APPROVED
        REJECTED
    }

    class PaymentStatus {
        <<enumeration>>
        PENDING
        RELEASED
        FAILED
        REFUNDED
    }

    class ConversationStatus {
        <<enumeration>>
        ACTIVE
        ARCHIVED
    }

    class NotificationStatus {
        <<enumeration>>
        PENDING
        SENT
        FAILED
    }

    Contract "1" --> "1" Money : monto
    Contract "1" --> "1" Money : comision
    Project "1" --> "1" Money : presupuesto
    Proposal "1" --> "1" Money : oferta
    Payment "1" --> "1" Money : bruto
    Payment "1" --> "1" Money : neto
    Contract "1" --> "*" MilestoneSpec : recibe al crear
    Project "*" --> "*" Skill : requiere
    Contract ..> DomainEvent : registra
    User ..> Role : tiene
```

## 3. Reportes administrativos (dominio de reporte)

```mermaid
classDiagram
    direction LR

    class ReportElement {
        <<interface>>
        <<Visitable>>
        +accept(ReportVisitor) AdminReport
    }

    class ReportVisitor {
        <<interface>>
        <<Visitor>>
        +visit(ProjectReport) AdminReport
        +visit(ContractReport) AdminReport
        +visit(UserReport) AdminReport
    }

    class ProjectReport {
        <<DTO>>
        +int total
        +int published
        +Map budgets
    }

    class ContractReport {
        <<DTO>>
        +int total
        +int active
        +Map amounts
    }

    class UserReport {
        <<DTO>>
        +long total
        +long clients
        +long freelancers
    }

    class AdminReport {
        <<DTO>>
        +String report
        +Map metrics
        +Instant generatedAt
    }

    ReportElement <|.. ProjectReport
    ReportElement <|.. ContractReport
    ReportElement <|.. UserReport
    ReportVisitor ..> AdminReport : produce
    ProjectReport ..> ReportVisitor : accept
    ContractReport ..> ReportVisitor : accept
    UserReport ..> ReportVisitor : accept
```

## Patrones en este módulo

| Patrón | Rol en el módulo | Clase/archivo donde vive | Notas |
|--------|------------------|--------------------------|-------|
| Aggregate Root | Consistencia transaccional y eventos | `src/main/java/com/freeconnect/shared/domain/AggregateRoot.java:12` | Acumula `DomainEvent`; la aplicación los drena con `pullDomainEvents()`. |
| Value Object | Inmutabilidad y validación | `src/main/java/com/freeconnect/shared/domain/Money.java:11`; `src/main/java/com/freeconnect/identity/domain/Email.java:10`; `src/main/java/com/freeconnect/contracts/domain/milestone/MilestoneSpec.java:9` | `Money` normaliza escala y moneda; `Email` normaliza a minúsculas. |
| Builder | Construcción validada | `src/main/java/com/freeconnect/contracts/domain/Contract.java:260`; `src/main/java/com/freeconnect/projects/domain/Project.java:193` | `Contract.Builder.build()` valida suma de hitos ≤ monto. |
| State | Transiciones de ciclo de vida | `src/main/java/com/freeconnect/contracts/domain/state/ContractStates.java:10`; `.../projects/domain/state/ProjectStates.java:13`; `.../messaging/domain/state/ConversationStates.java:10` | El agregado delega en el registro de estados. |
| Composite | Progreso uniforme | `src/main/java/com/freeconnect/contracts/domain/composite/WorkComponent.java:12`; `.../contracts/domain/Milestone.java:23`; `.../contracts/domain/Deliverable.java:18`; `.../contracts/domain/composite/TaskItem.java:9` | `TaskItem` es hoja; `Milestone` y `Deliverable` son compuestos. |
| Prototype | Plantillas clonables | `src/main/java/com/freeconnect/projects/domain/ProjectTemplate.java:18`; `.../projects/domain/Prototype.java:9` | `copy()` genera id nuevo y copia profunda de habilidades. |
| Observer (emisor) | Eventos de dominio | `src/main/java/com/freeconnect/shared/domain/DomainEvent.java`; eventos `*Event` en cada módulo | Los agregados sólo registran eventos; no conocen listeners. |
| Factory Method (creación de agregados) | Alta de propuestas, pagos, reseñas | `src/main/java/com/freeconnect/proposals/domain/Proposal.java` (`submit`); `.../payments/domain/Payment.java:49` (`create`); `.../reviews/domain/Review.java:32` (`create`) | Fábricas estáticas que validan invariantes antes de exponer el objeto. |
| Visitor (elementos) | Reportes administrativos | `src/main/java/com/freeconnect/admin/domain/report/ReportElement.java:10`; `.../ReportVisitor.java:6` | Doble despacho; los reportes no conocen el formato de salida. |

## Notas de diseño

- **Inmutabilidad**: `Money`, `Email`, `MilestoneSpec` y `Skill` son `record` o clases sin setters.
  `Contract`, `Milestone` y `Conversation` exponen listas como `Collections.unmodifiableList(...)`.
- **Igualdad**: `Conversation` implementa `equals/hashCode` por `id` (`messaging/domain/Conversation.java:172-180`).
  Los `record` obtienen igualdad estructural por componentes.
- **Eventos**: los cambios de estado registran eventos (`ContractStatusChangedEvent`,
  `MilestoneStatusChangedEvent`, `MessageSentEvent`, `PaymentReleasedEvent`, `ReviewCreatedEvent`).
  La publicación ocurre en la capa de aplicación, no en el agregado.
- **`AdminReport` no es una entidad**: es un `record` DTO calculado por el Visitor
  (`admin/domain/report/AdminReport.java:9`). No tiene tabla ni repositorio.
- **`Role` y `UserStatus`** viven en `shared/security` e `identity/domain` respectivamente; se incluyen
  aquí porque son parte del lenguaje del dominio.
- `%% TODO: confirmar` si se añaden agregados como `Dispute`, `Invoice` o `Subscription`; hoy no existen.
