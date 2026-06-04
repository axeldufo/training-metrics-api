package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.CorrelationAlert;
import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.domain.WeeklyReport;
import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.domain.exception.WeeklyReportNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyReportServiceImplTest {

    private static final long ATHLETE_ID = 1L;
    private static final LocalDate MONDAY = LocalDate.of(2025, Month.MAY, 19);

    @Mock
    private LoadReportRepository loadReportRepository;

    @Mock
    private WeeklyWellnessRepository weeklyWellnessRepository;

    @Mock
    private LoadReportService loadReportService;

    @InjectMocks
    private WeeklyReportServiceImpl weeklyReportService;

    @Test
    void getWeeklyReport_nominal_loadFoundInDB_wellnessFound_returnsReport() {
        LoadReport current = aLoad(MONDAY, 100, 2);
        LoadReport prev1 = aLoad(MONDAY.minusWeeks(1), 80, 1);
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, MONDAY.minusWeeks(3), MONDAY))
            .thenReturn(List.of(current, prev1));
        WeeklyWellness wellness = aWellness(MONDAY, 3, 3, 4);
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY.minusWeeks(4), MONDAY))
            .thenReturn(List.of(wellness));

        WeeklyReport report = weeklyReportService.getWeeklyReport(ATHLETE_ID, MONDAY);

        assertThat(report.athleteId()).isEqualTo(ATHLETE_ID);
        assertThat(report.weekStartDate()).isEqualTo(MONDAY);
        assertThat(report.wellnessAvailable()).isTrue();
        assertThat(report.sessionCount()).isEqualTo(2);
        assertThat(report.totalFosterLoad()).isEqualTo(100);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
    }

    @Test
    void getWeeklyReport_loadReportsEmpty_onTheFlyLoadHasSessions_returnsReport() {
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, MONDAY.minusWeeks(3), MONDAY))
            .thenReturn(List.of());
        LoadReport onTheFlyLoad = aLoad(MONDAY, 120, 2);
        when(loadReportService.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(onTheFlyLoad);
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY.minusWeeks(4), MONDAY))
            .thenReturn(List.of(aWellness(MONDAY, 3, 3, 4)));

        WeeklyReport report = weeklyReportService.getWeeklyReport(ATHLETE_ID, MONDAY);

        assertThat(report.sessionCount()).isEqualTo(2);
        assertThat(report.wellnessAvailable()).isTrue();
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
        verify(loadReportService).findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);
    }

    @Test
    void getWeeklyReport_loadReportsEmpty_noWellness_throwsNotFoundException() {
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, MONDAY.minusWeeks(3), MONDAY))
            .thenReturn(List.of());
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY.minusWeeks(4), MONDAY))
            .thenReturn(List.of());

        assertThatThrownBy(() -> weeklyReportService.getWeeklyReport(ATHLETE_ID, MONDAY))
            .isInstanceOf(WeeklyReportNotFoundException.class);

        verifyNoInteractions(loadReportService);
    }

    @Test
    void getWeeklyReport_zeroLoad_onTheFlyZeroLoadReport_wellnessPresent_returns200() {
        // 404 only when BOTH load absent AND wellness absent
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, MONDAY.minusWeeks(3), MONDAY))
            .thenReturn(List.of());
        LoadReport onTheFlyZeroLoad = new LoadReport(ATHLETE_ID, MONDAY, 0, 0, null);
        when(loadReportService.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(onTheFlyZeroLoad);
        // Two wellness entries: current + previous week → deltas non-null → correlationAlert != INSUFFICIENT_DATA
        WeeklyWellness wellness = aWellness(MONDAY, 3, 2, 4);
        WeeklyWellness wellnessPrev = aWellness(MONDAY.minusWeeks(1), 3, 3, 4);
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY.minusWeeks(4), MONDAY))
            .thenReturn(List.of(wellness, wellnessPrev));

        WeeklyReport report = weeklyReportService.getWeeklyReport(ATHLETE_ID, MONDAY);

        assertThat(report.sessionCount()).isZero();
        assertThat(report.totalFosterLoad()).isZero();
        assertThat(report.wellnessAvailable()).isTrue();
        assertThat(report.correlationAlert()).isNotEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
        verify(loadReportService).findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);
    }

    @Test
    void getWeeklyReport_loadFoundWithSessionCount0_wellnessFound_returnsReportWithNoInsufficientData() {
        LoadReport zeroLoad = new LoadReport(ATHLETE_ID, MONDAY, 0, 0, LocalDateTime.now());
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, MONDAY.minusWeeks(3), MONDAY))
            .thenReturn(List.of(zeroLoad));
        WeeklyWellness wellness = aWellness(MONDAY, 3, 3, 4);
        WeeklyWellness wellnessPrev = aWellness(MONDAY.minusWeeks(1), 3, 3, 4);
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY.minusWeeks(4), MONDAY))
            .thenReturn(List.of(wellness, wellnessPrev));

        WeeklyReport report = weeklyReportService.getWeeklyReport(ATHLETE_ID, MONDAY);

        assertThat(report.sessionCount()).isZero();
        assertThat(report.wellnessAvailable()).isTrue();
        assertThat(report.correlationAlert()).isNotEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
    }

    @Test
    void getWeeklyReport_loadFoundNoWellness_returnsInsufficientData() {
        LoadReport load = aLoad(MONDAY, 100, 2);
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, MONDAY.minusWeeks(3), MONDAY))
            .thenReturn(List.of(load));
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY.minusWeeks(4), MONDAY))
            .thenReturn(List.of());

        WeeklyReport report = weeklyReportService.getWeeklyReport(ATHLETE_ID, MONDAY);

        assertThat(report.wellnessAvailable()).isFalse();
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
        verifyNoInteractions(loadReportService);
    }

    @Test
    void getLatestWeeklyReport_loadReportFound_delegatesToGetWeeklyReport() {
        LoadReport latest = aLoad(MONDAY, 150, 2);
        when(loadReportRepository.findLatestByAthleteId(ATHLETE_ID)).thenReturn(Optional.of(latest));
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            eq(ATHLETE_ID), any(), eq(MONDAY)))
            .thenReturn(List.of(latest));
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            eq(ATHLETE_ID), any(), eq(MONDAY)))
            .thenReturn(List.of());

        WeeklyReport report = weeklyReportService.getLatestWeeklyReport(ATHLETE_ID);

        assertThat(report.weekStartDate()).isEqualTo(MONDAY);
        assertThat(report.sessionCount()).isEqualTo(2);
        assertThat(report.wellnessAvailable()).isFalse();
        verify(loadReportRepository).findLatestByAthleteId(ATHLETE_ID);
    }

    @Test
    void getLatestWeeklyReport_noLoadReport_throwsNotFoundException() {
        when(loadReportRepository.findLatestByAthleteId(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> weeklyReportService.getLatestWeeklyReport(ATHLETE_ID))
            .isInstanceOf(WeeklyReportNotFoundException.class);
    }

    @Test
    void getWeeklyReportsByPeriod_3MondaysInRange_2HaveLoad_1ZeroLoad_returns3Reports() {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 28);
        LocalDate to = LocalDate.of(2025, Month.MAY, 12);
        // Mondays: Apr-28, May-05, May-12
        LocalDate m1 = LocalDate.of(2025, Month.APRIL, 28);
        LocalDate m2 = LocalDate.of(2025, Month.MAY, 5);
        LocalDate m3 = LocalDate.of(2025, Month.MAY, 12);

        // m1: has load in DB
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, m1.minusWeeks(3), m1))
            .thenReturn(List.of(aLoad(m1, 100, 1)));
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, m1.minusWeeks(4), m1))
            .thenReturn(List.of());

        // m2: zero-load week (athlete did not train), wellness present
        LoadReport zeroLoadWithTs = new LoadReport(ATHLETE_ID, m2, 0, 0, null);
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, m2.minusWeeks(3), m2))
            .thenReturn(List.of());
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, m2.minusWeeks(4), m2))
            .thenReturn(List.of(aWellness(m2, 3, 3, 4)));
        when(loadReportService.findByAthleteIdAndWeekStartDate(ATHLETE_ID, m2))
            .thenReturn(zeroLoadWithTs);

        // m3: has load in DB
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, m3.minusWeeks(3), m3))
            .thenReturn(List.of(aLoad(m3, 120, 2)));
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, m3.minusWeeks(4), m3))
            .thenReturn(List.of());

        List<WeeklyReport> reports = weeklyReportService.getWeeklyReportsByPeriod(ATHLETE_ID, from, to);

        assertThat(reports).hasSize(3);
        assertThat(reports).extracting(WeeklyReport::weekStartDate)
            .containsExactly(m1, m2, m3);
    }

    @Test
    void getWeeklyReportsByPeriod_athleteHasNoData_returnsEmptyList() {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 28);
        LocalDate to = LocalDate.of(2025, Month.MAY, 5);

        // Both weeks: empty DB, zero onTheFlyLoad (null updatedAt), no wellness → not-found caught and skipped
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(anyLong(), any(), any()))
            .thenReturn(List.of());
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(anyLong(), any(), any()))
            .thenReturn(List.of());

        List<WeeklyReport> reports = weeklyReportService.getWeeklyReportsByPeriod(ATHLETE_ID, from, to);

        assertThat(reports).isEmpty();
    }

    private LoadReport aLoad(LocalDate weekStart, int totalFosterLoad, int sessionCount) {
        return new LoadReport(ATHLETE_ID, weekStart, totalFosterLoad, sessionCount, LocalDateTime.now());
    }

    private WeeklyWellness aWellness(LocalDate weekStart, int difficulty, int fatigue, int motivation) {
        return new WeeklyWellness(ATHLETE_ID, weekStart, difficulty, fatigue, motivation);
    }
}
