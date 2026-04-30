# Training Metrics API

[![CI](https://github.com/axeldufo/training-metrics-api/actions/workflows/ci.yml/badge.svg)](https://github.com/axeldufo/training-metrics-api/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=axeldufo_training-metrics-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=axeldufo_training-metrics-api)

## About

REST API for tracking and analyzing endurance training sessions.  
Built as a portfolio project to demonstrate backend engineering practices with Java and Spring Boot.

Deliberately architected to evolve from a **layered monolith** (Phase 1) toward **hexagonal architecture** (Phase 2) 
— demonstrating how a well-structured monolith can grow incrementally and, if justified, be extracted into 
microservices without big-bang rewrites.

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

- **JUnit 6 + Mockito** — unit and web layer testing
- **AssertJ** — fluent assertions
- **Instancio** — random test data generation
- **ArchUnit** — architecture testing and design decision documentation

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
├── controller/     # REST endpoints and exception handlers
├── service/        # Business logic
├── repository/     # Data access (JPA adapters and Spring Data interfaces)
├── domain/         # Domain entities, enum, and business exceptions
├── dto/            # Request / Response objects
│   ├── request/
│   └── response/
├── config/         # Spring configuration
└── mapper/         # Object mapping (Domain <-> Entity, Domain <-> DTO)
```

## Roadmap

**Phase 1 — Foundation (current)**
- [x] Project setup (Docker, CI, SonarCloud, OpenAPI)
- [x] Athlete CRUD API
- [x] Architecture testing (ArchUnit)
- [ ] Training session API

**Phase 2 — Business logic**
- [ ] Training load calculation
- [ ] Overtraining detection
- [ ] Domain extraction (hexagonal principles)
- [ ] Integration tests with Testcontainers
- [ ] Deployment (Railway / Render)

