# Clases de dominio

Modelo inferido de las entidades JPA y de los agregados de dominio. Los importes se
representan con el value object `Money` (`amount` + `currency`), aunque en la
persistencia se aplanan en columnas `*_amount` / `*_currency`.

```mermaid
classDiagram
    direction LR

    class User {
        +UUID id
        +String email
        +String passwordHash
        +Role role
        +UserStatus status
        +String displayName
        +Instant createdAt
        +Instant updatedAt
    }

    class RefreshToken {
        +UUID id
        +UUID userId
        +String tokenDigest
        +Instant expiresAt
        +boolean revoked
        +Instant createdAt
    }

    class Profile {
        +UUID id
        +UUID userId
        +String type
        +String headline
        +String bio
        +BigDecimal hourlyRate
        +String currency
        +Availability availability
        +String companyName
        +String description
        +BigDecimal ratingAverage
        +int ratingCount
        +Instant createdAt
        +Instant updatedAt
    }

    class Skill {
        +UUID id
        +String name
    }

    class PortfolioItem {
        +UUID id
        +UUID profileId
        +String title
        +String url
        +String description
        +String storageKey
    }

    class Project {
        +UUID id
        +UUID clientId
        +String title
        +String description
        +ProjectCategory category
        +Modality modality
        +ProjectStatus status
        +BigDecimal budgetAmount
        +String budgetCurrency
        +LocalDate deadline
        +Instant publishedAt
        +Instant createdAt
        +Instant updatedAt
    }

    class Proposal {
        +UUID id
        +UUID projectId
        +UUID freelancerId
        +String coverLetter
        +BigDecimal amount
        +String currency
        +int estimatedDays
        +ProposalStatus status
        +Integer evaluationScore
        +String evaluationNote
        +Instant createdAt
        +Instant updatedAt
    }

    class Contract {
        +UUID id
        +UUID projectId
        +UUID proposalId
        +UUID clientId
        +UUID freelancerId
        +String title
        +String scope
        +BigDecimal amountAmount
        +String amountCurrency
        +BigDecimal commissionAmount
        +String commissionCurrency
        +LocalDate startDate
        +LocalDate endDate
        +ContractStatus status
        +Instant createdAt
        +Instant updatedAt
        +Instant completedAt
        +int progress()
    }

    class Milestone {
        +UUID id
        +String title
        +String description
        +BigDecimal amountAmount
        +String amountCurrency
        +LocalDate dueDate
        +int position
        +MilestoneStatus status
        +int progress()
    }

    class Deliverable {
        +UUID id
        +String title
        +String description
        +String url
        +DeliverableStatus status
        +Instant submittedAt
    }

    class TaskItem {
        +UUID id
        +String title
        +boolean done
    }

    class DeliverableComment {
        +UUID id
        +UUID authorId
        +String body
        +Instant createdAt
    }

    class Conversation {
        +UUID id
        +UUID contractId
        +UUID clientId
        +UUID freelancerId
        +String subject
        +ConversationStatus status
        +Instant createdAt
        +Instant updatedAt
        +Instant lastMessageAt
    }

    class ConversationMessage {
        +UUID id
        +UUID senderId
        +String body
        +Instant sentAt
        +Instant readAt
    }

    class Payment {
        +UUID id
        +UUID contractId
        +UUID payerId
        +UUID payeeId
        +BigDecimal amountAmount
        +String amountCurrency
        +BigDecimal commissionAmount
        +String commissionCurrency
        +BigDecimal netAmount
        +String netCurrency
        +PaymentStatus status
        +String provider
        +String providerReference
        +Instant createdAt
        +Instant processedAt
        +String failureReason
    }

    class Review {
        +UUID id
        +UUID contractId
        +UUID authorId
        +UUID targetId
        +int rating
        +String comment
        +Instant createdAt
    }

    class Notification {
        +UUID id
        +UUID recipientId
        +NotificationType type
        +NotificationChannelType channel
        +String title
        +String body
        +NotificationStatus status
        +int attempts
        +Instant createdAt
        +Instant sentAt
        +String lastError
    }

    class AuditLog {
        +UUID id
        +UUID actorId
        +String action
        +String entityType
        +UUID entityId
        +String beforeState
        +String afterState
        +String details
        +Instant occurredAt
    }

    class AdminReport {
        <<DTO>>
        +String report
        +Instant generatedAt
        +Map metrics
    }

    User "1" --> "0..1" Profile : tiene
    User "1" --> "0..*" RefreshToken : posee
    Profile "1" --> "0..*" PortfolioItem : publica
    Profile "0..*" --> "0..*" Skill : declara
    User "1" --> "0..*" Project : publica como cliente
    Project "1" --> "0..*" Proposal : recibe
    User "1" --> "0..*" Proposal : envía como freelancer
    Project "1" --> "0..1" Contract : origina
    Proposal "1" --> "0..1" Contract : aceptada genera
    Contract "1" *-- "1..*" Milestone : contiene
    Milestone "1" *-- "0..*" Deliverable : agrupa
    Deliverable "1" *-- "0..*" TaskItem : desglosa
    Deliverable "1" *-- "0..*" DeliverableComment : comenta
    Contract "1" --> "0..*" Conversation : habilita
    Conversation "1" *-- "0..*" ConversationMessage : contiene
    Contract "1" --> "0..*" Payment : registra
    Contract "1" --> "0..*" Review : recibe
    User "1" --> "0..*" Notification : recibe
    User "1" --> "0..*" AuditLog : actúa
    AdminReport ..> Project : agrega
    AdminReport ..> Contract : agrega
    AdminReport ..> User : agrega
```

## Enumeraciones relevantes

| Enum | Valores |
|------|---------|
| `Role` | `FREELANCER`, `CLIENT`, `ADMIN` |
| `UserStatus` | `ACTIVE`, `SUSPENDED` |
| `ProjectStatus` | `DRAFT`, `PUBLISHED`, `PAUSED`, `IN_PROGRESS`, `FINISHED`, `CANCELLED` |
| `ProposalStatus` | `SUBMITTED`, `WITHDRAWN`, `ACCEPTED`, `REJECTED` |
| `ContractStatus` | `DRAFT`, `ACTIVE`, `COMPLETED`, `CANCELLED` |
| `MilestoneStatus` | `PENDING`, `IN_PROGRESS`, `SUBMITTED`, `APPROVED`, `REJECTED` |
| `DeliverableStatus` | `DRAFT`, `SUBMITTED`, `APPROVED`, `REJECTED` |
| `PaymentStatus` | `PENDING`, `RELEASED`, `FAILED`, `REFUNDED` |
| `ConversationStatus` | `ACTIVE`, `ARCHIVED` |
| `NotificationStatus` | `PENDING`, `SENT`, `FAILED` |
| `Modality` | `REMOTE`, `ONSITE`, `HYBRID` |
| `Availability` | `AVAILABLE`, `PART_TIME`, `UNAVAILABLE` |

## Invariantes que el diagrama no muestra

- Un `Contract` necesita al menos un `Milestone` y la suma de hitos no puede superar el monto del contrato.
- Un `Milestone` no se aprueba sin entregables y solo pasa a `APPROVED`/`REJECTED` desde `SUBMITTED`.
- El contrato se completa automáticamente cuando todos sus hitos están aprobados.
- Un `Review` solo se crea sobre un contrato `COMPLETED` y por una de sus partes.
- `AdminReport` no es una tabla: es un DTO calculado con el patrón Visitor sobre proyectos, contratos y usuarios.

> `%% TODO: confirmar` si en el futuro se añaden agregados como `Dispute` o `Invoice`; hoy no existen en el código.
