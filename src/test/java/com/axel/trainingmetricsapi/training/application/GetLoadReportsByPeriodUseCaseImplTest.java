package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLoadReportsByPeriodUseCaseImplTest {

    private static final long ATHLETE_ID = 1L;
    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private LoadReportRepository loadReportRepository;

    @InjectMocks
    private GetLoadReportsByPeriodUseCaseImpl useCase;

    @Test
    void execute_shouldDelegateToRepository() {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 1);
        LocalDate to = LocalDate.of(2025, Month.MAY, 26);
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        LocalDate monday = LocalDate.of(2025, Month.MAY, 19);
        List<LoadReport> reports = List.of(new LoadReport(ATHLETE_ID, monday, 200, 2,
            LocalDateTime.of(2026, Month.JANUARY, 12, 10, 0)));
        when(loadReportRepository.findByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to))
            .thenReturn(reports);

        List<LoadReport> result = useCase.execute(ATHLETE_ID, COACH_ID, from, to);

        assertThat(result).isEqualTo(reports);
        verify(loadReportRepository).findByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to);
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 1);
        LocalDate to = LocalDate.of(2025, Month.MAY, 26);
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, from, to))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(loadReportRepository);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        LocalDate from = LocalDate.of(2025, Month.APRIL, 1);
        LocalDate to = LocalDate.of(2025, Month.MAY, 26);
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, from, to))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(loadReportRepository);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }
}
