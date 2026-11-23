# ApplyTrack

A small REST API for tracking job applications: where you applied, what stage each one is at, and which ones have gone quiet and need a follow-up.

## Stack

Java 21, Spring Boot 3.5, Spring Data JPA, H2 (file-based, zero setup).

## Run

```bash
mvn spring-boot:run
```

The API is served at `http://localhost:8080/api/applications`.

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/applications` | Create an application |
| GET | `/api/applications` | List all, optionally `?status=INTERVIEW` |
| GET | `/api/applications/{id}` | Get one |
| PUT | `/api/applications/{id}` | Update |
| DELETE | `/api/applications/{id}` | Delete |
| GET | `/api/applications/stale?days=14` | Still `APPLIED` after more than N days |

Statuses: `APPLIED`, `SCREENING`, `INTERVIEW`, `OFFER`, `REJECTED`, `WITHDRAWN`.

Example:

```bash
curl -X POST localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{"company":"Acme","role":"Backend Engineer","appliedDate":"2026-11-01"}'
```

## Test

```bash
mvn test
```

## Design docs

- [User flow](docs/user-flow.md)
- [High-level design](docs/hld.md)
- [Low-level design](docs/lld.md)

## Roadmap

- Contacts and interview notes per application
- Email or calendar reminders for follow-up dates
- Simple web UI
