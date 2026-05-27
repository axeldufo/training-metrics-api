package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.PostgresTestContainersConfiguration;
import com.axel.trainingmetricsapi.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestContainersConfiguration.class)
@Transactional
class CoachJpaAdapterIT {

    @Autowired
    private CoachJpaRepository coachJpaRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    private final CoachPersistenceMapper coachPersistenceMapper = new CoachPersistenceMapper();

    @Test
    void findById_shouldReturnCoachFromDatabase() {
        Coach saved = persistACoach();

        Optional<Coach> found = coachRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice Martin");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        Optional<Coach> found = coachRepository.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteById_shouldRemoveCoachFromDatabase() {
        Coach saved = persistACoach();

        coachRepository.deleteById(saved.getId());

        assertThat(coachRepository.findById(saved.getId())).isEmpty();
    }

    // FK constraints are only checked at commit time in PostgreSQL.
    // NOT_SUPPORTED suspends the class-level transaction so operations commit immediately.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deleteById_shouldThrowException_whenAthleteReferencesCoach() {
        Coach saved = persistACoach();
        long coachId = saved.getId();
        Athlete athleteSaved = athleteRepository.save(
            new Athlete("Bob", "Jones", LocalDate.of(1990, 1, 1), Sport.CYCLING, coachId, 70.0));

        assertThatThrownBy(() -> coachRepository.deleteById(coachId))
            .isInstanceOf(DataIntegrityViolationException.class);

        // Manual cleanup — no rollback outside transaction
        athleteRepository.deleteById(athleteSaved.getId());
        coachRepository.deleteById(coachId);
    }

    @Test
    void existsById_shouldReturnTrue_afterSave() {
        Coach saved = persistACoach();

        assertThat(coachRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_forNonExistentId() {
        assertThat(coachRepository.existsById(9999L)).isFalse();
    }

    @Test
    void updateName_shouldReturnCoachWithUpdatedField() {
        Coach saved = persistACoach();
        String newName = "AM Coaching";

        coachRepository.updateName(saved.getId(), newName);

        Optional<Coach> updated = coachRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo(newName);
    }

    private Coach persistACoach(String name, String email) {
        CoachJpaEntity entity = CoachJpaEntity.builder()
            .name(name)
            .email(email)
            .hashedPassword("hashedPassword")
            .build();
        return coachPersistenceMapper.entityToDomain(coachJpaRepository.save(entity));
    }

    private Coach persistACoach() {
        return persistACoach("Alice Martin", "coach@test.com");
    }
}
