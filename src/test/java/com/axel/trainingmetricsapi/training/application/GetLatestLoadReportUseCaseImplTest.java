package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.training.domain.exception.LoadReportNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLatestLoadReportUseCaseImplTest {

    private static final long ATHLETE_ID = 1L;
    private static final long COACH_ID = 2L;
    private static final LocalDate MONDAY = LocalDate.of(2025, Month.MAY, 19);

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private LoadReportRepository loadReportRepository;

    @InjectMocks
    private GetLatestLoadReportUseCaseImpl useCase;

    @Test
    void execute_shouldReturnReport_whenExists() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        LoadReport report = new LoadReport(ATHLETE_ID, MONDAY, 150, 2, LocalDateTime.of(2026, Month.JANUARY, 12, 10, 0));
        when(loadReportRepository.findLatestByAthleteId(ATHLETE_ID)).thenReturn(Optional.of(report));

        LoadReport result = useCase.execute(ATHLETE_ID, COACH_ID);

        assertThat(result).isEqualTo(report);
        verify(loadReportRepository).findLatestByAthleteId(ATHLETE_ID);
    }

    @Test
    void execute_shouldThrowLoadReportNotFoundException_whenNoReportExists() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(loadReportRepository.findLatestByAthleteId(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID))
            .isInstanceOf(LoadReportNotFoundException.class);
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(loadReportRepository);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(loadReportRepository);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }
}
