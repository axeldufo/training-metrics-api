package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.mapper.AthleteMapper;
import com.axel.trainingmetricsapi.service.AthleteService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void getAll_shouldReturnListAsJson() throws Exception {

        List<Athlete> athletes = Instancio.ofList(Athlete.class).size(3).create();
        when(athleteService.findAll()).thenReturn(athletes);
        when(athleteMapper.toResponse(any(Athlete.class))).thenReturn(Instancio.create(AthleteResponse.class));

        mvc.perform(get("/athletes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));

        verify(athleteMapper, times(3)).toResponse(any(Athlete.class));
    }
}
