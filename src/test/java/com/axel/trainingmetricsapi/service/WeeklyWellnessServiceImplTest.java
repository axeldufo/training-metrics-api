package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.WeeklyWellnessRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyWellnessServiceImplTest {

    @Mock
    private WeeklyWellnessRepository wellnessRepository;

    @InjectMocks
    private WeeklyWellnessServiceImpl wellnessService;

    @Test
    void save_shouldReturnPersistedWellness() {
        WeeklyWellness wellness = aValidWellness();
        when(wellnessRepository.existsByAthleteIdAndWeekStartDate(wellness.getAthleteId(), wellness.getWeekStartDate()))
            .thenReturn(false);
        WeeklyWellness persisted = aValidWellness();
        when(wellnessRepository.save(wellness)).thenReturn(persisted);

        WeeklyWellness result = wellnessService.save(wellness);

        verify(wellnessRepository).existsByAthleteIdAndWeekStartDate(wellness.getAthleteId(), wellness.getWeekStartDate());
        verify(wellnessRepository).save(wellness);
        assertThat(result).isEqualTo(persisted);
    }

    @Test
    void save_shouldThrowAlreadyExists_whenEntryAlreadyExistsForThatWeek() {
        WeeklyWellness wellness = aValidWellness();
        when(wellnessRepository.existsByAthleteIdAndWeekStartDate(wellness.getAthleteId(), wellness.getWeekStartDate()))
            .thenReturn(true);

        assertThatThrownBy(() -> wellnessService.save(wellness))
            .isInstanceOf(WeeklyWellnessAlreadyExistsException.class);

        verify(wellnessRepository).existsByAthleteIdAndWeekStartDate(wellness.getAthleteId(), wellness.getWeekStartDate());
        verify(wellnessRepository, never()).save(any());
    }

    @Test
    void findByAthleteIdAndPeriod_shouldReturnList() {
        long athleteId = 4L;
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 29);
        List<WeeklyWellness> expected = Instancio.ofList(WeeklyWellness.class).size(2).create();
        when(wellnessRepository.findByAthleteIdAndPeriod(athleteId, from, to)).thenReturn(expected);

        List<WeeklyWellness> result = wellnessService.findByAthleteIdAndPeriod(athleteId, from, to);

        verify(wellnessRepository).findByAthleteIdAndPeriod(athleteId, from, to);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findById_shouldReturnWellness_whenFound() {
        long wellnessId = 5L;
        long athleteId = 2L;
        WeeklyWellness wellness = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.of(wellness));

        WeeklyWellness result = wellnessService.findById(wellnessId, athleteId);

        verify(wellnessRepository).findById(wellnessId);
        assertThat(result).isEqualTo(wellness);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        long wellnessId = 5L;
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wellnessService.findById(wellnessId, 2L))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);

        verify(wellnessRepository).findById(wellnessId);
    }

    @Test
    void findById_shouldThrow_whenOwnershipMismatch() {
        long wellnessId = 5L;
        long athleteId = 2L;
        WeeklyWellness wellness = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), 99L)
            .set(field(WeeklyWellness::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.of(wellness));

        assertThatThrownBy(() -> wellnessService.findById(wellnessId, athleteId))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);

        verify(wellnessRepository).findById(wellnessId);
    }

    @Test
    void update_shouldReturnUpdatedWellness_whenValid() {
        long athleteId = 3L;
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        WeeklyWellness incoming = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), monday)
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        long wellnessId = incoming.getId();
        WeeklyWellness existing = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), monday) // same date, no uniqueness check
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.of(existing));
        WeeklyWellness saved = aValidWellness();
        when(wellnessRepository.save(incoming)).thenReturn(saved);

        WeeklyWellness result = wellnessService.update(incoming);

        verify(wellnessRepository).findById(wellnessId);
        verify(wellnessRepository, never()).existsByAthleteIdAndWeekStartDate(anyLong(), any());
        verify(wellnessRepository).save(incoming);
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void update_shouldCheckUniqueness_whenWeekStartDateChanged() {
        long athleteId = 3L;
        LocalDate newMonday = LocalDate.of(2024, Month.JANUARY, 15);
        LocalDate oldMonday = LocalDate.of(2024, Month.JANUARY, 8);
        WeeklyWellness incoming = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), newMonday)
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        long wellnessId = incoming.getId();
        WeeklyWellness existing = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), oldMonday)
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.of(existing));
        when(wellnessRepository.existsByAthleteIdAndWeekStartDate(athleteId, newMonday)).thenReturn(false);
        WeeklyWellness saved = aValidWellness();
        when(wellnessRepository.save(incoming)).thenReturn(saved);

        wellnessService.update(incoming);

        verify(wellnessRepository).existsByAthleteIdAndWeekStartDate(athleteId, newMonday);
        verify(wellnessRepository).save(incoming);
    }

    @Test
    void update_shouldThrowAlreadyExists_whenWeekStartDateChangedAndConflicts() {
        long athleteId = 3L;
        LocalDate newMonday = LocalDate.of(2024, Month.JANUARY, 15);
        LocalDate oldMonday = LocalDate.of(2024, Month.JANUARY, 8);
        WeeklyWellness incoming = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), newMonday)
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        long wellnessId = incoming.getId();
        WeeklyWellness existing = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), oldMonday)
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.of(existing));
        when(wellnessRepository.existsByAthleteIdAndWeekStartDate(athleteId, newMonday)).thenReturn(true);

        assertThatThrownBy(() -> wellnessService.update(incoming))
            .isInstanceOf(WeeklyWellnessAlreadyExistsException.class);

        verify(wellnessRepository).existsByAthleteIdAndWeekStartDate(athleteId, newMonday);
        verify(wellnessRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenNotFound() {
        WeeklyWellness incoming = aValidWellness();
        long wellnessId = incoming.getId();
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wellnessService.update(incoming))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);

        verify(wellnessRepository).findById(wellnessId);
        verify(wellnessRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenOwnershipMismatch() {
        long requestAthleteId = 3L;
        long ownerAthleteId = 9L;
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        WeeklyWellness incoming = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), requestAthleteId)
            .set(field(WeeklyWellness::getWeekStartDate), monday)
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        long wellnessId = incoming.getId();
        WeeklyWellness existing = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), ownerAthleteId)
            .set(field(WeeklyWellness::getWeekStartDate), monday)
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> wellnessService.update(incoming))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);

        verify(wellnessRepository).findById(wellnessId);
        verify(wellnessRepository, never()).save(any());
    }

    @Test
    void deleteById_shouldDelete_whenValid() {
        long wellnessId = 7L;
        long athleteId = 3L;
        WeeklyWellness wellness = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), athleteId)
            .set(field(WeeklyWellness::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.of(wellness));

        wellnessService.deleteById(wellnessId, athleteId);

        verify(wellnessRepository).findById(wellnessId);
        verify(wellnessRepository).deleteById(wellnessId);
    }

    @Test
    void deleteById_shouldThrow_whenNotFound() {
        long wellnessId = 7L;
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wellnessService.deleteById(wellnessId, 3L))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);

        verify(wellnessRepository).findById(wellnessId);
        verify(wellnessRepository, never()).deleteById(wellnessId);
    }

    @Test
    void deleteById_shouldThrow_whenOwnershipMismatch() {
        long wellnessId = 7L;
        long athleteId = 3L;
        WeeklyWellness wellness = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getAthleteId), 99L)
            .set(field(WeeklyWellness::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        when(wellnessRepository.findById(wellnessId)).thenReturn(Optional.of(wellness));

        assertThatThrownBy(() -> wellnessService.deleteById(wellnessId, athleteId))
            .isInstanceOf(WeeklyWellnessNotFoundException.class);

        verify(wellnessRepository).findById(wellnessId);
        verify(wellnessRepository, never()).deleteById(wellnessId);
    }

    private WeeklyWellness aValidWellness() {
        return Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
    }
}
