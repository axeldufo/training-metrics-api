package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.wellness.domain.CorrelationAlert;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.training.domain.TargetZone;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyReport;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyReportNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWeeklyReportByWeekUseCaseImplTest {

    private static final long ATHLETE_ID = 1L;
    private static final long COACH_ID = 2L;
    private static final LocalDate MONDAY = LocalDate.of(2025, Month.MAY, 19);

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private LoadReportRepository loadReportRepository;

    @Mock
    private WeeklyWellnessRepository weeklyWellnessRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private GetWeeklyReportByWeekUseCaseImpl useCase;

    @Test
    void execute_nominal_loadFoundInDB_wellnessFound_returnsReport() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        LoadReport current = aLoad(MONDAY, 100, 2);
        LoadReport prev1 = aLoad(MONDAY.minusWeeks(1), 80, 1);
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, MONDAY.minusWeeks(3), MONDAY))
            .thenReturn(List.of(current, prev1));
        WeeklyWellness wellness = aWellness(MONDAY, 3, 3, 4);
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY.minusWeeks(4), MONDAY))
            .thenReturn(List.of(wellness));

        WeeklyReport report = useCase.execute(ATHLETE_ID, COACH_ID, MONDAY);

        assertThat(report.athleteId()).isEqualTo(ATHLETE_ID);
        assertThat(report.weekStartDate()).isEqualTo(MONDAY);
        assertThat(report.wellnessAvailable()).isTrue();
        assertThat(report.sessionCount()).isEqualTo(2);
        assertThat(report.totalFosterLoad()).isEqualTo(100);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
    }

    @Test
    void execute_loadReportsEmpty_onTheFlyLoadHasSessions_returnsReport() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, MONDAY.minusWeeks(3), MONDAY))
            .thenReturn(List.of());
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY.minusWeeks(4), MONDAY))
            .thenReturn(List.of(aWellness(MONDAY, 3, 3, 4)));
        TrainingSession session1 = aSession(5, 60);
        TrainingSession session2 = aSession(8, 45);
        when(trainingSessionRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY, MONDAY.plusDays(6)))
            .thenReturn(List.of(session1, session2));

        WeeklyReport report = useCase.execute(ATHLETE_ID, COACH_ID, MONDAY);

        assertThat(report.sessionCount()).isEqualTo(2);
        assertThat(report.wellnessAvailable()).isTrue();
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
        verify(trainingSessionRepository).findByAthleteIdAndPeriod(ATHLETE_ID, MONDAY, MONDAY.plusDays(6));
    }

    @Test
    void execute_loadReportsEmpty_noWellness_throwsNotFoundException() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, MONDAY.minusWeeks(3), MONDAY))
            .thenReturn(List.of());
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY.minusWeeks(4), MONDAY))
            .thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, MONDAY))
            .isInstanceOf(WeeklyReportNotFoundException.class);

        verifyNoInteractions(trainingSessionRepository);
    }

    @Test
    void execute_loadFoundNoWellness_returnsInsufficientData() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        LoadReport load = aLoad(MONDAY, 100, 2);
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(
            ATHLETE_ID, MONDAY.minusWeeks(3), MONDAY))
            .thenReturn(List.of(load));
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(
            ATHLETE_ID, MONDAY.minusWeeks(4), MONDAY))
            .thenReturn(List.of());

        WeeklyReport report = useCase.execute(ATHLETE_ID, COACH_ID, MONDAY);

        assertThat(report.wellnessAvailable()).isFalse();
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
        verifyNoInteractions(trainingSessionRepository);
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, MONDAY))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(loadReportRepository, weeklyWellnessRepository, trainingSessionRepository);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, MONDAY))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(loadReportRepository, weeklyWellnessRepository, trainingSessionRepository);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }

    private LoadReport aLoad(LocalDate weekStart, int totalFosterLoad, int sessionCount) {
        return new LoadReport(ATHLETE_ID, weekStart, totalFosterLoad, sessionCount, LocalDateTime.now());
    }

    private WeeklyWellness aWellness(LocalDate weekStart, int difficulty, int fatigue, int motivation) {
        return new WeeklyWellness(ATHLETE_ID, weekStart, difficulty, fatigue, motivation);
    }

    private TrainingSession aSession(int rpe, int durationInMin) {
        return new TrainingSession(MONDAY, Sport.ROAD_RUNNING, rpe, durationInMin, TargetZone.Z2, ATHLETE_ID);
    }
}
