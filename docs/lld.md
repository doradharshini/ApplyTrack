# Low-Level Design (LLD)

Describes the code as it exists today, in package `com.applytrack`.

## 1. Package layout

```
com.applytrack
├── ApplyTrackApplication            Spring Boot entry point
└── application
    ├── JobApplication               JPA entity
    ├── ApplicationStatus            enum
    ├── JobApplicationRequest        request record (validated)
    ├── JobApplicationRepository     Spring Data JPA repository
    ├── JobApplicationController     REST controller
    └── ApplicationNotFoundException maps to HTTP 404
```

## 2. Class diagram

```mermaid
classDiagram
    class ApplyTrackApplication {
        +main(String[] args)$
    }

    class JobApplicationController {
        -JobApplicationRepository repository
        +list(ApplicationStatus status) List~JobApplication~
        +stale(int days) List~JobApplication~
        +get(Long id) JobApplication
        +create(JobApplicationRequest request) JobApplication
        +update(Long id, JobApplicationRequest request) JobApplication
        +delete(Long id) ResponseEntity~Void~
        -find(Long id) JobApplication
        -apply(JobApplication target, JobApplicationRequest r) JobApplication
    }

    class JobApplicationRepository {
        <<interface>>
        +findByStatus(ApplicationStatus status) List~JobApplication~
        +findByStatusAndAppliedDateBefore(ApplicationStatus status, LocalDate date) List~JobApplication~
    }

    class JpaRepository {
        <<interface>>
        +findAll() List
        +findById(ID id) Optional
        +save(S entity) S
        +delete(T entity)
    }

    class JobApplication {
        <<Entity>>
        -Long id
        -String company
        -String role
        -ApplicationStatus status
        -LocalDate appliedDate
        -LocalDate followUpDate
        -String jobUrl
        -String notes
    }

    class ApplicationStatus {
        <<enumeration>>
        APPLIED
        SCREENING
        INTERVIEW
        OFFER
        REJECTED
        WITHDRAWN
    }

    class JobApplicationRequest {
        <<record>>
        +String company
        +String role
        +ApplicationStatus status
        +LocalDate appliedDate
        +LocalDate followUpDate
        +String jobUrl
        +String notes
    }

    class ApplicationNotFoundException {
        <<RuntimeException>>
        +ApplicationNotFoundException(Long id)
    }

    JobApplicationController --> JobApplicationRepository : uses
    JobApplicationController ..> JobApplicationRequest : accepts
    JobApplicationController ..> ApplicationNotFoundException : throws
    JobApplicationRepository --|> JpaRepository
    JobApplicationRepository ..> JobApplication : manages
    JobApplication --> ApplicationStatus
    JobApplicationRequest --> ApplicationStatus
    ApplyTrackApplication ..> JobApplicationController : component scan
```

The controller talks to the repository directly. There is no service layer yet, which is acceptable while the logic is plain CRUD. A service layer becomes worthwhile once status-transition rules, history, or reminders are added.

## 3. Sequence diagrams

### 3.1 Create an application

```mermaid
sequenceDiagram
    actor Client
    participant C as JobApplicationController
    participant V as Bean Validation
    participant R as JobApplicationRepository
    participant DB as H2

    Client->>C: POST /api/applications (JSON)
    C->>V: validate JobApplicationRequest
    alt invalid (blank company or role, missing appliedDate)
        V-->>Client: 400 Bad Request
    else valid
        C->>C: apply(new JobApplication(), request)
        Note right of C: status defaults to APPLIED when null
        C->>R: save(entity)
        R->>DB: INSERT job_application
        DB-->>R: generated id
        R-->>C: saved entity
        C-->>Client: 201 Created + JSON body
    end
```

### 3.2 Get stale applications

```mermaid
sequenceDiagram
    actor Client
    participant C as JobApplicationController
    participant R as JobApplicationRepository
    participant DB as H2

    Client->>C: GET /api/applications/stale?days=14
    C->>C: cutoff = today minus days
    C->>R: findByStatusAndAppliedDateBefore(APPLIED, cutoff)
    R->>DB: SELECT ... WHERE status = 'APPLIED' AND applied_date < cutoff
    DB-->>R: rows
    R-->>C: List of JobApplication
    C-->>Client: 200 OK + JSON array
```

### 3.3 Update or delete (not found path)

```mermaid
sequenceDiagram
    actor Client
    participant C as JobApplicationController
    participant R as JobApplicationRepository

    Client->>C: PUT /api/applications/9999
    C->>R: findById(9999)
    R-->>C: Optional.empty
    C->>C: throw ApplicationNotFoundException
    C-->>Client: 404 Not Found
```

## 4. Data model

```mermaid
erDiagram
    JOB_APPLICATION {
        bigint id PK "auto-generated identity"
        varchar company "NOT NULL"
        varchar role "NOT NULL"
        varchar status "NOT NULL, enum stored as text"
        date applied_date "NOT NULL"
        date follow_up_date "nullable"
        varchar job_url "nullable"
        varchar notes "nullable, max 2000"
    }
```

The schema is created and updated by Hibernate (`spring.jpa.hibernate.ddl-auto=update`). Moving to Flyway migrations is planned together with PostgreSQL.

## 5. Request validation rules

| Field | Rule |
| --- | --- |
| `company` | required, not blank |
| `role` | required, not blank |
| `appliedDate` | required, ISO date (`yyyy-MM-dd`) |
| `status` | optional, defaults to `APPLIED` |
| `followUpDate`, `jobUrl`, `notes` | optional |

## 6. Error handling

| Situation | HTTP status | Mechanism |
| --- | --- | --- |
| Validation failure | 400 | `MethodArgumentNotValidException`, Spring default |
| Unknown status value or malformed JSON | 400 | Spring default |
| Application id not found | 404 | `ApplicationNotFoundException` with `@ResponseStatus` |

Error bodies use Spring Boot's default format. A custom error response is a possible later improvement.

## 7. Known limitations

- Status transitions are not validated: any status can be set from any other.
- `PUT` replaces all fields, so a client must send the full object.
- No pagination on list endpoints.
- No authentication, and no per-user data separation.
- `GET /stale` only considers `appliedDate`, not `followUpDate`.
