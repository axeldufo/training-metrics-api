package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.shared.interfaces.web.ApiConstants;
import com.axel.trainingmetricsapi.shared.interfaces.web.ControllerTestSupport;
import com.axel.trainingmetricsapi.training.application.port.in.GetLatestLoadReportUseCase;
import com.axel.trainingmetricsapi.training.application.port.in.GetLoadReportByWeekUseCase;
import com.axel.trainingmetricsapi.training.application.port.in.GetLoadReportsByPeriodUseCase;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.exception.LoadReportNotFoundException;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.LoadReportResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoadReportController.class)
class LoadReportControllerTest extends ControllerTestSupport {

    private static final long ATHLETE_ID = 4L;
    private static final long COACH_ID = 2L;
    private static final String BASE_URL = ApiConstants.API_VERSION + "/athletes/" + ATHLETE_ID + "/reports/load";

    @MockitoBean
    private LoadReportWebMapper loadReportWebMapper;

    @MockitoBean
    private GetLoadReportByWeekUseCase getLoadReportByWeekUseCase;

    @MockitoBean
    private GetLatestLoadReportUseCase getLatestLoadReportUseCase;

    @MockitoBean
    private GetLoadReportsByPeriodUseCase getLoadReportsByPeriodUseCase;

    @MockitoBean
    private AuthenticatedCoachResolver authenticatedCoachResolver;

    @Autowired
    private Clock clock;

    @Autowired
    private MockMvc mvc;

    @Test
    void getByWeekStartDate_shouldReturn200_withPersistedReport() throws Exception {
        LocalDate monday = LocalDate.of(2025, Month.APRIL, 28);
        LocalDateTime updatedAt = LocalDateTime.of(2025, Month.APRIL, 30, 10, 0);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LoadReport report = new LoadReport(ATHLETE_ID, monday, 200, 2, updatedAt);
        when(getLoadReportByWeekUseCase.execute(ATHLETE_ID, COACH_ID, monday)).thenReturn(report);
        LoadReportResponse response = new LoadReportResponse(ATHLETE_ID, monday, 200, 2, updatedAt);
        when(loadReportWebMapper.domainToResponse(report)).thenReturn(response);

        mvc.perform(get(BASE_URL).param("weekStartDate", "2025-04-28"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.athleteId").value(ATHLETE_ID))
            .andExpect(jsonPath("$.weekStartDate").value("2025-04-28"))
            .andExpect(jsonPath("$.totalFosterLoad").value(200))
            .andExpect(jsonPath("$.sessionCount").value(2));

        verify(getLoadReportByWeekUseCase).execute(ATHLETE_ID, COACH_ID, monday);
        verify(loadReportWebMapper).domainToResponse(report);
    }

    @Test
    void getByWeekStartDate_shouldReturn200_withZeroReport_whenNoSessions() throws Exception {
        LocalDate monday = LocalDate.of(2025, Month.APRIL, 28);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LoadReport report = new LoadReport(ATHLETE_ID, monday, 0, 0, null);
        when(getLoadReportByWeekUseCase.execute(ATHLETE_ID, COACH_ID, monday)).thenReturn(report);
        LoadReportResponse response = new LoadReportResponse(ATHLETE_ID, monday, 0, 0, null);
        when(loadReportWebMapper.domainToResponse(report)).thenReturn(response);

        mvc.perform(get(BASE_URL).param("weekStartDate", "2025-04-28"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalFosterLoad").value(0))
            .andExpect(jsonPath("$.sessionCount").value(0));

        verify(getLoadReportByWeekUseCase).execute(ATHLETE_ID, COACH_ID, monday);
    }

    @Test
    void getByWeekStartDate_shouldReturn400_whenWeekStartDateIsInFuture() throws Exception {
        LocalDate futureMonday = LocalDate.now(clock).plusWeeks(2).with(DayOfWeek.MONDAY);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));

        mvc.perform(get(BASE_URL).param("weekStartDate", futureMonday.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].code").value("HTTP_VALIDATION_ERROR"));

        verify(getLoadReportByWeekUseCase, never()).execute(anyLong(), anyLong(), any());
    }

    @Test
    void getLatest_shouldReturn200_whenReportExists() throws Exception {
        LocalDate monday = LocalDate.of(2025, Month.MAY, 19);
        LocalDateTime updatedAt = LocalDateTime.of(2025, Month.MAY, 22, 12, 0);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LoadReport report = new LoadReport(ATHLETE_ID, monday, 300, 3, updatedAt);
        when(getLatestLoadReportUseCase.execute(ATHLETE_ID, COACH_ID)).thenReturn(report);
        LoadReportResponse response = new LoadReportResponse(ATHLETE_ID, monday, 300, 3, updatedAt);
        when(loadReportWebMapper.domainToResponse(report)).thenReturn(response);

        mvc.perform(get(BASE_URL + "/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.athleteId").value(ATHLETE_ID))
            .andExpect(jsonPath("$.totalFosterLoad").value(300));

        verify(getLatestLoadReportUseCase).execute(ATHLETE_ID, COACH_ID);
        verify(loadReportWebMapper).domainToResponse(report);
    }

    @Test
    void getLatest_shouldReturn404_whenNoReportExists() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        when(getLatestLoadReportUseCase.execute(ATHLETE_ID, COACH_ID))
            .thenThrow(new LoadReportNotFoundException(ATHLETE_ID));

        mvc.perform(get(BASE_URL + "/latest"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(getLatestLoadReportUseCase).execute(ATHLETE_ID, COACH_ID);
    }

    @Test
    void getByPeriod_shouldReturn200_withList() throws Exception {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 7);
        LocalDate to = LocalDate.of(2025, Month.APRIL, 21);
        LocalDate monday1 = LocalDate.of(2025, Month.APRIL, 7);
        LocalDate monday2 = LocalDate.of(2025, Month.APRIL, 14);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 12, 10, 0);
        List<LoadReport> reports = List.of(
            new LoadReport(ATHLETE_ID, monday1, 150, 1, updatedAt),
            new LoadReport(ATHLETE_ID, monday2, 300, 2, updatedAt)
        );
        when(getLoadReportsByPeriodUseCase.execute(ATHLETE_ID, COACH_ID, from, to)).thenReturn(reports);
        when(loadReportWebMapper.domainToResponse(any(LoadReport.class))).thenAnswer(inv -> {
            LoadReport r = inv.getArgument(0);
            return new LoadReportResponse(r.athleteId(), r.weekStartDate(), r.totalFosterLoad(), r.sessionCount(), r.updatedAt());
        });

        mvc.perform(get(BASE_URL).param("from", "2025-04-07").param("to", "2025-04-21"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(getLoadReportsByPeriodUseCase).execute(ATHLETE_ID, COACH_ID, from, to);
    }

    @Test
    void getByPeriod_shouldReturn400_whenFromIsInFuture() throws Exception {
        LocalDate futureFromMonday = LocalDate.now(clock).plusWeeks(2).with(DayOfWeek.MONDAY);
        LocalDate futureToMonday = LocalDate.now(clock).plusWeeks(3).with(DayOfWeek.MONDAY);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));

        mvc.perform(get(BASE_URL).param("from", futureFromMonday.toString()).param("to", futureToMonday.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].code").value("HTTP_VALIDATION_ERROR"));

        verify(getLoadReportsByPeriodUseCase, never()).execute(anyLong(), anyLong(), any(), any());
    }

    @Test
    void getByPeriod_shouldReturn400_whenFromIsAfterTo() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));

        mvc.perform(get(BASE_URL).param("from", "2025-05-26").param("to", "2025-04-01"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].code").value("HTTP_VALIDATION_ERROR"));

        verify(getLoadReportsByPeriodUseCase, never()).execute(anyLong(), anyLong(), any(), any());
    }

    @Test
    void getByPeriod_shouldReturn200_withDefaultToToday_whenToNotProvided() throws Exception {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 7);
        LocalDate today = LocalDate.now(clock);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        when(getLoadReportsByPeriodUseCase.execute(ATHLETE_ID, COACH_ID, from, today)).thenReturn(List.of());

        mvc.perform(get(BASE_URL).param("from", "2025-04-07"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        verify(getLoadReportsByPeriodUseCase).execute(ATHLETE_ID, COACH_ID, from, today);
    }
}
