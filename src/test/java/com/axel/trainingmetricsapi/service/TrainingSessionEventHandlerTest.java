package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.config.CacheConfig;
import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.domain.TargetZone;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionCreatedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionDeletedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingSessionEventHandlerTest {

    private static final long ATHLETE_ID = 7L;
    private static final LocalDate MONDAY = LocalDate.of(2025, 5, 19);

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private AcwrReportService acwrReportService;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private LoadReportRepository loadReportRepository;

    @InjectMocks
    private TrainingSessionEventHandler handler;

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).thenReturn(cache);
    }

    @Test
    void onSessionCreated_shouldEvictCacheAndRefreshAcwr() {
        TrainingSessionCreatedEvent event = new TrainingSessionCreatedEvent(ATHLETE_ID, LocalDate.now());

        handler.onSessionCreated(event);

        verify(cache).evict(ATHLETE_ID);
        verify(acwrReportService).getAcwrReport(ATHLETE_ID);
    }

    @Test
    void onSessionUpdated_shouldEvictCacheAndRefreshAcwr() {
        TrainingSessionUpdatedEvent event = new TrainingSessionUpdatedEvent(ATHLETE_ID, LocalDate.now());

        handler.onSessionUpdated(event);

        verify(cache).evict(ATHLETE_ID);
        verify(acwrReportService).getAcwrReport(ATHLETE_ID);
    }

    @Test
    void onSessionDeleted_shouldEvictCacheAndRefreshAcwr() {
        TrainingSessionDeletedEvent event = new TrainingSessionDeletedEvent(ATHLETE_ID, LocalDate.now());

        handler.onSessionDeleted(event);

        verify(cache).evict(ATHLETE_ID);
        verify(acwrReportService).getAcwrReport(ATHLETE_ID);
    }

    @Test
    void refreshAcwrReport_shouldThrow_whenCacheNotConfigured() {
        when(cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).thenReturn(null);

        TrainingSessionCreatedEvent event = new TrainingSessionCreatedEvent(ATHLETE_ID, LocalDate.now());
        assertThatThrownBy(() -> handler.onSessionCreated(event))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onSessionCreated_shouldSaveLoadReport_whenSessionsExist() {
        LocalDate sessionDate = MONDAY.plusDays(2);
        TrainingSessionCreatedEvent event = new TrainingSessionCreatedEvent(ATHLETE_ID, sessionDate);
        TrainingSession session = aSession(5, 60, MONDAY);
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, MONDAY, MONDAY.plusDays(6)))
            .thenReturn(List.of(session));

        handler.onSessionCreated(event);

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
    void onSessionUpdated_shouldSaveLoadReport_whenSessionsExist() {
        LocalDate sessionDate = MONDAY.plusDays(3);
        TrainingSessionUpdatedEvent event = new TrainingSessionUpdatedEvent(ATHLETE_ID, sessionDate);
        TrainingSession session = aSession(8, 45, MONDAY);
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, MONDAY, MONDAY.plusDays(6)))
            .thenReturn(List.of(session));

        handler.onSessionUpdated(event);

        ArgumentCaptor<LoadReport> captor = ArgumentCaptor.forClass(LoadReport.class);
        verify(loadReportRepository).save(captor.capture());
        assertThat(captor.getValue().totalFosterLoad()).isEqualTo(360); // 8 * 45
        verify(loadReportRepository, never()).deleteByAthleteIdAndWeekStartDate(anyLong(), any());
    }

    @Test
    void onSessionDeleted_shouldDeleteLoadReport_whenNoSessionsRemain() {
        LocalDate sessionDate = MONDAY.plusDays(1);
        TrainingSessionDeletedEvent event = new TrainingSessionDeletedEvent(ATHLETE_ID, sessionDate);
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, MONDAY, MONDAY.plusDays(6)))
            .thenReturn(List.of());

        handler.onSessionDeleted(event);

        verify(loadReportRepository).deleteByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);
        verify(loadReportRepository, never()).save(any());
    }

    private TrainingSession aSession(int rpe, int durationInMin, LocalDate date) {
        return new TrainingSession(date, Sport.ROAD_RUNNING, rpe, durationInMin, TargetZone.Z2, ATHLETE_ID);
    }
}
