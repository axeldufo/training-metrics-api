package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.wellness.application.port.in.GetWeeklyReportByWeekUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.wellness.domain.CorrelationAlert;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyReport;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyReportNotFoundException;
import com.axel.trainingmetricsapi.training.domain.AcwrAlert;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWeeklyReportsByPeriodUseCaseImplTest {

    private static final long ATHLETE_ID = 1L;
    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private GetWeeklyReportByWeekUseCase getWeeklyReportByWeekUseCase;

    @InjectMocks
    private GetWeeklyReportsByPeriodUseCaseImpl useCase;

    @Test
    void execute_3MondaysInRange_2HaveData_1SkippedDueToNotFound_returns2Reports() {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 28);
        LocalDate to = LocalDate.of(2025, Month.MAY, 12);
        LocalDate m1 = LocalDate.of(2025, Month.APRIL, 28);
        LocalDate m2 = LocalDate.of(2025, Month.MAY, 5);
        LocalDate m3 = LocalDate.of(2025, Month.MAY, 12);
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        when(getWeeklyReportByWeekUseCase.execute(ATHLETE_ID, COACH_ID, m1)).thenReturn(aReport(m1));
        when(getWeeklyReportByWeekUseCase.execute(ATHLETE_ID, COACH_ID, m2))
            .thenThrow(new WeeklyReportNotFoundException(ATHLETE_ID, m2));
        when(getWeeklyReportByWeekUseCase.execute(ATHLETE_ID, COACH_ID, m3)).thenReturn(aReport(m3));

        List<WeeklyReport> reports = useCase.execute(ATHLETE_ID, COACH_ID, from, to);

        assertThat(reports).hasSize(2);
        assertThat(reports).extracting(WeeklyReport::weekStartDate).containsExactly(m1, m3);
    }

    @Test
    void execute_noData_returnsEmptyList() {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 28);
        LocalDate to = LocalDate.of(2025, Month.MAY, 5);
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(getWeeklyReportByWeekUseCase.execute(anyLong(), anyLong(), any()))
            .thenThrow(new WeeklyReportNotFoundException(ATHLETE_ID));

        List<WeeklyReport> reports = useCase.execute(ATHLETE_ID, COACH_ID, from, to);

        assertThat(reports).isEmpty();
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 28);
        LocalDate to = LocalDate.of(2025, Month.MAY, 5);
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, from, to))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(getWeeklyReportByWeekUseCase);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 28);
        LocalDate to = LocalDate.of(2025, Month.MAY, 5);
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, from, to))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(getWeeklyReportByWeekUseCase);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }

    private WeeklyReport aReport(LocalDate weekStart) {
        return new WeeklyReport(
            ATHLETE_ID, weekStart, false,
            100, 1,
            100.0, 80.0, 1.25,
            AcwrAlert.OK, true,
            null, null, null, null, null, null,
            Set.of(), Set.of(), Set.of(),
            CorrelationAlert.INSUFFICIENT_DATA
        );
    }
}
