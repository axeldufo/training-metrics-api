# ADR-002 — Bounded Contexts and Feature Module Organization

## Status
Accepted

## Context
After phase 2, the codebase contains five distinct business domains:
Coach/Auth, Athlete, TrainingSession/LoadReport, AcwrReport, and
WeeklyWellness/WeeklyReport. All classes currently live in flat
technology-oriented packages (controller/, service/, repository/, domain/).

This structure makes it increasingly difficult to identify which classes
belong together from a business perspective. As the codebase grows, the
absence of explicit boundaries creates risk: a developer adding a feature
in the wellness domain can accidentally introduce a direct dependency on
a training domain class, bypassing the intended event-based boundary.

The hexagonal refactoring (ADR-001) clarifies the technical layers but
does not address the business grouping problem. A second axis of organization
is needed: bounded contexts.

## Decision

### Four bounded contexts identified

| Context | Aggregates / Domain Objects | Responsibility |
|---|---|---|
| **identity** | Coach, CoachCredentials, CoachAuthData | Registration, authentication, JWT |
| **athlete** | Athlete | Athlete CRUD, Coach→Athlete ownership |
| **training** | TrainingSession, LoadReport, AcwrReport | Session recording, Foster load, ACWR |
| **wellness** | WeeklyWellness, WeeklyReport | Weekly surveys, load/wellness correlation |

### Inter-context boundary
The boundary between **training** and **wellness** is the only inter-context
dependency in the current system. `WeeklyReport` (wellness) is computed from
`LoadReport` data (training). This dependency is expressed in two ways:

**Via domain events (write path):**
TrainingSession changes are published via `TrainingSessionEventPort`
(application/port/out/). The current adapter `SpringTrainingSessionEventAdapter`
(infrastructure/event/) handles cache eviction and load recalculation via
`TrainingSessionChangedUseCase`. Future Kafka adapter will publish to a topic
consumed by the wellness context — zero changes to Use Cases.

The wellness context listens to these events to trigger cache eviction and
report recalculation. It does not hold a direct reference to any training
class.

**Via a port (read path) — planned:**
`GetWeeklyReportByWeekUseCase` (wellness) currently calls `LoadReportRepository`
and `LoadReportCalculator` from training directly — temporary coupling documented
as technical debt. An Anti-Corruption Layer (ACL) will be introduced (dedicated
ticket): wellness will define its own `WeeklyLoadPort` and `LoadData` record,
training will provide the adapter. Direct cross-module import will be eliminated
before Spring Modulith introduction (ticket #115).

No intermediate `LoadReportPort` or `LoadReportDomainService` is introduced —
`LoadReportCalculator` already encapsulates the calculation rule cleanly.

### Shared Kernel
A minimal `shared/` module is retained for true cross-cutting primitives
that carry no business logic and belong to no single bounded context:

- `PageResult<T>` — generic pagination wrapper
- `ResourceNotFoundException` — abstract base for all not-found exceptions
- `DomainValidationException` — domain invariant violation

**Rules:**
- `shared/` is frozen — nothing new enters without explicit discussion
- No feature module logic belongs here
- All feature modules may depend on `shared/`; `shared/` depends on nothing

`TestContainersConfiguration` lives in `shared/` on the test side only.

A `shared/` module that grows is a design smell — it signals a missing
bounded context or a port that should be introduced instead.

### Two separate workstreams
Bounded context extraction is deliberately separated from the hexagonal
refactoring (ADR-001) for two reasons:

1. **Risk isolation** — hexagonal restructuring changes class responsibilities;
   feature-first reorganization changes package structure. Doing both
   simultaneously makes rollback and review harder.
2. **Clarity** — once hexagonal boundaries are clean, the right package
   grouping becomes obvious. Doing feature extraction first risks locking
   in boundaries before the internal structure is settled.

Order: hexagonal refactoring first (ADR-001 tickets), module extraction second.

### Target package structure (post-extraction)
Spring Modulith requires top-level feature packages directly under the
application root — this is how it detects module boundaries. The target
structure is therefore feature-first, not layer-first:

athlete/
├── application/       ← Use Cases
├── domain/            ← POJOs, ports, exceptions specific to athlete
├── infrastructure/    ← JPA adapter, entity, persistence mapper
└── interfaces/        ← controller, web mapper, DTOs
identity/
├── application/
├── domain/
├── infrastructure/
└── interfaces/
training/
├── application/
├── domain/
├── infrastructure/
└── interfaces/
wellness/
├── application/
├── domain/
├── infrastructure/
└── interfaces/
shared/
├── domain/            ← PageResult, ResourceNotFoundException,
│                        DomainValidationException
└── infrastructure/    ← TestContainersConfiguration (test scope only)

Each module is self-contained. Cross-module dependencies are expressed
exclusively through domain events or ports — no direct class references
between feature modules.

This structure also prepares microservice extraction: each module is a
candidate autonomous service. Extracting `training` to a microservice means
moving one package, not hunting classes across four layer-oriented directories.

## Consequences
- Package reorganization is a separate ticket, executed after hexagonal
  refactoring is complete and all tests are green
- `domain/` stays flat until Spring Modulith is introduced — at that point
  the top-level module structure will be reconsidered
- `LoadReportCalculator` is instantiated with `new` directly in Use Cases
  that need on-the-fly load calculation — no intermediate Domain Service or
  port needed
- `shared/` is frozen — any addition requires explicit justification
- Any future cross-context dependency must go through domain events or a
  port, not direct class references
- ArchUnit rules will be updated to verify no direct cross-module class
  references once feature packages exist

## Alternatives considered
**Single flat package structure (current state)** — rejected. Acceptable
at phase 1 scale, increasingly problematic as the codebase grows. The
training/wellness boundary in particular needs to be explicit: the event
chain between the two contexts is a load-bearing architectural decision
that should be visible in the package structure.

**Separate Maven modules per bounded context** — deferred. Maven multi-module
adds build complexity and requires stable API boundaries between modules.
Spring Modulith provides the same enforcement guarantees within a single
module at lower cost. Maven extraction is a future option if the project
evolves toward microservices.

**Feature packages immediately (without hexagonal first)** — rejected.
Reorganizing by feature before the layer responsibilities are clear risks
mixing infrastructure and domain concerns inside feature packages, recreating
the same problem at a different granularity.

**No shared/ module (strict isolation)** — rejected. `PageResult`,
`ResourceNotFoundException`, and `DomainValidationException` are true
cross-cutting primitives with no business logic. Duplicating them across
modules provides no architectural benefit and creates maintenance overhead.
A frozen Shared Kernel (Vaughn Vernon) is the accepted pattern for this case.
