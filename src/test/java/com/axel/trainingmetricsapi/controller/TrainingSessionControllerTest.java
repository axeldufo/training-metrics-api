package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.application.port.in.CreateTrainingSessionUseCase;
import com.axel.trainingmetricsapi.application.port.in.DeleteTrainingSessionUseCase;
import com.axel.trainingmetricsapi.application.port.in.GetTrainingSessionUseCase;
import com.axel.trainingmetricsapi.application.port.in.GetTrainingSessionsByPeriodUseCase;
import com.axel.trainingmetricsapi.application.port.in.UpdateTrainingSessionUseCase;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.TrainingSessionNotFoundException;
import com.axel.trainingmetricsapi.dto.request.TrainingSessionRequest;
import com.axel.trainingmetricsapi.dto.response.TrainingSessionResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingSessionController.class)
class TrainingSessionControllerTest extends SecurityMockControllerSupport {

    private static final long ATHLETE_ID = 4L;
    private static final long COACH_ID = 2L;
    private static final String URL_PREFIX = ApiConstants.API_VERSION + "/athletes/" + ATHLETE_ID + "/sessions";

    @MockitoBean
    private TrainingSessionWebMapper trainingSessionWebMapper;

    @MockitoBean
    private CreateTrainingSessionUseCase createTrainingSessionUseCase;

    @MockitoBean
    private GetTrainingSessionUseCase getTrainingSessionUseCase;

    @MockitoBean
    private GetTrainingSessionsByPeriodUseCase getTrainingSessionsByPeriodUseCase;

    @MockitoBean
    private UpdateTrainingSessionUseCase updateTrainingSessionUseCase;

    @MockitoBean
    private DeleteTrainingSessionUseCase deleteTrainingSessionUseCase;

    @MockitoBean
    private AuthenticatedCoachResolver authenticatedCoachResolver;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_shouldReturnCreatedTrainingSession_whenRequestIsValid() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        TrainingSessionRequest trainingSessionRequest = aValidSessionRequest();
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionWebMapper.requestToDomain(trainingSessionRequest, ATHLETE_ID)).thenReturn(trainingSession);
        TrainingSession persistedTrainingSession = Instancio.create(TrainingSession.class);
        when(createTrainingSessionUseCase.execute(trainingSession, COACH_ID)).thenReturn(persistedTrainingSession);
        TrainingSessionResponse trainingSessionResponse = Instancio.of(TrainingSessionResponse.class)
            .set(field(TrainingSessionResponse::athleteId), ATHLETE_ID)
            .create();
        when(trainingSessionWebMapper.domainToResponse(persistedTrainingSession)).thenReturn(trainingSessionResponse);

        ResultActions result = mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", URL_PREFIX + "/" + trainingSessionResponse.id()));

        assertJsonMatchesTrainingSessionResponse(result, trainingSessionResponse);
        verify(trainingSessionWebMapper).requestToDomain(trainingSessionRequest, ATHLETE_ID);
        verify(createTrainingSessionUseCase).execute(trainingSession, COACH_ID);
        verify(trainingSessionWebMapper).domainToResponse(persistedTrainingSession);
    }

    @Test
    void create_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {
        TrainingSessionRequest trainingSessionRequest = new TrainingSessionRequest(LocalDate.now().plusDays(1), null, 11, -1, null);

        mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void create_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionWebMapper.requestToDomain(any(TrainingSessionRequest.class), eq(ATHLETE_ID)))
            .thenReturn(trainingSession);
        when(createTrainingSessionUseCase.execute(trainingSession, COACH_ID))
            .thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(aValidSessionRequest())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(trainingSessionWebMapper).requestToDomain(any(TrainingSessionRequest.class), eq(ATHLETE_ID));
        verify(createTrainingSessionUseCase).execute(trainingSession, COACH_ID);
        verify(trainingSessionWebMapper, never()).domainToResponse(any());
    }

    @Test
    void getByPeriod_shouldReturn200WithList_whenRequestIsValid() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 31);
        int count = 3;
        List<TrainingSession> trainingSessions = Instancio.ofList(TrainingSession.class).size(count).create();
        when(getTrainingSessionsByPeriodUseCase.execute(ATHLETE_ID, COACH_ID, from, to)).thenReturn(trainingSessions);
        when(trainingSessionWebMapper.domainToResponse(any(TrainingSession.class)))
            .thenReturn(Instancio.create(TrainingSessionResponse.class));

        mvc.perform(get(URL_PREFIX)
            .param("from", from.toString())
            .param("to", to.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(count)));

        verify(getTrainingSessionsByPeriodUseCase).execute(ATHLETE_ID, COACH_ID, from, to);
        verify(trainingSessionWebMapper, times(count)).domainToResponse(any(TrainingSession.class));
    }

    @Test
    void getByPeriod_shouldReturn400_whenFromIsAfterTo() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 20);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 13);

        mvc.perform(get(URL_PREFIX)
            .param("from", from.toString())
            .param("to", to.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].code").value("HTTP_VALIDATION_ERROR"))
            .andExpect(jsonPath("$[0].field").value("to"));

        verify(getTrainingSessionsByPeriodUseCase, never()).execute(anyLong(), anyLong(), any(), any());
    }

    @Test
    void getByPeriod_shouldUseToday_whenToIsAbsent() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        when(getTrainingSessionsByPeriodUseCase.execute(eq(ATHLETE_ID), eq(COACH_ID), eq(from), any(LocalDate.class)))
            .thenReturn(List.of());

        mvc.perform(get(URL_PREFIX)
            .param("from", from.toString()))
            .andExpect(status().isOk());

        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(getTrainingSessionsByPeriodUseCase).execute(eq(ATHLETE_ID), eq(COACH_ID), eq(from), toCaptor.capture());
        assertThat(toCaptor.getValue()).isEqualTo(LocalDate.now());
    }

    @Test
    void getByPeriod_shouldReturn400_whenFromIsMissing() throws Exception {
        mvc.perform(get(URL_PREFIX))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].code").value("HTTP_VALIDATION_ERROR"));

        verify(getTrainingSessionsByPeriodUseCase, never()).execute(anyLong(), anyLong(), any(), any());
    }

    @Test
    void getByPeriod_shouldReturn404_whenAthleteNotFound() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        when(getTrainingSessionsByPeriodUseCase.execute(eq(ATHLETE_ID), eq(COACH_ID), any(), any()))
            .thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(get(URL_PREFIX)
            .param("from", "2024-01-01"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(getTrainingSessionsByPeriodUseCase).execute(eq(ATHLETE_ID), eq(COACH_ID), any(), any());
    }

    @Test
    void getById_shouldReturnTrainingSessionResponse_whenTrainingSessionExists() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long trainingSessionId = 8L;
        TrainingSession persistedTrainingSession = Instancio.create(TrainingSession.class);
        when(getTrainingSessionUseCase.execute(trainingSessionId, ATHLETE_ID, COACH_ID))
            .thenReturn(persistedTrainingSession);
        TrainingSessionResponse trainingSessionResponse = Instancio.create(TrainingSessionResponse.class);
        when(trainingSessionWebMapper.domainToResponse(persistedTrainingSession)).thenReturn(trainingSessionResponse);

        ResultActions result = mvc.perform(get(URL_PREFIX + "/" + trainingSessionId))
            .andExpect(status().isOk());

        assertJsonMatchesTrainingSessionResponse(result, trainingSessionResponse);
        verify(getTrainingSessionUseCase).execute(trainingSessionId, ATHLETE_ID, COACH_ID);
        verify(trainingSessionWebMapper).domainToResponse(persistedTrainingSession);
    }

    @Test
    void getById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long trainingSessionId = 8L;
        when(getTrainingSessionUseCase.execute(trainingSessionId, ATHLETE_ID, COACH_ID))
            .thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(get(URL_PREFIX + "/" + trainingSessionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(getTrainingSessionUseCase).execute(trainingSessionId, ATHLETE_ID, COACH_ID);
    }

    @Test
    void getById_shouldReturnNotFound_whenTrainingSessionNotFoundException() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long trainingSessionId = 8L;
        when(getTrainingSessionUseCase.execute(trainingSessionId, ATHLETE_ID, COACH_ID))
            .thenThrow(new TrainingSessionNotFoundException(trainingSessionId));

        mvc.perform(get(URL_PREFIX + "/" + trainingSessionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(getTrainingSessionUseCase).execute(trainingSessionId, ATHLETE_ID, COACH_ID);
    }

    @Test
    void updateById_shouldReturnUpdatedTrainingSession() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        TrainingSessionRequest trainingSessionRequest = aValidSessionRequest();
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionWebMapper.requestToDomain(trainingSessionRequest, ATHLETE_ID))
            .thenReturn(trainingSession);
        TrainingSession persistedTrainingSession = Instancio.create(TrainingSession.class);
        when(updateTrainingSessionUseCase.execute(trainingSession, COACH_ID)).thenReturn(persistedTrainingSession);
        TrainingSessionResponse trainingSessionResponse = Instancio.create(TrainingSessionResponse.class);
        when(trainingSessionWebMapper.domainToResponse(persistedTrainingSession)).thenReturn(trainingSessionResponse);

        ResultActions result = mvc.perform(put(URL_PREFIX + "/" + 8L).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isOk());

        assertJsonMatchesTrainingSessionResponse(result, trainingSessionResponse);
        verify(trainingSessionWebMapper).requestToDomain(trainingSessionRequest, ATHLETE_ID);
        verify(updateTrainingSessionUseCase).execute(trainingSession, COACH_ID);
        verify(trainingSessionWebMapper).domainToResponse(persistedTrainingSession);
    }

    @Test
    void updateById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionWebMapper.requestToDomain(any(TrainingSessionRequest.class), eq(ATHLETE_ID)))
            .thenReturn(trainingSession);
        when(updateTrainingSessionUseCase.execute(trainingSession, COACH_ID))
            .thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(put(URL_PREFIX + "/" + 8L).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(aValidSessionRequest())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(trainingSessionWebMapper).requestToDomain(any(TrainingSessionRequest.class), eq(ATHLETE_ID));
        verify(updateTrainingSessionUseCase).execute(trainingSession, COACH_ID);
    }

    @Test
    void updateById_shouldReturnNotFound_whenTrainingSessionNotFoundException() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long trainingSessionId = 8L;
        TrainingSessionRequest trainingSessionRequest = aValidSessionRequest();
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionWebMapper.requestToDomain(trainingSessionRequest, ATHLETE_ID))
            .thenReturn(trainingSession);
        when(updateTrainingSessionUseCase.execute(trainingSession, COACH_ID))
            .thenThrow(new TrainingSessionNotFoundException(trainingSessionId));

        mvc.perform(put(URL_PREFIX + "/" + trainingSessionId).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(updateTrainingSessionUseCase).execute(any(TrainingSession.class), eq(COACH_ID));
    }

    @Test
    void updateById_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {
        TrainingSessionRequest trainingSessionRequest = new TrainingSessionRequest(null, null, 12, 0, null);

        mvc.perform(put(URL_PREFIX + "/" + 8L).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void deleteById_shouldReturnOK_whenTrainingSessionIsDeleted() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long sessionId = 4L;

        mvc.perform(delete(URL_PREFIX + "/" + sessionId))
            .andExpect(status().isNoContent());

        verify(deleteTrainingSessionUseCase).execute(sessionId, ATHLETE_ID, COACH_ID);
    }

    @Test
    void deleteById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long sessionId = 4L;
        doThrow(new AthleteNotFoundException(ATHLETE_ID))
            .when(deleteTrainingSessionUseCase).execute(sessionId, ATHLETE_ID, COACH_ID);

        mvc.perform(delete(URL_PREFIX + "/" + sessionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(deleteTrainingSessionUseCase).execute(sessionId, ATHLETE_ID, COACH_ID);
    }

    @Test
    void deleteById_shouldReturnNotFound_whenTrainingSessionNotFoundException() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long sessionId = 4L;
        doThrow(new TrainingSessionNotFoundException(sessionId))
            .when(deleteTrainingSessionUseCase).execute(sessionId, ATHLETE_ID, COACH_ID);

        mvc.perform(delete(URL_PREFIX + "/" + sessionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(deleteTrainingSessionUseCase).execute(sessionId, ATHLETE_ID, COACH_ID);
    }

    private TrainingSessionRequest aValidSessionRequest() {
        return Instancio.of(TrainingSessionRequest.class)
            .generate(field(TrainingSessionRequest::date), gen -> gen.temporal().localDate().past())
            .generate(field(TrainingSessionRequest::rpe), gen -> gen.ints().range(1, 10))
            .generate(field(TrainingSessionRequest::durationInMin), gen -> gen.ints().min(1))
            .create();
    }

    private void assertJsonMatchesTrainingSessionResponse(ResultActions result, TrainingSessionResponse trainingSessionResponse) throws Exception {
        result.andExpect(jsonPath("$.id").value(trainingSessionResponse.id()))
            .andExpect(jsonPath("$.date").value(trainingSessionResponse.date().toString()))
            .andExpect(jsonPath("$.sport").value(trainingSessionResponse.sport().name()))
            .andExpect(jsonPath("$.rpe").value(trainingSessionResponse.rpe()))
            .andExpect(jsonPath("$.durationInMin").value(trainingSessionResponse.durationInMin()))
            .andExpect(jsonPath("$.targetZone").value(trainingSessionResponse.targetZone().name()))
            .andExpect(jsonPath("$.athleteId").value(trainingSessionResponse.athleteId()))
            .andExpect(jsonPath("$.aboveTargetAlert").value(trainingSessionResponse.aboveTargetAlert()))
            .andExpect(jsonPath("$.belowTargetAlert").value(trainingSessionResponse.belowTargetAlert()));
    }
}
