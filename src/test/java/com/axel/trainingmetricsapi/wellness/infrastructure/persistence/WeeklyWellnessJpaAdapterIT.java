package com.axel.trainingmetricsapi.wellness.infrastructure.persistence;

import com.axel.trainingmetricsapi.TestContainersConfiguration;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.identity.infrastructure.persistence.CoachJpaEntity;
import com.axel.trainingmetricsapi.identity.infrastructure.persistence.CoachJpaRepository;
import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellnessRepository;
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
class WeeklyWellnessJpaAdapterIT {

    // Week dates — 2024-01-01 is a Monday
    private static final LocalDate WEEK_1 = LocalDate.of(2024, Month.JANUARY, 1);
    private static final LocalDate WEEK_3 = LocalDate.of(2024, Month.JANUARY, 15);
    private static final LocalDate WEEK_5 = LocalDate.of(2024, Month.JANUARY, 29);
    private static final LocalDate WEEK_7 = LocalDate.of(2024, Month.FEBRUARY, 12);

    @Autowired
    private WeeklyWellnessRepository wellnessRepository;

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
    void save_shouldPersistWithGeneratedId() {
        WeeklyWellness wellness = aWellness(athleteId, WEEK_1);

        WeeklyWellness saved = wellnessRepository.save(wellness);

        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(saved.getAthleteId()).isEqualTo(athleteId);
        assertThat(saved.getWeekStartDate()).isEqualTo(WEEK_1);
        assertThat(saved.getPerceivedDifficulty()).isEqualTo(3);
        assertThat(saved.getPerceivedFatigue()).isEqualTo(4);
        assertThat(saved.getMotivation()).isEqualTo(2);
    }

    // FK constraints are only checked at commit time in PostgreSQL.
    // NOT_SUPPORTED suspends the class-level transaction so operations commit immediately.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldThrowException_whenAthleteDoesNotExist() {
        WeeklyWellness wellness = aWellness(9999L, WEEK_1);

        assertThatThrownBy(() -> wellnessRepository.save(wellness))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    // UNIQUE constraints are only checked at commit time in PostgreSQL.
    // NOT_SUPPORTED suspends the class-level transaction so operations commit immediately.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldThrowException_whenDuplicateAthleteAndWeekStartDate() {
        WeeklyWellness firstWellness = wellnessRepository.save(aWellness(athleteId, WEEK_1));

        WeeklyWellness newWellness = aWellness(athleteId, WEEK_1);
        assertThatThrownBy(() -> wellnessRepository.save(newWellness))
            .isInstanceOf(DataIntegrityViolationException.class);

        // cleanup — NOT_SUPPORTED bypasses class-level @Transactional rollback
        wellnessRepository.deleteById(firstWellness.getId());
    }

    @Test
    void findById_shouldReturnFromDatabase() {
        WeeklyWellness saved = wellnessRepository.save(aWellness(athleteId, WEEK_1));

        Optional<WeeklyWellness> found = wellnessRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAthleteId()).isEqualTo(athleteId);
        assertThat(found.get().getWeekStartDate()).isEqualTo(WEEK_1);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        assertThat(wellnessRepository.findById(9999L)).isEmpty();
    }

    @Test
    void findByAthleteIdAndPeriod_shouldReturnOnlyEntriesWithinRange() {
        // 4 entries spanning 8 weeks; query a 4-week window, verify only 2 are returned
        wellnessRepository.save(aWellness(athleteId, WEEK_1)); // before range
        wellnessRepository.save(aWellness(athleteId, WEEK_3)); // in range
        wellnessRepository.save(aWellness(athleteId, WEEK_5)); // in range (inclusive boundary)
        wellnessRepository.save(aWellness(athleteId, WEEK_7)); // after range

        LocalDate from = LocalDate.of(2024, Month.JANUARY, 8);  // week 2
        LocalDate to = WEEK_5;                      // week 5 inclusive

        List<WeeklyWellness> result = wellnessRepository.findByAthleteIdAndPeriod(athleteId, from, to);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(WeeklyWellness::getWeekStartDate)
            .containsExactlyInAnyOrder(WEEK_3, WEEK_5);
    }

    @Test
    void findByAthleteIdAndPeriod_shouldReturnEmpty_whenNoneInRange() {
        wellnessRepository.save(aWellness(athleteId, WEEK_1));

        List<WeeklyWellness> result = wellnessRepository.findByAthleteIdAndPeriod(
            athleteId, LocalDate.of(2024, Month.FEBRUARY, 1), LocalDate.of(2024, Month.MARCH, 1));

        assertThat(result).isEmpty();
    }

    @Test
    void findByAthleteIdAndWeekStartDate_shouldReturnWellness_whenExists() {
        LocalDate monday = LocalDate.of(2026, Month.JANUARY, 12); // 12/01/26 is a Monday
        wellnessRepository.save(aWellness(athleteId, monday));

        Optional<WeeklyWellness> result = wellnessRepository.findByAthleteIdAndWeekStartDate(athleteId, monday);

        assertThat(result).isPresent();
        assertThat(result.get().getWeekStartDate()).isEqualTo(monday);
    }

    @Test
    void findLatestByAthleteId_shouldReturnMostRecentWellness() {
        LocalDate monday1 = LocalDate.of(2025, Month.MAY, 19);
        LocalDate monday2 = LocalDate.of(2025, Month.MAY, 26);
        wellnessRepository.save(aWellness(athleteId, monday1));
        wellnessRepository.save(aWellness(athleteId, monday2));

        Optional<WeeklyWellness> result = wellnessRepository.findLatestByAthleteId(athleteId);

        assertThat(result).isPresent();
        assertThat(result.get().getWeekStartDate()).isEqualTo(monday2);
    }

    @Test
    void deleteById_shouldRemoveFromDatabase() {
        WeeklyWellness saved = wellnessRepository.save(aWellness(athleteId, WEEK_1));

        wellnessRepository.deleteById(saved.getId());

        assertThat(wellnessRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void existsById_shouldReturnTrue_afterSave() {
        WeeklyWellness saved = wellnessRepository.save(aWellness(athleteId, WEEK_1));

        assertThat(wellnessRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_forNonExistentId() {
        assertThat(wellnessRepository.existsById(9999L)).isFalse();
    }

    @Test
    void existsByAthleteIdAndWeekStartDate_shouldReturnTrue_afterSave() {
        wellnessRepository.save(aWellness(athleteId, WEEK_1));

        assertThat(wellnessRepository.existsByAthleteIdAndWeekStartDate(athleteId, WEEK_1)).isTrue();
    }

    @Test
    void existsByAthleteIdAndWeekStartDate_shouldReturnFalse_whenNotExists() {
        assertThat(wellnessRepository.existsByAthleteIdAndWeekStartDate(athleteId, WEEK_1)).isFalse();
    }

    private CoachJpaEntity aCoach() {
        // Unique email required — NOT_SUPPORTED tests bypass class-level @Transactional rollback,
        // leaving the coach in DB across test method executions
        return CoachJpaEntity.builder()
            .name("Wellness Coach")
            .email("wellness-coach-" + System.nanoTime() + "@test.com")
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

    private WeeklyWellness aWellness(long forAthleteId, LocalDate weekStartDate) {
        return new WeeklyWellness(forAthleteId, weekStartDate, 3, 4, 2);
    }
}
