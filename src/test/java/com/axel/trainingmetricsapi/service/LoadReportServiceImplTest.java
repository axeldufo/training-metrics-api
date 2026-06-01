package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.domain.TargetZone;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.LoadReportNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadReportServiceImplTest {

    private static final long ATHLETE_ID = 1L;
    private static final LocalDate MONDAY = LocalDate.of(2025, 5, 19);

    @Mock
    private LoadReportRepository loadReportRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private LoadReportServiceImpl loadReportService;

    @Test
    void findByAthleteIdAndWeekStartDate_shouldReturnPersistedReport_whenFoundInDB() {
        LocalDateTime updatedAt = LocalDateTime.now();
        LoadReport persisted = new LoadReport(ATHLETE_ID, MONDAY, 200, 2, updatedAt);
        when(loadReportRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(Optional.of(persisted));

        LoadReport result = loadReportService.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);

        assertThat(result).isEqualTo(persisted);
        verify(loadReportRepository).findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);
        verifyNoInteractions(trainingSessionRepository);
    }

    @Test
    void findByAthleteIdAndWeekStartDate_shouldCalculateOnTheFly_whenNotInDB() {
        when(loadReportRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(Optional.empty());
        TrainingSession session1 = aSession(5, 60);
        TrainingSession session2 = aSession(8, 45);
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, MONDAY, MONDAY.plusDays(6)))
            .thenReturn(List.of(session1, session2));

        LoadReport result = loadReportService.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);

        assertThat(result.athleteId()).isEqualTo(ATHLETE_ID);
        assertThat(result.weekStartDate()).isEqualTo(MONDAY);
        assertThat(result.totalFosterLoad()).isEqualTo(660); // (5*60) + (8*45)
        assertThat(result.sessionCount()).isEqualTo(2);
        assertThat(result.updatedAt()).isNull();
    }

    @Test
    void findByAthleteIdAndWeekStartDate_shouldReturnZeroReport_whenNoSessionsExist() {
        when(loadReportRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(Optional.empty());
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, MONDAY, MONDAY.plusDays(6)))
            .thenReturn(List.of());

        LoadReport result = loadReportService.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);

        assertThat(result.athleteId()).isEqualTo(ATHLETE_ID);
        assertThat(result.weekStartDate()).isEqualTo(MONDAY);
        assertThat(result.totalFosterLoad()).isZero();
        assertThat(result.sessionCount()).isZero();
        assertThat(result.updatedAt()).isNull();
    }

    @Test
    void findLatestByAthleteId_shouldReturnReport_whenExists() {
        LoadReport report = new LoadReport(ATHLETE_ID, MONDAY, 150, 2, LocalDateTime.now());
        when(loadReportRepository.findLatestByAthleteId(ATHLETE_ID)).thenReturn(Optional.of(report));

        LoadReport result = loadReportService.findLatestByAthleteId(ATHLETE_ID);

        assertThat(result).isEqualTo(report);
        verify(loadReportRepository).findLatestByAthleteId(ATHLETE_ID);
    }

    @Test
    void findLatestByAthleteId_shouldThrowLoadReportNotFoundException_whenNoReportExists() {
        when(loadReportRepository.findLatestByAthleteId(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loadReportService.findLatestByAthleteId(ATHLETE_ID))
            .isInstanceOf(LoadReportNotFoundException.class);
    }

    @Test
    void findByAthleteIdAndPeriod_shouldDelegateToRepository() {
        LocalDate from = LocalDate.of(2025, 4, 1);
        LocalDate to = LocalDate.of(2025, 5, 26);
        List<LoadReport> reports = List.of(new LoadReport(ATHLETE_ID, MONDAY, 200, 2, LocalDateTime.now()));
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to))
            .thenReturn(reports);

        List<LoadReport> result = loadReportService.findByAthleteIdAndPeriod(ATHLETE_ID, from, to);

        assertThat(result).isEqualTo(reports);
        verify(loadReportRepository).findByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to);
    }

    private TrainingSession aSession(int rpe, int durationInMin) {
        return new TrainingSession(MONDAY, Sport.ROAD_RUNNING, rpe, durationInMin, TargetZone.Z2, ATHLETE_ID);
    }
}
