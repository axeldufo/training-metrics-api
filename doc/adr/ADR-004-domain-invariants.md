# ADR-004 — Domain Invariants with Manual Constructors

## Status
Accepted

## Context
Initial implementation used Lombok `@RequiredArgsConstructor` on domain entities.
Validation was handled at two boundaries:
- **HTTP boundary** — Bean Validation (`@NotBlank`, `@NotNull`, `@Min`, `@Max`) on request DTOs
- **Database boundary** — PostgreSQL constraints (`NOT NULL`, `CHECK`, `UNIQUE`, FK)

This three-layer approach (HTTP → domain → DB) is intentional: each layer protects
against its own failure mode. However, the domain layer had no protection of its own.

This is insufficient for a system designed to eventually support multiple entry points
(Kafka consumers, gRPC endpoints). These entry points bypass HTTP validation entirely —
a malformed object could reach the domain layer unchecked. DB constraints are a last
resort, not a substitute for domain validation.

**Coherence between layers:**
- HTTP validation mirrors DB constraints where applicable (`@Size(max=100)` ↔ `VARCHAR(100)`)
- DB constraints are the ultimate safety net — they catch anything that bypasses
  the upper layers
- Domain invariants are the middle layer — self-protecting objects regardless of entry point

## Decision
Replace `@RequiredArgsConstructor` with manual constructors on all domain entities
(`Athlete`, `Coach`, `TrainingSession`). Each constructor enforces invariants:

```java
if (firstName == null || firstName.isBlank())
    throw new DomainValidationException("Athlete first name is required");
```

**Rules:**
- `isBlank()` covers both null and blank — single check sufficient
- Constructor parameters use wrapper types (`Integer`, `Long`) to allow explicit
  null checks; fields use primitives for non-nullable values
- `DomainValidationException` in `domain/exception/` — mapped to 400 in
  `GlobalExceptionHandler`
- `@NonNull` Lombok rejected — throws raw `NullPointerException`, not mapped

**`DomainValidationException` handler note:** currently only reachable via HTTP.
The handler exists as defensive infrastructure for future Kafka/gRPC entry points.
Not covered by tests intentionally (YAGNI applied to the test, not the invariant).
Ticket open — will be covered when a second entry point is introduced.

**Rich domain model:** business logic lives in domain POJOs alongside invariants:
- `TrainingSession.getFosterLoad()` — `rpe × durationInMin`
- `TrainingSession.isAboveTargetZone()` / `isBelowTargetZone()`
- `TargetZone.isRpeTooHigh(int rpe)` / `isRpeTooLow(int rpe)`

## Consequences
- `@RequiredArgsConstructor` removed from all domain entities
- Domain objects are self-protecting — invalid state is impossible after construction
- Test structure: `@Nested` with `BusinessLogic` and `Invariants` inner classes
- `@ParameterizedTest + @ValueSource` for multiple invariant cases
- Instancio bypasses constructors via reflection — constrain bounded fields
  explicitly in tests (`rpe`, `durationInMin`, email, password)

## Trade-off
`DomainValidationException` handler in `GlobalExceptionHandler` has no test
coverage today — artificial test would not reflect any real production path.
Accepted consciously. Tracked in open ticket.