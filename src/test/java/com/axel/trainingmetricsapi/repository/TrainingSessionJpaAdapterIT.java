package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.PostgresTestContainersConfiguration;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.domain.TargetZone;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestContainersConfiguration.class)
@Transactional
class TrainingSessionJpaAdapterIT {

    @Autowired
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    private long athleteId;

    @BeforeEach
    void setUp() {
        Athlete athlete = athleteRepository.save(
            new Athlete("Alice", "Smith", LocalDate.of(1990, 1, 1), Sport.CYCLING, null, 56.0));
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

    @Test
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
    void findAllByAthleteId_shouldReturnOnlySessionsForGivenAthlete() {
        Athlete otherAthlete = athleteRepository.save(
            new Athlete("Bob", "Jones", LocalDate.of(1985, 6, 15), Sport.ROAD_RUNNING, null, 75.0));
        trainingSessionRepository.save(aSession(athleteId));
        trainingSessionRepository.save(aSession(athleteId));
        trainingSessionRepository.save(aSession(otherAthlete.getId()));

        List<TrainingSession> sessions = trainingSessionRepository.findAllByAthleteId(athleteId);

        assertThat(sessions).hasSize(2).allMatch(s -> s.getAthleteId() == athleteId);
    }

    @Test
    void findAllByAthleteId_shouldReturnEmpty_whenAthleteHasNoSessions() {
        List<TrainingSession> sessions = trainingSessionRepository.findAllByAthleteId(athleteId);

        assertThat(sessions).isEmpty();
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

    private TrainingSession aSession(long forAthleteId) {
        return new TrainingSession(LocalDate.of(2024, 3, 1), Sport.CYCLING, 5, 60, TargetZone.Z2, forAthleteId);
    }
}
