package com.axel.trainingmetricsapi.interfaces.web;

import com.axel.trainingmetricsapi.application.port.in.DeleteCoachUseCase;
import com.axel.trainingmetricsapi.application.port.in.GetCoachUseCase;
import com.axel.trainingmetricsapi.application.port.in.UpdateCoachUseCase;
import com.axel.trainingmetricsapi.interfaces.web.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.interfaces.web.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.domain.exception.CoachNotFoundException;
import com.axel.trainingmetricsapi.interfaces.web.dto.request.CoachUpdateRequest;
import com.axel.trainingmetricsapi.interfaces.web.dto.response.CoachResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoachController.class)
class CoachControllerTest extends SecurityMockControllerSupport {

    static final String URL_PREFIX = ApiConstants.API_VERSION + "/coaches/me";

    @MockitoBean
    private CoachWebMapper coachWebMapper;

    @MockitoBean
    private GetCoachUseCase getCoachUseCase;

    @MockitoBean
    private UpdateCoachUseCase updateCoachUseCase;

    @MockitoBean
    private DeleteCoachUseCase deleteCoachUseCase;

    @MockitoBean
    private AuthenticatedCoachResolver authenticatedCoachResolver;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getMe_shouldReturnCoachResponse_whenCoachExists() throws Exception {
        long coachId = 4L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        Coach persistedCoach = Instancio.create(Coach.class);
        when(getCoachUseCase.execute(coachId)).thenReturn(persistedCoach);
        CoachResponse coachResponse = Instancio.create(CoachResponse.class);
        when(coachWebMapper.domainToResponse(persistedCoach)).thenReturn(coachResponse);

        ResultActions result = mvc.perform(get(URL_PREFIX))
            .andExpect(status().isOk());

        assertJsonMatchesCoachResponse(result, coachResponse);
        verify(getCoachUseCase).execute(coachId);
        verify(coachWebMapper).domainToResponse(persistedCoach);
    }

    @Test
    void getMe_shouldReturnNotFound_whenCoachNotFoundException() throws Exception {
        long coachId = 4L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        when(getCoachUseCase.execute(coachId)).thenThrow(new CoachNotFoundException(coachId));

        mvc.perform(get(URL_PREFIX))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(getCoachUseCase).execute(coachId);
    }

    @Test
    void update_shouldReturnUpdatedCoach() throws Exception {
        long coachId = 4L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        CoachUpdateRequest coachUpdateRequest = Instancio.create(CoachUpdateRequest.class);
        String name = coachUpdateRequest.name();
        Coach persistedCoach = Instancio.create(Coach.class);
        when(getCoachUseCase.execute(coachId)).thenReturn(persistedCoach);
        CoachResponse coachResponse = Instancio.create(CoachResponse.class);
        when(coachWebMapper.domainToResponse(persistedCoach)).thenReturn(coachResponse);

        ResultActions result = mvc.perform(put(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(coachUpdateRequest)))
            .andExpect(status().isOk());

        assertJsonMatchesCoachResponse(result, coachResponse);
        verify(updateCoachUseCase).execute(coachId, name);
        verify(getCoachUseCase).execute(coachId);
        verify(coachWebMapper).domainToResponse(persistedCoach);
    }

    @Test
    void update_shouldReturnNotFound_whenCoachNotFoundException() throws Exception {
        long coachId = 4L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        doThrow(new CoachNotFoundException(coachId)).when(updateCoachUseCase).execute(eq(coachId), any());

        mvc.perform(put(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Instancio.create(CoachUpdateRequest.class))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(updateCoachUseCase).execute(eq(coachId), any());
        verify(getCoachUseCase, never()).execute(coachId);
    }

    @Test
    void update_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {
        CoachUpdateRequest coachUpdateRequest = new CoachUpdateRequest("");

        mvc.perform(put(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(coachUpdateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void delete_shouldReturnOK_whenCoachIsDeleted() throws Exception {
        long coachId = 4L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));

        mvc.perform(delete(URL_PREFIX))
            .andExpect(status().isNoContent());

        verify(deleteCoachUseCase).execute(coachId);
    }

    @Test
    void delete_shouldReturnNotFound_whenCoachNotFoundException() throws Exception {
        long coachId = 4L;
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(coachId));
        doThrow(new CoachNotFoundException(coachId)).when(deleteCoachUseCase).execute(coachId);

        mvc.perform(delete(URL_PREFIX))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(deleteCoachUseCase).execute(coachId);
    }

    private void assertJsonMatchesCoachResponse(ResultActions result, CoachResponse coachResponse) throws Exception {
        result.andExpect(jsonPath("$.id").value(coachResponse.id()))
            .andExpect(jsonPath("$.name").value(coachResponse.name()));
    }
}
