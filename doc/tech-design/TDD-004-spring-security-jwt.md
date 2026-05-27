# TDD-004 — Spring Security & JWT Authentication

## Context
Phase 2 security implementation. Introduces JWT-based stateless authentication
for the multi-coach model. Each coach authenticates via email/password and receives
a JWT token used to identify themselves on subsequent requests.

Coach creation was previously handled by `POST /v1/coaches` — this endpoint is
replaced by `POST /v1/auth/register`. All other endpoints require a valid JWT.

## Data Model

### Flyway Migration
- `V5__add_email_and_password_to_coach.sql`

```sql
ALTER TABLE coach
    ADD COLUMN email           VARCHAR(255) NOT NULL UNIQUE,
    ADD COLUMN hashed_password VARCHAR(255) NOT NULL;
```

### Coach domain entity (updated)
- `email` field added — needed for async notifications (Kafka phase 2)
- `hashed_password` — NOT in domain, NOT in response, only in `CoachJpaEntity`

## Domain Interfaces

### New ports (domain/)
```java
public interface AuthRepository {
    CoachAuthData register(CoachCredentials credentials, String hashedPassword);
    Optional<CoachAuthData> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### New domain objects (domain/)
```java
public record CoachCredentials(String name, String email, String rawPassword) {}
public record CoachAuthData(long id, String hashedPassword) {}
```

No domain invariants on these records — validation is handled at HTTP boundary
(`RegisterRequest`, `LoginRequest`) and DB constraints. These objects are specific
to the authentication channel and will not transit via Kafka/gRPC.

### New exceptions (domain/exception/)
- `EmailAlreadyExistsException(String email)` → 409 Conflict
- `InvalidCredentialsException()` → 401 Unauthorized
  Note: no detail exposed (email or password) — prevents account enumeration

### New ErrorCodes
- `EMAIL_ALREADY_EXISTS` — email must be unique across all accounts
- `INVALID_CREDENTIALS` — wrong email or password

## API Contract

### Endpoints
| Method | URL | Request | Response | Success | Errors |
|---|---|---|---|---|---|
| POST | /v1/auth/register | `RegisterRequest` | `AuthResponse` | 201 | 400, 409 |
| POST | /v1/auth/login | `LoginRequest` | `AuthResponse` | 200 | 400, 401 |

### Public endpoints (no JWT required)
- `POST /v1/auth/register`
- `POST /v1/auth/login`

### Protected endpoints (JWT required)
- All `/v1/athletes/**`
- All `/v1/coaches/**`
- All `/v1/athletes/**/sessions/**`

### DTOs
**`RegisterRequest`** (record)
- `@NotBlank String name`
- `@NotBlank @Email String email`
- `@NotBlank @Size(min=8) String rawPassword`

**`LoginRequest`** (record)
- `@NotBlank @Email String email`
- `@NotBlank String rawPassword`
  Note: no `@Size(min=8)` on login — avoids locking out accounts created before
  the rule existed

**`AuthResponse`** (record)
- `String token` — JWT token

### Error format
```json
[{ "code": "EMAIL_ALREADY_EXISTS", "field": null, "message": "Email already exists: coach@example.com" }]
[{ "code": "INVALID_CREDENTIALS", "field": null, "message": "Invalid credentials" }]
```

## Service flows

### Register flow
RegisterRequest
→ AuthWebMapper.toCredentials()
→ AuthService.register(CoachCredentials)
→ AuthRepository.existsByEmail() → EmailAlreadyExistsException if true
→ BCryptPasswordEncoder.encode(rawPassword)
→ AuthRepository.register(credentials, hashedPassword) → CoachAuthData
→ AuthWebMapper.toAuthResponse(CoachAuthData) → AuthResponse(token)

### Login flow
LoginRequest
→ AuthController passes email + password directly (no mapper for request)
→ AuthService.login(email, rawPassword)
→ AuthRepository.findByEmail() → InvalidCredentialsException if empty
→ BCryptPasswordEncoder.matches(rawPassword, hashedPassword)
→ InvalidCredentialsException if false
→ returns CoachAuthData
→ AuthWebMapper.toAuthResponse(CoachAuthData) → AuthResponse(token)

## JWT Implementation

### Package
All security classes in `controller/security/` — web infrastructure,
consistent with `GlobalExceptionHandler` placement in `controller/`.

### JwtUtils
- `generateToken(long coachId)` → signed JWT with `coachId` claim
- `extractCoachId(String token)` → `Long` — returns `null` if token is
  invalid or expired (JJWT exception absorbed internally)

### JWT payload
```json
{ "coachId": 42, "exp": 1234567890 }
```

### JwtAuthenticationFilter
- Extends `OncePerRequestFilter`
- Extracts `Authorization: Bearer <token>` header
- Calls `extractCoachId(token)` — single parse, validation is implicit
  (JJWT throws on invalid/expired token, caught internally → returns null)
- If `coachId` non-null and no existing authentication:
  sets `UsernamePasswordAuthenticationToken(new AuthenticatedCoach(coachId), null, emptyList())`
  in `SecurityContextHolder` — `AuthenticatedCoach` stored as principal
- If header absent, malformed, or token invalid: continues filter chain
  without authentication — `SecurityFilterChain` handles the 401

### SecurityFilterChain (in `controller/security/SecurityConfig.java`)
- CSRF disabled — JWT transmitted in `Authorization` header, not cookies;
  browser cannot auto-send custom headers, CSRF attack vector eliminated
- Session management: `STATELESS` — no server-side session, each request
  is fully autonomous
- Public: `"*/auth/**"` — Spring Boot 4 PathPatternParser
- `JwtAuthenticationFilter` added before `UsernamePasswordAuthenticationFilter`

### AuthWebMapper
- Depends on `JwtUtils` (constructor injection)
- `toCredentials(RegisterRequest)` → `CoachCredentials`
- `toAuthResponse(CoachAuthData)` → `AuthResponse(jwtUtils.generateToken(authData.id()))`

## Dependencies added
- `spring-boot-starter-security`
- `jjwt-api` / `jjwt-impl` / `jjwt-jackson` (version 0.13.0)
- `spring-security-test` (scope: test)

## Impact on existing code
- `POST /v1/coaches` removed from `CoachController`
- `CoachRequest` renamed to `CoachUpdateRequest`
- `CoachService.save()` removed — creation via `AuthService.register()` only
- `CoachService.update()` replaced by `CoachService.updateName(long id, String name)`
- `CoachPersistenceMapper.domainToEntity()` — maps id + name only (partial update)
- `CoachJpaEntity` — `@Builder` added, `email` and `hashed_password` fields added
- `BCryptPasswordEncoder` declared as `@Bean` in `SecurityConfig`
- `AuthWebMapper` depends on `JwtUtils` — constructor injection
- All `@WebMvcTest` tests extend `SecurityMockControllerSupport` — provides `@MockitoBean JwtUtils`
  (previously named BaseControllerTest, renamed for clarity)
- `@WithMockUser` removed from `SecurityMockControllerSupport` — Spring Security not active
  in `@WebMvcTest` context without explicit SecurityConfig; no 401 raised without it
- `AuthenticatedCoachResolver` added in `controller/security/` — resolves `AuthenticatedCoach`
  from `SecurityContextHolder`; exists to work around conflict between
  `ProxyingHandlerMethodArgumentResolver` (Spring Data) and `AuthenticationPrincipalArgumentResolver`
  when a custom record is used as principal in `@WebMvcTest` context
  (see https://github.com/spring-projects/spring-data-commons/issues/2937)
- `AthleteControllerTest`, `CoachControllerTest`, `TrainingSessionControllerTest` inject
  `@MockitoBean AuthenticatedCoachResolver` — mocked per test with `when(resolver.resolve()).thenReturn(...)`
- `AuthControllerTest` does not extend `BaseControllerTest` — auth endpoints
  are public, `@MockitoBean JwtUtils` added directly without `@WithMockUser`
- ArchUnit rules updated:
  - `no_field_injection` — rewrites to target `@Autowired`/`@Inject` only,
    excludes `@Value` (configuration injection, not dependency injection)
  - `should_use_java_time` — excludes `JwtUtils` (JJWT API requires `java.util.Date`)
  - `layer_dependencies_are_respected` — checks `productionClasses` only
    (test classes legitimately cross layer boundaries)

## Decisions
- JWT stateless — no session, no server-side state
- `coachId` as JWT claim — sufficient to identify and filter data per coach;
  stored as `principal` in `UsernamePasswordAuthenticationToken` for use in ticket #67
- `hashed_password` never leaves `CoachJpaEntity` — not in domain, not in response
- `email` in `Coach` domain — needed for future Kafka notifications
- `CoachAuthData` contains `id` + `hashedPassword` only — minimal auth data
- `AuthRepository` separate from `CoachRepository` — authentication concerns
  isolated from business CRUD
- `AuthJpaAdapter` accesses `CoachJpaRepository` directly — justified exception
  to service→port→adapter pattern for auth infrastructure
- `SecurityConfig` in `controller/security/` — web infrastructure
- Login does not expose which field is wrong (email vs password) — prevents
  account enumeration attack
- No `UserDetailsService` — stateless pure approach; `coachId` in token is
  sufficient to establish authentication without a DB lookup per request;
  a deleted coach's token remains valid up to 24h — acceptable risk for this domain
- `extractCoachId` returns `null` on invalid/expired token instead of throwing —
  filter responsibility is to attempt authentication, not to reject requests;
  rejection is handled by `SecurityFilterChain`
- `JwtUtils` tested with `ReflectionTestUtils` — avoids full Spring context
  startup for `@Value` injection in unit tests
- `AuthenticatedCoach record(long id)` — custom principal stored in SecurityContextHolder;
    replaces bare `Long coachId` as principal for type safety
- `AuthenticatedCoachResolver` — `@Component` in `controller/security/`; decouples controllers
  from `SecurityContextHolder` for testability; workaround for Spring Data/Security conflict
  in `@WebMvcTest` — `@AuthenticationPrincipal` not viable with custom record principal
- `@PreAuthorize` not used — no differentiated roles in phase 2; all coaches have the same rights;
  ownership enforced via data-level checks, not role-based access control

## Out of scope
- `@PreAuthorize` — not applicable in phase 2 (no roles); may be introduced in phase 3 with OAuth2
- Profile update (email/password change) — future ProfileController
- OAuth2/OIDC (phase 3)
- E2E tests with JWT (after SecurityFilterChain is complete)
