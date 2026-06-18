package com.axel.trainingmetricsapi.athlete.interfaces.web;

import com.axel.trainingmetricsapi.athlete.application.port.in.CreateAthleteUseCase;
import com.axel.trainingmetricsapi.athlete.application.port.in.DeleteAthleteUseCase;
import com.axel.trainingmetricsapi.athlete.application.port.in.GetAthleteUseCase;
import com.axel.trainingmetricsapi.athlete.application.port.in.GetAthletesByCoachUseCase;
import com.axel.trainingmetricsapi.athlete.application.port.in.UpdateAthleteUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.athlete.interfaces.web.dto.AthleteRequest;
import com.axel.trainingmetricsapi.athlete.interfaces.web.dto.AthleteResponse;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.shared.interfaces.web.ControllerTestSupport;
import com.axel.trainingmetricsapi.shared.domain.PageResult;
import com.axel.trainingmetricsapi.shared.interfaces.web.ApiConstants;
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

import static com.axel.trainingmetricsapi.shared.interfaces.web.ApiConstants.DEFAULT_PAGE;
import static com.axel.trainingmetricsapi.shared.interfaces.web.ApiConstants.DEFAULT_SIZE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AthleteController.class)
class AthleteControllerTest extends ControllerTestSupport {

    static final String URL_PREFIX = ApiConstants.API_VERSION + "/athletes";

    @MockitoBean
    private AthleteWebMapper athleteWebMapper;

    @MockitoBean
    private GetAthletesByCoachUseCase getAthletesByCoachUseCase;

    @MockitoBean
    private CreateAthleteUseCase createAthleteUseCase;

    @MockitoBean
    private GetAthleteUseCase getAthleteUseCase;

    @MockitoBean
    private UpdateAthleteUseCase updateAthleteUseCase;

    @MockitoBean
    private DeleteAthleteUseCase deleteAthleteUseCase;

    @MockitoBean
    private AuthenticatedCoachResolver authenticatedCoachResolver;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_withDefaultPagination_shouldReturnListAsJson() throws Exception {
        long coachId = 4L;
        int pageNumber = Integer.parseInt(DEFAULT_PAGE);
        int pageSize = Integer.parseInt(DEFAULT_SIZE);
        int nbAthletesFound = 3;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        List<Athlete> athletes = Instancio.ofList(Athlete.class).size(nbAthletesFound).create();
        when(getAthletesByCoachUseCase.execute(coachId, pageNumber, pageSize)).thenReturn(
            new PageResult<>(athletes, nbAthletesFound, pageNumber, pageSize));
        when(athleteWebMapper.domainToResponse(any(Athlete.class))).thenReturn(Instancio.create(AthleteResponse.class));

        mvc.perform(get(URL_PREFIX))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(nbAthletesFound)))
            .andExpect(jsonPath("$.totalElements", is(nbAthletesFound)))
            .andExpect(jsonPath("$.page", is(pageNumber)))
            .andExpect(jsonPath("$.size", is(pageSize)));

        verify(getAthletesByCoachUseCase).execute(coachId, pageNumber, pageSize);
        verify(athleteWebMapper, times(nbAthletesFound)).domainToResponse(any(Athlete.class));
    }

    @Test
    void getAll_withCustomPagination_shouldReturnListAsJson() throws Exception {
        long coachId = 4L;
        int pageNumber = 1;
        int pageSize = 5;
        int nbAthletesFound = 3;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        List<Athlete> athletes = Instancio.ofList(Athlete.class).size(nbAthletesFound).create();
        when(getAthletesByCoachUseCase.execute(coachId, pageNumber, pageSize)).thenReturn(
            new PageResult<>(athletes, nbAthletesFound, pageNumber, pageSize));
        when(athleteWebMapper.domainToResponse(any(Athlete.class))).thenReturn(Instancio.create(AthleteResponse.class));

        mvc.perform(get(URL_PREFIX)
            .param("page", String.valueOf(pageNumber))
            .param("size", String.valueOf(pageSize)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(nbAthletesFound)))
            .andExpect(jsonPath("$.totalElements", is(nbAthletesFound)))
            .andExpect(jsonPath("$.page", is(pageNumber)))
            .andExpect(jsonPath("$.size", is(pageSize)));

        verify(getAthletesByCoachUseCase).execute(coachId, pageNumber, pageSize);
        verify(athleteWebMapper, times(nbAthletesFound)).domainToResponse(any(Athlete.class));
    }

    @Test
    void create_shouldReturnCreatedAthlete_whenRequestIsValid() throws Exception {
        long coachId = 4L;
        AthleteRequest athleteRequest = Instancio.create(AthleteRequest.class);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        Athlete athlete = Instancio.create(Athlete.class);
        when(athleteWebMapper.requestToDomain(athleteRequest, coachId)).thenReturn(athlete);
        Athlete persistedAthlete = Instancio.create(Athlete.class);
        when(createAthleteUseCase.execute(athlete)).thenReturn(persistedAthlete);
        AthleteResponse athleteResponse = Instancio.create(AthleteResponse.class);
        when(athleteWebMapper.domainToResponse(persistedAthlete)).thenReturn(athleteResponse);

        ResultActions result = mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(athleteRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", URL_PREFIX + "/" + athleteResponse.id()));

        assertJsonMatchesAthleteResponse(result, athleteResponse);
        verify(athleteWebMapper).requestToDomain(athleteRequest, coachId);
        verify(createAthleteUseCase).execute(athlete);
        verify(athleteWebMapper).domainToResponse(persistedAthlete);
    }

    @Test
    void create_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {
        AthleteRequest athleteRequest = new AthleteRequest("   ", "", null, null, null);

        mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(athleteRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getById_shouldReturnAthleteResponse_whenAthleteExists() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        long athleteId = 4L;
        Athlete persistedAthlete = Instancio.create(Athlete.class);
        when(getAthleteUseCase.execute(athleteId, coachId)).thenReturn(persistedAthlete);
        AthleteResponse athleteResponse = Instancio.create(AthleteResponse.class);
        when(athleteWebMapper.domainToResponse(persistedAthlete)).thenReturn(athleteResponse);

        ResultActions result = mvc.perform(get(URL_PREFIX + "/" + athleteId))
            .andExpect(status().isOk());

        assertJsonMatchesAthleteResponse(result, athleteResponse);
        verify(getAthleteUseCase).execute(athleteId, coachId);
        verify(athleteWebMapper).domainToResponse(persistedAthlete);
    }

    @Test
    void getById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        long athleteId = 4L;
        when(getAthleteUseCase.execute(athleteId, coachId)).thenThrow(new AthleteNotFoundException(athleteId));

        mvc.perform(get(URL_PREFIX + "/" + athleteId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(getAthleteUseCase).execute(athleteId, coachId);
    }

    @Test
    void updateById_shouldReturnUpdatedAthlete() throws Exception {
        long coachId = 4L;
        AthleteRequest athleteRequest = Instancio.create(AthleteRequest.class);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        Athlete athlete = Instancio.create(Athlete.class);
        when(athleteWebMapper.requestToDomain(athleteRequest, coachId)).thenReturn(athlete);
        Athlete persistedAthlete = Instancio.create(Athlete.class);
        when(updateAthleteUseCase.execute(athlete, coachId)).thenReturn(persistedAthlete);
        AthleteResponse athleteResponse = Instancio.create(AthleteResponse.class);
        when(athleteWebMapper.domainToResponse(persistedAthlete)).thenReturn(athleteResponse);

        ResultActions result = mvc.perform(put(URL_PREFIX + "/" + 4L).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(athleteRequest)))
            .andExpect(status().isOk());

        assertJsonMatchesAthleteResponse(result, athleteResponse);
        verify(athleteWebMapper).requestToDomain(athleteRequest, coachId);
        verify(updateAthleteUseCase).execute(athlete, coachId);
        verify(athleteWebMapper).domainToResponse(persistedAthlete);
    }

    @Test
    void updateById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long athleteId = 4L;
        long coachId = 2L;
        Athlete athlete = Instancio.create(Athlete.class);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        when(athleteWebMapper.requestToDomain(any(AthleteRequest.class), anyLong())).thenReturn(athlete);
        when(updateAthleteUseCase.execute(athlete, coachId)).thenThrow(new AthleteNotFoundException(athleteId));

        mvc.perform(put(URL_PREFIX + "/" + athleteId).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Instancio.create(AthleteRequest.class))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(updateAthleteUseCase).execute(any(Athlete.class), eq(coachId));
    }

    @Test
    void updateById_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {
        AthleteRequest athleteRequest = new AthleteRequest("   ", "", null, null, null);

        mvc.perform(put(URL_PREFIX + "/" + 4L).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(athleteRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void deleteById_shouldReturnOK_whenAthleteIsDeleted() throws Exception {
        long athleteId = 4L;
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));

        mvc.perform(delete(URL_PREFIX + "/" + athleteId))
            .andExpect(status().isNoContent());

        verify(deleteAthleteUseCase).execute(athleteId, coachId);
    }

    @Test
    void deleteById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long athleteId = 4L;
        long coachId = 2L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        doThrow(new AthleteNotFoundException(athleteId)).when(deleteAthleteUseCase).execute(athleteId, coachId);

        mvc.perform(delete(URL_PREFIX + "/" + athleteId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(deleteAthleteUseCase).execute(athleteId, coachId);
    }

    private void assertJsonMatchesAthleteResponse(ResultActions result, AthleteResponse athleteResponse) throws Exception {
        result.andExpect(jsonPath("$.id").value(athleteResponse.id()))
            .andExpect(jsonPath("$.firstName").value(athleteResponse.firstName()))
            .andExpect(jsonPath("$.lastName").value(athleteResponse.lastName()))
            .andExpect(jsonPath("$.birthDate").value(athleteResponse.birthDate().toString()))
            .andExpect(jsonPath("$.sport").value(athleteResponse.sport().name()))
            .andExpect(jsonPath("$.coachId").value(athleteResponse.coachId()))
            .andExpect(jsonPath("$.weightInKg").value(athleteResponse.weightInKg()));
    }
}
