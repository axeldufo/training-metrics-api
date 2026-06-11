package com.axel.trainingmetricsapi.application.wellness;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.WeeklyWellnessAlreadyExistsException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWeeklyWellnessUseCaseImplTest {

    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private WeeklyWellnessRepository weeklyWellnessRepository;

    @InjectMocks
    private CreateWeeklyWellnessUseCaseImpl useCase;

    @Test
    void execute_shouldReturnPersistedWellness() {
        Athlete athlete = anAthleteOwnedByCoach();
        WeeklyWellness wellness = aValidWellness(athlete.getId());
        when(athleteRepository.findById(wellness.getAthleteId())).thenReturn(Optional.of(athlete));
        when(weeklyWellnessRepository.existsByAthleteIdAndWeekStartDate(
            wellness.getAthleteId(), wellness.getWeekStartDate())).thenReturn(false);
        WeeklyWellness persisted = aValidWellness(athlete.getId());
        when(weeklyWellnessRepository.save(wellness)).thenReturn(persisted);

        WeeklyWellness result = useCase.execute(wellness, COACH_ID);

        verify(weeklyWellnessRepository).existsByAthleteIdAndWeekStartDate(
            wellness.getAthleteId(), wellness.getWeekStartDate());
        verify(weeklyWellnessRepository).save(wellness);
        assertThat(result).isEqualTo(persisted);
    }

    @Test
    void execute_shouldThrowAlreadyExists_whenEntryAlreadyExistsForThatWeek() {
        Athlete athlete = anAthleteOwnedByCoach();
        WeeklyWellness wellness = aValidWellness(athlete.getId());
        when(athleteRepository.findById(wellness.getAthleteId())).thenReturn(Optional.of(athlete));
        when(weeklyWellnessRepository.existsByAthleteIdAndWeekStartDate(
            wellness.getAthleteId(), wellness.getWeekStartDate())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(wellness, COACH_ID))
            .isInstanceOf(WeeklyWellnessAlreadyExistsException.class);

        verify(weeklyWellnessRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        WeeklyWellness wellness = aValidWellness(4L);
        when(athleteRepository.findById(wellness.getAthleteId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(wellness, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(weeklyWellnessRepository);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        WeeklyWellness wellness = aValidWellness(athlete.getId());
        when(athleteRepository.findById(wellness.getAthleteId())).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(wellness, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(weeklyWellnessRepository);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }

    private WeeklyWellness aValidWellness(long athleteId) {
        return Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
    }
}
