package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.config.CacheConfig;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionCreatedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionDeletedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingSessionEventHandlerTest {

    private static final long ATHLETE_ID = 7L;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private AcwrReportService acwrReportService;

    @InjectMocks
    private TrainingSessionEventHandler handler;

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).thenReturn(cache);
    }

    @Test
    void onSessionCreated_shouldEvictCacheAndRefresh() {
        TrainingSessionCreatedEvent event = new TrainingSessionCreatedEvent(ATHLETE_ID, LocalDate.now());

        handler.onSessionCreated(event);

        verify(cache).evict(ATHLETE_ID);
        verify(acwrReportService).getAcwrReport(ATHLETE_ID);
    }

    @Test
    void onSessionUpdated_shouldEvictCacheAndRefresh() {
        TrainingSessionUpdatedEvent event = new TrainingSessionUpdatedEvent(ATHLETE_ID, LocalDate.now());

        handler.onSessionUpdated(event);

        verify(cache).evict(ATHLETE_ID);
        verify(acwrReportService).getAcwrReport(ATHLETE_ID);
    }

    @Test
    void onSessionDeleted_shouldEvictCacheAndRefresh() {
        TrainingSessionDeletedEvent event = new TrainingSessionDeletedEvent(ATHLETE_ID, LocalDate.now());

        handler.onSessionDeleted(event);

        verify(cache).evict(ATHLETE_ID);
        verify(acwrReportService).getAcwrReport(ATHLETE_ID);
    }

    @Test
    void refreshAcwrReport_shouldThrow_whenCacheNotConfigured() {
        when(cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).thenReturn(null);

        assertThatThrownBy(() ->
            handler.onSessionCreated(new TrainingSessionCreatedEvent(ATHLETE_ID, LocalDate.now())))
            .isInstanceOf(IllegalStateException.class);
    }

}
