package com.axel.trainingmetricsapi;

import com.axel.trainingmetricsapi.application.port.out.TrainingSessionEventPort;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.domain.TargetZone;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.repository.CoachJpaEntity;
import com.axel.trainingmetricsapi.repository.CoachJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestContainersConfiguration.class)
@Transactional
class LoadReportEventIT {

    private static final LocalDate MONDAY = LocalDate.of(2025, Month.MAY, 19);
    private static final LocalDate SESSION_DATE = LocalDate.of(2025, Month.MAY, 21); // Wednesday

    @Autowired
    private TrainingSessionEventPort trainingSessionEventPort;

    @Autowired
    private LoadReportRepository loadReportRepository;

    @Autowired
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private CoachJpaRepository coachJpaRepository;

    private long athleteId;

    @BeforeEach
    void setUp() {
        CoachJpaEntity coach = coachJpaRepository.save(aCoach());
        Athlete athlete = athleteRepository.save(anAthlete(coach.getId()));
        athleteId = athlete.getId();
    }

    @Test
    void onSessionCreated_shouldPersistLoadReport() {
        TrainingSession session = trainingSessionRepository.save(aSession(athleteId, SESSION_DATE, 5, 60));

        trainingSessionEventPort.sessionCreated(athleteId, SESSION_DATE);

        Optional<LoadReport> report = loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, MONDAY);
        assertThat(report).isPresent();
        assertThat(report.get().totalFosterLoad()).isEqualTo(session.getFosterLoad());
        assertThat(report.get().sessionCount()).isEqualTo(1);
        assertThat(report.get().updatedAt()).isNotNull();
    }

    @Test
    void onSessionUpdated_shouldUpdateLoadReport() {
        TrainingSession session = trainingSessionRepository.save(aSession(athleteId, SESSION_DATE, 5, 60));
        trainingSessionEventPort.sessionCreated(athleteId, SESSION_DATE);

        TrainingSession updated = new TrainingSession(SESSION_DATE, Sport.ROAD_RUNNING, 8, 90, TargetZone.Z2, athleteId);
        updated.setId(session.getId());
        trainingSessionRepository.save(updated);

        trainingSessionEventPort.sessionUpdated(athleteId, SESSION_DATE);

        Optional<LoadReport> report = loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, MONDAY);
        assertThat(report).isPresent();
        assertThat(report.get().totalFosterLoad()).isEqualTo(720); // 8 * 90
        assertThat(report.get().sessionCount()).isEqualTo(1);
    }

    @Test
    void onSessionDeleted_shouldDeleteLoadReport_whenLastSessionRemoved() {
        TrainingSession session = trainingSessionRepository.save(aSession(athleteId, SESSION_DATE, 5, 60));
        trainingSessionEventPort.sessionCreated(athleteId, SESSION_DATE);
        assertThat(loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, MONDAY)).isPresent();

        trainingSessionRepository.deleteById(session.getId());

        trainingSessionEventPort.sessionDeleted(athleteId, SESSION_DATE);

        assertThat(loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, MONDAY)).isEmpty();
    }

    private CoachJpaEntity aCoach() {
        return CoachJpaEntity.builder()
            .name("Event Coach")
            .email("event-coach-" + System.nanoTime() + "@test.com")
            .hashedPassword("hashed")
            .build();
    }

    private Athlete anAthlete(long coachId) {
        return new Athlete("Bob", "Johnson", LocalDate.of(1985, Month.MARCH, 15), Sport.ROAD_RUNNING, coachId, 75.0);
    }

    private TrainingSession aSession(long forAthleteId, LocalDate date, int rpe, int durationInMin) {
        return new TrainingSession(date, Sport.ROAD_RUNNING, rpe, durationInMin, TargetZone.Z2, forAthleteId);
    }
}
