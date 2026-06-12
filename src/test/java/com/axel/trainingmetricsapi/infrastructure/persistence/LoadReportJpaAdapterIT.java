package com.axel.trainingmetricsapi.infrastructure.persistence;

import com.axel.trainingmetricsapi.TestContainersConfiguration;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.domain.Sport;
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
class LoadReportJpaAdapterIT {

    private static final LocalDate WEEK_1 = LocalDate.of(2025, Month.APRIL, 7);  // Monday
    private static final LocalDate WEEK_2 = LocalDate.of(2025, Month.APRIL, 14); // Monday
    private static final LocalDate WEEK_3 = LocalDate.of(2025, Month.APRIL, 21); // Monday
    private static final LocalDate WEEK_5 = LocalDate.of(2025, Month.MAY, 5);  // Monday

    @Autowired
    private LoadReportRepository loadReportRepository;

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
    void save_shouldPersistWithGeneratedUpdatedAt() {
        LoadReport report = aReport(athleteId, WEEK_1, 300, 3);

        LoadReport saved = loadReportRepository.save(report);

        assertThat(saved.athleteId()).isEqualTo(athleteId);
        assertThat(saved.weekStartDate()).isEqualTo(WEEK_1);
        assertThat(saved.totalFosterLoad()).isEqualTo(300);
        assertThat(saved.sessionCount()).isEqualTo(3);
        assertThat(saved.updatedAt()).isNotNull();
    }

    @Test
    void save_shouldUpsertExistingReport_whenSameAthleteAndWeek() {
        loadReportRepository.save(aReport(athleteId, WEEK_1, 100, 1));

        LoadReport updated = loadReportRepository.save(aReport(athleteId, WEEK_1, 400, 4));

        assertThat(updated.totalFosterLoad()).isEqualTo(400);
        assertThat(updated.sessionCount()).isEqualTo(4);
        Optional<LoadReport> found = loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, WEEK_1);
        assertThat(found).isPresent();
        assertThat(found.get().totalFosterLoad()).isEqualTo(400);
    }

    // FK constraints are only checked at commit time in PostgreSQL.
    // NOT_SUPPORTED suspends the class-level transaction so operations commit immediately.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldThrowException_whenAthleteDoesNotExist() {
        LoadReport report = aReport(9999L, WEEK_1, 100, 1);

        assertThatThrownBy(() -> loadReportRepository.save(report))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByAthleteIdAndWeekStartDate_shouldReturnFromDatabase() {
        loadReportRepository.save(aReport(athleteId, WEEK_1, 250, 2));

        Optional<LoadReport> found = loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, WEEK_1);

        assertThat(found).isPresent();
        assertThat(found.get().athleteId()).isEqualTo(athleteId);
        assertThat(found.get().totalFosterLoad()).isEqualTo(250);
        assertThat(found.get().sessionCount()).isEqualTo(2);
    }

    @Test
    void findByAthleteIdAndWeekStartDate_shouldReturnEmpty_whenNotFound() {
        assertThat(loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, WEEK_1)).isEmpty();
    }

    @Test
    void findLatestByAthleteId_shouldReturnMostRecentReport() {
        loadReportRepository.save(aReport(athleteId, WEEK_1, 100, 1));
        loadReportRepository.save(aReport(athleteId, WEEK_3, 300, 3));
        loadReportRepository.save(aReport(athleteId, WEEK_2, 200, 2));

        Optional<LoadReport> latest = loadReportRepository.findLatestByAthleteId(athleteId);

        assertThat(latest).isPresent();
        assertThat(latest.get().weekStartDate()).isEqualTo(WEEK_3);
    }

    @Test
    void findLatestByAthleteId_shouldReturnEmpty_whenNoReports() {
        assertThat(loadReportRepository.findLatestByAthleteId(athleteId)).isEmpty();
    }

    @Test
    void findByAthleteIdAndWeekStartDateBetween_shouldReturnEntriesInRange() {
        loadReportRepository.save(aReport(athleteId, WEEK_1, 100, 1)); // before range
        loadReportRepository.save(aReport(athleteId, WEEK_2, 200, 2)); // in range
        loadReportRepository.save(aReport(athleteId, WEEK_3, 300, 3)); // in range (inclusive)
        loadReportRepository.save(aReport(athleteId, WEEK_5, 500, 5)); // after range

        List<LoadReport> result = loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            athleteId, WEEK_2, WEEK_3);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(LoadReport::weekStartDate)
            .containsExactlyInAnyOrder(WEEK_2, WEEK_3);
    }

    @Test
    void findByAthleteIdAndWeekStartDateBetween_shouldReturnEmpty_whenNoneInRange() {
        loadReportRepository.save(aReport(athleteId, WEEK_1, 100, 1));

        List<LoadReport> result = loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            athleteId, WEEK_5, WEEK_5);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByAthleteIdAndWeekStartDate_shouldRemoveFromDatabase() {
        loadReportRepository.save(aReport(athleteId, WEEK_1, 100, 1));

        loadReportRepository.deleteByAthleteIdAndWeekStartDate(athleteId, WEEK_1);

        assertThat(loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, WEEK_1)).isEmpty();
    }

    @Test
    void deleteByAthleteIdAndWeekStartDate_shouldDoNothing_whenNotFound() {
        loadReportRepository.deleteByAthleteIdAndWeekStartDate(athleteId, WEEK_1);

        assertThat(loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, WEEK_1)).isEmpty();
    }

    private CoachJpaEntity aCoach() {
        // Unique email required — NOT_SUPPORTED tests bypass class-level @Transactional rollback,
        // leaving the coach in DB across test method executions
        return CoachJpaEntity.builder()
            .name("Load Coach")
            .email("load-coach-" + System.nanoTime() + "@test.com")
            .hashedPassword("hashed")
            .build();
    }

    private Athlete anAthlete(long coachId) {
        return new Athlete("Alice", "Smith", LocalDate.of(1990, Month.JANUARY, 1), Sport.CYCLING, coachId, 60.0);
    }

    private LoadReport aReport(long forAthleteId, LocalDate weekStartDate, int totalFosterLoad, int sessionCount) {
        return new LoadReport(forAthleteId, weekStartDate, totalFosterLoad, sessionCount, null);
    }
}
