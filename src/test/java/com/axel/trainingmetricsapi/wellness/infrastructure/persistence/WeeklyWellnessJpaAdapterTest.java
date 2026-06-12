package com.axel.trainingmetricsapi.wellness.infrastructure.persistence;

import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
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
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyWellnessJpaAdapterTest {

    private static final long ATHLETE_ID = 4L;

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
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 29);
        int size = 3;
        List<WeeklyWellnessJpaEntity> entities = Instancio.ofList(WeeklyWellnessJpaEntity.class).size(size).create();
        when(wellnessJpaRepository.findAllByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to))
            .thenReturn(entities);
        when(persistenceMapper.entityToDomain(any(WeeklyWellnessJpaEntity.class)))
            .thenReturn(Instancio.create(WeeklyWellness.class));

        List<WeeklyWellness> result = adapter.findByAthleteIdAndPeriod(ATHLETE_ID, from, to);

        verify(wellnessJpaRepository).findAllByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to);
        verify(persistenceMapper, times(size)).entityToDomain(any(WeeklyWellnessJpaEntity.class));
        assertThat(result).hasSize(size);
    }

    @Test
    void findByAthleteIdAndPeriod_shouldReturnEmpty_whenNone() {
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 29);
        when(wellnessJpaRepository.findAllByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to))
            .thenReturn(List.of());

        List<WeeklyWellness> result = adapter.findByAthleteIdAndPeriod(ATHLETE_ID, from, to);

        verify(wellnessJpaRepository).findAllByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to);
        verify(persistenceMapper, never()).entityToDomain(any());
        assertThat(result).isEmpty();
    }

    @Test
    void findByAthleteIdAndWeekStartDate_shouldReturnWellness_whenFound() {
        LocalDate weekStartDate = LocalDate.now().with(DayOfWeek.MONDAY);
        WeeklyWellnessJpaEntity entity = Instancio.create(WeeklyWellnessJpaEntity.class);
        WeeklyWellness expected = Instancio.create(WeeklyWellness.class);
        when(wellnessJpaRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, weekStartDate))
            .thenReturn(Optional.of(entity));
        when(persistenceMapper.entityToDomain(entity)).thenReturn(expected);

        Optional<WeeklyWellness> result = adapter.findByAthleteIdAndWeekStartDate(ATHLETE_ID, weekStartDate);

        assertThat(result).contains(expected);
    }

    @Test
    void findByAthleteIdAndWeekStartDate_shouldReturnEmpty_whenNotFound() {
        LocalDate weekStartDate = LocalDate.now().with(DayOfWeek.MONDAY);
        when(wellnessJpaRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, weekStartDate))
            .thenReturn(Optional.empty());

        assertThat(adapter.findByAthleteIdAndWeekStartDate(ATHLETE_ID, weekStartDate)).isEmpty();
    }

    @Test
    void findLatestByAthleteId_shouldReturnLatestWellness_whenFound() {
        WeeklyWellnessJpaEntity entity = Instancio.create(WeeklyWellnessJpaEntity.class);
        WeeklyWellness expected = Instancio.create(WeeklyWellness.class);
        when(wellnessJpaRepository.findTopByAthleteIdOrderByWeekStartDateDesc(ATHLETE_ID))
            .thenReturn(Optional.of(entity));
        when(persistenceMapper.entityToDomain(entity)).thenReturn(expected);

        Optional<WeeklyWellness> result = adapter.findLatestByAthleteId(ATHLETE_ID);

        assertThat(result).contains(expected);
    }

    @Test
    void findLatestByAthleteId_shouldReturnEmpty_whenNotFound() {
        when(wellnessJpaRepository.findTopByAthleteIdOrderByWeekStartDateDesc(ATHLETE_ID))
            .thenReturn(Optional.empty());

        assertThat(adapter.findLatestByAthleteId(ATHLETE_ID)).isEmpty();
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
