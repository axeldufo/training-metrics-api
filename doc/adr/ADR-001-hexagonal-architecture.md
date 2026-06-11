# ADR-001 — Hexagonal Architecture

## Status
Accepted

## Context
Phase 1 and phase 2 were implemented with a clean layered architecture
(controller / service / repository / domain). Dependency inversion was already
in place: domain interfaces (Repository ports) implemented by JPA adapters,
services depending on interfaces only, domain POJOs free of framework annotations.

This layered structure is solid but has one structural limitation: infrastructure
concerns leak into the service layer. Concretely:
- `TrainingSessionServiceImpl` depends on Spring's `ApplicationEventPublisher`
- `AuthServiceImpl` depends on Spring's `BCryptPasswordEncoder`
- `TrainingSessionEventHandler` uses Spring's `CacheManager` directly

These dependencies mean the application layer implicitly knows it lives inside
Spring. Swapping the event mechanism (Spring ApplicationEvent → Kafka) or the
password encoder requires modifying service classes, not just adding an adapter.

The system is also designed to eventually support multiple entry points (Kafka
consumers, gRPC) beyond HTTP. Infrastructure concerns must be fully isolated
before these entry points are introduced.

## Decision
Migrate to hexagonal architecture (Ports & Adapters).

### Core principle
The domain and application layers have zero knowledge of infrastructure.
All infrastructure concerns are hidden behind ports (interfaces) implemented
by adapters. The application layer orchestrates domain logic through ports —
it does not know whether events are dispatched via Spring, Kafka, or any other
mechanism.

### Package structure
domain/                        ← unchanged: POJOs, enums, domain events,
│                                domain exceptions, Repository port interfaces,
└── service/                     domain services (pure POJOs, no Spring)
application/                   ← replaces service/: Use Cases only
└── port/
├── in/                  ← Use Case interfaces (driving ports)
└── out/                 ← infrastructure port interfaces (driven ports):
	TrainingSessionEventPort
	AcwrCachePort
	PasswordEncoderPort
interfaces/                    ← replaces controller/: primary adapters
└── web/                         HTTP controllers, web mappers, DTOs
└── security/              JWT filter, SecurityConfig, AuthenticatedCoach
infrastructure/                ← replaces repository/ + config/ + event handler
├── persistence/               ← JPA adapters, entities, persistence mappers
├── cache/                     ← RedisCacheAdapter (implements AcwrCachePort)
└── event/                     ← SpringTrainingSessionEventAdapter
	(implements TrainingSessionEventPort)

### Repository interfaces stay in domain/
Repository interfaces (`AthleteRepository`, `TrainingSessionRepository`, etc.)
are defined in `domain/` following Vaughn Vernon's convention. These interfaces
express domain intent ("give me all Athletes for a Coach") using ubiquitous
language — they are domain contracts, not application orchestration concerns.
The application layer consumes them but does not define them.

This is a deliberate choice. An alternative approach (Herberto Graca) places
all driven ports in `application/port/out/`. Both are valid. The Vernon approach
is retained here for consistency with the existing codebase and because Repository
interfaces carry domain semantics, not infrastructure semantics.

### New outbound ports (application/port/out/)
Infrastructure concerns currently embedded in service classes are extracted
into explicit ports:

- `TrainingSessionEventPort` — decouples event publishing from Spring
  `ApplicationEventPublisher`. Current adapter: `SpringTrainingSessionEventAdapter`.
  Future adapter: Kafka adapter (phase 3), zero changes to Use Cases.
- `AcwrCachePort` — decouples cache eviction/refresh from Spring `CacheManager`.
  Current adapter: `RedisCacheAdapter`.
- `PasswordEncoderPort` — decouples password hashing from Spring Security
  `BCryptPasswordEncoder`. Current adapter: `BcryptPasswordEncoderAdapter`.

### Security placement
Security classes (`SecurityConfig`, `JwtAuthenticationFilter`, `JwtUtils`,
`AuthenticatedCoach`, `AuthenticatedCoachResolver`) are placed in
`interfaces/web/security/`.

Most of these are tightly coupled to Spring MVC and HTTP (filter chain,
HTTP headers, Spring MVC argument resolution). `JwtUtils` alone could live
in `infrastructure/security/` since JWT generation/validation is not HTTP-specific
and would be reusable for a gRPC adapter. This is noted as a future refactoring
opportunity — keeping all security classes together in `interfaces/web/security/`
is the pragmatic choice for now.

### Use Cases replace God Services
Current `*ServiceImpl` classes mix application orchestration (ownership checks,
repository calls, exception throwing) with infrastructure concerns (event
publishing, cache management). These are replaced by fine-grained Use Case
classes in `application/`, one Use Case per business action.

Each Use Case is an interface with a single `execute()` method, implemented
by a `@Service` class in the same package. Controllers inject the interface.

### Domain Services
Pure business logic shared across Use Cases is extracted into Domain Services
in `domain/service/`. These are plain POJOs instantiated with `new` inside
Use Cases — no Spring annotations, no framework dependency.

Initial Domain Service: `LoadReportDomainService` — encapsulates the rule
"a week with no training sessions produces a zero load report (injury or
deliberate rest)". This rule belongs to the domain, not to a specific
application scenario.

## Consequences
- `service/` package removed — replaced by `application/`
- `repository/` package renamed to `infrastructure/persistence/`
- `controller/` package renamed to `interfaces/web/`
- All existing service tests rewritten as Use Case tests (same coverage,
  adapted mocks)
- Controller tests minimally impacted — mock target renamed, behavior unchanged
- ArchUnit rules updated: no Spring imports in `domain/` or `application/`,
  `infrastructure/` and `interfaces/` do not depend on each other
- Future adapter swap (e.g. Kafka): add adapter class, update Spring wiring,
  zero changes to domain or application layer

## Alternatives considered
**Keep layered architecture** — rejected. The infrastructure leakage into
service classes (ApplicationEventPublisher, BCryptPasswordEncoder, CacheManager)
is not a theoretical concern: it already creates friction when tracing the
boundary between business logic and infrastructure in code review. The gap
will widen as Kafka and additional entry points are introduced.

**Full Clean Architecture with separate Use Case input/output DTOs** — rejected
for now. Introducing dedicated request/response objects per Use Case would
require duplicating DTOs across boundaries, adding mappers at every layer.
The current approach (domain objects flow through Use Cases, web DTOs mapped
at the controller boundary) is sufficient for a single-team project at this stage.
