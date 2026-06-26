# Training Metrics API

[![CI](https://github.com/axeldufo/training-metrics-api/actions/workflows/ci.yml/badge.svg)](https://github.com/axeldufo/training-metrics-api/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=axeldufo_training-metrics-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=axeldufo_training-metrics-api)

## About

REST API for tracking and analyzing endurance training sessions.  
Built as a portfolio project to demonstrate backend engineering practices with Java and Spring Boot.

Deliberately architected to evolve from a **layered monolith** (Phase 1) toward **hexagonal architecture** (Phase 2) 
— demonstrating how a well-structured monolith can grow incrementally and, if justified, be extracted into 
microservices without big-bang rewrites.

See [CLAUDE.md](./CLAUDE.md) for detailed design decisions and development conventions.

## Tech Stack

- **Java 21** — records, modern language features
- **Spring Boot 4.0.6** — REST API, dependency injection, auto-configuration
- **PostgreSQL 16** — relational database, running via Docker
- **Redis 7** — cache for ACWR reports
- **Maven** — build tool and dependency management
- **Lombok** — boilerplate reduction
- **Docker + Docker Compose** — local environment (PostgreSQL, Redis, Jaeger)
- **GitHub Actions** — CI pipeline (build, test, quality analysis)
- **SonarCloud** — static code analysis and quality gate
- **OpenAPI / Swagger UI** — auto-generated API documentation
- **Spring Security + JWT** — stateless authentication
- **Micrometer + Actuator** — metrics and operational endpoints
- **OpenTelemetry + Jaeger** — distributed tracing

## Testing Stack

- **JUnit 5 + Mockito** — unit and web layer testing
- **AssertJ** — fluent assertions
- **Instancio** — random test data generation
- **ArchUnit** — architecture testing and design decision documentation
- **Testcontainers** — integration tests with real PostgreSQL container
- **TestRestTemplate** — end-to-end tests with real HTTP server

## Prerequisites

- Java 21+
- Docker Desktop

## Run Locally

```bash
# 1. Clone the repository
git clone https://github.com/axeldufo/training-metrics-api.git
cd training-metrics-api

# 2. Start PostgreSQL
docker compose up -d

# 3. Start the application
./mvnw spring-boot:run
```

API available at: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui/index.html
Actuator: http://localhost:8081/actuator/health  
Jaeger UI: http://localhost:16686

## Observability

The application implements the three pillars of observability:

- **Structured logs** — ECS (Elastic Common Schema) format in production profile, compatible with ELK and Loki
- **Metrics** — Spring Boot Actuator on port 8081, endpoints: `health`, `metrics`, `info`, `loggers`
- **Traces** — Micrometer Tracing + OpenTelemetry, exported to Jaeger (UI on port 16686)

To run with production logging (structured JSON):
```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

## Architecture & Design Decisions

### Phase 1 — Layered architecture
Started with a **layered architecture** (Controller → Service → Repository), which represents the majority of production systems today — pragmatic choice for rapid
delivery and broad market compatibility.

Clean separation of concerns, domain independence from frameworks (no Spring or JPA annotations in domain objects), and systematic dependency inversion applied throughout.

### Phase 2 — Toward hexagonal architecture
**Hexagonal architecture** with ports & adapters pattern that naturally decouples the domain from infrastructure.

Feature-first package organization with DDD bounded contexts:
```
src/main/java/com/axel/trainingmetricsapi/
├── identity/       # Coach authentication and registration
├── athlete/        # Athlete management
├── training/       # Training sessions, load reports, ACWR
├── wellness/       # Weekly wellness tracking
└── shared/         # Cross-cutting concerns (exceptions, pagination, security, MDC)
```
Facilitates future microservice extraction if the need arises.

Each module follows the hexagonal structure:
```
{module}/
├── domain/              # Pure POJOs, business rules, domain exceptions
├── application/
│   ├── port/in/         # Use Case interfaces (29 interfaces)
│   └── port/out/        # Outbound ports (repositories, cache, events)
├── infrastructure/      # JPA adapters, Redis cache, event adapters
└── interfaces/web/      # Controllers, mappers, DTOs, exception handlers
```

### Architectural rules
Encoded as executable tests in `ArchitectureTests.java` (ArchUnit), covering:
- Layer dependencies
- Domain isolation
- Spring conventions
- Clean Code rules
- Module boundaries enforcement

These tests serve as living documentation — if a rule changes, the test must be explicitly updated, making every architectural decision conscious and traceable.

### Key design decisions
Documented as Architecture Decision Records in `docs/adr/`:
- ADR-001 — Hexagonal architecture adoption
- ADR-002 — Bounded contexts
- ADR-003 — Testcontainers for integration tests
- ADR-004 — Domain invariants enforcement
- ADR-005 — Observability stack

## Roadmap

**Phase 1 — Foundation (complete)**
- [x] Project setup (Docker, CI, SonarCloud, OpenAPI)
- [x] Athlete, Coach, TrainingSession CRUD APIs
- [x] Pagination and period filters
- [x] Architecture testing (ArchUnit)
- [x] Integration tests (Testcontainers)

**Phase 2 — Business logic & architecture (in progress)**
- [x] Spring Security / JWT stateless authentication
- [x] Multi-coach ownership enforcement (404 over 403)
- [x] WeeklyWellness domain and CRUD
- [x] Foster load calculation and TargetZone alerts
- [x] ACWR report with Redis cache and proactive invalidation
- [x] LoadReport persistence with event-driven recalculation
- [x] WeeklyReport (on-the-fly, no persistence)
- [x] Full hexagonal architecture with DDD bounded contexts
- [x] 29 Use Cases with explicit ownership enforcement
- [x] E2E tests (TestRestTemplate + real HTTP + Testcontainers)
- [x] Structured logging (ECS), Actuator, Micrometer Tracing, Jaeger
- [ ] AOP logging on Use Cases (#144)
- [ ] ACL ports between bounded contexts (#126, #136)
- [ ] Spring Modulith + async events (#115)

**Phase 3 — Backlog**
- [ ] Kafka — async overload alerts
- [ ] Spring Batch — Garmin .fit file import
- [ ] External API integration (Strava OAuth2)
- [ ] Deployment (Railway or Render)
