package com.axel.trainingmetricsapi.application.wellness;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.WeeklyWellnessNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWeeklyWellnessUseCaseImplTest {

    private static final long ATHLETE_ID = 3L;
    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private WeeklyWellnessRepository weeklyWellnessRepository;

    @InjectMocks
    private GetWeeklyWellnessUseCaseImpl useCase;

    @Test
    void execute_shouldReturnWellness_whenFound() {
        long wellnessId = 5L;
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        WeeklyWellness wellness = aWellnessForAthlete(ATHLETE_ID);
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.of(wellness));

        WeeklyWellness result = useCase.execute(wellnessId, ATHLETE_ID, COACH_ID);

        verify(athleteRepository).findById(ATHLETE_ID);
        verify(weeklyWellnessRepository).findById(wellnessId);
        assertThat(result).isEqualTo(wellness);
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(5L, ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(5L, ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);
    }

    @Test
    void execute_shouldThrow_whenWellnessNotFound() {
        long wellnessId = 5L;
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(wellnessId, ATHLETE_ID, COACH_ID))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);
    }

    @Test
    void execute_shouldThrow_whenWellnessDoesntBelongToAthlete() {
        long wellnessId = 5L;
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        WeeklyWellness wellness = aWellnessForAthlete(99L);
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.of(wellness));

        assertThatThrownBy(() -> useCase.execute(wellnessId, ATHLETE_ID, COACH_ID))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }

    private WeeklyWellness aWellnessForAthlete(long athleteId) {
        return Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
    }
}
