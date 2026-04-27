package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.dto.request.AthleteRequest;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.mapper.AthleteMapper;
import com.axel.trainingmetricsapi.service.AthleteService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
            .andExpect(jsonPath("$.length()").value(3));

        verify(athleteMapper, times(3)).domainToResponse(any(Athlete.class));
    }

    @Test
    void post_shouldCallSaveAndReturnThePersistedAthlete() throws Exception {

        Athlete athlete = Instancio.create(Athlete.class);
        when(athleteMapper.requestToDomain(any(AthleteRequest.class))).thenReturn(athlete);
        Athlete persistedAthlete = Instancio.create(Athlete.class);
        when(athleteService.save(athlete)).thenReturn(persistedAthlete);
        AthleteResponse athleteResponse = Instancio.create(AthleteResponse.class);
        when(athleteMapper.domainToResponse(any(Athlete.class))).thenReturn(athleteResponse);

        mvc.perform(post("/athletes").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Instancio.create(AthleteRequest.class))))
            .andExpect(status().isCreated())
            .andExpect(content().json(objectMapper.writeValueAsString(athleteResponse)));

        verify(athleteMapper).requestToDomain(any(AthleteRequest.class));
        verify(athleteService).save(athlete);
        verify(athleteMapper).domainToResponse(persistedAthlete);
    }

    @Test
    void post_shouldReturnBadRequestWhenArgumentsNotValid() throws Exception {

        AthleteRequest athleteRequest = new AthleteRequest("   ", "", null, null, null);

        mvc.perform(post("/athletes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(athleteRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.length()").value(3));

    }
}
