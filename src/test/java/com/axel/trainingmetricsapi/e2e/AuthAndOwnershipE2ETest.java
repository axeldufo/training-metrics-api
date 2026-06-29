package com.axel.trainingmetricsapi.e2e;

import com.axel.trainingmetricsapi.TestContainersConfiguration;
import com.axel.trainingmetricsapi.TrainingMetricsApiApplication;
import com.axel.trainingmetricsapi.athlete.interfaces.web.dto.AthleteRequest;
import com.axel.trainingmetricsapi.athlete.interfaces.web.dto.AthleteResponse;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.AuthResponse;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.LoginRequest;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.RegisterRequest;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.JwtUtils;
import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.PagedResponse;
import com.axel.trainingmetricsapi.training.domain.TargetZone;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.TrainingSessionRequest;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.TrainingSessionResponse;
import com.axel.trainingmetricsapi.wellness.interfaces.web.dto.WeeklyWellnessRequest;
import com.axel.trainingmetricsapi.wellness.interfaces.web.dto.WeeklyWellnessResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = TrainingMetricsApiApplication.class, webEnvironment = RANDOM_PORT)
@Import(TestContainersConfiguration.class)
class AuthAndOwnershipE2ETest {

    private static final LocalDate PAST_MONDAY = LocalDate.of(2025, Month.MAY, 19);
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, Month.JANUARY, 1);

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        restTemplate = new TestRestTemplate();
    }

    // --- Nominal flow ---

    @Test
    void coach_can_register_login_and_manage_athlete() {
        String email = UUID.randomUUID() + "@e2e.test";
        String password = "password123";

        String tokenFromRegister = register("Coach Full", email, password);
        String tokenFromLogin = login(email, password);
        assertThat(tokenFromRegister).isNotBlank();
        assertThat(tokenFromLogin).isNotBlank();

        AthleteRequest createRequest = new AthleteRequest("Alice", "Smith", BIRTH_DATE, Sport.ROAD_RUNNING, 60.0);
        ResponseEntity<AthleteResponse> createResponse = restTemplate.exchange(
            url("/v1/athletes"), HttpMethod.POST,
            new HttpEntity<>(createRequest, authHeaders(tokenFromLogin)),
            AthleteResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long athleteId = createResponse.getBody().id();

        ResponseEntity<PagedResponse<AthleteResponse>> listAfterCreate = restTemplate.exchange(
            url("/v1/athletes"), HttpMethod.GET,
            new HttpEntity<>(authHeaders(tokenFromLogin)),
            new ParameterizedTypeReference<PagedResponse<AthleteResponse>>() {});
        assertThat(listAfterCreate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listAfterCreate.getBody().content()).extracting(AthleteResponse::id).contains(athleteId);

        AthleteRequest updateRequest = new AthleteRequest("Alice", "Updated", BIRTH_DATE, Sport.CYCLING, 62.0);
        ResponseEntity<AthleteResponse> updateResponse = restTemplate.exchange(
            url("/v1/athletes/" + athleteId), HttpMethod.PUT,
            new HttpEntity<>(updateRequest, authHeaders(tokenFromLogin)),
            AthleteResponse.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().lastName()).isEqualTo("Updated");

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            url("/v1/athletes/" + athleteId), HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(tokenFromLogin)),
            Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<PagedResponse<AthleteResponse>> listAfterDelete = restTemplate.exchange(
            url("/v1/athletes"), HttpMethod.GET,
            new HttpEntity<>(authHeaders(tokenFromLogin)),
            new ParameterizedTypeReference<PagedResponse<AthleteResponse>>() {});
        assertThat(listAfterDelete.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listAfterDelete.getBody().content()).isEmpty();
    }

    @Test
    void coach_can_create_and_retrieve_training_session() {
        String token = register("Coach Session", UUID.randomUUID() + "@e2e.test", "password123");

        long athleteId = createAthlete(token, "Bob", "Johnson");

        int rpe = 5;
        int durationInMin = 60;
        int expectedFosterLoad = rpe * durationInMin;

        TrainingSessionRequest sessionRequest =
            new TrainingSessionRequest(PAST_MONDAY, Sport.ROAD_RUNNING, rpe, durationInMin, TargetZone.Z2);
        ResponseEntity<TrainingSessionResponse> createSession = restTemplate.exchange(
            url("/v1/athletes/" + athleteId + "/sessions"), HttpMethod.POST,
            new HttpEntity<>(sessionRequest, authHeaders(token)),
            TrainingSessionResponse.class);
        assertThat(createSession.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<List<TrainingSessionResponse>> sessionsResponse = restTemplate.exchange(
            url("/v1/athletes/" + athleteId + "/sessions?from=2025-01-01"), HttpMethod.GET,
            new HttpEntity<>(authHeaders(token)),
            new ParameterizedTypeReference<List<TrainingSessionResponse>>() {});
        assertThat(sessionsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sessionsResponse.getBody()).hasSize(1);
        TrainingSessionResponse session = sessionsResponse.getBody().get(0);
        assertThat(session.rpe() * session.durationInMin()).isEqualTo(expectedFosterLoad);
    }

    @Test
    void coach_can_create_and_retrieve_weekly_wellness() {
        String token = register("Coach Wellness", UUID.randomUUID() + "@e2e.test", "password123");

        long athleteId = createAthlete(token, "Carol", "White");

        WeeklyWellnessRequest wellnessRequest = new WeeklyWellnessRequest(PAST_MONDAY, 3, 2, 4);
        ResponseEntity<WeeklyWellnessResponse> createResponse = restTemplate.exchange(
            url("/v1/athletes/" + athleteId + "/wellness"), HttpMethod.POST,
            new HttpEntity<>(wellnessRequest, authHeaders(token)),
            WeeklyWellnessResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<List<WeeklyWellnessResponse>> getResponse = restTemplate.exchange(
            url("/v1/athletes/" + athleteId + "/wellness?from=" + PAST_MONDAY + "&to=" + PAST_MONDAY),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(token)),
            new ParameterizedTypeReference<List<WeeklyWellnessResponse>>() {});
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).hasSize(1);
        WeeklyWellnessResponse wellness = getResponse.getBody().get(0);
        assertThat(wellness.perceivedDifficulty()).isEqualTo(3);
        assertThat(wellness.perceivedFatigue()).isEqualTo(2);
        assertThat(wellness.motivation()).isEqualTo(4);
    }

    // --- Security ---

    @Test
    void request_without_token_returns_401() {
        ResponseEntity<Void> response = restTemplate.exchange(
            url("/v1/athletes"), HttpMethod.GET, HttpEntity.EMPTY, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void request_with_malformed_token_returns_401() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer invalidtoken");
        ResponseEntity<Void> response = restTemplate.exchange(
            url("/v1/athletes"), HttpMethod.GET, new HttpEntity<>(headers), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void request_with_expired_token_returns_401() {
        long originalExpiration = (long) ReflectionTestUtils.getField(jwtUtils, "expiration");
        ReflectionTestUtils.setField(jwtUtils, "expiration", -1000L);
        String expiredToken = jwtUtils.generateToken(1L);
        ReflectionTestUtils.setField(jwtUtils, "expiration", originalExpiration);

        ResponseEntity<Void> response = restTemplate.exchange(
            url("/v1/athletes"), HttpMethod.GET,
            new HttpEntity<>(authHeaders(expiredToken)),
            Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --- Ownership ---

    @Test
    void coach_cannot_see_athletes_of_another_coach() {
        String tokenA = register("Coach A", UUID.randomUUID() + "@e2e.test", "password123");
        String tokenB = register("Coach B", UUID.randomUUID() + "@e2e.test", "password123");

        long athleteAId = createAthlete(tokenA, "Athlete", "OfA");
        createAthlete(tokenB, "Athlete", "OfB");

        ResponseEntity<PagedResponse<AthleteResponse>> listByA = restTemplate.exchange(
            url("/v1/athletes"), HttpMethod.GET,
            new HttpEntity<>(authHeaders(tokenA)),
            new ParameterizedTypeReference<PagedResponse<AthleteResponse>>() {});
        assertThat(listByA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listByA.getBody().content()).extracting(AthleteResponse::id).containsOnly(athleteAId);
    }

    @Test
    void coach_cannot_delete_athlete_of_another_coach() {
        String tokenA = register("Coach A", UUID.randomUUID() + "@e2e.test", "password123");
        String tokenB = register("Coach B", UUID.randomUUID() + "@e2e.test", "password123");

        long athleteBId = createAthlete(tokenB, "Athlete", "OfB");

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            url("/v1/athletes/" + athleteBId), HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(tokenA)),
            Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void coach_cannot_update_athlete_of_another_coach() {
        String tokenA = register("Coach A", UUID.randomUUID() + "@e2e.test", "password123");
        String tokenB = register("Coach B", UUID.randomUUID() + "@e2e.test", "password123");

        long athleteBId = createAthlete(tokenB, "Athlete", "OfB");

        AthleteRequest updateRequest = new AthleteRequest("Hacked", "Athlete", BIRTH_DATE, Sport.CYCLING, 70.0);
        ResponseEntity<Void> updateResponse = restTemplate.exchange(
            url("/v1/athletes/" + athleteBId), HttpMethod.PUT,
            new HttpEntity<>(updateRequest, authHeaders(tokenA)),
            Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void coach_cannot_access_session_of_another_coach() {
        String tokenA = register("Coach A", UUID.randomUUID() + "@e2e.test", "password123");
        String tokenB = register("Coach B", UUID.randomUUID() + "@e2e.test", "password123");

        createAthlete(tokenA, "Athlete", "OfA");
        long athleteBId = createAthlete(tokenB, "Athlete", "OfB");

        TrainingSessionRequest sessionRequest =
            new TrainingSessionRequest(PAST_MONDAY, Sport.ROAD_RUNNING, 5, 60, TargetZone.Z2);
        restTemplate.exchange(
            url("/v1/athletes/" + athleteBId + "/sessions"), HttpMethod.POST,
            new HttpEntity<>(sessionRequest, authHeaders(tokenB)),
            TrainingSessionResponse.class);

        ResponseEntity<Void> accessResponse = restTemplate.exchange(
            url("/v1/athletes/" + athleteBId + "/sessions?from=2025-01-01"), HttpMethod.GET,
            new HttpEntity<>(authHeaders(tokenA)),
            Void.class);
        assertThat(accessResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- Helpers ---

    private String register(String name, String email, String password) {
        RegisterRequest request = new RegisterRequest(name, email, password);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            url("/v1/auth/register"), request, AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().token();
    }

    private String login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            url("/v1/auth/login"), request, AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().token();
    }

    private long createAthlete(String token, String firstName, String lastName) {
        AthleteRequest request = new AthleteRequest(firstName, lastName, BIRTH_DATE, Sport.ROAD_RUNNING, 70.0);
        ResponseEntity<AthleteResponse> response = restTemplate.exchange(
            url("/v1/athletes"), HttpMethod.POST,
            new HttpEntity<>(request, authHeaders(token)),
            AthleteResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().id();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
