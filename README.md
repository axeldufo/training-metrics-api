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
- **Spring Boot 4.0.5** — REST API, dependency injection, auto-configuration
- **PostgreSQL 16** — relational database, running via Docker
- **Maven** — build tool and dependency management
- **Lombok** — boilerplate reduction (getters, constructors, equals/hashCode)
- **Docker + Docker Compose** — local environment
- **GitHub Actions** — CI pipeline (build, test, quality analysis)
- **SonarCloud** — static code analysis and quality gate
- **OpenAPI / Swagger UI** — auto-generated API documentation

## Testing Stack

- **JUnit 5 + Mockito** — unit and web layer testing
- **AssertJ** — fluent assertions
- **Instancio** — random test data generation
- **ArchUnit** — architecture testing and design decision documentation
- **Testcontainers** — integration tests with real PostgreSQL container

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

## Architecture & Design Decisions

### Phase 1 — Layered architecture
Started with a **layered architecture** (Controller → Service → Repository), which represents the majority of production systems today — pragmatic choice for rapid
delivery and broad market compatibility.

Clean separation of concerns, domain independence from frameworks (no Spring or JPA annotations in domain objects), and systematic dependency inversion applied throughout.

### Phase 2 — Toward hexagonal architecture
Domain extraction on a complex business feature (training load calculation), illustrating the evolution toward 
**hexagonal architecture** — ports & adapters pattern that naturally decouples the domain from infrastructure and facilitates future microservice extraction if the need arises.

### Architectural rules
Encoded as executable tests in `ArchitectureTests.java`, covering:
- Layer dependencies
- Domain isolation
- Spring conventions
- Clean Code rules

These tests serve as living documentation — if a rule changes, the test must be explicitly updated, making every architectural decision conscious and traceable.

## Project Structure

```
src/main/java/com/axel/trainingmetricsapi/
├── controller/     # REST endpoints, mappers, exception handlers and security
├── service/        # Business logic
├── repository/     # Data access (JPA adapters and Spring Data interfaces) and mappers
├── domain/         # Domain entities, enums, business exceptions and PageResult<T>
├── dto/            # Request / Response objects
│   ├── request/
│   └── response/
├── config/         # Spring configuration
```

## Roadmap

**Phase 1 — Foundation (complete)**
- [x] Project setup (Docker, CI, SonarCloud, OpenAPI)
- [x] Athlete CRUD API with pagination
- [x] Coach CRUD API
- [x] TrainingSession CRUD API with pagination
- [x] Architecture testing (ArchUnit)
- [x] Integration tests (Testcontainers + PostgreSQL)

**Phase 2 — Business logic (in progress)**
- [x] Spring Security / JWT authentication
- [x] Multi-coach ownership filtering
- [ ] WeeklyWellness domain and CRUD
- [ ] WeeklyReport with Foster load, ACWR and overload alerts
- [ ] Domain extraction toward hexagonal architecture
- [ ] CQRS pattern on WeeklyReport
- [ ] Redis cache on WeeklyReport
- [ ] Kafka for asynchronous overload alerts
- [ ] Observability (SLF4J MDC, Actuator, Micrometer)


