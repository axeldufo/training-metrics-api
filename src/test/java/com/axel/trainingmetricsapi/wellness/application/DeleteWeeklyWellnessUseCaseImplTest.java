package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyWellnessNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteWeeklyWellnessUseCaseImplTest {

    private static final long ATHLETE_ID = 3L;
    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private WeeklyWellnessRepository weeklyWellnessRepository;

    @InjectMocks
    private DeleteWeeklyWellnessUseCaseImpl useCase;

    @Test
    void execute_shouldDelete_whenValid() {
        long wellnessId = 7L;
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        WeeklyWellness wellness = aWellnessForAthlete(ATHLETE_ID);
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.of(wellness));

        useCase.execute(wellnessId, ATHLETE_ID, COACH_ID);

        verify(weeklyWellnessRepository).findById(wellnessId);
        verify(weeklyWellnessRepository).deleteById(wellnessId);
    }

    @Test
    void execute_shouldThrow_whenNotFound() {
        long wellnessId = 7L;
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(wellnessId, ATHLETE_ID, COACH_ID))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);

        verify(weeklyWellnessRepository, never()).deleteById(wellnessId);
    }

    @Test
    void execute_shouldThrow_whenOwnershipMismatch() {
        long wellnessId = 7L;
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        WeeklyWellness wellness = aWellnessForAthlete(99L);
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.of(wellness));

        assertThatThrownBy(() -> useCase.execute(wellnessId, ATHLETE_ID, COACH_ID))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);

        verify(weeklyWellnessRepository, never()).deleteById(wellnessId);
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(weeklyWellnessRepository);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(7L, ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(weeklyWellnessRepository);
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
