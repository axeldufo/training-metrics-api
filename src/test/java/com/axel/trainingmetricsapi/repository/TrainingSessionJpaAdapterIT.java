package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.TestContainersConfiguration;
import com.axel.trainingmetricsapi.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestContainersConfiguration.class)
@Transactional
class TrainingSessionJpaAdapterIT {

    @Autowired
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private CoachJpaRepository coachJpaRepository;

    private long athleteId;
    private long coachId;

    @BeforeEach
    void setUp() {
        CoachJpaEntity requestingCoach = coachJpaRepository.save(aCoach());
        coachId = requestingCoach.getId();
        Athlete athlete = athleteRepository.save(anAthlete(coachId));
        athleteId = athlete.getId();
    }

    @Test
    void save_shouldPersistSessionWithGeneratedId() {
        TrainingSession session = aSession(athleteId);

        TrainingSession saved = trainingSessionRepository.save(session);

        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(saved.getSport()).isEqualTo(Sport.CYCLING);
        assertThat(saved.getRpe()).isEqualTo(5);
        assertThat(saved.getDurationInMin()).isEqualTo(60);
        assertThat(saved.getTargetZone()).isEqualTo(TargetZone.Z2);
        assertThat(saved.getAthleteId()).isEqualTo(athleteId);
    }

    // FK constraints are only checked at commit time in PostgreSQL.
    // NOT_SUPPORTED suspends the class-level transaction so operations commit immediately.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldThrowException_whenAthleteDoesNotExist() {
        TrainingSession session = aSession(9999L);

        assertThatThrownBy(() -> trainingSessionRepository.save(session))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findById_shouldReturnSessionFromDatabase() {
        TrainingSession saved = trainingSessionRepository.save(aSession(athleteId));

        Optional<TrainingSession> found = trainingSessionRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSport()).isEqualTo(Sport.CYCLING);
        assertThat(found.get().getAthleteId()).isEqualTo(athleteId);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        Optional<TrainingSession> found = trainingSessionRepository.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findByAthleteIdAndPeriod_shouldReturnOnlySessionsWithinRange() {
        trainingSessionRepository.save(aSessionOn(athleteId, LocalDate.of(2024, Month.JANUARY, 1)));  // before range
        trainingSessionRepository.save(aSessionOn(athleteId, LocalDate.of(2024, Month.JANUARY, 15))); // in range
        trainingSessionRepository.save(aSessionOn(athleteId, LocalDate.of(2024, Month.JANUARY, 31))); // in range (inclusive)
        trainingSessionRepository.save(aSessionOn(athleteId, LocalDate.of(2024, Month.FEBRUARY, 15))); // after range

        LocalDate from = LocalDate.of(2024, Month.JANUARY, 8);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 31);

        List<TrainingSession> result = trainingSessionRepository.findByAthleteIdAndPeriod(athleteId, from, to);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TrainingSession::getDate)
            .containsExactlyInAnyOrder(LocalDate.of(2024, Month.JANUARY, 15), LocalDate.of(2024, Month.JANUARY, 31));
    }

    @Test
    void findByAthleteIdAndPeriod_shouldReturnEmpty_whenNoneInRange() {
        trainingSessionRepository.save(aSession(athleteId)); // date: 2024-03-01 (outside range)

        List<TrainingSession> result = trainingSessionRepository.findByAthleteIdAndPeriod(
            athleteId, LocalDate.of(2024, Month.JANUARY, 1), LocalDate.of(2024, Month.FEBRUARY, 28));

        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_shouldRemoveSessionFromDatabase() {
        TrainingSession saved = trainingSessionRepository.save(aSession(athleteId));

        trainingSessionRepository.deleteById(saved.getId());

        assertThat(trainingSessionRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void existsById_shouldReturnTrue_afterSave() {
        TrainingSession saved = trainingSessionRepository.save(aSession(athleteId));

        assertThat(trainingSessionRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_forNonExistentId() {
        assertThat(trainingSessionRepository.existsById(9999L)).isFalse();
    }

    private CoachJpaEntity aCoach() {
        return CoachJpaEntity.builder()
            .name("John Coach")
            .email("coach@test.com")
            .hashedPassword("hashed")
            .build();
    }

    private Athlete anAthlete(long coachId) {
        return new Athlete(
            "Alice",
            "Smith",
            LocalDate.of(1990, Month.JANUARY, 1),
            Sport.CYCLING,
            coachId,
            56.0);
    }

    private TrainingSession aSession(long forAthleteId) {
        return aSessionOn(forAthleteId, LocalDate.of(2024, Month.MARCH, 1));
    }

    private TrainingSession aSessionOn(long forAthleteId, LocalDate date) {
        return new TrainingSession(date, Sport.CYCLING, 5, 60, TargetZone.Z2, forAthleteId);
    }
}
