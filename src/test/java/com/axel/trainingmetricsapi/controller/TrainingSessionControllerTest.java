package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.PageResult;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.TrainingSessionNotFoundException;
import com.axel.trainingmetricsapi.dto.request.TrainingSessionRequest;
import com.axel.trainingmetricsapi.dto.response.TrainingSessionResponse;
import com.axel.trainingmetricsapi.service.AthleteService;
import com.axel.trainingmetricsapi.service.TrainingSessionService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static com.axel.trainingmetricsapi.controller.ApiConstants.DEFAULT_PAGE;
import static com.axel.trainingmetricsapi.controller.ApiConstants.DEFAULT_SIZE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingSessionController.class)
class TrainingSessionControllerTest  extends SecurityMockControllerSupport {

    private static final long ATHLETE_ID = 4L;
    private static final String URL_PREFIX = ApiConstants.API_VERSION + "/athletes/" + ATHLETE_ID + "/sessions";

    @MockitoBean
    private TrainingSessionWebMapper trainingSessionWebMapper;

    @MockitoBean
    private TrainingSessionService trainingSessionService;

    @MockitoBean
    private AthleteService athleteService;

    @MockitoBean
    private AuthenticatedCoachResolver authenticatedCoachResolver;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_shouldReturnCreatedTrainingSession_whenRequestIsValid() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        TrainingSessionRequest trainingSessionRequest = aValidSessionRequest();
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionWebMapper.requestToDomain(trainingSessionRequest, ATHLETE_ID)).thenReturn(trainingSession);
        TrainingSession persistedTrainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionService.save(trainingSession)).thenReturn(persistedTrainingSession);
        TrainingSessionResponse trainingSessionResponse = Instancio.of(TrainingSessionResponse.class)
            .set(field(TrainingSessionResponse::athleteId), ATHLETE_ID)
            .create(); // need the correct athleteId to return the location in header
        when(trainingSessionWebMapper.domainToResponse(persistedTrainingSession)).thenReturn(trainingSessionResponse);

        ResultActions result = mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", URL_PREFIX + "/" + trainingSessionResponse.id()));

        assertJsonMatchesTrainingSessionResponse(result, trainingSessionResponse);
        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionWebMapper).requestToDomain(trainingSessionRequest, ATHLETE_ID);
        verify(trainingSessionService).save(trainingSession);
        verify(trainingSessionWebMapper).domainToResponse(persistedTrainingSession);
    }

    @Test
    void create_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {
        TrainingSessionRequest trainingSessionRequest = new TrainingSessionRequest(null, null, 11, -1, null);

        mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void create_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        when(athleteService.findById(ATHLETE_ID, coachId)).thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aValidSessionRequest())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionWebMapper, never()).requestToDomain(any(), anyLong());
        verify(trainingSessionService, never()).save(any());
        verify(trainingSessionWebMapper, never()).domainToResponse(any());
    }

    @Test
    void getAll_withDefaultPagination_shouldReturnListAsJson() throws Exception {
        long coachId = 2L;
        int pageNumber = Integer.parseInt(DEFAULT_PAGE);
        int pageSize = Integer.parseInt(DEFAULT_SIZE);
        int nbSessionsFound = 3;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        List<TrainingSession> trainingSessions = Instancio.ofList(TrainingSession.class).size(nbSessionsFound).create();
        when(trainingSessionService.findAllByAthleteId(ATHLETE_ID, pageNumber, pageSize)).thenReturn(
            new PageResult<>(trainingSessions, nbSessionsFound, pageNumber, pageSize));
        when(trainingSessionWebMapper.domainToResponse(any(TrainingSession.class)))
            .thenReturn(Instancio.create(TrainingSessionResponse.class));

        mvc.perform(get(URL_PREFIX))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(nbSessionsFound)))
            .andExpect(jsonPath("$.totalElements", is(nbSessionsFound)))
            .andExpect(jsonPath("$.page", is(pageNumber)))
            .andExpect(jsonPath("$.size", is(pageSize)));

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService).findAllByAthleteId(ATHLETE_ID, pageNumber, pageSize);
        verify(trainingSessionWebMapper, times(nbSessionsFound)).domainToResponse(any(TrainingSession.class));
    }

    @Test
    void getAll_withCustomPagination_shouldReturnListAsJson() throws Exception {
        long coachId = 2L;
        int pageNumber = 1;
        int pageSize = 5;
        int nbSessionsFound = 3;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        List<TrainingSession> trainingSessions = Instancio.ofList(TrainingSession.class).size(nbSessionsFound).create();
        when(trainingSessionService.findAllByAthleteId(ATHLETE_ID, pageNumber, pageSize)).thenReturn(
            new PageResult<>(trainingSessions, nbSessionsFound, pageNumber, pageSize));
        when(trainingSessionWebMapper.domainToResponse(any(TrainingSession.class)))
            .thenReturn(Instancio.create(TrainingSessionResponse.class));

        mvc.perform(get(URL_PREFIX)
            .param("page", String.valueOf(pageNumber))
            .param("size", String.valueOf(pageSize)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(nbSessionsFound)))
            .andExpect(jsonPath("$.totalElements", is(nbSessionsFound)))
            .andExpect(jsonPath("$.page", is(pageNumber)))
            .andExpect(jsonPath("$.size", is(pageSize)));

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService).findAllByAthleteId(ATHLETE_ID, pageNumber, pageSize);
        verify(trainingSessionWebMapper, times(nbSessionsFound)).domainToResponse(any(TrainingSession.class));
    }

    @Test
    void getAll_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long coachId = 2L;
        int pageNumber = Integer.parseInt(DEFAULT_PAGE);
        int pageSize = Integer.parseInt(DEFAULT_SIZE);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        when(athleteService.findById(ATHLETE_ID, coachId)).thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(get(URL_PREFIX))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService, never()).findAllByAthleteId(ATHLETE_ID, pageNumber, pageSize);
        verify(trainingSessionWebMapper, never()).domainToResponse(any());
    }

    @Test
    void getById_shouldReturnTrainingSessionResponse_whenTrainingSessionExists() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        long trainingSessionId = 8L;
        TrainingSession persistedTrainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionService.findById(trainingSessionId, ATHLETE_ID)).thenReturn(persistedTrainingSession);
        TrainingSessionResponse trainingSessionResponse = Instancio.create(TrainingSessionResponse.class);
        when(trainingSessionWebMapper.domainToResponse(persistedTrainingSession)).thenReturn(trainingSessionResponse);

        ResultActions result = mvc.perform(get(URL_PREFIX + "/" + trainingSessionId))
            .andExpect(status().isOk());

        assertJsonMatchesTrainingSessionResponse(result, trainingSessionResponse);
        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService).findById(trainingSessionId, ATHLETE_ID);
        verify(trainingSessionWebMapper).domainToResponse(persistedTrainingSession);
    }

    @Test
    void getById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        when(athleteService.findById(ATHLETE_ID, coachId)).thenThrow(new AthleteNotFoundException(ATHLETE_ID));
        long trainingSessionId = 8L;

        mvc.perform(get(URL_PREFIX + "/" + trainingSessionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService, never()).findById(trainingSessionId, ATHLETE_ID);
    }

    @Test
    void getById_shouldReturnNotFound_whenTrainingSessionNotFoundException() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        long trainingSessionId = 8L;
        when(trainingSessionService.findById(trainingSessionId, ATHLETE_ID))
            .thenThrow(new TrainingSessionNotFoundException(trainingSessionId));

        mvc.perform(get(URL_PREFIX + "/" + trainingSessionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService).findById(trainingSessionId, ATHLETE_ID);
    }

    @Test
    void updateById_shouldReturnUpdatedTrainingSession() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        TrainingSessionRequest trainingSessionRequest = aValidSessionRequest();
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionWebMapper.requestToDomain(trainingSessionRequest, ATHLETE_ID))
            .thenReturn(trainingSession);
        TrainingSession persistedTrainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionService.update(trainingSession)).thenReturn(persistedTrainingSession);
        TrainingSessionResponse trainingSessionResponse = Instancio.create(TrainingSessionResponse.class);
        when(trainingSessionWebMapper.domainToResponse(persistedTrainingSession)).thenReturn(trainingSessionResponse);

        ResultActions result = mvc.perform(put(URL_PREFIX + "/" + 8L).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isOk());

        assertJsonMatchesTrainingSessionResponse(result, trainingSessionResponse);
        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionWebMapper).requestToDomain(trainingSessionRequest, ATHLETE_ID);
        verify(trainingSessionService).update(trainingSession);
        verify(trainingSessionWebMapper).domainToResponse(persistedTrainingSession);
    }

    @Test
    void updateById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        when(athleteService.findById(ATHLETE_ID, coachId)).thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(put(URL_PREFIX + "/" + 8L).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aValidSessionRequest())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService, never()).update(any(TrainingSession.class));
    }

    @Test
    void updateById_shouldReturnNotFound_whenTrainingSessionNotFoundException() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        long trainingSessionId = 8L;
        TrainingSessionRequest trainingSessionRequest = aValidSessionRequest();
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionWebMapper.requestToDomain(trainingSessionRequest, ATHLETE_ID))
            .thenReturn(trainingSession);
        when(trainingSessionService.update(trainingSession))
            .thenThrow(new TrainingSessionNotFoundException(trainingSessionId));

        mvc.perform(put(URL_PREFIX + "/" + trainingSessionId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService).update(any(TrainingSession.class));
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
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        long sessionId = 4L;

        mvc.perform(delete(URL_PREFIX + "/" + sessionId))
            .andExpect(status().isNoContent());

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService).deleteById(sessionId, ATHLETE_ID);
    }

    @Test
    void deleteById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        when(athleteService.findById(ATHLETE_ID, coachId)).thenThrow(new AthleteNotFoundException(ATHLETE_ID));
        long sessionId = 4L;

        mvc.perform(delete(URL_PREFIX + "/" + sessionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService, never()).deleteById(sessionId, ATHLETE_ID);
    }

    @Test
    void deleteById_shouldReturnNotFound_whenTrainingSessionNotFoundException() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        long sessionId = 4L;
        doThrow(new TrainingSessionNotFoundException(sessionId)).when(trainingSessionService).deleteById(sessionId, ATHLETE_ID);

        mvc.perform(delete(URL_PREFIX + "/" + sessionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(athleteService).findById(ATHLETE_ID, coachId);
        verify(trainingSessionService).deleteById(sessionId, ATHLETE_ID);
    }

    private TrainingSessionRequest aValidSessionRequest() {
        return Instancio.of(TrainingSessionRequest.class)
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
