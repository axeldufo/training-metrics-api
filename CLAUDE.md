# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start PostgreSQL (required before running the app)
docker compose up -d

# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Run tests with coverage report
./mvnw verify

# Build
./mvnw clean package
```

## Project Overview

Spring Boot 4.0.6 / Java 21 REST API for tracking endurance training sessions (athletes, coaches, training sessions). PostgreSQL with Flyway migrations.

Layered monolith intentionally designed to evolve toward hexagonal architecture in phase 2. Core principle: **domain classes have zero Spring/JPA dependencies**.

## Architecture

### Layer Overview

```
controller/     — @RestController, @RestControllerAdvice, WebMapper (DTO ↔ Domain)
service/        — interfaces + implementations, @Transactional coordination
repository/     — JPA adapters (implements domain interfaces), PersistenceMapper, Spring Data repos
domain/         — pure POJOs (Athlete, TrainingSession, Coach), enums, exceptions
dto/            — request/response objects
```

### Data Flow

```
HTTP → Controller → WebMapper (request→domain) → Service → JPA Adapter → PersistenceMapper (domain↔entity) → DB
```

### Key Patterns

**Repository pattern with dependency inversion:** Domain interfaces (e.g., `AthleteRepository`) live in `domain/` and are implemented by JPA adapters (e.g., `AthleteJpaAdapter`) in `repository/`. Spring Data interfaces are internal implementation details of the adapters.

**Two mapper types:**
- `WebMapper` in `controller/`: DTO ↔ Domain (`requestToDomain()`, `domainToResponse()`)
- `PersistenceMapper` in `repository/`: Domain ↔ JPA Entity (`entityToDomain()`, `domainToEntity()`)

**Domain entities:**
- Pure POJOs with manual constructors enforcing invariants. Immutable with final fields
- Contain business logic methods (e.g., `getFosterLoad()`, `isAboveTargetZone()`)
- No Spring or JPA annotations
- Exceptions in `domain/exception/`

**Exception hierarchy:** `DomainValidationException` for constructor validation -> 400; `AthleteNotFoundException`, `CoachNotFoundException` for not-found cases -> 404. A global `@RestControllerAdvice` maps these to standardized `ApiError` responses.

**API versioning:** All endpoints prefixed with `ApiConstants.API_VERSION` (`/v1`).

**Architecture enforcement:** `ArchitectureTests.java` uses ArchUnit — it is the source of truth for architectural constraints and design rules (e.g., no Spring in domain, correct dependency directions). Update it explicitly if the architecture evolves.

**Authenticated coach resolution:** Controllers inject `AuthenticatedCoachResolver` (not `@AuthenticationPrincipal`) 
to resolve the current coach id from the security context.

**Pagination:** `PageResult<T>` (domain/) and `PagedResponse<T>` (dto/response/) used for athlete list only. Session and WeeklyWellness lists use period filter instead.

**Period filter endpoints:** `from` (required) + `to` (optional, defaults to `LocalDate.now()`). Validate `from <= to` at controller level → 400. Port always receives two `LocalDate` parameters.

**Cache pattern:** `@Cacheable` for read path (declarative, Spring-managed).
`RedisTemplate` for manual eviction (explicit control).
Cache names as public constants in `CacheConfig` (config/).
Always use `Jackson2JsonRedisSerializer` — human-readable entries, no Java serialization.

**Domain Events:** Records in `domain/event/`. Published via `ApplicationEventPublisher`
in service layer after successful persistence. Handled by `*EventHandler` classes in `service/`.
Synchronous in phase 2 layered — will become `@Async` in hexagonal phase.

## Development Rules

**TDD is mandatory.** Always Red → Green → Refactor. Write the test first.
Outside-in: start from controller test, then service, then repository.

**No new dependencies** without explicit request. Never modify pom.xml autonomously.

**Mappers are manual** — no MapStruct in phase 1.

**Always explain the why** of each significant choice in one sentence when proposing code.

**Permissions:** Read any file or directory without asking. Always ask before writing files or running non-read commands.

**When used interactively (guided mode):** No complete classes unless explicitly blocked.
Guide with questions, validate before moving on. Always ask before deciding.

**When used autonomously (Claude Code):** Follow the TDD spec exactly.
If a decision is not covered by CLAUDE.md or the TDD, make the most conservative choice
and document it in a comment.

## Code Conventions

**DTOs:**
- Request: wrapper types (`Integer`, `Long`) — required for `@NotNull`
- Response: primitives for always-present fields, wrappers for nullable fields
- Domain: primitives for non-nullable fields

**Transactional:** `@Transactional(readOnly = true)` at class level, `@Transactional` on write methods.

**Lombok on domain:** `@Getter @EqualsAndHashCode(exclude="id") @ToString`. Manual constructor mandatory — enforces domain invariants. No `@RequiredArgsConstructor`.

**Lombok on JPA entities:** `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` only. No `@ToString` (N+1 risk), no `@EqualsAndHashCode`.

**Logging:** Use `@Slf4j` (Lombok) on any class that needs logging.
Logger variable is `log` (generated by Lombok). Never use `System.out` or `java.util.logging`.

**Date literals:** Always use `java.time.Month` enum constants instead of int literals  when constructing 
`LocalDate` — e.g. `LocalDate.of(2025, Month.APRIL, 28)` not `LocalDate.of(2025, 4, 28)`. Applies to all layers 
including tests.

## Test Conventions

**Stack:**
- JUnit 5 + Mockito + AssertJ
- `@WebMvcTest` for controller layer tests
- Repository layer: Mockito & Testcontainers with real PostgreSQL; no @DataJpaTest -> H2 is not PostgreSQL

**Instancio:** Use throughout all test layers (unit tests with Mockito). Always constrain bounded or validated fields:
- `TrainingSessionRequest.rpe`: `gen.ints().range(1, 10)`
- `TrainingSessionRequest.durationInMin`: `gen.ints().min(1)`
- `TrainingSessionRequest.date`: `gen.temporal().localDate().past()`
- `RegisterRequest.email` / `LoginRequest.email`: `gen.net().email()`
- `RegisterRequest.password` / `LoginRequest.password`: `gen.string().minLength(8)`
- `perceivedDifficulty`, `perceivedFatigue`, `motivation`: `gen.ints().range(1, 5)`
- `weekStartDate`: must be a Monday — use `LocalDate.now().with(DayOfWeek.MONDAY)`

For request DTOs with multiple validation constraints, extract a private helper:
```java
private RegisterRequest aValidRegisterRequest() {
    return Instancio.of(RegisterRequest.class)
        .generate(field(RegisterRequest::email), gen -> gen.net().email())
        .generate(field(RegisterRequest::password), gen -> gen.string().minLength(8))
        .create();
}
```
This keeps test setup concise and reusable across multiple test methods in the same class.

**Controller tests:** All `@WebMvcTest` test classes must extend `SecurityMockControllerSupport`.
Inject `@MockitoBean AuthenticatedCoachResolver` and mock per test with
`when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId))`.

**Integration tests (IT):** explicit manual construction — readable, no FK risk, failures mean real issues.
Use a private helper (e.g. `anAthlete()`) when the same object is constructed 3+ times in the same IT class.

**Structure:**
- One test = one scenario
- `@Nested` to separate `BusinessLogic` and `Invariants` in domain tests
- `@ParameterizedTest + @ValueSource` for multiple invariant cases
- given/when/then spacing without inline comments

