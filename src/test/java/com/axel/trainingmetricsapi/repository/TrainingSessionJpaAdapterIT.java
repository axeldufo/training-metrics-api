package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.PostgresTestContainersConfiguration;
import com.axel.trainingmetricsapi.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
            new Athlete("Bob", "Jones", LocalDate.of(1985, 6, 15), Sport.ROAD_RUNNING, coachId, 75.0));
        trainingSessionRepository.save(aSession(athleteId));
        trainingSessionRepository.save(aSession(athleteId));
        trainingSessionRepository.save(aSession(otherAthlete.getId()));

        PageResult<TrainingSession> sessions = trainingSessionRepository.findAllByAthleteId(athleteId, 0, 20);

        assertThat(sessions.content()).hasSize(2).allMatch(s -> s.getAthleteId() == athleteId);
    }

    @Test
    void findAllByAthleteId_shouldReturnEmpty_whenAthleteHasNoSessions() {
        PageResult<TrainingSession> sessions = trainingSessionRepository.findAllByAthleteId(athleteId, 0, 20);

        assertThat(sessions.content()).isEmpty();
        assertThat(sessions.totalElements()).isEqualTo(0);
    }

    @Test
    void findAllByAthleteId_shouldPaginateAthleteSessions() {
        int nbSessionsCreated = 6;
        for (int i = 0; i < nbSessionsCreated; i++) {
            trainingSessionRepository.save(aSession(athleteId));
        }
        int pageSize = 4;

        PageResult<TrainingSession> firstPage = trainingSessionRepository.findAllByAthleteId(athleteId, 0, pageSize);
        PageResult<TrainingSession> secondPage = trainingSessionRepository.findAllByAthleteId(athleteId, 1, pageSize);

        assertThat(firstPage.totalElements()).isEqualTo(nbSessionsCreated);
        assertThat(firstPage.content()).hasSize(pageSize);
        assertThat(firstPage.pageNumber()).isEqualTo(0);

        assertThat(secondPage.totalElements()).isEqualTo(nbSessionsCreated);
        assertThat(secondPage.content()).hasSize(nbSessionsCreated-pageSize);
        assertThat(secondPage.pageNumber()).isEqualTo(1);
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
            LocalDate.of(1990, 1, 1),
            Sport.CYCLING,
            coachId,
            56.0);
    }

    private TrainingSession aSession(long forAthleteId) {
        return new TrainingSession(
            LocalDate.of(2024, 3, 1),
            Sport.CYCLING,
            5,
            60,
            TargetZone.Z2,
            forAthleteId);
    }
}
