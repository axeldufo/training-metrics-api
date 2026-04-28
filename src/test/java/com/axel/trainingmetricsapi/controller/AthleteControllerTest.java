package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.dto.request.AthleteRequest;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.mapper.AthleteMapper;
import com.axel.trainingmetricsapi.service.AthleteService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AthleteController.class)
class AthleteControllerTest {

    @MockitoBean
    private AthleteMapper athleteMapper;

    @MockitoBean
    private AthleteService athleteService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_shouldReturnListAsJson() throws Exception {

        List<Athlete> athletes = Instancio.ofList(Athlete.class).size(3).create();
        when(athleteService.findAll()).thenReturn(athletes);
        when(athleteMapper.domainToResponse(any(Athlete.class))).thenReturn(Instancio.create(AthleteResponse.class));

        mvc.perform(get("/athletes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));

        verify(athleteMapper, times(3)).domainToResponse(any(Athlete.class));
    }

    @Test
    void create_shouldReturnCreatedAthlete_whenRequestIsValid() throws Exception {

        Athlete athlete = Instancio.create(Athlete.class);
        when(athleteMapper.requestToDomain(any(AthleteRequest.class))).thenReturn(athlete);
        Athlete persistedAthlete = Instancio.create(Athlete.class);
        when(athleteService.save(athlete)).thenReturn(persistedAthlete);
        AthleteResponse athleteResponse = Instancio.create(AthleteResponse.class);
        when(athleteMapper.domainToResponse(persistedAthlete)).thenReturn(athleteResponse);

        ResultActions result = mvc.perform(post("/athletes").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Instancio.create(AthleteRequest.class))))
            .andExpect(status().isCreated());

        assertJsonMatchesAthleteResponse(result, athleteResponse);
        verify(athleteMapper).requestToDomain(any(AthleteRequest.class));
        verify(athleteService).save(athlete);
        verify(athleteMapper).domainToResponse(persistedAthlete);
    }

    @Test
    void create_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {

        AthleteRequest athleteRequest = new AthleteRequest("   ", "", null, null, null);

        mvc.perform(post("/athletes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(athleteRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getById_shouldReturnAthleteResponse_whenAthleteExists() throws Exception {
        long athleteId = 4L;
        Athlete persistedAthlete = Instancio.create(Athlete.class);
        when(athleteService.findById(athleteId)).thenReturn(persistedAthlete);
        AthleteResponse athleteResponse = Instancio.create(AthleteResponse.class);
        when(athleteMapper.domainToResponse(persistedAthlete)).thenReturn(athleteResponse);

        ResultActions result = mvc.perform(get("/athletes/" + athleteId))
            .andExpect(status().isOk());

        assertJsonMatchesAthleteResponse(result, athleteResponse);
        verify(athleteService).findById(athleteId);
        verify(athleteMapper).domainToResponse(persistedAthlete);
    }

    @Test
    void getById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long athleteId = 4L;
        when(athleteService.findById(athleteId)).thenThrow(new AthleteNotFoundException(athleteId));

        mvc.perform(get("/athletes/" + athleteId))
            .andExpect(status().isNotFound());

        verify(athleteService).findById(athleteId);
    }

    @Test
    void updateById_shouldReturnUpdatedAthlete() throws Exception {
        Athlete athlete = Instancio.create(Athlete.class);
        when(athleteMapper.requestToDomain(any(AthleteRequest.class))).thenReturn(athlete);
        Athlete persistedAthlete = Instancio.create(Athlete.class);
        when(athleteService.update(athlete)).thenReturn(persistedAthlete);
        AthleteResponse athleteResponse = Instancio.create(AthleteResponse.class);
        when(athleteMapper.domainToResponse(persistedAthlete)).thenReturn(athleteResponse);

        ResultActions result = mvc.perform(put("/athletes/" + 4L).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Instancio.create(AthleteRequest.class))))
            .andExpect(status().isOk());

        assertJsonMatchesAthleteResponse(result, athleteResponse);
        verify(athleteMapper).requestToDomain(any(AthleteRequest.class));
        verify(athleteService).update(athlete);
        verify(athleteMapper).domainToResponse(persistedAthlete);
    }

    @Test
    void updateById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long athleteId = 4L;
        Athlete athlete = Instancio.create(Athlete.class);
        when(athleteMapper.requestToDomain(any(AthleteRequest.class))).thenReturn(athlete);
        when(athleteService.update(athlete)).thenThrow(new AthleteNotFoundException(athleteId));

        mvc.perform(put("/athletes/" + athleteId).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Instancio.create(AthleteRequest.class))))
            .andExpect(status().isNotFound());

        verify(athleteService).update(any(Athlete.class));
    }

    @Test
    void updateById_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {
        AthleteRequest athleteRequest = new AthleteRequest("   ", "", null, null, null);

        mvc.perform(put("/athletes/" + 4L).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(athleteRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void deleteById_shouldReturnOK_whenAthleteIsDeleted() throws Exception {
        long athleteId = 4L;

        mvc.perform(delete("/athletes/" + athleteId))
            .andExpect(status().isNoContent());

        verify(athleteService).deleteById(athleteId);
    }

    @Test
    void deleteById_shouldReturnNotFound_whenAthleteNotFoundException() throws Exception {
        long athleteId = 4L;
        doThrow(new AthleteNotFoundException(athleteId)).when(athleteService).deleteById(athleteId);

        mvc.perform(delete("/athletes/" + athleteId))
            .andExpect(status().isNotFound());

        verify(athleteService).deleteById(athleteId);
    }

    private void assertJsonMatchesAthleteResponse(ResultActions result, AthleteResponse athleteResponse) throws Exception {
            result.andExpect(jsonPath("$.id").value(athleteResponse.id()))
            .andExpect(jsonPath("$.firstName").value(athleteResponse.firstName()))
            .andExpect(jsonPath("$.lastName").value(athleteResponse.lastName()))
            .andExpect(jsonPath("$.birthDate").value(athleteResponse.birthDate().toString()))
            .andExpect(jsonPath("$.sport").value(athleteResponse.sport().name()))
            .andExpect(jsonPath("$.weightInKg").value(athleteResponse.weightInKg()));
    }
}
