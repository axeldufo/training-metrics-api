package com.axel.trainingmetricsapi;

import com.axel.trainingmetricsapi.application.port.out.TrainingSessionEventPort;
import com.axel.trainingmetricsapi.config.CacheConfig;
import com.axel.trainingmetricsapi.domain.AcwrReport;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.service.AcwrReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class AcwrReportCacheIT {

    private static final long ATHLETE_ID = 999L;

    @Autowired
    private AcwrReportService acwrReportService;

    @MockitoSpyBean
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private TrainingSessionEventPort trainingSessionEventPort;

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
    @MethodSource("sessionEventPublishers")
    void onTrainingSessionEvent_shouldEvictAndRefreshCache(
        java.util.function.Consumer<TrainingSessionEventPort> publishEvent) {
        acwrReportService.getAcwrReport(ATHLETE_ID);
        clearInvocations(trainingSessionRepository);

        LocalDate beforeEvent = LocalDate.now();
        publishEvent.accept(trainingSessionEventPort);

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
    @MethodSource("sessionEventPublishers")
    void onTrainingSessionEvent_subsequentGetShouldHitCache(
        java.util.function.Consumer<TrainingSessionEventPort> publishEvent) {
        publishEvent.accept(trainingSessionEventPort);
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

        trainingSessionEventPort.sessionCreated(ATHLETE_ID, LocalDate.now());

        // otherAthlete cache untouched — no repository call
        verify(trainingSessionRepository, never()).findByAthleteIdAndPeriod(eq(otherAthleteId), any(), any());
        // ATHLETE_ID cache refreshed — one repository call
        verify(trainingSessionRepository, times(1)).findByAthleteIdAndPeriod(eq(ATHLETE_ID), any(), any());
    }

    static Stream<java.util.function.Consumer<TrainingSessionEventPort>> sessionEventPublishers() {
        LocalDate date = LocalDate.now();
        return Stream.of(
            port -> port.sessionCreated(ATHLETE_ID, date),
            port -> port.sessionUpdated(ATHLETE_ID, date),
            port -> port.sessionDeleted(ATHLETE_ID, date)
        );
    }

}
