# REFACTOR — LoadReportCalculator

## Context and motivation

Two problems identified during TDD-008 (WeeklyReport) design:

**Problem 1 — business logic in the wrong layer**
`TrainingSessionEventHandler.recalculateLoadReport()` computes `totalFosterLoad`
via an inline stream. This is domain logic (definition of a week's load) living
in a service handler.

**Problem 2 — ACWR business rules would be duplicated**
`WeeklyReportCalculator` (TDD-008) needs to compute ACWR anchored on `weekStartDate`
from `LoadReport` objects. `AcwrCalculator` computes ACWR from raw sessions anchored
on today. The business rules are identical (thresholds 0.8/1.3, reliability,
chronicLoad=0 → acwr=0.0) but inputs differ. Without refactoring, these rules
would be duplicated across two classes.

**Solution**
- Introduce `LoadReportCalculator` in `domain/` — single source of truth for
  sessions → weekly load computation
- Extract a package-private `computeFromWeeklyLoads()` method in `AcwrCalculator`
  containing the pure ACWR business rules, reusable by `WeeklyReportCalculator`

No behavior change — all existing tests must still pass after refactoring.

---

## New class: LoadReportCalculator (domain/)

```java
public class LoadReportCalculator {
    public LoadReport calculate(long athleteId, LocalDate weekStartDate,
                                List<TrainingSession> sessions) {
        int totalFosterLoad = sessions.stream()
            .mapToInt(TrainingSession::getFosterLoad)
            .sum();
        return new LoadReport(athleteId, weekStartDate,
                              totalFosterLoad, sessions.size(), LocalDateTime.now());
    }
}
```

Pure POJO, no Spring annotations. Instantiated with `new` where needed.
**Never called with an empty session list** — the caller is responsible for the
empty guard (delete path) before calling this method. See `TrainingSessionEventHandler`
below for the established pattern.

---

## Changes to AcwrCalculator (domain/)

### What changes
Extract the pure ACWR computation into a package-private method
`computeFromWeeklyLoads()`. The existing public method `calculate()` delegates
to it internally — its signature and behavior are unchanged.

### Target structure

```java
public class AcwrCalculator {

    private final LoadReportCalculator loadReportCalculator;

    public AcwrCalculator() {
        this.loadReportCalculator = new LoadReportCalculator();
    }

    // Public API — used by AcwrReportServiceImpl. Signature unchanged.
    public AcwrReport calculate(long athleteId, List<TrainingSession> sessions, LocalDate today) {
        // 1. Group sessions by weekStartDate:
        //    weekStartDate = session.getDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        // 2. For each group → loadReportCalculator.calculate(athleteId, weekStartDate, group)
        //    builds a LoadReport per week (non-empty groups only — empty weeks have no sessions)
        // 3. Build List<LoadReport> ordered current week first (sort by weekStartDate descending)
        //    current week = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        // 4. Delegate to computeFromWeeklyLoads(athleteId, today, loadReports)
    }

    // Package-private — used by WeeklyReportCalculator (TDD-008)
    // Single source of truth for ACWR business rules.
    // No visibility modifier = package-private by default. Do NOT add `public`.
    AcwrReport computeFromWeeklyLoads(
        long athleteId,
        LocalDate weekStartDate,      // used as calculatedAt in returned AcwrReport
        List<LoadReport> loadReports  // up to 4 weeks, current week first, descending
    ) {
        int acuteLoad = loadReports.isEmpty() ? 0 : loadReports.get(0).totalFosterLoad();

        long weeksOfData = loadReports.stream()
            .filter(r -> r.sessionCount() > 0)
            .count();

        double totalLoad = loadReports.stream()
            .mapToInt(LoadReport::totalFosterLoad)
            .sum();

        double chronicLoad = weeksOfData == 0 ? 0.0 : totalLoad / weeksOfData;
        boolean acwrReliable = weeksOfData >= 4;
        double acwr = chronicLoad == 0.0 ? 0.0 : acuteLoad / chronicLoad;
        AcwrAlert acwrAlert = AcwrAlert.from(acwr, acwrReliable);

        return new AcwrReport(athleteId, weekStartDate, acuteLoad, chronicLoad,
                              acwr, acwrAlert, (int) weeksOfData, acwrReliable);
    }
}
```

**Key rule preserved**: `chronicLoad == 0.0 → acwr = 0.0, acwrReliable = false, NO_DATA`
— identical to existing `calculate()` behavior.

---

## Changes to TrainingSessionEventHandler (service/)

### Current
```java
private void recalculateLoadReport(long athleteId, LocalDate sessionDate) {
    LocalDate weekStartDate = sessionDate.with(DayOfWeek.MONDAY);
    List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(
        athleteId, weekStartDate, weekStartDate.plusDays(6));

    if (sessions.isEmpty()) {
        loadReportRepository.deleteByAthleteIdAndWeekStartDate(athleteId, weekStartDate);
        return;
    }

    int totalFosterLoad = sessions.stream().mapToInt(TrainingSession::getFosterLoad).sum();
    loadReportRepository.save(new LoadReport(
        athleteId, weekStartDate, totalFosterLoad, sessions.size(), LocalDateTime.now()));
}
```

### Target
```java
private void recalculateLoadReport(long athleteId, LocalDate sessionDate) {
    LocalDate weekStartDate = sessionDate.with(DayOfWeek.MONDAY);
    List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(
        athleteId, weekStartDate, weekStartDate.plusDays(6));

    if (sessions.isEmpty()) {
        loadReportRepository.deleteByAthleteIdAndWeekStartDate(athleteId, weekStartDate);
        return;
    }

    loadReportRepository.save(
        loadReportCalculator.calculate(athleteId, weekStartDate, sessions));
}
```

`loadReportCalculator` instantiated with `new LoadReportCalculator()` in
`TrainingSessionEventHandler` constructor — same pattern as `AcwrCalculator`
in `AcwrReportServiceImpl`.

---

## Changes to AcwrReportServiceImpl (service/)

No change to the call site — `calculate(athleteId, sessions, today)` signature
is unchanged. `AcwrCalculator` manages its own `LoadReportCalculator` dependency
internally.

---

## How TDD-008 will use this

`WeeklyReportCalculator` will receive `AcwrCalculator` as a constructor parameter,
instantiated with `new` in `WeeklyReportServiceImpl`:

```java
// WeeklyReportServiceImpl constructor
AcwrCalculator acwrCalculator = new AcwrCalculator();
WeeklyReportCalculator calculator = new WeeklyReportCalculator(acwrCalculator);
```

`WeeklyReportCalculator.calculate()` will call `acwrCalculator.computeFromWeeklyLoads()`
directly with the `LoadReport` list already loaded — no extra DB access, ACWR
business rules reused without duplication.

---

## Files to create
- `domain/LoadReportCalculator.java`

## Files to modify
- `domain/AcwrCalculator.java` — add `LoadReportCalculator` dependency,
  extract `computeFromWeeklyLoads()`, refactor `calculate()` to delegate internally
- `service/TrainingSessionEventHandler.java` — inject `LoadReportCalculator`,
  simplify `recalculateLoadReport()`

## Files NOT to modify
- `service/AcwrReportServiceImpl.java` — call site unchanged
- All existing Flyway migrations — no schema change
- `pom.xml` — no new dependency

---

## Tests

### New: LoadReportCalculatorTest (domain/) — pure unit
- nominal: 3 sessions with known Foster loads → totalFosterLoad = sum, sessionCount = 3
- single session → totalFosterLoad = session.getFosterLoad(), sessionCount = 1

### New: coverage for computeFromWeeklyLoads() in AcwrCalculatorTest
- nominal: 4 LoadReports with sessionCount > 0 → acwrReliable=true, correct acwr
- weeksOfData < 4 → acwrReliable=false
- all loads = 0 (all sessionCount=0) → chronicLoad=0.0, acwr=0.0, acwrAlert=NO_DATA
- boundary: acwr exactly 0.8 → OK
- boundary: acwr exactly 1.3 → OK
- current week sessionCount=0 (acuteLoad=0), previous weeks have data →
  acwr=0.0, acwrReliable depends on weeksOfData

### Existing tests — verify still pass
- `AcwrCalculatorTest` — existing cases unchanged (public API unchanged)
- `TrainingSessionEventHandlerTest` — update mock: verify
  `loadReportCalculator.calculate()` called instead of inline stream
- All IT tests — no behavior change, must pass as-is

---

## Invariant: no behavior change
Pure refactoring. Run full test suite to verify:
```bash
./mvnw verify
```
