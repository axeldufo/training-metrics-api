package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyWellnessJpaAdapterTest {

    @Mock
    private WeeklyWellnessJpaRepository wellnessJpaRepository;

    @Mock
    private WeeklyWellnessPersistenceMapper persistenceMapper;

    @InjectMocks
    private WeeklyWellnessJpaAdapter adapter;

    @Test
    void save_shouldMapAndSave() {
        WeeklyWellness wellness = aValidWellness();
        WeeklyWellnessJpaEntity entity = Instancio.create(WeeklyWellnessJpaEntity.class);
        long persistedId = 53L;
        WeeklyWellnessJpaEntity savedEntity = Instancio.create(WeeklyWellnessJpaEntity.class);
        savedEntity.setId(persistedId);
        WeeklyWellness expected = aValidWellness();
        expected.setId(persistedId);

        when(persistenceMapper.domainToEntity(wellness)).thenReturn(entity);
        when(wellnessJpaRepository.save(entity)).thenReturn(savedEntity);
        when(persistenceMapper.entityToDomain(savedEntity)).thenReturn(expected);

        WeeklyWellness result = adapter.save(wellness);

        verify(persistenceMapper).domainToEntity(wellness);
        verify(wellnessJpaRepository).save(entity);
        verify(persistenceMapper).entityToDomain(savedEntity);
        assertThat(result.getId()).isEqualTo(persistedId);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findById_shouldReturnMapped() {
        long id = 42L;
        WeeklyWellnessJpaEntity entity = Instancio.create(WeeklyWellnessJpaEntity.class);
        entity.setId(id);
        WeeklyWellness expected = aValidWellness();
        expected.setId(id);

        when(wellnessJpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(persistenceMapper.entityToDomain(entity)).thenReturn(expected);

        Optional<WeeklyWellness> result = adapter.findById(id);

        verify(wellnessJpaRepository).findById(id);
        verify(persistenceMapper).entityToDomain(entity);
        assertThat(result).contains(expected);
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(wellnessJpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();

        verify(persistenceMapper, never()).entityToDomain(any());
    }

    @Test
    void findByAthleteIdAndPeriod_shouldMapAll() {
        long athleteId = 4L;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 29);
        int size = 3;
        List<WeeklyWellnessJpaEntity> entities = Instancio.ofList(WeeklyWellnessJpaEntity.class).size(size).create();
        when(wellnessJpaRepository.findAllByAthleteIdAndWeekStartDateBetween(athleteId, from, to))
            .thenReturn(entities);
        when(persistenceMapper.entityToDomain(any(WeeklyWellnessJpaEntity.class)))
            .thenReturn(Instancio.create(WeeklyWellness.class));

        List<WeeklyWellness> result = adapter.findByAthleteIdAndPeriod(athleteId, from, to);

        verify(wellnessJpaRepository).findAllByAthleteIdAndWeekStartDateBetween(athleteId, from, to);
        verify(persistenceMapper, times(size)).entityToDomain(any(WeeklyWellnessJpaEntity.class));
        assertThat(result).hasSize(size);
    }

    @Test
    void findByAthleteIdAndPeriod_shouldReturnEmpty_whenNone() {
        long athleteId = 4L;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 29);
        when(wellnessJpaRepository.findAllByAthleteIdAndWeekStartDateBetween(athleteId, from, to))
            .thenReturn(List.of());

        List<WeeklyWellness> result = adapter.findByAthleteIdAndPeriod(athleteId, from, to);

        verify(wellnessJpaRepository).findAllByAthleteIdAndWeekStartDateBetween(athleteId, from, to);
        verify(persistenceMapper, never()).entityToDomain(any());
        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_shouldDelete() {
        adapter.deleteById(5L);

        verify(wellnessJpaRepository).deleteById(5L);
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        when(wellnessJpaRepository.existsById(3L)).thenReturn(true);

        assertThat(adapter.existsById(3L)).isTrue();

        verify(wellnessJpaRepository).existsById(3L);
    }

    @Test
    void existsById_shouldReturnFalse_whenDoesNotExist() {
        when(wellnessJpaRepository.existsById(3L)).thenReturn(false);

        assertThat(adapter.existsById(3L)).isFalse();

        verify(wellnessJpaRepository).existsById(3L);
    }

    @Test
    void existsByAthleteIdAndWeekStartDate_shouldReturnTrue_whenExists() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        when(wellnessJpaRepository.existsByAthleteIdAndWeekStartDate(4L, monday)).thenReturn(true);

        assertThat(adapter.existsByAthleteIdAndWeekStartDate(4L, monday)).isTrue();

        verify(wellnessJpaRepository).existsByAthleteIdAndWeekStartDate(4L, monday);
    }

    @Test
    void existsByAthleteIdAndWeekStartDate_shouldReturnFalse_whenDoesNotExist() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        when(wellnessJpaRepository.existsByAthleteIdAndWeekStartDate(4L, monday)).thenReturn(false);

        assertThat(adapter.existsByAthleteIdAndWeekStartDate(4L, monday)).isFalse();

        verify(wellnessJpaRepository).existsByAthleteIdAndWeekStartDate(4L, monday);
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
