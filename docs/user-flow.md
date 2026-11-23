# User Flow

How a job seeker uses ApplyTrack, from logging an application to following up on it.

## 1. End-to-end flow

```mermaid
flowchart TD
    A([Start]) --> B[Find a job to apply for]
    B --> C[Submit application on the company site]
    C --> D["POST /api/applications<br/>company, role, appliedDate"]
    D --> E{Valid request?}
    E -- No --> F["400 Bad Request<br/>fix missing or blank fields"]
    F --> D
    E -- Yes --> G["201 Created<br/>status = APPLIED"]
    G --> H[Wait for a response]
    H --> I["GET /api/applications/stale?days=14"]
    I --> J{Any stale applications?}
    J -- Yes --> K[Send a follow-up to the company]
    K --> L["PUT /api/applications/{id}<br/>update status, followUpDate, notes"]
    J -- No --> M[Keep waiting]
    M --> H
    H --> N[Company replies]
    N --> L
    L --> O{Outcome}
    O -- Progressing --> H
    O -- Offer --> P([OFFER])
    O -- Not selected --> Q([REJECTED])
    O -- Not interested --> R([WITHDRAWN])
```

## 2. Application status lifecycle

The statuses a typical application moves through. The API does not currently enforce these transitions: any status can be set directly with an update.

```mermaid
stateDiagram-v2
    [*] --> APPLIED: create application
    APPLIED --> SCREENING: recruiter call
    SCREENING --> INTERVIEW: invited to interview
    INTERVIEW --> OFFER: offer received
    APPLIED --> REJECTED
    SCREENING --> REJECTED
    INTERVIEW --> REJECTED
    APPLIED --> WITHDRAWN
    SCREENING --> WITHDRAWN
    INTERVIEW --> WITHDRAWN
    OFFER --> WITHDRAWN: decline offer
    OFFER --> [*]
    REJECTED --> [*]
    WITHDRAWN --> [*]
```

## 3. Daily routine

```mermaid
flowchart LR
    A[Open tracker] --> B[List active applications<br/>GET /api/applications?status=INTERVIEW]
    B --> C[Check stale applications<br/>GET /stale]
    C --> D[Follow up on stale ones]
    D --> E[Log any new applications]
    E --> F[Update statuses and notes]
```
