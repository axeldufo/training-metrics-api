package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.TestContainersConfiguration;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.PageResult;
import com.axel.trainingmetricsapi.domain.Sport;
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
@Import(TestContainersConfiguration.class)
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
        saveAthlete("Bob", "Jones", requestingCoachId);
        saveAthlete("Dylan", "Vernon", requestingCoachId);

        CoachJpaEntity anOtherCoach = coachJpaRepository.save(aCoach("Other Coach", "other@test.com"));
        saveAthlete("John", "Thomas", anOtherCoach.getId());

        PageResult<Athlete> athletePage = athleteRepository.findAllByCoachId(requestingCoachId, 0, 20);

        assertThat(athletePage.totalElements()).isEqualTo(2);
        assertThat(athletePage.content()).hasSize(2);
        assertThat(athletePage.content())
            .extracting(Athlete::getFirstName)
            .containsExactlyInAnyOrder("Bob", "Dylan");
        assertThat(athletePage.pageNumber()).isZero();
    }

    @Test
    void findAllByCoachId_shouldPaginateAthletesOfRequestingCoach() {
        CoachJpaEntity requestingCoach = coachJpaRepository.save(aCoach("Coach Requesting", "coach@test.com"));
        long requestingCoachId = requestingCoach.getId();
        int nbAthletesCreated = 6;
        for (int i = 0; i < nbAthletesCreated; i++) {
            saveAthlete("Athlete" + i, "LastName" + i, requestingCoachId);
        }
        int pageSize = 5;

        PageResult<Athlete> firstPage = athleteRepository.findAllByCoachId(requestingCoachId, 0, pageSize);
        PageResult<Athlete> secondPage = athleteRepository.findAllByCoachId(requestingCoachId, 1, pageSize);

        assertThat(firstPage.totalElements()).isEqualTo(nbAthletesCreated);
        assertThat(firstPage.content()).hasSize(pageSize);
        assertThat(firstPage.pageNumber()).isZero();

        assertThat(secondPage.totalElements()).isEqualTo(nbAthletesCreated);
        assertThat(secondPage.content()).hasSize(nbAthletesCreated-pageSize);
        assertThat(secondPage.pageNumber()).isEqualTo(1);
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

    private void saveAthlete(String firstName, String lastName, long coachId) {
        athleteRepository.save(new Athlete(firstName, lastName,
            LocalDate.of(1990, 1, 1), Sport.DUATHLON, coachId, 70.0));
    }
}
