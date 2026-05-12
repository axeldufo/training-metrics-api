package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.dto.request.TrainingSessionRequest;
import com.axel.trainingmetricsapi.dto.response.TrainingSessionResponse;
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

import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingSessionController.class)
class TrainingSessionControllerTest {

    private static final long ATHLETE_ID = 4L;
    private static final String URL_PREFIX = ApiConstants.API_VERSION + "/athletes/" + ATHLETE_ID + "/sessions";

    @MockitoBean
    private TrainingSessionWebMapper trainingSessionWebMapper;

    @MockitoBean
    private TrainingSessionService trainingSessionService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_shouldReturnCreatedTrainingSession_whenRequestIsValid() throws Exception {
        TrainingSessionRequest trainingSessionRequest = Instancio.of(TrainingSessionRequest.class)
            .generate(field(TrainingSessionRequest::rpe), gen -> gen.ints().range(1, 10))
            .generate(field(TrainingSessionRequest::durationInMin), gen -> gen.ints().min(1))
            .create();
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionWebMapper.requestToDomain(trainingSessionRequest, ATHLETE_ID)).thenReturn(trainingSession);
        TrainingSession persistedTrainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionService.save(trainingSession)).thenReturn(persistedTrainingSession);
        TrainingSessionResponse trainingSessionResponse = Instancio.create(TrainingSessionResponse.class);
        when(trainingSessionWebMapper.domainToResponse(persistedTrainingSession)).thenReturn(trainingSessionResponse);

        ResultActions result = mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isCreated());

        assertJsonMatchesTrainingSessionResponse(result, trainingSessionResponse);
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
        TrainingSessionRequest trainingSessionRequest = Instancio.of(TrainingSessionRequest.class)
            .generate(field(TrainingSessionRequest::rpe), gen -> gen.ints().range(1, 10))
            .generate(field(TrainingSessionRequest::durationInMin), gen -> gen.ints().min(1))
            .create();
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionWebMapper.requestToDomain(trainingSessionRequest, ATHLETE_ID)).thenReturn(trainingSession);
        when(trainingSessionService.save(trainingSession)).thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainingSessionRequest)))
            .andExpect(status().isNotFound());

        verify(trainingSessionWebMapper).requestToDomain(trainingSessionRequest, ATHLETE_ID);
        verify(trainingSessionService).save(trainingSession);
        verify(trainingSessionWebMapper, never()).domainToResponse(any());
    }

    @Test
    void getAll_shouldReturnListAsJson() throws Exception {
        List<TrainingSession> trainingSessions = Instancio.ofList(TrainingSession.class).size(3).create();
        when(trainingSessionService.findAllByAthleteId(ATHLETE_ID)).thenReturn(trainingSessions);
        when(trainingSessionWebMapper.domainToResponse(any(TrainingSession.class)))
            .thenReturn(Instancio.create(TrainingSessionResponse.class));

        mvc.perform(get(URL_PREFIX))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));

        verify(trainingSessionService).findAllByAthleteId(ATHLETE_ID);
        verify(trainingSessionWebMapper, times(3)).domainToResponse(any(TrainingSession.class));
    }

    @Test
    void getAll_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        when(trainingSessionService.findAllByAthleteId(ATHLETE_ID)).thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(get(URL_PREFIX))
            .andExpect(status().isNotFound());

        verify(trainingSessionService).findAllByAthleteId(ATHLETE_ID);
        verify(trainingSessionWebMapper, never()).domainToResponse(any());
    }

    private void assertJsonMatchesTrainingSessionResponse(ResultActions result, TrainingSessionResponse trainingSessionResponse) throws Exception {
        result.andExpect(jsonPath("$.id").value(trainingSessionResponse.id()))
            .andExpect(jsonPath("$.date").value(trainingSessionResponse.date().toString()))
            .andExpect(jsonPath("$.sport").value(trainingSessionResponse.sport().name()))
            .andExpect(jsonPath("$.rpe").value(trainingSessionResponse.rpe()))
            .andExpect(jsonPath("$.durationInMin").value(trainingSessionResponse.durationInMin()))
            .andExpect(jsonPath("$.targetZone").value(trainingSessionResponse.targetZone().name()))
            .andExpect(jsonPath("$.athleteId").value(trainingSessionResponse.athleteId()))
            .andExpect(jsonPath("$.aboveTargetAlert").value(trainingSessionResponse.aboveTargetAlert()));
    }

}
