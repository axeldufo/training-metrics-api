package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.domain.exception.CoachNotFoundException;
import com.axel.trainingmetricsapi.dto.request.CoachUpdateRequest;
import com.axel.trainingmetricsapi.dto.response.CoachResponse;
import com.axel.trainingmetricsapi.service.CoachService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoachController.class)
class CoachControllerTest  extends BaseControllerTest{

    static final String URL_PREFIX = ApiConstants.API_VERSION + "/coaches";

    @MockitoBean
    private CoachWebMapper coachWebMapper;

    @MockitoBean
    private CoachService coachService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_shouldReturnListAsJson() throws Exception {
        List<Coach> coaches = Instancio.ofList(Coach.class).size(3).create();
        when(coachService.findAll()).thenReturn(coaches);
        when(coachWebMapper.domainToResponse(any(Coach.class))).thenReturn(Instancio.create(CoachResponse.class));

        mvc.perform(get(URL_PREFIX))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));

        verify(coachService).findAll();
        verify(coachWebMapper, times(3)).domainToResponse(any(Coach.class));
    }

    @Test
    void getById_shouldReturnCoachResponse_whenCoachExists() throws Exception {
        long coachId = 4L;
        Coach persistedCoach = Instancio.create(Coach.class);
        when(coachService.findById(coachId)).thenReturn(persistedCoach);
        CoachResponse coachResponse = Instancio.create(CoachResponse.class);
        when(coachWebMapper.domainToResponse(persistedCoach)).thenReturn(coachResponse);

        ResultActions result = mvc.perform(get(URL_PREFIX + "/" + coachId))
            .andExpect(status().isOk());

        assertJsonMatchesCoachResponse(result, coachResponse);
        verify(coachService).findById(coachId);
        verify(coachWebMapper).domainToResponse(persistedCoach);
    }

    @Test
    void getById_shouldReturnNotFound_whenCoachNotFoundException() throws Exception {
        long coachId = 4L;
        when(coachService.findById(coachId)).thenThrow(new CoachNotFoundException(coachId));

        mvc.perform(get(URL_PREFIX + "/" + coachId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(coachService).findById(coachId);
    }

    @Test
    void updateById_shouldReturnUpdatedCoach() throws Exception {
        long coachId = 4L;
        CoachUpdateRequest coachUpdateRequest = Instancio.create(CoachUpdateRequest.class);
        String name = coachUpdateRequest.name();
        Coach persistedCoach = Instancio.create(Coach.class);
        when(coachService.updateName(coachId, name)).thenReturn(persistedCoach);
        CoachResponse coachResponse = Instancio.create(CoachResponse.class);
        when(coachWebMapper.domainToResponse(persistedCoach)).thenReturn(coachResponse);

        ResultActions result = mvc.perform(put(URL_PREFIX + "/" + coachId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coachUpdateRequest)))
            .andExpect(status().isOk());

        assertJsonMatchesCoachResponse(result, coachResponse);
        verify(coachService).updateName(coachId, name);
        verify(coachWebMapper).domainToResponse(persistedCoach);
    }

    @Test
    void updateById_shouldReturnNotFound_whenCoachNotFoundException() throws Exception {
        long coachId = 4L;
        when(coachService.updateName(eq(coachId), any())).thenThrow(new CoachNotFoundException(coachId));

        mvc.perform(put(URL_PREFIX + "/" + coachId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Instancio.create(CoachUpdateRequest.class))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(coachService).updateName(eq(coachId), any());
    }

    @Test
    void updateById_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {
        CoachUpdateRequest coachUpdateRequest = new CoachUpdateRequest("");

        mvc.perform(put(URL_PREFIX + "/" + 4L).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coachUpdateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deleteById_shouldReturnOK_whenCoachIsDeleted() throws Exception {
        long coachId = 4L;

        mvc.perform(delete(URL_PREFIX + "/" + coachId))
            .andExpect(status().isNoContent());

        verify(coachService).deleteById(coachId);
    }

    @Test
    void deleteById_shouldReturnNotFound_whenCoachNotFoundException() throws Exception {
        long coachId = 4L;
        doThrow(new CoachNotFoundException(coachId)).when(coachService).deleteById(coachId);

        mvc.perform(delete(URL_PREFIX + "/" + coachId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(coachService).deleteById(coachId);
    }

    private void assertJsonMatchesCoachResponse(ResultActions result, CoachResponse coachResponse) throws Exception {
        result.andExpect(jsonPath("$.id").value(coachResponse.id()))
            .andExpect(jsonPath("$.name").value(coachResponse.name()));
    }
}
