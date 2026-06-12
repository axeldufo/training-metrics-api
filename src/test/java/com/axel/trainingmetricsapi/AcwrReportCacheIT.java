package com.axel.trainingmetricsapi;

import com.axel.trainingmetricsapi.application.port.in.GetAcwrReportUseCase;
import com.axel.trainingmetricsapi.application.port.out.TrainingSessionEventPort;
import com.axel.trainingmetricsapi.infrastructure.cache.CacheConfig;
import com.axel.trainingmetricsapi.domain.AcwrReport;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.infrastructure.persistence.CoachJpaEntity;
import com.axel.trainingmetricsapi.infrastructure.persistence.CoachJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import java.util.UUID;

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

    @Autowired
    private GetAcwrReportUseCase getAcwrReportUseCase;

    @Autowired
    private CoachJpaRepository coachJpaRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    @MockitoSpyBean
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private TrainingSessionEventPort trainingSessionEventPort;

    @Autowired
    private CacheManager cacheManager;

    private long athleteId;
    private long coachId;
    private long otherAthleteId;
    private long otherCoachId;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).clear();
        clearInvocations(trainingSessionRepository);

        CoachJpaEntity coach = coachJpaRepository.save(aCoach());
        coachId = coach.getId();
        athleteId = athleteRepository.save(anAthlete(coachId)).getId();

        CoachJpaEntity otherCoach = coachJpaRepository.save(aCoach());
        otherCoachId = otherCoach.getId();
        otherAthleteId = athleteRepository.save(anAthlete(otherCoachId)).getId();
    }

    @Test
    void getAcwrReport_shouldHitCacheOnSecondCall() {
        AcwrReport firstReport = getAcwrReportUseCase.execute(athleteId, coachId);

        assertThat(Objects.requireNonNull(cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).get(athleteId)).isNotNull();
        verify(trainingSessionRepository, times(1)).findByAthleteIdAndPeriod(eq(athleteId), any(), any());
        clearInvocations(trainingSessionRepository);

        AcwrReport secondReport = getAcwrReportUseCase.execute(athleteId, coachId);
        assertThat(secondReport).isEqualTo(firstReport);
        verify(trainingSessionRepository, never()).findByAthleteIdAndPeriod(eq(athleteId), any(), any());
    }

    @ParameterizedTest
    @EnumSource(SessionEvent.class)
    void onTrainingSessionEvent_shouldEvictAndRefreshCache(SessionEvent eventType) {
        getAcwrReportUseCase.execute(athleteId, coachId);
        clearInvocations(trainingSessionRepository);

        LocalDate beforeEvent = LocalDate.now();
        fireEvent(eventType, athleteId);

        verify(trainingSessionRepository, times(1)).findByAthleteIdAndPeriod(eq(athleteId), any(), any());
        Cache.ValueWrapper cached = Objects.requireNonNull(
            cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).get(athleteId);
        assertThat(cached).isNotNull();
        assertThat(((AcwrReport) Objects.requireNonNull(cached.get())).calculatedAt())
            .isAfterOrEqualTo(beforeEvent)
            .isBeforeOrEqualTo(LocalDate.now());
    }

    @ParameterizedTest
    @EnumSource(SessionEvent.class)
    void onTrainingSessionEvent_subsequentGetShouldHitCache(SessionEvent eventType) {
        fireEvent(eventType, athleteId);
        clearInvocations(trainingSessionRepository);

        AcwrReport report = getAcwrReportUseCase.execute(athleteId, coachId);

        assertThat(report).isNotNull();
        verify(trainingSessionRepository, never()).findByAthleteIdAndPeriod(anyLong(), any(), any());
    }

    @Test
    void publishTrainingSessionCreatedEvent_shouldOnlyEvictCacheForAffectedAthlete() {
        getAcwrReportUseCase.execute(athleteId, coachId);
        getAcwrReportUseCase.execute(otherAthleteId, otherCoachId);
        clearInvocations(trainingSessionRepository);

        trainingSessionEventPort.sessionCreated(athleteId, LocalDate.now());

        verify(trainingSessionRepository, never()).findByAthleteIdAndPeriod(eq(otherAthleteId), any(), any());
        verify(trainingSessionRepository, times(1)).findByAthleteIdAndPeriod(eq(athleteId), any(), any());
    }

    private void fireEvent(SessionEvent eventType, long forAthleteId) {
        LocalDate date = LocalDate.of(2025, Month.MAY, 19);
        switch (eventType) {
            case CREATED -> trainingSessionEventPort.sessionCreated(forAthleteId, date);
            case UPDATED -> trainingSessionEventPort.sessionUpdated(forAthleteId, date);
            case DELETED -> trainingSessionEventPort.sessionDeleted(forAthleteId, date);
        }
    }

    private CoachJpaEntity aCoach() {
        return CoachJpaEntity.builder()
            .name("IT Coach")
            .email("coach-" + UUID.randomUUID() + "@test.com")
            .hashedPassword("hashed")
            .build();
    }

    private Athlete anAthlete(long forCoachId) {
        return new Athlete("Test", "Athlete",
            LocalDate.of(1990, Month.JANUARY, 1), Sport.ROAD_RUNNING, forCoachId, 70.0);
    }

    enum SessionEvent { CREATED, UPDATED, DELETED }
}
