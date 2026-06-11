package com.axel.trainingmetricsapi.application.wellness;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.WeeklyWellnessAlreadyExistsException;
import com.axel.trainingmetricsapi.domain.exception.WeeklyWellnessNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateWeeklyWellnessUseCaseImplTest {

    private static final long ATHLETE_ID = 3L;
    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private WeeklyWellnessRepository weeklyWellnessRepository;

    @InjectMocks
    private UpdateWeeklyWellnessUseCaseImpl useCase;

    @Test
    void execute_shouldReturnUpdatedWellness_whenValid() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        Athlete athlete = anAthleteOwnedByCoach();
        WeeklyWellness incoming = aWellnessForAthlete(ATHLETE_ID, monday);
        long wellnessId = incoming.getId();
        WeeklyWellness existing = aWellnessForAthlete(ATHLETE_ID, monday);
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.of(existing));
        WeeklyWellness saved = aWellnessForAthlete(ATHLETE_ID, monday);
        when(weeklyWellnessRepository.save(incoming)).thenReturn(saved);

        WeeklyWellness result = useCase.execute(incoming, COACH_ID);

        verify(weeklyWellnessRepository).findById(wellnessId);
        verify(weeklyWellnessRepository, never()).existsByAthleteIdAndWeekStartDate(anyLong(), any());
        verify(weeklyWellnessRepository).save(incoming);
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void execute_shouldCheckUniqueness_whenWeekStartDateChanged() {
        LocalDate newMonday = LocalDate.of(2024, Month.JANUARY, 15);
        LocalDate oldMonday = LocalDate.of(2024, Month.JANUARY, 8);
        Athlete athlete = anAthleteOwnedByCoach();
        WeeklyWellness incoming = aWellnessForAthlete(ATHLETE_ID, newMonday);
        long wellnessId = incoming.getId();
        WeeklyWellness existing = aWellnessForAthlete(ATHLETE_ID, oldMonday);
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.of(existing));
        when(weeklyWellnessRepository.existsByAthleteIdAndWeekStartDate(ATHLETE_ID, newMonday)).thenReturn(false);
        WeeklyWellness saved = aWellnessForAthlete(ATHLETE_ID, newMonday);
        when(weeklyWellnessRepository.save(incoming)).thenReturn(saved);

        useCase.execute(incoming, COACH_ID);

        verify(weeklyWellnessRepository).existsByAthleteIdAndWeekStartDate(ATHLETE_ID, newMonday);
        verify(weeklyWellnessRepository).save(incoming);
    }

    @Test
    void execute_shouldThrowAlreadyExists_whenWeekStartDateChangedAndConflicts() {
        LocalDate newMonday = LocalDate.of(2024, Month.JANUARY, 15);
        LocalDate oldMonday = LocalDate.of(2024, Month.JANUARY, 8);
        Athlete athlete = anAthleteOwnedByCoach();
        WeeklyWellness incoming = aWellnessForAthlete(ATHLETE_ID, newMonday);
        long wellnessId = incoming.getId();
        WeeklyWellness existing = aWellnessForAthlete(ATHLETE_ID, oldMonday);
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.of(existing));
        when(weeklyWellnessRepository.existsByAthleteIdAndWeekStartDate(ATHLETE_ID, newMonday)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(incoming, COACH_ID))
            .isInstanceOf(WeeklyWellnessAlreadyExistsException.class);

        verify(weeklyWellnessRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrow_whenNotFound() {
        Athlete athlete = anAthleteOwnedByCoach();
        WeeklyWellness incoming = aWellnessForAthlete(ATHLETE_ID, LocalDate.now().with(DayOfWeek.MONDAY));
        long wellnessId = incoming.getId();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(incoming, COACH_ID))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);

        verify(weeklyWellnessRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrow_whenOwnershipMismatch() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        Athlete athlete = anAthleteOwnedByCoach();
        WeeklyWellness incoming = aWellnessForAthlete(ATHLETE_ID, monday);
        long wellnessId = incoming.getId();
        WeeklyWellness existing = aWellnessForAthlete(99L, monday);
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(weeklyWellnessRepository.findById(wellnessId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(incoming, COACH_ID))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);

        verify(weeklyWellnessRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        WeeklyWellness incoming = aWellnessForAthlete(ATHLETE_ID, LocalDate.now().with(DayOfWeek.MONDAY));
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(incoming, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(weeklyWellnessRepository);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        WeeklyWellness incoming = aWellnessForAthlete(ATHLETE_ID, LocalDate.now().with(DayOfWeek.MONDAY));
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(incoming, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(weeklyWellnessRepository);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }

    private WeeklyWellness aWellnessForAthlete(long athleteId, LocalDate weekStartDate) {
        return Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), weekStartDate)
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
    }
}
