# TDD-008 — WeeklyReport

## Context
Complete weekly analysis report. Crosses LoadReport (last 4 weeks) and WeeklyWellness
(last 5 weeks) to compute ACWR, wellness alerts, and load/wellness correlations.
No persistence — calculated on demand from existing persisted data.
Both LoadReport AND WeeklyWellness required for full correlation —
INSUFFICIENT_DATA CorrelationAlert when either is missing for current week.

## No new persistence
No Flyway migration. No JPA entity. No new repository port.
Reads from existing `LoadReportRepository`, `WeeklyWellnessRepository`,
`LoadReportService`, and `AcwrReportService`.

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
- 5-week window: 4 weeks misses subtle decline (5→5→4→4→4→3 gives slope -0.2
  over 4 weeks, not detected); 6 weeks dilutes recent signal (same scenario gives
  slope -0.28, also not detected); 5 weeks gives slope exactly -0.3, detected at threshold
- Example detected: 5→4→4→3→3 (slope ≈ -0.5) ✓
- Example detected: 5→5→4→4→4→3 (slope ≈ -0.3, at threshold) ✓
- Example not triggered: 5→5→5→5→4 (slope ≈ -0.2, normal minor variation) ✓
These thresholds are defined as named constants in `WeeklyReportCalculator`
for easy future adjustment.

### CorrelationAlert enum (domain/)
```java
public enum CorrelationAlert {
    NO_ALERT,                        // all metrics nominal
    INSUFFICIENT_DATA,               // LoadReport or WeeklyWellness missing for current week
    STABLE_LOAD_RISING_FATIGUE,      // load stable, fatigue trending up → external factors
    OVERLOAD_RISK,                   // ACWR > 1.3 AND fatigue trending up
    GOOD_ADAPTATION,                 // ACWR rising AND fatigue stable or improving
    UNDERLOAD_DECLINING_MOTIVATION,  // ACWR < 0.8 AND motivation declining
    POTENTIAL_OVERTRAINING           // load stable AND motivation declining AND fatigue rising
}
```

### WeeklyReportCalculator (domain/)
Pure POJO, no Spring annotations. Anticipates hexagonal Domain Service extraction.
All threshold constants defined here as `static final` fields.

```java
public class WeeklyReportCalculator {
    static final int ABSOLUTE_LOW_THRESHOLD = 2;
    static final int WEEK_OVER_WEEK_THRESHOLD = -2;
    static final double TREND_DECLINING_SLOPE_THRESHOLD = -0.3;
    static final int TREND_DECLINING_WINDOW_WEEKS = 5;

    public WeeklyReport calculate(
        long athleteId,
        LocalDate weekStartDate,
        List<LoadReport> lastFourLoadReports,   // current week first, descending
        List<WeeklyWellness> lastFiveWellness,  // current week first, descending
        AcwrReport acwrReport
    );
}
```

Returns `WeeklyReport` record.
If `lastFourLoadReports` empty OR current week's `WeeklyWellness` absent
→ `correlationAlert = INSUFFICIENT_DATA`.
Delta fields = null when no previous WeeklyWellness exists (first week).

### WeeklyReport (domain/)
Java record.

```java
public record WeeklyReport(
    long athleteId,
    LocalDate weekStartDate,
    // Availability flags
    boolean loadAvailable,
    boolean wellnessAvailable,
    // Load data (from LoadReport current week)
    int totalFosterLoad,
    int sessionCount,
    // ACWR data (from AcwrReport)
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
    // Wellness alerts per indicator
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
- `AcwrReportService.getAcwrReport()` — already exists and cached (TDD-006)
- `LoadReportService.findByAthleteIdAndWeekStartDate()` — already exists (TDD-007),
  encapsulates on-the-fly calculation: DB hit → sessions fallback → zero report if no sessions

## API Contract

### Endpoints
| Method | URL | Query params | Response | Success | Errors |
|---|---|---|---|---|---|
| GET | /v1/athletes/{id}/reports/weekly | `weekStartDate` (required) | `WeeklyReportResponse` | 200 | 400, 401, 404 |
| GET | /v1/athletes/{id}/reports/weekly/latest | — | `WeeklyReportResponse` | 200 | 401, 404 |
| GET | /v1/athletes/{id}/reports/weekly | `from` (required), `to` (optional) | `List<WeeklyReportResponse>` | 200 | 400, 401, 404 |

`weekStartDate`, `from`, `to` : `@PastOrPresent` — future weeks rejected at HTTP boundary → 400.

The two `GET /v1/athletes/{id}/reports/weekly` endpoints are implemented as two separate
controller methods discriminated by Spring MVC params (same pattern as LoadReportController):
- `@GetMapping(params = "weekStartDate")` → single WeeklyReportResponse
- `@GetMapping(params = "from")` → List<WeeklyReportResponse>

### DTO
`WeeklyReportResponse` — Java record (same convention as all response DTOs).
Mirrors `WeeklyReport` record fields exactly. Nullable fields use wrapper types
(`Integer`, `Double`). `Set<WellnessAlert>` and `CorrelationAlert` map directly
(enum serialization by Jackson).

### Error format
```json
[{ "code": "NOT_FOUND", "field": null, "message": "No weekly report available for athlete 3 on week 2026-05-19" }]
```
Note: 404 only when athlete has no sessions AND no LoadReport at all for that week.
Partial data (load without wellness or vice versa) returns 200 with `INSUFFICIENT_DATA`
correlation alert and availability flags.

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
     → probe = LoadReportService.findByAthleteIdAndWeekStartDate(athleteId, weekStartDate)
       // probe returns zero report (updatedAt=null) if athlete has no sessions at all
       → if probe.updatedAt() == null → throw WeeklyReportNotFoundException
       // else: sessions exist but not yet persisted as LoadReport (edge case)
       → loadReports = List.of(probe)

→ wellness = WeeklyWellnessRepository.findByAthleteIdAndPeriod(
     athleteId,
     weekStartDate.minusWeeks(4),   // 5 weeks total, weekStartDate inclusive
     weekStartDate)

→ acwrReport = AcwrReportService.getAcwrReport(athleteId)

→ WeeklyReportCalculator.calculate(athleteId, weekStartDate, loadReports, wellness, acwrReport)
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
GET /v1/athletes/{id}/reports/weekly?from=2026-04-01&to=2026-05-26
→ WeeklyReportController — @PastOrPresent on from and to, validate from <= to → 400
→ athleteService.findById(athleteId, coachId) — Coach→Athlete ownership
→ WeeklyReportService.getWeeklyReportsByPeriod(athleteId, from, to)
→ for each Monday in [from, to]:
     → getWeeklyReport(athleteId, weekStartDate)  // reuses single-week logic
→ returns list (empty list if no data in range — no 404)
→ ResponseEntity.ok(list)
```

Note: `from` and `to` must be Mondays — validated at domain level via
`WeeklyReportCalculator` invariants. If not a Monday → 400 DomainValidationException.

## Impact on existing code
- `WeeklyReportNotFoundException` → 404, extends `ResourceNotFoundException`
- `GlobalExceptionHandler` — already handles `ResourceNotFoundException`, no change needed
- `AcwrReportService` injected in `WeeklyReportService` — cross-service dependency,
  acceptable compromise until hexagonal Use Cases refactoring in phase 2
- `LoadReportService` injected in `WeeklyReportService` — for on-the-fly probe only

## Decisions
- No persistence — WeeklyReport calculated on demand; source data already persisted
  (LoadReport, WeeklyWellness) or cached (AcwrReport)
- No Redis cache — calculation is lightweight (small list aggregation); data freshness
  guaranteed by source caches (AcwrReport TTL 7 days) and DB (LoadReport, WeeklyWellness)
- Partial data returns 200 with INSUFFICIENT_DATA — better UX than 404 when some
  data exists; `loadAvailable` and `wellnessAvailable` flags guide the client display
- 404 only when athlete has absolutely no load data for requested week
  (zero-report probe confirms no sessions exist)
- Future weekStartDate → 400 at HTTP boundary — invalid input, not missing resource
- ACWR sourced from AcwrReportService (already cached) — no duplication of calculation logic
- WellnessAlert computed independently per indicator (difficulty, fatigue, motivation)
- CorrelationAlert is a single value — most significant cross-indicator signal
- WeeklyReportCalculator in domain/ — pure POJO, all business rules testable without Spring
- `Set<WellnessAlert>` in WeeklyReport record (interface type, Jackson-serializable).
  `EnumSet.noneOf(WellnessAlert.class)` used internally in WeeklyReportCalculator —
  performant bitfield implementation, converted to `Set` before returning
- deltaX = null when no previous WeeklyWellness — null means no reference data;
  0 would be misleading (false signal of stability)
- Threshold constants in WeeklyReportCalculator as named `static final` —
  easy to adjust without hunting magic numbers across codebase
- weekStartDate included in the 4-week load window and 5-week wellness window —
  current week is valid (coach can consult in-progress week, consistent with TDD-007)

### Alert threshold documentation
See WellnessAlert enum section above for full rationale on:
- ABSOLUTE_LOW ≤ 2 on 1-5 scale
- WEEK_OVER_WEEK delta ≤ -2
- TREND_DECLINING slope ≤ -0.3/week over 5 weeks (calibrated — see examples above)

## Test strategy

### WeeklyReportCalculatorTest — pure unit, no Spring
- nominal: 4 weeks load + 5 weeks wellness → correct report, all fields asserted
- ABSOLUTE_LOW: motivation = 2 → alert in motivationAlerts
- WEEK_OVER_WEEK: fatigue 4→2 → alert in fatigueAlerts
- TREND_DECLINING detected: motivation 5→5→4→4→3 over 5 weeks (slope ≈ -0.3) → alert
- TREND_DECLINING not triggered: motivation 5→5→5→5→4 (slope ≈ -0.2) → no alert
- delta = null when no previous wellness (first week, single entry in lastFiveWellness)
- INSUFFICIENT_DATA: loadReports empty → correlationAlert = INSUFFICIENT_DATA
- INSUFFICIENT_DATA: load present, no current week wellness → correlationAlert = INSUFFICIENT_DATA
- All CorrelationAlert cases covered with concrete data scenarios
- Boundary: acwr exactly 1.3 + fatigue rising → OVERLOAD_RISK

### WeeklyReportWebMapperTest — unit
- nominal: all fields mapped including Set<WellnessAlert> and CorrelationAlert
- nullable fields: perceivedDifficulty=null, deltaFatigue=null → mapped as null in response

### WeeklyReportServiceImplTest — Mockito, mock all dependencies for isolation
- nominal: load found in DB + wellness found → report calculated and returned
- loadReports empty, probe has sessions (updatedAt not null) → List.of(probe) used, report returned
- loadReports empty, probe is zero report (updatedAt null) → WeeklyReportNotFoundException thrown
- load found, no wellness → partial report returned with INSUFFICIENT_DATA and wellnessAvailable=false
- latest: LoadReport found → delegates to getWeeklyReport → report returned
- latest: no LoadReport → WeeklyReportNotFoundException thrown

### WeeklyReportControllerTest — @WebMvcTest, extends SecurityMockControllerSupport
Mock all service dependencies for isolation (@MockitoBean).
- GET by weekStartDate — 200 nominal: loadAvailable=true, wellnessAvailable=true,
  all response fields asserted (sessionCount, correlationAlert, perceivedDifficulty, etc.)
- GET by weekStartDate — 200 partial: INSUFFICIENT_DATA, wellnessAvailable=false,
  perceivedDifficulty=null
- GET by weekStartDate — 400 future weekStartDate (@PastOrPresent violated)
- GET by weekStartDate — 404 no data at all (WeeklyReportNotFoundException)
- GET latest — 200 nominal
- GET latest — 404 no LoadReport exists
- GET by period — 200 returns list

### WeeklyReportIT — @SpringBootTest + Testcontainers (PostgreSQL + Redis)

**Scenario 1: nominal end-to-end**
1. Register coach + create athlete (JWT auth required for all calls)
2. POST 3 training sessions on current week for that athlete
   (TrainingSessionCreatedEvent → TrainingSessionEventHandler → LoadReport persisted)
3. POST one WeeklyWellness for that week
   (perceivedDifficulty=4, perceivedFatigue=3, motivation=5, weekStartDate=current Monday)
4. GET /v1/athletes/{id}/reports/weekly?weekStartDate={currentMonday}
5. Assert: HTTP 200, loadAvailable=true, wellnessAvailable=true,
   sessionCount=3, correlationAlert != INSUFFICIENT_DATA,
   perceivedDifficulty=4, perceivedFatigue=3, motivation=5

**Scenario 2: partial data — load only, no wellness**
1. Register coach + create athlete
2. POST 2 training sessions, no WeeklyWellness created
3. GET /v1/athletes/{id}/reports/weekly?weekStartDate={currentMonday}
4. Assert: HTTP 200, loadAvailable=true, wellnessAvailable=false,
   correlationAlert=INSUFFICIENT_DATA, perceivedDifficulty=null,
   perceivedFatigue=null, motivation=null

**Scenario 3: no data → 404**
1. Register coach + create athlete (no sessions, no wellness)
2. GET /v1/athletes/{id}/reports/weekly?weekStartDate={currentMonday}
3. Assert: HTTP 404, error code NOT_FOUND

## Out of scope
- Redis cache (not justified for this report type)
- Persistence (data already in load_report + weekly_wellness)
- @Async events (hexagonal phase)
- Kafka notifications (phase 3)