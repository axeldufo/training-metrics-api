package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.identity.interfaces.web.security.SecurityMockControllerSupport;
import com.axel.trainingmetricsapi.training.application.port.in.GetAcwrReportUseCase;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.training.domain.AcwrAlert;
import com.axel.trainingmetricsapi.training.domain.AcwrReport;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.AcwrReportResponse;
import com.axel.trainingmetricsapi.shared.interfaces.web.ApiConstants;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcwrReportController.class)
class AcwrReportControllerTest extends SecurityMockControllerSupport {

    private static final long ATHLETE_ID = 4L;
    private static final long COACH_ID = 2L;
    private static final String URL = ApiConstants.API_VERSION + "/athletes/" + ATHLETE_ID + "/reports/acwr";

    @MockitoBean
    private AcwrReportWebMapper acwrReportWebMapper;

    @MockitoBean
    private GetAcwrReportUseCase getAcwrReportUseCase;

    @MockitoBean
    private AuthenticatedCoachResolver authenticatedCoachResolver;

    @Autowired
    private MockMvc mvc;

    @Test
    void getAcwrReport_shouldReturn200_withAllFieldsAsserted() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        AcwrReport report = Instancio.create(AcwrReport.class);
        when(getAcwrReportUseCase.execute(ATHLETE_ID, COACH_ID)).thenReturn(report);
        AcwrReportResponse response = new AcwrReportResponse(
            ATHLETE_ID, LocalDate.of(2024, Month.JANUARY, 28), 120.0, 100.0, 1.2, AcwrAlert.OK, 4, true);
        when(acwrReportWebMapper.domainToResponse(report)).thenReturn(response);

        mvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.athleteId").value(ATHLETE_ID))
            .andExpect(jsonPath("$.calculatedAt").value("2024-01-28"))
            .andExpect(jsonPath("$.acuteLoad").value(120.0))
            .andExpect(jsonPath("$.chronicLoad").value(100.0))
            .andExpect(jsonPath("$.acwr").value(1.2))
            .andExpect(jsonPath("$.acwrAlert").value("OK"))
            .andExpect(jsonPath("$.weeksOfDataAvailable").value(4))
            .andExpect(jsonPath("$.acwrReliable").value(true));

        verify(getAcwrReportUseCase).execute(ATHLETE_ID, COACH_ID);
        verify(acwrReportWebMapper).domainToResponse(report);
    }

    @Test
    void getAcwrReport_shouldReturn404_whenAthleteNotFound() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        when(getAcwrReportUseCase.execute(ATHLETE_ID, COACH_ID)).thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(get(URL))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(getAcwrReportUseCase).execute(ATHLETE_ID, COACH_ID);
        verify(acwrReportWebMapper, never()).domainToResponse(any());
    }
}
