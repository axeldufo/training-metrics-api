package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.PostgresTestContainersConfiguration;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.domain.CoachRepository;
import com.axel.trainingmetricsapi.domain.Sport;
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
class CoachJpaAdapterIT {

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    @Test
    void save_shouldPersistCoachWithGeneratedId() {
        Coach coach = aCoach();

        Coach saved = coachRepository.save(coach);

        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(saved.getName()).isEqualTo("Alice Martin");
    }

    @Test
    void findById_shouldReturnCoachFromDatabase() {
        Coach saved = coachRepository.save(aCoach());

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
    void findAll_shouldReturnAllPersistedCoaches() {
        coachRepository.save(aCoach());
        coachRepository.save(new Coach("Bob Durand"));

        List<Coach> coaches = coachRepository.findAll();

        assertThat(coaches).hasSize(2);
    }

    @Test
    void deleteById_shouldRemoveCoachFromDatabase() {
        Coach saved = coachRepository.save(aCoach());

        coachRepository.deleteById(saved.getId());

        assertThat(coachRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteById_shouldThrowException_whenAthleteReferencesCoach() {
        Coach saved = coachRepository.save(aCoach());
        athleteRepository.save(new Athlete("Bob", "Jones", LocalDate.of(1990, 1, 1), Sport.CYCLING, saved.getId(), 70.0));

        assertThatThrownBy(() -> coachRepository.deleteById(saved.getId()))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existsById_shouldReturnTrue_afterSave() {
        Coach saved = coachRepository.save(aCoach());

        assertThat(coachRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_forNonExistentId() {
        assertThat(coachRepository.existsById(9999L)).isFalse();
    }

    private Coach aCoach() {
        return new Coach("Alice Martin");
    }
}
