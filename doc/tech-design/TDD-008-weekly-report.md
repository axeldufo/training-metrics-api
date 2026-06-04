# TDD-008 — WeeklyReport

## Context
Complete weekly analysis report. Crosses LoadReport (last 4 weeks) and WeeklyWellness
(last 5 weeks) to compute ACWR, wellness alerts, and load/wellness correlations.
No persistence — calculated on demand from existing persisted data.
WeeklyWellness required for full correlation — INSUFFICIENT_DATA CorrelationAlert when WeeklyWellness is missing for current week
OR when no previous wellness week exists (delta unavailable — first week). A week with zero sessions is valid
load information (injury, rest) and never triggers INSUFFICIENT_DATA on its own.

## No new persistence
No Flyway migration. No JPA entity. No new repository port.
Reads from existing `LoadReportRepository`, `WeeklyWellnessRepository`,
and `LoadReportService`. No call to `AcwrReportService` — ACWR computed
directly from LoadReports via `AcwrCalculator.computeFromWeeklyLoads()`.

## Domain Objects

### WellnessAlert enum (domain/)
```java
public enum WellnessAlert {
    ABSOLUTE_LOW,    // current value ≤ 2 on 1-5 scale
    WEEK_OVER_WEEK,  // delta vs previous week ≤ -2
    TREND_DECLINING  // linear regression slope over 5 weeks ≤ -0.3/week
}
```

### Alert thresholds — rationale and calibration

**ABSOLUTE_LOW ≤ 2**
On a 1-5 scale, value ≤ 2 indicates severe difficulty/fatigue/low motivation
regardless of trend. Immediate signal requiring attention.

**WEEK_OVER_WEEK delta ≤ -2**
A drop of 2 points in one week on a 5-point scale (40%) is significant even if
punctual. Delta of -1 is notable but not sufficient for alert — may be normal
weekly variation. Example: fatigue 4→2 triggers alert; fatigue 5→4 does not.

**TREND_DECLINING slope ≤ -0.3/week over 5 weeks**
Chosen parameters after calibration:
- Threshold -0.3/week = equivalent to -1.5 points over 5 weeks — detects
  progressive decline invisible week-over-week
- 5-week window: 4 weeks misses subtle decline; 6 weeks dilutes recent signal;
  5 weeks is the right balance
- Example detected: 5→4→4→3→3 (slope ≈ -0.5) ✓
- Example detected: 5→5→4→4→4→3 (slope ≈ -0.3, at threshold) ✓
- Example not triggered: 5→5→5→5→4 (slope ≈ -0.2, normal minor variation) ✓
These thresholds are defined as named constants in `WeeklyReportCalculator`
for easy future adjustment.

### CorrelationAlert enum (domain/)
```java
public enum CorrelationAlert {
    NO_ALERT,                        // all metrics nominal
    INSUFFICIENT_DATA,               // WeeklyWellness missing for current week
    STABLE_LOAD_RISING_FATIGUE,      // load stable, fatigue trending up → external factors
    OVERLOAD_RISK,                   // ACWR > 1.3 AND fatigue trending up
    GOOD_ADAPTATION,                 // ACWR rising AND fatigue stable or improving
    UNDERLOAD_DECLINING_MOTIVATION,  // ACWR < 0.8 AND motivation declining
    POTENTIAL_OVERTRAINING           // load stable AND motivation declining AND fatigue rising
}
```

### WeeklyReportCalculator (domain/)
Pure POJO, no Spring annotations, no Lombok annotation (manual constructor only).
Instantiated with `new AcwrCalculator()` passed as constructor parameter in `WeeklyReportServiceImpl`.
No fields to expose publicly. AcwrCalculator held as private final field.
All threshold constants defined here as `static final` fields.

```java
public class WeeklyReportCalculator {
    static final int ABSOLUTE_LOW_THRESHOLD = 2;
    static final int WEEK_OVER_WEEK_THRESHOLD = -2;
    static final double TREND_DECLINING_SLOPE_THRESHOLD = -0.3;
    static final int TREND_DECLINING_WINDOW_WEEKS = 5;
    static final double ACWR_HIGH_THRESHOLD = 1.3;
    static final double ACWR_LOW_THRESHOLD = 0.8;
    static final double ACWR_RISING_THRESHOLD = 1.03;

    private final AcwrCalculator acwrCalculator;

    public WeeklyReportCalculator(AcwrCalculator acwrCalculator) {
        this.acwrCalculator = acwrCalculator;
    }

    public WeeklyReport calculate(
        long athleteId,
        LocalDate weekStartDate,
        List<LoadReport> lastFourLoadReports,   // current week first, descending
        List<WeeklyWellness> lastFiveWellness   // current week first, descending
    );
}
```

Returns `WeeklyReport` record.
ACWR computed via `acwrCalculator.computeFromWeeklyLoads(athleteId, weekStartDate, lastFourLoadReports)`
— package-private method, accessible because WeeklyReportCalculator is in the same `domain/` package.
No call to `AcwrReportService` — avoids stale cache on historical weeks and double DB reads.

CorrelationAlert conditions — ACWR range is the primary discriminant:
- deltaFatigue or deltaMotivation null (first week, no previous wellness) → INSUFFICIENT_DATA
- acwr > 1.3 AND deltaFatigue > 0                         → OVERLOAD_RISK
- acwr > 1.3 AND deltaFatigue <= 0                        → NO_ALERT
- acwr > 1.03 AND deltaFatigue <= 0                       → GOOD_ADAPTATION
- acwr > 1.03 AND deltaFatigue > 0                        → NO_ALERT
- 0.8 <= acwr <= 1.3 AND fatigueTrendingUp AND motivationDeclining → POTENTIAL_OVERTRAINING
- 0.8 <= acwr <= 1.3 AND fatigueTrendingUp                → STABLE_LOAD_RISING_FATIGUE
- 0.8 <= acwr <= 1.3                                      → NO_ALERT
- acwr < 0.8 AND motivationDeclining                      → UNDERLOAD_DECLINING_MOTIVATION
- acwr < 0.8                                              → NO_ALERT

Where:
- fatigueTrendingUp = deltaFatigue > 0
- motivationDeclining = deltaMotivation < 0
- ACWR_HIGH_THRESHOLD = 1.3 (strict >) — 1.3 is AcwrAlert.OK, not overload
- ACWR_RISING_THRESHOLD = 1.03 (strict >) — 3% above chronic filters noise

**Availability flags:**
- `wellnessAvailable = true` if `lastFiveWellness` contains an entry whose
  `weekStartDate` equals the requested `weekStartDate`
- Load data always present — `LoadReportService` always returns a report (zero or real).
  No `loadAvailable` flag — `sessionCount=0, totalFosterLoad=0` is the signal for
  a week with no training. A zero-load week is valid information, never triggers
  INSUFFICIENT_DATA on its own.

### WeeklyReport (domain/)
Java record.

```java
public record WeeklyReport(
    long athleteId,
    LocalDate weekStartDate,
    // Availability flag — load always present, only wellness can be missing
    boolean wellnessAvailable,
    // Load data (from LoadReport current week — 0 if athlete did not train)
    int totalFosterLoad,
    int sessionCount,
    // ACWR data — computed from LoadReports via AcwrCalculator.computeFromWeeklyLoads()
    double acuteLoad,
    double chronicLoad,
    double acwr,
    AcwrAlert acwrAlert,
    boolean acwrReliable,
    // Wellness data — current week raw values
    Integer perceivedDifficulty,  // null if wellnessAvailable = false
    Integer perceivedFatigue,
    Integer motivation,
    // Wellness deltas vs previous week — null if no previous week or wellness unavailable
    Double deltaDifficulty,
    Double deltaFatigue,
    Double deltaMotivation,
    // Wellness alerts per indicator — empty Set if wellnessAvailable = false
    Set<WellnessAlert> difficultyAlerts,
    Set<WellnessAlert> fatigueAlerts,
    Set<WellnessAlert> motivationAlerts,
    // Correlation
    CorrelationAlert correlationAlert
) {}
```

## Domain Interfaces
No new port. Reuses existing ports — all already implemented in TDD-006 and TDD-007:

- `LoadReportRepository.findByAthleteIdAndWeekStartDateBetween()` — already exists
- `LoadReportRepository.findLatestByAthleteId()` — already exists
  (implemented via `findFirstByAthleteIdOrderByWeekStartDateDesc` in LoadReportJpaAdapter)
- `WeeklyWellnessRepository.findByAthleteIdAndPeriod()` — already exists
- `LoadReportService.findByAthleteIdAndWeekStartDate()` — already exists (TDD-007),
  encapsulates: DB hit → on-the-fly from sessions → zero report (updatedAt=null) if no sessions
- `AcwrCalculator.computeFromWeeklyLoads()` — package-private, accessible from domain/
  (introduced in REFACTOR-LoadReportCalculator)

## API Contract

### Endpoints
| Method | URL | Query params | Response | Success | Errors |
|---|---|---|---|---|---|
| GET | /v1/athletes/{id}/reports/weekly | `weekStartDate` (required) | `WeeklyReportResponse` | 200 | 400, 401, 404 |
| GET | /v1/athletes/{id}/reports/weekly/latest | — | `WeeklyReportResponse` | 200 | 401, 404 |
| GET | /v1/athletes/{id}/reports/weekly | `from` (required), `to` (optional, defaults to today) | `List<WeeklyReportResponse>` | 200 | 400, 401 |

`weekStartDate`, `from`, `to` : `@PastOrPresent` — future weeks rejected at HTTP boundary → 400.

The two `GET /v1/athletes/{id}/reports/weekly` endpoints are implemented as two separate
controller methods discriminated by Spring MVC params (same pattern as LoadReportController):
- `@GetMapping(params = "weekStartDate")` → single WeeklyReportResponse
- `@GetMapping(params = "from")` → List<WeeklyReportResponse>

Note: GET by period never returns 404 — empty list if no data exists in range.

### DTO
`WeeklyReportResponse` — Java record (same convention as all response DTOs).
Mirrors `WeeklyReport` record fields exactly. Nullable fields use wrapper types
(`Integer`, `Double`). `Set<WellnessAlert>` and `CorrelationAlert` map directly
(enum serialization by Jackson).

### Error format
```json
[{ "code": "NOT_FOUND", "field": null, "message": "No weekly report available for athlete 3 on week 2026-05-19" }]
```
404 only on GET by weekStartDate and GET latest when athlete has no sessions
AND no LoadReport at all for that week (zero-report onTheFlyLoad with updatedAt=null confirms no data).
GET by period never returns 404.

## Service flow

### GET by weekStartDate
```
GET /v1/athletes/{id}/reports/weekly?weekStartDate=2026-05-19
→ WeeklyReportController — @PastOrPresent on weekStartDate, @Validated on controller
→ athleteService.findById(athleteId, coachId) — Coach→Athlete ownership
→ WeeklyReportService.getWeeklyReport(athleteId, weekStartDate)

→ loadReports = LoadReportRepository.findByAthleteIdAndWeekStartDateBetween(
     athleteId,
     weekStartDate.minusWeeks(3),   // 4 weeks total, weekStartDate inclusive
     weekStartDate)

→ if loadReports.isEmpty()
     → wellness checked first — if no wellness entry for weekStartDate
          → throw WeeklyReportNotFoundException
            // no load AND no wellness — nothing to report
     → onTheFlyLoad = LoadReportService.findByAthleteIdAndWeekStartDate(athleteId, weekStartDate)
          // called only when wellness exists — worth computing a zero-load report
     → loadReports = List.of(onTheFlyLoad)

→ else (loadReports not empty)
     → wellness = WeeklyWellnessRepository.findByAthleteIdAndPeriod(
     		athleteId,
     		weekStartDate.minusWeeks(4),   // 5 weeks total, weekStartDate inclusive
     		weekStartDate)

→ WeeklyReportCalculator.calculate(athleteId, weekStartDate, loadReports, wellness)
→ WeeklyReportWebMapper.domainToResponse(report)
→ ResponseEntity.ok(response)
```

### GET latest
```
GET /v1/athletes/{id}/reports/weekly/latest
→ WeeklyReportController
→ athleteService.findById(athleteId, coachId) — Coach→Athlete ownership
→ WeeklyReportService.getLatestWeeklyReport(athleteId)
→ LoadReportRepository.findLatestByAthleteId(athleteId)
→ empty → throw WeeklyReportNotFoundException → 404
→ found → delegate to getWeeklyReport(athleteId, latestLoadReport.weekStartDate())
→ WeeklyReportWebMapper.domainToResponse(report)
→ ResponseEntity.ok(response)
```

### GET by period
```
GET /v1/athletes/{id}/reports/weekly?from=2026-04-28&to=2026-05-26
→ WeeklyReportController — @PastOrPresent on from and to, validate from <= to → 400
→ athleteService.findById(athleteId, coachId) — Coach→Athlete ownership
→ WeeklyReportService.getWeeklyReportsByPeriod(athleteId, from, to)

→ Iterate over every Monday in [from, to] inclusive:
     firstMonday = from.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
     while weekStartDate <= to:
         → getWeeklyReport(athleteId, weekStartDate)
           // always returns a report — zero-load weeks included
           // WeeklyReportNotFoundException caught and skipped silently
             (athlete has no data at all for this week)
         weekStartDate = weekStartDate.plusWeeks(1)

→ returns list — one entry per Monday in range, including zero-load weeks
  empty list only if athlete has no data at all across the entire period
→ ResponseEntity.ok(list)
```

Note: `from` and `to` do not need to be Mondays — iteration normalizes automatically.
Volume management (large date ranges) is client responsibility — no server-side pagination.

## Impact on existing code
- `WeeklyReportNotFoundException` → 404, extends `ResourceNotFoundException`
- `GlobalExceptionHandler` — already handles `ResourceNotFoundException`, no change needed
- `LoadReportService` injected in `WeeklyReportService` — for on-the-fly onTheFlyLoad only
- `AcwrReportService` NOT injected — ACWR computed directly via `AcwrCalculator.computeFromWeeklyLoads()`

## Decisions
- No persistence — WeeklyReport calculated on demand; source data already persisted
  (LoadReport, WeeklyWellness)
- No Redis cache — calculation lightweight (small list aggregation); AcwrReport cache
  not used here (stale on historical weeks); freshness guaranteed by DB reads
- ACWR computed from LoadReports via AcwrCalculator.computeFromWeeklyLoads() —
  weekly granularity consistent with WeeklyReport; no call to AcwrReportService
  (its cache is anchored on today, semantically wrong for historical weeks;
  would also cause double DB reads on cache miss)
- GET by period iterates every Monday in [from, to] — zero-load week (no sessions)
  is valid information (injury, rest); never skipped
- No `loadAvailable` flag — LoadReportService always returns a report; sessionCount=0
  is sufficient signal for the client
- `wellnessAvailable` drives INSUFFICIENT_DATA — wellness questionnaire not filled
  = insufficient data for correlation regardless of load quality
- Partial data returns 200 with INSUFFICIENT_DATA — better UX than 404;
  `wellnessAvailable` flag guides client display
- 404 only on GET by weekStartDate / GET latest when athlete has truly no data
  (zero-report onTheFlyLoad with updatedAt=null confirms no sessions ever recorded)
- GET by period never returns 404 — empty list is the correct response
- Future weekStartDate → 400 at HTTP boundary — invalid input, not missing resource
- WellnessAlert computed independently per indicator (difficulty, fatigue, motivation)
- CorrelationAlert is a single value — most significant cross-indicator signal
- WeeklyReportCalculator in domain/ — pure POJO, all business rules testable without Spring
- `Set<WellnessAlert>` in WeeklyReport record — `EnumSet.noneOf(WellnessAlert.class)`
  used internally in WeeklyReportCalculator, converted to `Set` before returning
- deltaX = null when no previous WeeklyWellness — null means no reference data;
  0 would be misleading (false signal of stability)
- Threshold constants in WeeklyReportCalculator as named `static final` —
  easy to adjust without hunting magic numbers
- weekStartDate included in the 4-week load window and 5-week wellness window —
  current week is valid (coach can consult in-progress week, consistent with TDD-007)
- CorrelationAlert conditions use week-over-week deltas (deltaFatigue, deltaMotivation)
  already computed in WeeklyReport — no separate trend computation needed
- INSUFFICIENT_DATA when deltas null (first week, no previous wellness) —
  insufficient context for meaningful correlation
- 404 condition: loadReports empty AND no wellness for weekStartDate —
  wellness checked before calling LoadReportService to avoid unnecessary DB read
- sessionCount == 0 is the signal for zero-load week — not updatedAt == null
  (on-the-fly reports always have updatedAt=null, making it a redundant check)
- AcwrReportService NOT used in WeeklyReportService — ACWR computed directly via
  AcwrCalculator.computeFromWeeklyLoads() from LoadReports already loaded;
  AcwrReportService cache is anchored on today, semantically wrong for historical weeks

## Test strategy

### WeeklyReportCalculatorTest — pure unit, no Spring
- nominal: 4 weeks load + 5 weeks wellness → correct report, all fields asserted,
  wellnessAvailable=true, correlationAlert != INSUFFICIENT_DATA
- zero-load week: loadReports contains current week with sessionCount=0, totalFosterLoad=0,
  wellness present → wellnessAvailable=true, sessionCount=0,
  correlationAlert != INSUFFICIENT_DATA (zero load does not trigger INSUFFICIENT_DATA)
- wellnessAvailable=false: no wellness entry for current weekStartDate →
  wellnessAvailable=false, correlationAlert=INSUFFICIENT_DATA,
  perceivedDifficulty=null, difficultyAlerts=empty Set
- zero-load + no wellness → wellnessAvailable=false, correlationAlert=INSUFFICIENT_DATA
- ABSOLUTE_LOW: motivation=2 → alert in motivationAlerts
- WEEK_OVER_WEEK: fatigue 4→2 → alert in fatigueAlerts
- TREND_DECLINING detected: motivation 5→5→4→4→3 over 5 weeks (slope ≈ -0.3) → alert
- TREND_DECLINING not triggered: motivation 5→5→5→5→4 (slope ≈ -0.2) → no alert
- delta=null when no previous wellness (single entry in lastFiveWellness)
- All CorrelationAlert cases covered with concrete data scenarios
- Boundary: acwr exactly 1.31 + fatigue rising → OVERLOAD_RISK
- Boundary: acwr exactly 1.3 + fatigue rising → NO_ALERT (1.3 is OK, not overload)
- Boundary: acwr exactly 1.03 + fatigue stable → NO_ALERT (threshold is strict >)
- Boundary: acwr exactly 1.04 + fatigue stable → GOOD_ADAPTATION
- INSUFFICIENT_DATA: delta null (single wellness entry, no previous week) → INSUFFICIENT_DATA
- All CorrelationAlert cases covered (OVERLOAD_RISK, GOOD_ADAPTATION, STABLE_LOAD_RISING_FATIGUE,
  POTENTIAL_OVERTRAINING, UNDERLOAD_DECLINING_MOTIVATION, NO_ALERT)

### WeeklyReportWebMapperTest — unit
- nominal: all fields mapped including Set<WellnessAlert> and CorrelationAlert
- nullable fields: perceivedDifficulty=null, deltaFatigue=null → mapped as null in response
- empty Set<WellnessAlert> → mapped as empty array in response (not null)

### WeeklyReportServiceImplTest — Mockito, mock all dependencies for isolation
- WeeklyReportCalculator is instantiated with `new` inside WeeklyReportServiceImpl
constructor (same pattern as LoadReportCalculator in LoadReportServiceImpl) —
not mockable, tests verify the final result instead.
- nominal: load found in DB + wellness found → report calculated and returned
- loadReports empty, no wellness → WeeklyReportNotFoundException,
  LoadReportService never called (verifyNoInteractions)
- loadReports empty, wellness present → LoadReportService called,
  zero-load report used (verify(loadReportService).findByAthleteIdAndWeekStartDate())
- load found with sessionCount=0 + wellness found →
  200 report, sessionCount=0, wellnessAvailable=true, correlationAlert != INSUFFICIENT_DATA
- load found, no wellness → INSUFFICIENT_DATA, wellnessAvailable=false
- latest: LoadReport found → delegates to getWeeklyReport → report returned
- latest: no LoadReport → WeeklyReportNotFoundException
- period: 3 Mondays in range, 2 have load + 1 zero-load → 3 reports, all returned
- period: athlete has no data at all → empty list
  (WeeklyReportNotFoundException caught and skipped for each Monday)

### WeeklyReportControllerTest — @WebMvcTest, extends SecurityMockControllerSupport
Mock all service dependencies for isolation (@MockitoBean).
- GET by weekStartDate — 200 nominal: wellnessAvailable=true, all fields asserted
- GET by weekStartDate — 200 zero-load: sessionCount=0, wellnessAvailable=true,
  correlationAlert=NO_ALERT
- GET by weekStartDate — 200 partial: wellnessAvailable=false,
  correlationAlert=INSUFFICIENT_DATA, perceivedDifficulty=null
- GET by weekStartDate — 400 future weekStartDate
- GET by weekStartDate — 404 no data at all
- GET latest — 200 nominal
- GET latest — 404 no LoadReport exists
- GET by period — 200 returns list including zero-load entries
- GET by period — 200 returns empty list when no data at all
- GET by period — 400 from > to

### WeeklyReportIT — @SpringBootTest + Testcontainers (PostgreSQL + Redis)
Events are synchronous in phase 2 (@EventListener without @Async) —
no wait or sleep needed between POST session and GET report.

**Scenario 1: nominal end-to-end**
1. Register coach + create athlete
2. POST 3 training sessions on current week
   (events → LoadReport persisted synchronously)
3. POST WeeklyWellness for that week
   (perceivedDifficulty=4, perceivedFatigue=3, motivation=5, weekStartDate=current Monday)
4. GET /v1/athletes/{id}/reports/weekly?weekStartDate={currentMonday}
5. Assert: 200, wellnessAvailable=true, sessionCount=3,
   correlationAlert != INSUFFICIENT_DATA,
   perceivedDifficulty=4, perceivedFatigue=3, motivation=5

**Scenario 2: zero-load week — no sessions, wellness present**
1. Register coach + create athlete
2. No training sessions
3. POST WeeklyWellness (perceivedDifficulty=3, perceivedFatigue=2, motivation=4)
4. GET /v1/athletes/{id}/reports/weekly?weekStartDate={currentMonday}
5. Assert: 200, wellnessAvailable=true, sessionCount=0, totalFosterLoad=0,
   perceivedDifficulty=3, correlationAlert != INSUFFICIENT_DATA

**Scenario 3: load only, no wellness**
1. Register coach + create athlete
2. POST 2 training sessions, no WeeklyWellness
3. GET /v1/athletes/{id}/reports/weekly?weekStartDate={currentMonday}
4. Assert: 200, wellnessAvailable=false, correlationAlert=INSUFFICIENT_DATA,
   perceivedDifficulty=null, perceivedFatigue=null, motivation=null

**Scenario 4: no data → 404**
1. Register coach + create athlete (no sessions, no wellness)
2. GET /v1/athletes/{id}/reports/weekly?weekStartDate={currentMonday}
3. Assert: 404, code=NOT_FOUND

## Out of scope
- Redis cache (not justified for this report type)
- Persistence (data already in load_report + weekly_wellness)
- @Async events (hexagonal phase)
- Kafka notifications (phase 3)
