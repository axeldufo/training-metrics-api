package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.training.domain.TargetZone;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
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
class GetLoadReportByWeekUseCaseImplTest {

    private static final long ATHLETE_ID = 1L;
    private static final long COACH_ID = 2L;
    private static final LocalDate MONDAY = LocalDate.of(2025, Month.MAY, 19);

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private LoadReportRepository loadReportRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private GetLoadReportByWeekUseCaseImpl useCase;

    @Test
    void execute_shouldReturnPersistedReport_whenFoundInDB() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        LocalDateTime updatedAt = LocalDateTime.now();
        LoadReport persisted = new LoadReport(ATHLETE_ID, MONDAY, 200, 2, updatedAt);
        when(loadReportRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(Optional.of(persisted));

        LoadReport result = useCase.execute(ATHLETE_ID, COACH_ID, MONDAY);

        assertThat(result).isEqualTo(persisted);
        verify(loadReportRepository).findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);
        verifyNoInteractions(trainingSessionRepository);
    }

    @Test
    void execute_shouldCalculateOnTheFly_whenNotInDB() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(loadReportRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(Optional.empty());
        TrainingSession session1 = aSession(5, 60);
        TrainingSession session2 = aSession(8, 45);
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, MONDAY, MONDAY.plusDays(6)))
            .thenReturn(List.of(session1, session2));

        LoadReport result = useCase.execute(ATHLETE_ID, COACH_ID, MONDAY);

        assertThat(result.athleteId()).isEqualTo(ATHLETE_ID);
        assertThat(result.weekStartDate()).isEqualTo(MONDAY);
        assertThat(result.totalFosterLoad()).isEqualTo(660);
        assertThat(result.sessionCount()).isEqualTo(2);
        assertThat(result.updatedAt()).isNotNull();
    }

    @Test
    void execute_shouldReturnZeroReport_whenNoSessionsExist() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(loadReportRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(Optional.empty());
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, MONDAY, MONDAY.plusDays(6)))
            .thenReturn(List.of());

        LoadReport result = useCase.execute(ATHLETE_ID, COACH_ID, MONDAY);

        assertThat(result.athleteId()).isEqualTo(ATHLETE_ID);
        assertThat(result.weekStartDate()).isEqualTo(MONDAY);
        assertThat(result.totalFosterLoad()).isZero();
        assertThat(result.sessionCount()).isZero();
        assertThat(result.updatedAt()).isNull();
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, MONDAY))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(loadReportRepository, trainingSessionRepository);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, MONDAY))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(loadReportRepository, trainingSessionRepository);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }

    private TrainingSession aSession(int rpe, int durationInMin) {
        return new TrainingSession(MONDAY, Sport.ROAD_RUNNING, rpe, durationInMin, TargetZone.Z2, ATHLETE_ID);
    }
}
