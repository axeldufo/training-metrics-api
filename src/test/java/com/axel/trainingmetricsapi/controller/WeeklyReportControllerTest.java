package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.AcwrAlert;
import com.axel.trainingmetricsapi.domain.CorrelationAlert;
import com.axel.trainingmetricsapi.domain.WeeklyReport;
import com.axel.trainingmetricsapi.domain.exception.WeeklyReportNotFoundException;
import com.axel.trainingmetricsapi.dto.response.WeeklyReportResponse;
import com.axel.trainingmetricsapi.service.AthleteService;
import com.axel.trainingmetricsapi.service.WeeklyReportService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeeklyReportController.class)
class WeeklyReportControllerTest extends SecurityMockControllerSupport {

    private static final long ATHLETE_ID = 5L;
    private static final long COACH_ID = 2L;
    private static final String URL_PREFIX = ApiConstants.API_VERSION + "/athletes/" + ATHLETE_ID + "/reports/weekly";
    private static final LocalDate MONDAY = LocalDate.of(2025, 5, 19);

    @MockitoBean
    private WeeklyReportService weeklyReportService;

    @MockitoBean
    private WeeklyReportWebMapper weeklyReportWebMapper;

    @MockitoBean
    private AthleteService athleteService;

    @MockitoBean
    private AuthenticatedCoachResolver authenticatedCoachResolver;

    @Autowired
    private MockMvc mvc;

    @Test
    void getByWeekStartDate_200_nominal_allFieldsReturned() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        WeeklyReport domain = aReport(MONDAY, true, 100, 2, CorrelationAlert.GOOD_ADAPTATION);
        when(weeklyReportService.getWeeklyReport(ATHLETE_ID, MONDAY)).thenReturn(domain);
        WeeklyReportResponse response = aResponse(MONDAY, true, 100, 2, CorrelationAlert.GOOD_ADAPTATION);
        when(weeklyReportWebMapper.domainToResponse(domain)).thenReturn(response);

        mvc.perform(get(URL_PREFIX).param("weekStartDate", "2025-05-19"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.athleteId").value(ATHLETE_ID))
            .andExpect(jsonPath("$.weekStartDate").value("2025-05-19"))
            .andExpect(jsonPath("$.wellnessAvailable").value(true))
            .andExpect(jsonPath("$.sessionCount").value(2))
            .andExpect(jsonPath("$.totalFosterLoad").value(100))
            .andExpect(jsonPath("$.acuteLoad").value(100.0))
            .andExpect(jsonPath("$.chronicLoad").value(80.0))
            .andExpect(jsonPath("$.acwr").value(1.0))
            .andExpect(jsonPath("$.acwrAlert").value("OK"))
            .andExpect(jsonPath("$.acwrReliable").value(true))
            .andExpect(jsonPath("$.correlationAlert").value("GOOD_ADAPTATION"));

        verify(athleteService).findById(ATHLETE_ID, COACH_ID);
        verify(weeklyReportService).getWeeklyReport(ATHLETE_ID, MONDAY);
        verify(weeklyReportWebMapper).domainToResponse(domain);
    }

    @Test
    void getByWeekStartDate_200_zeroLoad_sessionCount0_wellnessAvailable() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        WeeklyReport domain = aReport(MONDAY, true, 0, 0, CorrelationAlert.NO_ALERT);
        when(weeklyReportService.getWeeklyReport(ATHLETE_ID, MONDAY)).thenReturn(domain);
        WeeklyReportResponse response = aResponse(MONDAY, true, 0, 0, CorrelationAlert.NO_ALERT);
        when(weeklyReportWebMapper.domainToResponse(domain)).thenReturn(response);

        mvc.perform(get(URL_PREFIX).param("weekStartDate", "2025-05-19"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionCount").value(0))
            .andExpect(jsonPath("$.wellnessAvailable").value(true))
            .andExpect(jsonPath("$.correlationAlert").value("NO_ALERT"));

        verify(athleteService).findById(ATHLETE_ID, COACH_ID);
        verify(weeklyReportService).getWeeklyReport(ATHLETE_ID, MONDAY);
        verify(weeklyReportWebMapper).domainToResponse(domain);
    }

    @Test
    void getByWeekStartDate_200_partial_wellnessUnavailable_insufficientData() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        WeeklyReport domain = aReport(MONDAY, false, 100, 2, CorrelationAlert.INSUFFICIENT_DATA);
        when(weeklyReportService.getWeeklyReport(ATHLETE_ID, MONDAY)).thenReturn(domain);
        WeeklyReportResponse response = aResponse(MONDAY, false, 100, 2, CorrelationAlert.INSUFFICIENT_DATA);
        when(weeklyReportWebMapper.domainToResponse(domain)).thenReturn(response);

        mvc.perform(get(URL_PREFIX).param("weekStartDate", "2025-05-19"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wellnessAvailable").value(false))
            .andExpect(jsonPath("$.correlationAlert").value("INSUFFICIENT_DATA"))
            .andExpect(jsonPath("$.perceivedDifficulty").doesNotExist());

        verify(athleteService).findById(ATHLETE_ID, COACH_ID);
        verify(weeklyReportService).getWeeklyReport(ATHLETE_ID, MONDAY);
        verify(weeklyReportWebMapper).domainToResponse(domain);
    }

    @Test
    void getByWeekStartDate_400_futureWeekStartDate() throws Exception {
        LocalDate futureMonday = LocalDate.now().plusWeeks(2).with(DayOfWeek.MONDAY);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));

        mvc.perform(get(URL_PREFIX).param("weekStartDate", futureMonday.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].code").value("HTTP_VALIDATION_ERROR"));

        verifyNoInteractions(athleteService);
        verifyNoInteractions(weeklyReportService);
        verifyNoInteractions(weeklyReportWebMapper);
    }

    @Test
    void getByWeekStartDate_404_noDataAtAll() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        when(weeklyReportService.getWeeklyReport(ATHLETE_ID, MONDAY))
            .thenThrow(new WeeklyReportNotFoundException(ATHLETE_ID, MONDAY));

        mvc.perform(get(URL_PREFIX).param("weekStartDate", "2025-05-19"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(weeklyReportService).getWeeklyReport(ATHLETE_ID, MONDAY);
        verifyNoInteractions(weeklyReportWebMapper);
    }

    @Test
    void getLatest_200_nominal() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        WeeklyReport domain = aReport(MONDAY, true, 150, 3, CorrelationAlert.NO_ALERT);
        when(weeklyReportService.getLatestWeeklyReport(ATHLETE_ID)).thenReturn(domain);
        WeeklyReportResponse response = aResponse(MONDAY, true, 150, 3, CorrelationAlert.NO_ALERT);
        when(weeklyReportWebMapper.domainToResponse(domain)).thenReturn(response);

        mvc.perform(get(URL_PREFIX + "/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionCount").value(3));

        verify(athleteService).findById(ATHLETE_ID, COACH_ID);
        verify(weeklyReportService).getLatestWeeklyReport(ATHLETE_ID);
        verify(weeklyReportWebMapper).domainToResponse(domain);
    }

    @Test
    void getLatest_404_noLoadReportExists() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        when(weeklyReportService.getLatestWeeklyReport(ATHLETE_ID))
            .thenThrow(new WeeklyReportNotFoundException(ATHLETE_ID));

        mvc.perform(get(URL_PREFIX + "/latest"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(athleteService).findById(ATHLETE_ID, COACH_ID);
        verify(weeklyReportService).getLatestWeeklyReport(ATHLETE_ID);
        verifyNoInteractions(weeklyReportWebMapper);
    }

    @Test
    void getByPeriod_200_returnsListIncludingZeroLoadEntries() throws Exception {
        LocalDate from = LocalDate.of(2025, 4, 28);
        LocalDate to = LocalDate.of(2025, 5, 12);
        LocalDate m1 = LocalDate.of(2025, 4, 28);
        LocalDate m2 = LocalDate.of(2025, 5, 5);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        List<WeeklyReport> reports = List.of(
            aReport(m1, true, 100, 2, CorrelationAlert.NO_ALERT),
            aReport(m2, true, 0, 0, CorrelationAlert.NO_ALERT)
        );
        when(weeklyReportService.getWeeklyReportsByPeriod(ATHLETE_ID, from, to)).thenReturn(reports);
        when(weeklyReportWebMapper.domainToResponse(reports.get(0)))
            .thenReturn(aResponse(m1, true, 100, 2, CorrelationAlert.NO_ALERT));
        when(weeklyReportWebMapper.domainToResponse(reports.get(1)))
            .thenReturn(aResponse(m2, true, 0, 0, CorrelationAlert.NO_ALERT));

        mvc.perform(get(URL_PREFIX)
                .param("from", from.toString())
                .param("to", to.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(athleteService).findById(ATHLETE_ID, COACH_ID);
        verify(weeklyReportService).getWeeklyReportsByPeriod(ATHLETE_ID, from, to);
        verify(weeklyReportWebMapper, times(reports.size())).domainToResponse(any(WeeklyReport.class));
    }

    @Test
    void getByPeriod_200_returnsEmptyList_whenNoDataAtAll() throws Exception {
        LocalDate from = LocalDate.of(2025, 4, 28);
        LocalDate to = LocalDate.of(2025, 5, 5);
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        when(weeklyReportService.getWeeklyReportsByPeriod(ATHLETE_ID, from, to)).thenReturn(List.of());

        mvc.perform(get(URL_PREFIX)
                .param("from", from.toString())
                .param("to", to.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        verify(athleteService).findById(ATHLETE_ID, COACH_ID);
        verify(weeklyReportService).getWeeklyReportsByPeriod(ATHLETE_ID, from, to);
        verify(weeklyReportWebMapper, never()).domainToResponse(any(WeeklyReport.class));
    }

    @Test
    void getByPeriod_200_shouldUseToday_whenToIsAbsent() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LocalDate from = LocalDate.of(2024, 1, 1);
        when(weeklyReportService.getWeeklyReportsByPeriod(eq(ATHLETE_ID), eq(from), any(LocalDate.class)))
            .thenReturn(List.of());

        mvc.perform(get(URL_PREFIX).param("from", from.toString()))
            .andExpect(status().isOk());

        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(weeklyReportService).getWeeklyReportsByPeriod(eq(ATHLETE_ID), eq(from), toCaptor.capture());
        assertThat(toCaptor.getValue()).isEqualTo(LocalDate.now());
    }

    @Test
    void getByPeriod_400_fromAfterTo() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));

        mvc.perform(get(URL_PREFIX)
                .param("from", "2025-05-12")
                .param("to", "2025-04-28"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].code").value("HTTP_VALIDATION_ERROR"));

        verify(athleteService).findById(ATHLETE_ID, COACH_ID);
        verifyNoInteractions(weeklyReportService);
        verifyNoInteractions(weeklyReportWebMapper);
    }

    private WeeklyReport aReport(LocalDate weekStart, boolean wellnessAvailable,
                                  int totalLoad, int sessionCount, CorrelationAlert alert) {
        return new WeeklyReport(
            ATHLETE_ID, weekStart, wellnessAvailable,
            totalLoad, sessionCount,
            (double) totalLoad, 80.0, 1.0,
            AcwrAlert.OK, true,
            wellnessAvailable ? 3 : null,
            wellnessAvailable ? 3 : null,
            wellnessAvailable ? 4 : null,
            wellnessAvailable ? 0.0 : null,
            wellnessAvailable ? 0.0 : null,
            wellnessAvailable ? 0.0 : null,
            Set.of(), Set.of(), Set.of(),
            alert
        );
    }

    private WeeklyReportResponse aResponse(LocalDate weekStart, boolean wellnessAvailable,
                                            int totalLoad, int sessionCount, CorrelationAlert alert) {
        return new WeeklyReportResponse(
            ATHLETE_ID, weekStart, wellnessAvailable,
            totalLoad, sessionCount,
            (double) totalLoad, 80.0, 1.0,
            AcwrAlert.OK, true,
            wellnessAvailable ? 3 : null,
            wellnessAvailable ? 3 : null,
            wellnessAvailable ? 4 : null,
            wellnessAvailable ? 0.0 : null,
            wellnessAvailable ? 0.0 : null,
            wellnessAvailable ? 0.0 : null,
            Set.of(), Set.of(), Set.of(),
            alert
        );
    }
}
