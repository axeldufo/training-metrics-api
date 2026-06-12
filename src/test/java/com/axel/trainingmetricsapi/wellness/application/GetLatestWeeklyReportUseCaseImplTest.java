package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.wellness.application.port.in.GetWeeklyReportByWeekUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.wellness.domain.CorrelationAlert;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
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
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetLatestWeeklyReportUseCaseImplTest {

    private static final long ATHLETE_ID = 1L;
    private static final long COACH_ID = 2L;
    private static final LocalDate MONDAY = LocalDate.of(2025, Month.MAY, 19);

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private LoadReportRepository loadReportRepository;

    @Mock
    private GetWeeklyReportByWeekUseCase getWeeklyReportByWeekUseCase;

    @InjectMocks
    private GetLatestWeeklyReportUseCaseImpl useCase;

    @Test
    void execute_loadReportFound_delegatesToGetWeeklyReportByWeek() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        LoadReport latest = new LoadReport(ATHLETE_ID, MONDAY, 150, 2, LocalDateTime.now());
        when(loadReportRepository.findLatestByAthleteId(ATHLETE_ID)).thenReturn(Optional.of(latest));
        WeeklyReport weeklyReport = aReport(MONDAY);
        when(getWeeklyReportByWeekUseCase.execute(ATHLETE_ID, COACH_ID, MONDAY)).thenReturn(weeklyReport);

        WeeklyReport result = useCase.execute(ATHLETE_ID, COACH_ID);

        assertThat(result.weekStartDate()).isEqualTo(MONDAY);
        verify(loadReportRepository).findLatestByAthleteId(ATHLETE_ID);
        verify(getWeeklyReportByWeekUseCase).execute(ATHLETE_ID, COACH_ID, MONDAY);
    }

    @Test
    void execute_noLoadReport_throwsNotFoundException() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(loadReportRepository.findLatestByAthleteId(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID))
            .isInstanceOf(WeeklyReportNotFoundException.class);

        verifyNoInteractions(getWeeklyReportByWeekUseCase);
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(loadReportRepository, getWeeklyReportByWeekUseCase);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(loadReportRepository, getWeeklyReportByWeekUseCase);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }

    private WeeklyReport aReport(LocalDate weekStart) {
        return new WeeklyReport(
            ATHLETE_ID, weekStart, false,
            150, 2,
            150.0, 100.0, 1.5,
            AcwrAlert.OK, true,
            null, null, null, null, null, null,
            Set.of(), Set.of(), Set.of(),
            CorrelationAlert.INSUFFICIENT_DATA
        );
    }
}
