# TDD-006 — AcwrReport

## Context
First reporting domain. Introduces rolling ACWR calculation (J-7/J-28 sliding window).
No persistence — volatile by nature, recalculated on demand and proactively on data changes.
Introduces Redis cache, @Cacheable abstraction, and manual eviction with CacheManager,
and ApplicationEvent Spring (sync) for proactive cache refresh on TrainingSession changes.
AcwrCalculator in domain/ isolates pure business logic — anticipates hexagonal Domain Service.

## Dependencies added
- `org.springframework.boot:spring-boot-starter-data-redis` (no version — managed by BOM)
- `org.springframework.boot:spring-boot-starter-cache` (no version — managed by BOM)
  No new test dependencies required — GenericContainer already available via existing testcontainers-postgresql.

## Calculation

### Definitions
- `acuteLoad` = total Foster load over last 7 days (J-6 to J0 inclusive)
- `cchronicLoad` = total Foster load over last 28 days / weeksOfDataAvailable
             (weeksOfDataAvailable = distinct weeks with at least one session)
- `acwr` = acuteLoad / chronicLoad
- Standard Banister/Gabbett definition — chronic window includes acute week

### AcwrAlert enum (domain/)
```java
public enum AcwrAlert {
    NO_DATA,  // chronicLoad = 0 or insufficient data (acwrReliable = false)
    LOW,      // acwr < 0.8 — undertraining risk
    OK,       // 0.8 <= acwr <= 1.3
    HIGH;     // acwr > 1.3 — overload risk

    public static AcwrAlert from(double acwr, boolean reliable) {
        if (!reliable) return NO_DATA;
        if (acwr < 0.8) return LOW;
        if (acwr > 1.3) return HIGH;
        return OK;
    }
}
```

### AcwrCalculator (domain/)
Pure POJO, no Spring annotations. Anticipates hexagonal Domain Service extraction.
Owns a `LoadReportCalculator` instance (instantiated internally with `new`).

Public API:
```java
public AcwrReport calculate(long athleteId, List<TrainingSession> sessions, LocalDate today);
```
Groups sessions by weekStartDate via `LoadReportCalculator`, builds List<LoadReport>
ordered current week first, then delegates to `computeFromWeeklyLoads()`.

Package-private API (used by WeeklyReportCalculator in TDD-008):
```java
AcwrReport computeFromWeeklyLoads(long athleteId, LocalDate weekStartDate, List<LoadReport> loadReports);
```
Single source of truth for ACWR business rules: ratio, thresholds 0.8/1.3,
reliability (weeksOfData >= 4), chronicLoad=0 → acwr=0.0.

## Domain Object

### AcwrReport (domain/)
Pure POJO, no Lombok except @Getter @ToString. Not persisted.

Fields:
- `long athleteId`
- `LocalDate calculatedAt` — date of calculation (today)
- `double acuteLoad`
- `double chronicLoad`
- `double acwr` — 0.0 if not reliable
- `AcwrAlert acwrAlert`
- `int weeksOfDataAvailable`
- `boolean acwrReliable`

No domain invariants — AcwrCalculator enforces correctness.

## No new Repository port
AcwrReportService reuses existing `TrainingSessionRepository.findByAthleteIdAndPeriod()`
— no new port, no new adapter, no new JPA entity.

## Cache

### Configuration (CacheConfig.java in config/)
Single bean, extensible for future caches (TDD-007/008/009 will add entries).
Use Jackson2JsonRedisSerializer (human-readable, no Serializable needed on domain objects).
ObjectMapper injected via constructor.
TTL: 7 days for acwr-report cache.
Public constant: `ACWR_REPORT_CACHE = "acwr-report"`.

### @Cacheable on service
```java
@Cacheable(value = ACWR_REPORT_CACHE, key = "#athleteId")
public AcwrReport getAcwrReport(long athleteId) { ... }
```

### Proactive refresh on event
On TrainingSession create/update/delete:
1. Evict cache entry for athleteId
2. Immediately recalculate and re-cache → coach always finds report ready

```java
// In TrainingSessionEventHandler
Cache cache = cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE).evict(athleteId);
AcwrReport fresh = acwrReportService.getAcwrReport(athleteId); // re-caches via @Cacheable
```

TTL 7 days — safety net only (Redis restart, missed event).
Real freshness guaranteed by proactive refresh.


## Domain Events

### New events (domain/event/)
```java
public record TrainingSessionCreatedEvent(long athleteId, LocalDate date) {}
public record TrainingSessionUpdatedEvent(long athleteId, LocalDate date) {}
public record TrainingSessionDeletedEvent(long athleteId, LocalDate date) {}
```
Published from TrainingSessionServiceImpl after successful save/update/delete.
Sync ApplicationEvent — same transaction, same thread.
Will become @Async in hexagonal phase.

### New handler
`TrainingSessionEventHandler` in service/ — cache coherence is business concern, not web layer.
Listens to all 3 events, evicts and proactively refreshes acwr cache.

## Infrastructure changes

### docker-compose.yml
```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
```

### application.yml
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    type: redis
```

### application.yml (test profile)
Redis host/port overridden by Testcontainers @ServiceConnection —
do NOT add redis config in src/test/resources/application.yml.

### Testcontainers configuration
Rename `PostgresTestContainersConfiguration` → `TestContainersConfiguration`.
Add a Redis `GenericContainer("redis:7-alpine").withExposedPorts(6379)` bean
with `@ServiceConnection(name = "redis")` alongside the existing PostgreSQL bean.
Update all existing IT `@Import` references accordingly.

## API Contract

### Endpoint
| Method | URL | Request | Response | Success | Errors |
|---|---|---|---|---|---|
| GET | /v1/athletes/{id}/reports/acwr | — | `AcwrReportResponse` | 200 | 401, 404 |

### DTO
**`AcwrReportResponse`** (record)
- `long athleteId`
- `LocalDate calculatedAt`
- `double acuteLoad`
- `double chronicLoad`
- `double acwr`
- `AcwrAlert acwrAlert`
- `int weeksOfDataAvailable`
- `boolean acwrReliable`

## Service flow

### GET flow
GET /v1/athletes/{id}/reports/acwr
→ AcwrReportController
→ athleteService.findById(athleteId, coachId) — Coach→Athlete ownership
→ AcwrReportService.getAcwrReport(athleteId) [@Cacheable]
→ cache hit → return cached AcwrReport
→ cache miss → trainingSessionRepository.findByAthleteIdAndPeriod(athleteId, today-27, today)
→ AcwrCalculator.calculate(sessions, today)
→ cache result (TTL 7d) → return AcwrReport
→ AcwrReportWebMapper.domainToResponse(report)
→ ResponseEntity.ok(response)

### Event flow
TrainingSession created/updated/deleted
→ TrainingSessionServiceImpl publishes TrainingSessionCreatedEvent(athleteId, date)
→ TrainingSessionEventHandler.onSessionChanged(event)
→ cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE).evict(athleteId)
→ acwrReportService.getAcwrReport(athleteId) — recalculates + re-caches

## Impact on existing code
- `TrainingSessionServiceImpl.save()` — publish TrainingSessionCreatedEvent after save
- `TrainingSessionServiceImpl.update()` — publish TrainingSessionUpdatedEvent after update
- `TrainingSessionServiceImpl.deleteById()` — publish TrainingSessionDeletedEvent after delete
- `CacheConfig` added to config/
- `docker-compose.yml` — add Redis service
- `application.yml` — add Redis + cache config

## Decisions
- Rolling window J-7/J-28 standard Banister/Gabbett — chronic includes acute week
- chronicLoad = 0 → acwr = 0.0, acwrReliable = false, acwrAlert = NO_DATA
- chronicLoad divided by weeksOfDataAvailable (not hardcoded 4) — meaningful average regardless of data availability; acwrReliable signals insufficient historical depth- acwrReliable = true only if weeksOfDataAvailable >= 4
- AcwrAlert.from(acwr, reliable) factory in enum — business rule in domain
- AcwrCalculator in domain/ — Java record. Not persisted. No domain invariants — AcwrCalculator enforces correctness. 
- AcwrCalculator instantiated with `new AcwrCalculator()` inside AcwrReportServiceImpl
  constructor — no Spring annotation on domain class, consistent with domain purity rule.
  AcwrReportServiceImplTest verifies final result (AcwrReport fields),
  not the internal call to AcwrCalculator — calculator logic covered by AcwrCalculatorTest.
- - No new Repository port — reuses TrainingSessionRepository.findByAthleteIdAndPeriod()
- Proactive refresh on event — coach always finds report ready, optimal UX
- TTL 7 days — safety net only, real freshness via proactive refresh
- @Cacheable for read path (declarative, simple)
- CacheManager used for eviction in TrainingSessionEventHandler — avoids hardcoding Spring Cache internal key format; evicts via cache.evict(athleteId)
- IllegalStateException thrown if CacheManager cannot find cache by name — configuration error, not a business error
- ApplicationEvent sync (same transaction) — @Async in hexagonal phase
- TrainingSessionEventHandler in service/ — cache coherence is business concern
- Events in domain/event/ — domain owns its events
- AcwrCalculator owns LoadReportCalculator internally — sessions grouped into weekly
  LoadReports before ACWR computation; single aggregation primitive across the domain
- computeFromWeeklyLoads() package-private — ACWR business rules reusable by
  WeeklyReportCalculator (TDD-008) without duplication, not exposed outside domain/

## Test strategy
- `AcwrCalculatorTest` — pure unit, no Spring
  - nominal: 4 weeks of data → correct acuteLoad, chronicLoad, acwr, alert
  - edge: chronicLoad = 0 → acwr = 0.0, NO_DATA, acwrReliable = false
  - edge: weeksOfDataAvailable = 2 → acwrReliable = false
  - boundary: acwr exactly 0.8 → OK (0.8 inclusive = OK per spec)
  - boundary: acwr exactly 1.3 → OK (1.3 inclusive = OK per spec)
  - verify calculatedAt = today (ArgumentCaptor on LocalDate)
  - computeFromWeeklyLoads — nominal: 4 LoadReports, sessionCount > 0 → acwrReliable=true
  - computeFromWeeklyLoads — weeksOfData < 4 → acwrReliable=false
  - computeFromWeeklyLoads — all sessionCount=0 → chronicLoad=0.0, acwr=0.0, NO_DATA
  - computeFromWeeklyLoads — boundary: acwr exactly 0.8 → OK
  - computeFromWeeklyLoads — boundary: acwr exactly 1.3 → OK
  - computeFromWeeklyLoads — current week sessionCount=0 → acuteLoad=0
- `LoadReportCalculatorTest` — pure unit
  - nominal: 3 sessions → totalFosterLoad = sum, sessionCount = 3
  - single session → totalFosterLoad = session.getFosterLoad(), sessionCount = 1
- `AcwrReportWebMapperTest` — unit, all fields mapped
- `AcwrReportServiceImplTest` — Mockito
  - verify findByAthleteIdAndPeriod called with today-27 to today
  - verify ArgumentCaptor on `to` date = LocalDate.now()
  - verify result fields match expected AcwrReport (acuteLoad, chronicLoad, acwr, acwrAlert)
- `AcwrReportControllerTest` — @WebMvcTest
  - 200 nominal, all response fields asserted
  - 404 athlete not found
  - 401 missing JWT
- `TrainingSessionEventHandlerTest` — Mockito
  - verify cache eviction called on each event type
  - verify getAcwrReport called after eviction (proactive refresh)
- `AcwrReportCacheIT` — @SpringBootTest + Testcontainers (PostgreSQL + Redis)
  - verify cache hit on second call (repository called only once)
  - verify cache evicted and refreshed on TrainingSessionCreatedEvent

## Out of scope
- LoadReport (TDD-007)
- WellnessReport (TDD-008)
- CorrelationReport (TDD-009)
- @Async events (hexagonal phase)
- Kafka (phase 3)
