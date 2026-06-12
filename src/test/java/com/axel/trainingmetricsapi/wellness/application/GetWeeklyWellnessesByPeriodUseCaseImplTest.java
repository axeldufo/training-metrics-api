package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWeeklyWellnessesByPeriodUseCaseImplTest {

    private static final long ATHLETE_ID = 3L;
    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private WeeklyWellnessRepository weeklyWellnessRepository;

    @InjectMocks
    private GetWeeklyWellnessesByPeriodUseCaseImpl useCase;

    @Test
    void execute_shouldReturnList() {
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 29);
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        List<WeeklyWellness> expected = Instancio.ofList(WeeklyWellness.class).size(2).create();
        when(weeklyWellnessRepository.findByAthleteIdAndPeriod(ATHLETE_ID, from, to)).thenReturn(expected);

        List<WeeklyWellness> result = useCase.execute(ATHLETE_ID, COACH_ID, from, to);

        verify(weeklyWellnessRepository).findByAthleteIdAndPeriod(ATHLETE_ID, from, to);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 29);
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, from, to))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(weeklyWellnessRepository);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 29);
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, from, to))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(weeklyWellnessRepository);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }
}
