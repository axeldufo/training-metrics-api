package com.axel.trainingmetricsapi;

import com.axel.trainingmetricsapi.config.CacheConfig;
import com.axel.trainingmetricsapi.domain.AcwrReport;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionCreatedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionDeletedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionUpdatedEvent;
import com.axel.trainingmetricsapi.service.AcwrReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class AcwrReportCacheIT {

    private static final long ATHLETE_ID = 999L;

    @Autowired
    private AcwrReportService acwrReportService;

    @MockitoSpyBean
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).clear();
        clearInvocations(trainingSessionRepository);
    }

    @Test
    void getAcwrReport_shouldHitCacheOnSecondCall() {
        AcwrReport firstReport = acwrReportService.getAcwrReport(ATHLETE_ID);

        assertThat(Objects.requireNonNull(cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).get(ATHLETE_ID)).isNotNull();
        verify(trainingSessionRepository, times(1)).findByAthleteIdAndPeriod(eq(ATHLETE_ID), any(), any());
        clearInvocations(trainingSessionRepository);

        AcwrReport secondReport = acwrReportService.getAcwrReport(ATHLETE_ID);
        assertThat(secondReport).isEqualTo(firstReport);
        verify(trainingSessionRepository, never()).findByAthleteIdAndPeriod(eq(ATHLETE_ID), any(), any());
    }

    @ParameterizedTest
    @MethodSource("sessionEvents")
    void onTrainingSessionEvent_shouldEvictAndRefreshCache(Object event) {
        acwrReportService.getAcwrReport(ATHLETE_ID);
        clearInvocations(trainingSessionRepository);

        LocalDate beforeEvent = LocalDate.now();
        eventPublisher.publishEvent(event);

        verify(trainingSessionRepository, times(1)).findByAthleteIdAndPeriod(eq(ATHLETE_ID), any(), any());
        // Cache containing a new report (first one has been evicted)
        Cache.ValueWrapper cached = Objects.requireNonNull(
            cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).get(ATHLETE_ID);
        assertThat(cached).isNotNull();
        assertThat(((AcwrReport) Objects.requireNonNull(cached.get())).calculatedAt())
            .isAfterOrEqualTo(beforeEvent)
            .isBeforeOrEqualTo(LocalDate.now());
    }

    @ParameterizedTest
    @MethodSource("sessionEvents")
    void onTrainingSessionEvent_subsequentGetShouldHitCache(Object event) {
        eventPublisher.publishEvent(event);
        clearInvocations(trainingSessionRepository);

        AcwrReport report = acwrReportService.getAcwrReport(ATHLETE_ID);

        assertThat(report).isNotNull();
        verify(trainingSessionRepository, never()).findByAthleteIdAndPeriod(anyLong(), any(), any());
    }

    @Test
    void publishTrainingSessionCreatedEvent_shouldOnlyEvictCacheForAffectedAthlete() {
        long otherAthleteId = 998L;
        acwrReportService.getAcwrReport(ATHLETE_ID);
        acwrReportService.getAcwrReport(otherAthleteId);
        clearInvocations(trainingSessionRepository);

        eventPublisher.publishEvent(new TrainingSessionCreatedEvent(ATHLETE_ID, LocalDate.now()));

        // otherAthlete cache untouched — no repository call
        verify(trainingSessionRepository, never()).findByAthleteIdAndPeriod(eq(otherAthleteId), any(), any());
        // ATHLETE_ID cache refreshed — one repository call
        verify(trainingSessionRepository, times(1)).findByAthleteIdAndPeriod(eq(ATHLETE_ID), any(), any());
    }

    static Stream<Object> sessionEvents() {
        return Stream.of(
            new TrainingSessionCreatedEvent(ATHLETE_ID, LocalDate.now()),
            new TrainingSessionUpdatedEvent(ATHLETE_ID, LocalDate.now()),
            new TrainingSessionDeletedEvent(ATHLETE_ID, LocalDate.now())
        );
    }

}
