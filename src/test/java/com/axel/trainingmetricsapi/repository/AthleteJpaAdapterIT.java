package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.PostgresTestContainersConfiguration;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
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
class AthleteJpaAdapterIT {

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private CoachJpaRepository coachJpaRepository;

    @Test
    void save_shouldPersistAthleteWithGeneratedId() {
        Athlete athlete = anAthlete();

        Athlete saved = athleteRepository.save(athlete);

        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(saved.getFirstName()).isEqualTo("Alice");
        assertThat(saved.getLastName()).isEqualTo("Smith");
        assertThat(saved.getSport()).isEqualTo(Sport.CYCLING);
    }

    @Test
    void save_shouldThrowException_whenCoachDoesNotExist() {
        Athlete athlete = new Athlete("Alice", "Smith",
            LocalDate.of(1990, 1, 1), Sport.CYCLING, 9999L, 60.0);

        assertThatThrownBy(() -> athleteRepository.save(athlete))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findById_shouldReturnAthleteFromDatabase() {
        Athlete saved = athleteRepository.save(anAthlete());

        Optional<Athlete> found = athleteRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Alice");
        assertThat(found.get().getSport()).isEqualTo(Sport.CYCLING);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        Optional<Athlete> found = athleteRepository.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAllByCoachId_shouldReturnOnlyAthletesOfRequestingCoach() {
        CoachJpaEntity requestingCoach = coachJpaRepository.save(aCoach("Coach Requesting", "coach@test.com"));
        long requestingCoachId = requestingCoach.getId();
        athleteRepository.save(new Athlete("Bob", "Jones", LocalDate.of(1985, 6, 15), Sport.ROAD_RUNNING,
            requestingCoachId, 75.0));
        athleteRepository.save(new Athlete("Dylan", "Vernon", LocalDate.of(1999, 8, 27), Sport.DUATHLON,
            requestingCoachId, 69.0));

        CoachJpaEntity anOtherCoach = coachJpaRepository.save(aCoach("Other Coach", "other@test.com"));
        athleteRepository.save(new Athlete("John", "Thomas", LocalDate.of(2001, 10, 9), Sport.CYCLING,
            anOtherCoach.getId(), 72.0));

        List<Athlete> athletes = athleteRepository.findAllByCoachId(requestingCoachId);

        assertThat(athletes).hasSize(2);
        assertThat(athletes)
            .extracting(Athlete::getFirstName)
            .containsExactlyInAnyOrder("Bob", "Dylan");
    }

    @Test
    void deleteById_shouldRemoveAthleteFromDatabase() {
        Athlete saved = athleteRepository.save(anAthlete());

        athleteRepository.deleteById(saved.getId());

        assertThat(athleteRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void existsById_shouldReturnTrue_afterSave() {
        Athlete saved = athleteRepository.save(anAthlete());

        assertThat(athleteRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_forNonExistentId() {
        assertThat(athleteRepository.existsById(9999L)).isFalse();
    }

    private Athlete anAthlete() {
        CoachJpaEntity requestingCoach = coachJpaRepository.save(
            aCoach("Coach Requesting", "coach@test.com"));
        return new Athlete("Alice", "Smith", LocalDate.of(1990, 1, 1), Sport.CYCLING, requestingCoach.getId(), 60.0);
    }

    private CoachJpaEntity aCoach(String name, String email) {
        return CoachJpaEntity.builder()
            .name(name)
            .email(email)
            .hashedPassword("hashed")
            .build();
    }
}
