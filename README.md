# Training Metrics API

[![CI](https://github.com/axeldufo/training-metrics-api/actions/workflows/ci.yml/badge.svg)](https://github.com/axeldufo/training-metrics-api/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=axeldufo_training-metrics-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=axeldufo_training-metrics-api)

## About

REST API for tracking and analyzing endurance training sessions.  
Built as a portfolio project to demonstrate backend engineering practices with Java and Spring Boot.

## Tech Stack

- **Java 21** — virtual threads ready, modern language features
- **Spring Boot 4.0.5** — REST API, dependency injection, auto-configuration
- **PostgreSQL 16** — relational database, running via Docker
- **Maven** — build tool and dependency management
- **JUnit 5 + Mockito** — unit testing
- **Docker + Docker Compose** — local environment
- **GitHub Actions** — CI pipeline (build, test, quality analysis)
- **SonarCloud** — static code analysis and quality gate
- **OpenAPI / Swagger UI** — auto-generated API documentation

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

## Project Structure

```
src/main/java/com/axel/trainingmetricsapi/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access interfaces
├── domain/         # Domain entities
├── dto/            # Request / Response objects
│   ├── request/
│   └── response/
├── exception/      # Exception handling
├── config/         # Spring configuration
└── mapper/         # Entity <-> DTO mapping
```

## Architecture

Layered architecture (Controller → Service → Repository) with clean separation of concerns:

- Services depend on **interfaces**, not implementations — enabling isolated unit testing
- Domain objects are **pure Java** — no Spring or JPA annotations
- `@SpringBootTest` reserved for a single context load test — all other tests use Mockito or `@WebMvcTest`

Phase 2 will introduce domain extraction on a complex business feature (training load calculation), illustrating the evolution toward hexagonal architecture principles.

## Roadmap

**Phase 1 — Foundation (current)**
- [x] Project setup (Docker, CI, SonarCloud, OpenAPI)
- [ ] Athlete CRUD API
- [ ] Training session API

**Phase 2 — Business logic**
- [ ] Training load calculation
- [ ] Overtraining detection
- [ ] Domain extraction (hexagonal principles)
- [ ] Integration tests with Testcontainers
- [ ] Deployment (Railway / Render)

