# TDD-009 — Use Cases (Hexagonal Application Layer)

## Context
Replaces all God Services (`*ServiceImpl` in `service/`) with fine-grained
Use Cases in `application/`. Each Use Case has a single `execute()` method,
is an interface implemented by a `@Service` class in the same package.
Controllers inject the interface, not the implementation.

This is ticket #118. Prerequisites: ADR-001, ADR-002, tickets #116 complete.

## Conventions

### Use Case interface
```java
public interface CreateAthleteUseCase {
    Athlete execute(Athlete athlete, long coachId);
}
```

### Use Case implementation
```java
@Service
public class CreateAthleteUseCaseImpl implements CreateAthleteUseCase {
    // constructor injection only
    // no @Autowired
    // @Transactional(readOnly = true) at class level if reads involved
    // @Transactional on write execute() methods
}
```

### Ownership
Controllers resolve coachId once via `authenticatedCoachResolver.resolve()`
and pass it to `execute()`. Ownership checks (Coach→Athlete, Athlete→Session,
Athlete→Wellness) remain inside the Use Case — never in the controller.
Controllers no longer call any service directly for ownership verification.

### Package structure
application/
├── port/
│   ├── in/      ← Use Case interfaces
│   └── out/     ← TrainingSessionEventPort, AcwrCachePort, PasswordEncoderPort
├── athlete/     ← athlete Use Case implementations
├── auth/        ← auth Use Case implementations
├── coach/       ← coach Use Case implementations
├── training/    ← training Use Case implementations
└── wellness/    ← wellness Use Case implementations
Use Case interfaces go in `application/port/in/`.
Use Case implementations go in `application/{feature}/`.

### Testing
- One test class per Use Case implementation
- Package mirrors production: `src/test/.../application/{feature}/`
- Mockito only — no Spring context
- Pattern: given/when/then, one scenario per test
- Migrate scenarios from `service/*ServiceImplTest` — same coverage, adapted class names and packages
- Delete `service/*ServiceImplTest` classes once their Use Case counterpart is created and green
- Controller tests: replace `@MockitoBean *Service` with
  `@MockitoBean *UseCase` — behavior unchanged

---

## Bounded Context: auth

### RegisterCoachUseCase
**Interface:** `application/port/in/RegisterCoachUseCase.java`
```java
CoachAuthData execute(CoachCredentials credentials);
```
**Implementation:** `application/auth/RegisterCoachUseCaseImpl.java`

**Orchestration:**
1. `authRepository.existsByEmail(credentials.email())` → `EmailAlreadyExistsException` if true
2. `passwordEncoderPort.encode(credentials.rawPassword())`
3. `authRepository.register(credentials, hashedPassword)` → `CoachAuthData`

**Dependencies:** `AuthRepository`, `PasswordEncoderPort`

**Tests:** reuse `AuthServiceTest.register_*` scenarios

---

### LoginUseCase
**Interface:** `application/port/in/LoginUseCase.java`
```java
CoachAuthData execute(String email, String rawPassword);
```
**Implementation:** `application/auth/LoginUseCaseImpl.java`

**Orchestration:**
1. `authRepository.findByEmail(email)` → `InvalidCredentialsException` if empty
2. `passwordEncoderPort.matches(rawPassword, hashedPassword)` → `InvalidCredentialsException` if false
3. return `CoachAuthData`

**Dependencies:** `AuthRepository`, `PasswordEncoderPort`

**Tests:** reuse `AuthServiceTest.login_*` scenarios

---

## Bounded Context: coach

### GetCoachUseCase
**Interface:** `application/port/in/GetCoachUseCase.java`
```java
Coach execute(long coachId);
```
**Implementation:** `application/coach/GetCoachUseCaseImpl.java`

**Orchestration:**
1. `coachRepository.findById(coachId)` → `CoachNotFoundException` if empty

**Dependencies:** `CoachRepository`

---

### UpdateCoachUseCase
**Interface:** `application/port/in/UpdateCoachUseCase.java`
```java
void execute(long coachId, String name);
```
**Implementation:** `application/coach/UpdateCoachUseCaseImpl.java`

**Orchestration:**
1. `coachRepository.existsById(coachId)` → `CoachNotFoundException` if false
2. `coachRepository.updateName(coachId, name)`

**Dependencies:** `CoachRepository`

---

### DeleteCoachUseCase
**Interface:** `application/port/in/DeleteCoachUseCase.java`
```java
void execute(long coachId);
```
**Implementation:** `application/coach/DeleteCoachUseCaseImpl.java`

**Orchestration:**
1. `coachRepository.existsById(coachId)` → `CoachNotFoundException` if false
2. `coachRepository.deleteById(coachId)`

**Dependencies:** `CoachRepository`

---

## Bounded Context: athlete

### CreateAthleteUseCase
**Interface:** `application/port/in/CreateAthleteUseCase.java`
```java
Athlete execute(Athlete athlete);
```
**Note:** `athlete.coachId` is already set by the controller before calling execute().

**Implementation:** `application/athlete/CreateAthleteUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.save(athlete)` → `Athlete`

**Ownership note:** No ownership check in this Use Case — `athlete.coachId` is
injected by the controller from the JWT token before calling execute(). The coach
can only create athletes for themselves by construction. This is the only Use Case
where ownership is guaranteed at the boundary, not verified inside the Use Case.

**Dependencies:** `AthleteRepository`

**Transactions:** `@Transactional` on execute()

---

### GetAthleteUseCase
**Interface:** `application/port/in/GetAthleteUseCase.java`
```java
Athlete execute(long athleteId, long coachId);
```
**Implementation:** `application/athlete/GetAthleteUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.coachId() != coachId` → `AthleteNotFoundException` (ownership — 404 not 403)

**Dependencies:** `AthleteRepository`

---

### GetAthletesByCoachUseCase
**Interface:** `application/port/in/GetAthletesByCoachUseCase.java`
```java
PageResult<Athlete> execute(long coachId, int pageNumber, int pageSize);
```
**Implementation:** `application/athlete/GetAthletesByCoachUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findAllByCoachId(coachId, pageNumber, pageSize)` → `PageResult<Athlete>`

**Dependencies:** `AthleteRepository`

---

### UpdateAthleteUseCase
**Interface:** `application/port/in/UpdateAthleteUseCase.java`
```java
Athlete execute(Athlete athlete, long coachId);
```
**Implementation:** `application/athlete/UpdateAthleteUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athlete.getId())` → `AthleteNotFoundException` if empty
2. `existing.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `athleteRepository.save(athlete)` → `Athlete`

**Dependencies:** `AthleteRepository`

**Transactions:** `@Transactional` on execute()

---

### DeleteAthleteUseCase
**Interface:** `application/port/in/DeleteAthleteUseCase.java`
```java
void execute(long athleteId, long coachId);
```
**Implementation:** `application/athlete/DeleteAthleteUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `athleteRepository.deleteById(athleteId)`

**Dependencies:** `AthleteRepository`

**Transactions:** `@Transactional` on execute()

---

## Bounded Context: training

### CreateTrainingSessionUseCase
**Interface:** `application/port/in/CreateTrainingSessionUseCase.java`
```java
TrainingSession execute(TrainingSession session, long coachId);
```
**Implementation:** `application/training/CreateTrainingSessionUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(session.getAthleteId())` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `trainingSessionRepository.save(session)` → `TrainingSession`
4. `trainingSessionEventPort.sessionCreated(saved.getAthleteId(), saved.getDate())`

**Dependencies:** `AthleteRepository`, `TrainingSessionRepository`,
`TrainingSessionEventPort`

**Transactions:** `@Transactional` on execute()

---

### GetTrainingSessionUseCase
**Interface:** `application/port/in/GetTrainingSessionUseCase.java`
```java
TrainingSession execute(long sessionId, long athleteId, long coachId);
```
**Implementation:** `application/training/GetTrainingSessionUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `trainingSessionRepository.findById(sessionId)` → `TrainingSessionNotFoundException` if empty
4. `session.getAthleteId() != athleteId` → `TrainingSessionNotFoundException` (ownership)

**Dependencies:** `AthleteRepository`, `TrainingSessionRepository`

---

### GetTrainingSessionsByPeriodUseCase
**Interface:** `application/port/in/GetTrainingSessionsByPeriodUseCase.java`
```java
List<TrainingSession> execute(long athleteId, long coachId, LocalDate from, LocalDate to);
```
**Implementation:** `application/training/GetTrainingSessionsByPeriodUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `trainingSessionRepository.findByAthleteIdAndPeriod(athleteId, from, to)`

**Dependencies:** `AthleteRepository`, `TrainingSessionRepository`

---

### UpdateTrainingSessionUseCase
**Interface:** `application/port/in/UpdateTrainingSessionUseCase.java`
```java
TrainingSession execute(TrainingSession session, long coachId);
```
**Implementation:** `application/training/UpdateTrainingSessionUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(session.getAthleteId())` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `trainingSessionRepository.findById(session.getId())` → `TrainingSessionNotFoundException` if empty
4. `existing.getAthleteId() != session.getAthleteId()` → `TrainingSessionNotFoundException` (ownership)
5. `trainingSessionRepository.save(session)` → `TrainingSession`
6. `trainingSessionEventPort.sessionUpdated(updated.getAthleteId(), updated.getDate())`

**Dependencies:** `AthleteRepository`, `TrainingSessionRepository`,
`TrainingSessionEventPort`

**Transactions:** `@Transactional` on execute()

---

### DeleteTrainingSessionUseCase
**Interface:** `application/port/in/DeleteTrainingSessionUseCase.java`
```java
void execute(long sessionId, long athleteId, long coachId);
```
**Implementation:** `application/training/DeleteTrainingSessionUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `trainingSessionRepository.findById(sessionId)` → `TrainingSessionNotFoundException` if empty
4. `existing.getAthleteId() != athleteId` → `TrainingSessionNotFoundException` (ownership)
5. `trainingSessionRepository.deleteById(sessionId)`
6. `trainingSessionEventPort.sessionDeleted(athleteId, existing.getDate())`

**Dependencies:** `AthleteRepository`, `TrainingSessionRepository`,
`TrainingSessionEventPort`

**Transactions:** `@Transactional` on execute()

---

### GetLoadReportByWeekUseCase
**Interface:** `application/port/in/GetLoadReportByWeekUseCase.java`
```java
LoadReport execute(long athleteId, long coachId, LocalDate weekStartDate);
```
**Implementation:** `application/training/GetLoadReportByWeekUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, weekStartDate)`
4. if present → return
5. if absent → fetch sessions via `trainingSessionRepository.findByAthleteIdAndPeriod(
   athleteId, weekStartDate, weekStartDate.plusDays(6))`
6. `loadReportCalculator.calculate(athleteId, weekStartDate, sessions,
   sessions.isEmpty() ? null : LocalDateTime.now())`

**Note:** `loadReportCalculator` instantiated with `new LoadReportCalculator()`
in constructor.

**Dependencies:** `AthleteRepository`, `LoadReportRepository`,
`TrainingSessionRepository`

---

### GetLatestLoadReportUseCase
**Interface:** `application/port/in/GetLatestLoadReportUseCase.java`
```java
LoadReport execute(long athleteId, long coachId);
```
**Implementation:** `application/training/GetLatestLoadReportUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `loadReportRepository.findLatestByAthleteId(athleteId)` → `LoadReportNotFoundException` if empty

**Dependencies:** `AthleteRepository`, `LoadReportRepository`

---

### GetLoadReportsByPeriodUseCase
**Interface:** `application/port/in/GetLoadReportsByPeriodUseCase.java`
```java
List<LoadReport> execute(long athleteId, long coachId, LocalDate from, LocalDate to);
```
**Implementation:** `application/training/GetLoadReportsByPeriodUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `loadReportRepository.findByAthleteIdAndWeekStartDateBetween(athleteId, from, to)`

**Dependencies:** `AthleteRepository`, `LoadReportRepository`

---

### GetAcwrReportUseCase
**Interface:** `application/port/in/GetAcwrReportUseCase.java`
```java
AcwrReport execute(long athleteId, long coachId);
```
**Implementation:** `application/training/GetAcwrReportUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `acwrCachePort.get(athleteId)`
4. if present → return cached report
5. if absent → `trainingSessionRepository.findByAthleteIdAndPeriod(
   athleteId, today.minusDays(27), today)`
6. `acwrCalculator.calculate(athleteId, sessions, today)`
7. `acwrCachePort.put(athleteId, report)`
8. return report

**Note:** `acwrCalculator` instantiated with `new AcwrCalculator()` in constructor.
`today = LocalDate.now()`

**Dependencies:** `AthleteRepository`, `TrainingSessionRepository`, `AcwrCachePort`

---

## Bounded Context: wellness

### CreateWeeklyWellnessUseCase
**Interface:** `application/port/in/CreateWeeklyWellnessUseCase.java`
```java
WeeklyWellness execute(WeeklyWellness wellness, long coachId);
```
**Implementation:** `application/wellness/CreateWeeklyWellnessUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(wellness.getAthleteId())` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `weeklyWellnessRepository.existsByAthleteIdAndWeekStartDate(
   wellness.getAthleteId(), wellness.getWeekStartDate())` → `WeeklyWellnessAlreadyExistsException` if true
4. `weeklyWellnessRepository.save(wellness)` → `WeeklyWellness`

**Dependencies:** `AthleteRepository`, `WeeklyWellnessRepository`

**Transactions:** `@Transactional` on execute()

---

### GetWeeklyWellnessUseCase
**Interface:** `application/port/in/GetWeeklyWellnessUseCase.java`
```java
WeeklyWellness execute(long wellnessId, long athleteId, long coachId);
```
**Implementation:** `application/wellness/GetWeeklyWellnessUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `weeklyWellnessRepository.findById(wellnessId)` → `WeeklyWellnessNotFoundException` if empty
4. `wellness.getAthleteId() != athleteId` → `WeeklyWellnessNotFoundException` (ownership)

**Dependencies:** `AthleteRepository`, `WeeklyWellnessRepository`

---

### GetWeeklyWellnessByWeekUseCase
**Interface:** `application/port/in/GetWeeklyWellnessByWeekUseCase.java`
```java
WeeklyWellness execute(long athleteId, long coachId, LocalDate weekStartDate);
```
**Implementation:** `application/wellness/GetWeeklyWellnessByWeekUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `weeklyWellnessRepository.findByAthleteIdAndWeekStartDate(athleteId, weekStartDate)`
   → `WeeklyWellnessNotFoundException` if empty

**Dependencies:** `AthleteRepository`, `WeeklyWellnessRepository`

---

### GetLatestWeeklyWellnessUseCase
**Interface:** `application/port/in/GetLatestWeeklyWellnessUseCase.java`
```java
WeeklyWellness execute(long athleteId, long coachId);
```
**Implementation:** `application/wellness/GetLatestWeeklyWellnessUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `weeklyWellnessRepository.findLatestByAthleteId(athleteId)`
   → `WeeklyWellnessNotFoundException` if empty

**Note:** `WeeklyWellnessRepository` needs a new port method:
```java
Optional<WeeklyWellness> findLatestByAthleteId(long athleteId);
```
Add to `WeeklyWellnessRepository` interface and `WeeklyWellnessJpaAdapter`.
JPA derived method: `findTopByAthleteIdOrderByWeekStartDateDesc(long athleteId)`

**Dependencies:** `AthleteRepository`, `WeeklyWellnessRepository`

---

### GetWeeklyWellnessesByPeriodUseCase
**Interface:** `application/port/in/GetWeeklyWellnessesByPeriodUseCase.java`
```java
List<WeeklyWellness> execute(long athleteId, long coachId, LocalDate from, LocalDate to);
```
**Implementation:** `application/wellness/GetWeeklyWellnessesByPeriodUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `weeklyWellnessRepository.findByAthleteIdAndPeriod(athleteId, from, to)`

**Dependencies:** `AthleteRepository`, `WeeklyWellnessRepository`

---

### UpdateWeeklyWellnessUseCase
**Interface:** `application/port/in/UpdateWeeklyWellnessUseCase.java`
```java
WeeklyWellness execute(WeeklyWellness wellness, long coachId);
```
**Implementation:** `application/wellness/UpdateWeeklyWellnessUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(wellness.getAthleteId())` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `weeklyWellnessRepository.findById(wellness.getId())` → `WeeklyWellnessNotFoundException` if empty
4. `existing.getAthleteId() != wellness.getAthleteId()` → `WeeklyWellnessNotFoundException` (ownership)
5. if `wellness.getWeekStartDate()` changed → `weeklyWellnessRepository
   .existsByAthleteIdAndWeekStartDate()` → `WeeklyWellnessAlreadyExistsException` if true
6. `weeklyWellnessRepository.save(wellness)` → `WeeklyWellness`

**Dependencies:** `AthleteRepository`, `WeeklyWellnessRepository`

**Transactions:** `@Transactional` on execute()

---

### DeleteWeeklyWellnessUseCase
**Interface:** `application/port/in/DeleteWeeklyWellnessUseCase.java`
```java
void execute(long wellnessId, long athleteId, long coachId);
```
**Implementation:** `application/wellness/DeleteWeeklyWellnessUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `weeklyWellnessRepository.findById(wellnessId)` → `WeeklyWellnessNotFoundException` if empty
4. `wellness.getAthleteId() != athleteId` → `WeeklyWellnessNotFoundException` (ownership)
5. `weeklyWellnessRepository.deleteById(wellnessId)`

**Dependencies:** `AthleteRepository`, `WeeklyWellnessRepository`

**Transactions:** `@Transactional` on execute()

---

### GetWeeklyReportByWeekUseCase
**Interface:** `application/port/in/GetWeeklyReportByWeekUseCase.java`
```java
WeeklyReport execute(long athleteId, long coachId, LocalDate weekStartDate);
```
**Implementation:** `application/wellness/GetWeeklyReportByWeekUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. Fetch load reports: `loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
   athleteId, weekStartDate.minusWeeks(3), weekStartDate)` sorted descending
4. Fetch wellness: `weeklyWellnessRepository.findByAthleteIdAndPeriod(
   athleteId, weekStartDate.minusWeeks(4), weekStartDate)` sorted descending
5. if loadReports empty:
   - if no wellness for this week → `WeeklyReportNotFoundException`
   - else → compute on-the-fly: fetch sessions via
     `trainingSessionRepository.findByAthleteIdAndPeriod(
     athleteId, weekStartDate, weekStartDate.plusDays(6))`
     → `loadReportCalculator.calculate(athleteId, weekStartDate, sessions, null)`
     → loadReports = List.of(onTheFlyReport)
6. `weeklyReportCalculator.calculate(athleteId, weekStartDate, loadReports, wellness)`

**Note:** `loadReportCalculator` instantiated with `new LoadReportCalculator()`.
`weeklyReportCalculator` instantiated with `new WeeklyReportCalculator(new AcwrCalculator())`.

**Dependencies:** `AthleteRepository`, `LoadReportRepository`,
`WeeklyWellnessRepository`, `TrainingSessionRepository`

---

### GetLatestWeeklyReportUseCase
**Interface:** `application/port/in/GetLatestWeeklyReportUseCase.java`
```java
WeeklyReport execute(long athleteId, long coachId);
```
**Implementation:** `application/wellness/GetLatestWeeklyReportUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. `loadReportRepository.findLatestByAthleteId(athleteId)`
   → `WeeklyReportNotFoundException` if empty
4. delegate to `GetWeeklyReportByWeekUseCase.execute(athleteId, coachId,
   latest.weekStartDate())`

**Note:** `GetLatestWeeklyReportUseCase` injects `GetWeeklyReportByWeekUseCase`
— this is an intentional exception to the no-use-case-calls-use-case rule.
The alternative (duplicating the full orchestration) is worse. Documented here
explicitly.

**Dependencies:** `AthleteRepository`, `LoadReportRepository`,
`GetWeeklyReportByWeekUseCase`

---

### GetWeeklyReportsByPeriodUseCase
**Interface:** `application/port/in/GetWeeklyReportsByPeriodUseCase.java`
```java
List<WeeklyReport> execute(long athleteId, long coachId, LocalDate from, LocalDate to);
```
**Implementation:** `application/wellness/GetWeeklyReportsByPeriodUseCaseImpl.java`

**Orchestration:**
1. `athleteRepository.findById(athleteId)` → `AthleteNotFoundException` if empty
2. `athlete.getCoachId() != coachId` → `AthleteNotFoundException` (ownership)
3. Iterate Mondays from `from` to `to`:
   - call `GetWeeklyReportByWeekUseCase.execute(athleteId, coachId, monday)`
   - catch `WeeklyReportNotFoundException` → skip week
4. return list

**Note:** Same intentional exception — delegates to `GetWeeklyReportByWeekUseCase`.

**Dependencies:** `AthleteRepository`, `GetWeeklyReportByWeekUseCase`

---

## Execution order

Claude Code must implement in this sequence to keep the project compiling
at each step:

1. Add `findLatestByAthleteId` to `WeeklyWellnessRepository` + `WeeklyWellnessJpaAdapter`
2. Create all Use Case interfaces in `application/port/in/`
3. Create all Use Case implementations in `application/{feature}/`
   — start with auth, then coach, athlete, training, wellness
   — do NOT delete any service class yet
4. Update all controllers to inject Use Cases instead of Services
5. Update all controller tests (@MockitoBean targets)
6. Create all Use Case tests in `application/{feature}/` test packages
7. Run ./mvnw test — all tests must pass
8. Delete all *Service interfaces and *ServiceImpl classes in service/
   — do NOT delete the service/ package itself (ticket #119)
9. Run ./mvnw test again — confirm still green
10. Update ArchUnit: service_annotations_should_be_in_service →
    resideInAnyPackage("..service..", "..application..")

## Controllers update

Each controller replaces its injected `*Service` with the corresponding
Use Case(s). The controller resolves `coachId` once per method via
`authenticatedCoachResolver.resolve().id()` and passes it to `execute()`.

Example — `AthleteController.create()`:
```java
// Before
AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
Athlete athlete = athleteWebMapper.requestToDomain(request, coach.id());
Athlete saved = athleteService.save(athlete);

// After
AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
Athlete athlete = athleteWebMapper.requestToDomain(request, coach.id());
Athlete saved = createAthleteUseCase.execute(athlete, coach.id());
```

Note: `coachId` is passed both in the domain object (via mapper) AND as
explicit parameter to the Use Case — the Use Case uses the explicit parameter
for ownership verification, not the one embedded in the domain object.

## Impact on existing tests

### Controller tests
- Replace `@MockitoBean AthleteService` with individual Use Case mocks
- `when(athleteService.save(...))` → `when(createAthleteUseCase.execute(...))`
- Behavior and assertions unchanged

### Service tests → Use Case tests
- Rename `*ServiceImplTest` → `*UseCaseImplTest`
- Move to `src/test/.../application/{feature}/`
- Same scenarios, same mocks — only class names change
- Remove ownership resolution from controller test setup
  (now handled inside Use Case)

## New repository method required
`WeeklyWellnessRepository` — add:
```java
Optional<WeeklyWellness> findLatestByAthleteId(long athleteId);
```
`WeeklyWellnessJpaAdapter` — implement via derived method:
`findTopByAthleteIdOrderByWeekStartDateDesc`

## ArchUnit update
`service_annotations_should_be_in_service` — update to accept
`application` package:
```java
.should().resideInAnyPackage("..service..", "..application..")
```
Once `service/` is empty and deleted, simplify to `..application..` only.

## Out of scope
- `@PreAuthorize` ownership enforcement (ticket #120)
- Package renaming to feature-first structure (ticket #119)
- `service/` package deletion — happens after all controllers
  are migrated to Use Cases and all tests are green
