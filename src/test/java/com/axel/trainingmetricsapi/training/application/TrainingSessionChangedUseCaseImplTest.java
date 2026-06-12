package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.training.application.port.out.AcwrCachePort;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.training.domain.TargetZone;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingSessionChangedUseCaseImplTest {

    private static final long ATHLETE_ID = 7L;
    private static final LocalDate MONDAY = LocalDate.of(2025, Month.MAY, 19);

    @Mock
    private AcwrCachePort acwrCachePort;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private LoadReportRepository loadReportRepository;

    @InjectMocks
    private TrainingSessionChangedUseCaseImpl trainingSessionChangedUseCase;

    @Test
    void execute_shouldEvictCacheAndRefreshAcwr() {
        trainingSessionChangedUseCase.execute(ATHLETE_ID, LocalDate.now());

        verify(acwrCachePort).evict(ATHLETE_ID);
        verify(acwrCachePort).put(eq(ATHLETE_ID), any());
    }

    @Test
    void execute_shouldSaveLoadReport_whenSessionsExist() {
        LocalDate today = LocalDate.now();
        LocalDate sessionDate = MONDAY.plusDays(2);
        TrainingSession session = aSession(5, 60, MONDAY);
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, today.minusDays(27), today))
            .thenReturn(List.of());
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, MONDAY, MONDAY.plusDays(6)))
            .thenReturn(List.of(session));

        trainingSessionChangedUseCase.execute(ATHLETE_ID, sessionDate);

        ArgumentCaptor<LoadReport> captor = ArgumentCaptor.forClass(LoadReport.class);
        verify(loadReportRepository).save(captor.capture());
        LoadReport saved = captor.getValue();
        assertThat(saved.athleteId()).isEqualTo(ATHLETE_ID);
        assertThat(saved.weekStartDate()).isEqualTo(MONDAY);
        assertThat(saved.totalFosterLoad()).isEqualTo(300); // 5 * 60
        assertThat(saved.sessionCount()).isEqualTo(1);
        assertThat(saved.updatedAt()).isNotNull();
        verify(loadReportRepository, never()).deleteByAthleteIdAndWeekStartDate(anyLong(), any());
    }

    @Test
    void execute_shouldDeleteLoadReport_whenNoSessionsRemain() {
        LocalDate today = LocalDate.now();
        LocalDate sessionDate = MONDAY.plusDays(1);
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, today.minusDays(27), today))
            .thenReturn(List.of());
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, MONDAY, MONDAY.plusDays(6)))
            .thenReturn(List.of());

        trainingSessionChangedUseCase.execute(ATHLETE_ID, sessionDate);

        verify(loadReportRepository).deleteByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);
        verify(loadReportRepository, never()).save(any());
    }

    private TrainingSession aSession(int rpe, int durationInMin, LocalDate date) {
        return new TrainingSession(date, Sport.ROAD_RUNNING, rpe, durationInMin, TargetZone.Z2, ATHLETE_ID);
    }
}
