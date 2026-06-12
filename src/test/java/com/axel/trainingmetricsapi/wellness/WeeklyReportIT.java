package com.axel.trainingmetricsapi.wellness;

import com.axel.trainingmetricsapi.TestContainersConfiguration;
import com.axel.trainingmetricsapi.wellness.application.port.in.GetWeeklyReportByWeekUseCase;
import com.axel.trainingmetricsapi.training.application.port.out.TrainingSessionEventPort;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.wellness.domain.CorrelationAlert;
import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.training.domain.TargetZone;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyReport;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyReportNotFoundException;
import com.axel.trainingmetricsapi.identity.infrastructure.persistence.CoachJpaEntity;
import com.axel.trainingmetricsapi.identity.infrastructure.persistence.CoachJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestContainersConfiguration.class)
@Transactional
class WeeklyReportIT {

    private static final LocalDate MONDAY = LocalDate.of(2025, Month.MAY, 19);
    private static final LocalDate SESSION_DATE = MONDAY.plusDays(2);

    @Autowired
    private GetWeeklyReportByWeekUseCase getWeeklyReportByWeekUseCase;

    @Autowired
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private WeeklyWellnessRepository weeklyWellnessRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private CoachJpaRepository coachJpaRepository;

    @Autowired
    private TrainingSessionEventPort trainingSessionEventPort;

    private long athleteId;
    private long coachId;

    @BeforeEach
    void setUp() {
        CoachJpaEntity coach = coachJpaRepository.save(aCoach());
        coachId = coach.getId();
        Athlete athlete = athleteRepository.save(anAthlete(coachId));
        athleteId = athlete.getId();
    }

    @Test
    void scenario1_nominalEndToEnd_sessionsAndWellnessPresent_returns200WithCorrectFields() {
        trainingSessionRepository.save(aSession(athleteId, SESSION_DATE, 6, 60));
        trainingSessionRepository.save(aSession(athleteId, SESSION_DATE, 7, 45));
        trainingSessionRepository.save(aSession(athleteId, SESSION_DATE, 5, 30));
        trainingSessionEventPort.sessionCreated(athleteId, SESSION_DATE);

        weeklyWellnessRepository.save(new WeeklyWellness(athleteId, MONDAY.minusWeeks(1), 4, 3, 4));
        weeklyWellnessRepository.save(new WeeklyWellness(athleteId, MONDAY, 4, 3, 5));

        WeeklyReport report = getWeeklyReportByWeekUseCase.execute(athleteId, coachId, MONDAY);

        assertThat(report.wellnessAvailable()).isTrue();
        assertThat(report.sessionCount()).isEqualTo(3);
        assertThat(report.perceivedDifficulty()).isEqualTo(4);
        assertThat(report.perceivedFatigue()).isEqualTo(3);
        assertThat(report.motivation()).isEqualTo(5);
        assertThat(report.acwrReliable()).isFalse();
        assertThat(report.totalFosterLoad()).isGreaterThan(0);
        assertThat(report.correlationAlert()).isNotEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
    }

    @Test
    void scenario2_zeroLoadWeek_noSessions_wellnessPresent_returns200() {
        weeklyWellnessRepository.save(new WeeklyWellness(athleteId, MONDAY.minusWeeks(1), 3, 3, 4));
        weeklyWellnessRepository.save(new WeeklyWellness(athleteId, MONDAY, 3, 2, 4));

        WeeklyReport report = getWeeklyReportByWeekUseCase.execute(athleteId, coachId, MONDAY);

        assertThat(report.wellnessAvailable()).isTrue();
        assertThat(report.sessionCount()).isZero();
        assertThat(report.totalFosterLoad()).isZero();
        assertThat(report.perceivedDifficulty()).isEqualTo(3);
        assertThat(report.acwr()).isEqualTo(0.0);
        assertThat(report.acwrReliable()).isFalse();
        assertThat(report.correlationAlert()).isNotEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
    }

    @Test
    void scenario3_loadOnly_noWellness_returns200WithInsufficientData() {
        trainingSessionRepository.save(aSession(athleteId, SESSION_DATE, 5, 60));
        trainingSessionRepository.save(aSession(athleteId, SESSION_DATE, 6, 45));
        trainingSessionEventPort.sessionCreated(athleteId, SESSION_DATE);

        WeeklyReport report = getWeeklyReportByWeekUseCase.execute(athleteId, coachId, MONDAY);

        assertThat(report.wellnessAvailable()).isFalse();
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
        assertThat(report.perceivedDifficulty()).isNull();
        assertThat(report.perceivedFatigue()).isNull();
        assertThat(report.motivation()).isNull();
    }

    @Test
    void scenario4_noDataAtAll_throws404() {
        assertThatThrownBy(() -> getWeeklyReportByWeekUseCase.execute(athleteId, coachId, MONDAY))
            .isInstanceOf(WeeklyReportNotFoundException.class)
            .hasMessageContaining("No weekly report available for athlete " + athleteId)
            .hasMessageContaining("on week " + MONDAY);
    }

    private CoachJpaEntity aCoach() {
        return CoachJpaEntity.builder()
            .name("IT Coach")
            .email("coach@test.com")
            .hashedPassword("hashed")
            .build();
    }

    private Athlete anAthlete(long forCoachId) {
        return new Athlete("Test", "Athlete", LocalDate.of(1990, Month.JANUARY, 1), Sport.ROAD_RUNNING, forCoachId, 70.0);
    }

    private TrainingSession aSession(long forAthleteId, LocalDate date, int rpe, int durationInMin) {
        return new TrainingSession(date, Sport.ROAD_RUNNING, rpe, durationInMin, TargetZone.Z2, forAthleteId);
    }
}
