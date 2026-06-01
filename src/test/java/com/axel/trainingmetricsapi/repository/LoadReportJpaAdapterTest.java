package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.LoadReport;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadReportJpaAdapterTest {

    private static final LocalDate MONDAY = LocalDate.of(2025, 5, 19);
    private static final long ATHLETE_ID = 1L;

    @Mock
    private LoadReportJpaRepository loadReportJpaRepository;

    @Mock
    private LoadReportPersistenceMapper persistenceMapper;

    @InjectMocks
    private LoadReportJpaAdapter adapter;

    @Test
    void save_shouldSetExistingId_andSave_whenEntityAlreadyExists() {
        LoadReport report = new LoadReport(ATHLETE_ID, MONDAY, 200, 2, LocalDateTime.now());
        LoadReportJpaEntity entityToSave = new LoadReportJpaEntity();
        LoadReportJpaEntity existingEntity = new LoadReportJpaEntity();
        existingEntity.setId(5L);
        LoadReportJpaEntity savedEntity = Instancio.create(LoadReportJpaEntity.class);
        LoadReport expected = new LoadReport(ATHLETE_ID, MONDAY, 200, 2, LocalDateTime.now());

        when(loadReportJpaRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(Optional.of(existingEntity));
        when(persistenceMapper.domainToEntity(report)).thenReturn(entityToSave);
        when(loadReportJpaRepository.save(entityToSave)).thenReturn(savedEntity);
        when(persistenceMapper.entityToDomain(savedEntity)).thenReturn(expected);

        LoadReport result = adapter.save(report);

        assertThat(entityToSave.getId()).isEqualTo(5L);
        verify(loadReportJpaRepository).save(entityToSave);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void save_shouldInsertWithNullId_whenNoExistingEntity() {
        LoadReport report = new LoadReport(ATHLETE_ID, MONDAY, 200, 2, LocalDateTime.now());
        LoadReportJpaEntity entityToSave = new LoadReportJpaEntity();
        LoadReportJpaEntity savedEntity = Instancio.create(LoadReportJpaEntity.class);
        LoadReport expected = new LoadReport(ATHLETE_ID, MONDAY, 200, 2, LocalDateTime.now());

        when(loadReportJpaRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(Optional.empty());
        when(persistenceMapper.domainToEntity(report)).thenReturn(entityToSave);
        when(loadReportJpaRepository.save(entityToSave)).thenReturn(savedEntity);
        when(persistenceMapper.entityToDomain(savedEntity)).thenReturn(expected);

        LoadReport result = adapter.save(report);

        assertThat(entityToSave.getId()).isNull();
        verify(loadReportJpaRepository).save(entityToSave);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findByAthleteIdAndWeekStartDate_shouldReturnMapped_whenFound() {
        LoadReportJpaEntity entity = Instancio.create(LoadReportJpaEntity.class);
        LoadReport expected = new LoadReport(ATHLETE_ID, MONDAY, 100, 1, LocalDateTime.now());

        when(loadReportJpaRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(Optional.of(entity));
        when(persistenceMapper.entityToDomain(entity)).thenReturn(expected);

        Optional<LoadReport> result = adapter.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);

        assertThat(result).contains(expected);
        verify(persistenceMapper).entityToDomain(entity);
    }

    @Test
    void findByAthleteIdAndWeekStartDate_shouldReturnEmpty_whenNotFound() {
        when(loadReportJpaRepository.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY))
            .thenReturn(Optional.empty());

        assertThat(adapter.findByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY)).isEmpty();

        verify(persistenceMapper, never()).entityToDomain(any());
    }

    @Test
    void findLatestByAthleteId_shouldReturnMapped_whenFound() {
        LoadReportJpaEntity entity = Instancio.create(LoadReportJpaEntity.class);
        LoadReport expected = new LoadReport(ATHLETE_ID, MONDAY, 100, 1, LocalDateTime.now());

        when(loadReportJpaRepository.findFirstByAthleteIdOrderByWeekStartDateDesc(ATHLETE_ID))
            .thenReturn(Optional.of(entity));
        when(persistenceMapper.entityToDomain(entity)).thenReturn(expected);

        Optional<LoadReport> result = adapter.findLatestByAthleteId(ATHLETE_ID);

        assertThat(result).contains(expected);
    }

    @Test
    void findLatestByAthleteId_shouldReturnEmpty_whenNoReportsExist() {
        when(loadReportJpaRepository.findFirstByAthleteIdOrderByWeekStartDateDesc(ATHLETE_ID))
            .thenReturn(Optional.empty());

        assertThat(adapter.findLatestByAthleteId(ATHLETE_ID)).isEmpty();

        verify(persistenceMapper, never()).entityToDomain(any());
    }

    @Test
    void findByAthleteIdAndWeekStartDateBetween_shouldMapAll() {
        LocalDate from = LocalDate.of(2025, 4, 1);
        LocalDate to = LocalDate.of(2025, 5, 26);
        int size = 3;
        List<LoadReportJpaEntity> entities = Instancio.ofList(LoadReportJpaEntity.class).size(size).create();
        when(loadReportJpaRepository.findAllByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to))
            .thenReturn(entities);
        LoadReport dummyReport = new LoadReport(ATHLETE_ID, MONDAY, 100, 1, LocalDateTime.now());
        when(persistenceMapper.entityToDomain(any(LoadReportJpaEntity.class))).thenReturn(dummyReport);

        List<LoadReport> result = adapter.findByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to);

        assertThat(result).hasSize(size);
        verify(loadReportJpaRepository).findAllByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to);
    }

    @Test
    void findByAthleteIdAndWeekStartDateBetween_shouldReturnEmpty_whenNone() {
        LocalDate from = LocalDate.of(2025, 4, 1);
        LocalDate to = LocalDate.of(2025, 5, 26);
        when(loadReportJpaRepository.findAllByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to))
            .thenReturn(List.of());

        assertThat(adapter.findByAthleteIdAndWeekStartDateBetween(ATHLETE_ID, from, to)).isEmpty();

        verify(persistenceMapper, never()).entityToDomain(any());
    }

    @Test
    void deleteByAthleteIdAndWeekStartDate_shouldDelegate() {
        adapter.deleteByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);

        verify(loadReportJpaRepository).deleteByAthleteIdAndWeekStartDate(ATHLETE_ID, MONDAY);
    }
}
