package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.TrainingSession;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionJpaAdapterTest {

    @Mock
    private TrainingSessionJpaRepository trainingSessionJpaRepository;

    @Mock
    private TrainingSessionPersistenceMapper trainingSessionPersistenceMapper;

    @InjectMocks
    private TrainingSessionJpaAdapter trainingSessionJpaAdapter;

    @Test
    void save_shouldMapAndSaveTrainingSession() {
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        TrainingSessionJpaEntity trainingSessionEntity = Instancio.create(TrainingSessionJpaEntity.class);
        long persistedId = 53L;
        TrainingSessionJpaEntity savedTrainingSessionEntity = Instancio.create(TrainingSessionJpaEntity.class);
        savedTrainingSessionEntity.setId(persistedId);
        TrainingSession expectedTrainingSession = Instancio.create(TrainingSession.class);
        expectedTrainingSession.setId(persistedId);

        when(trainingSessionPersistenceMapper.domainToEntity(trainingSession)).thenReturn(trainingSessionEntity);
        when(trainingSessionJpaRepository.save(trainingSessionEntity)).thenReturn(savedTrainingSessionEntity);
        when(trainingSessionPersistenceMapper.entityToDomain(savedTrainingSessionEntity))
            .thenReturn(expectedTrainingSession);

        TrainingSession savedTrainingSession = trainingSessionJpaAdapter.save(trainingSession);

        verify(trainingSessionPersistenceMapper).domainToEntity(trainingSession);
        verify(trainingSessionJpaRepository).save(trainingSessionEntity);
        verify(trainingSessionPersistenceMapper).entityToDomain(savedTrainingSessionEntity);
        assertThat(savedTrainingSession.getId()).isEqualTo(persistedId); // id is excluded from TrainingSession.isEqualTo()
        assertThat(savedTrainingSession).isEqualTo(expectedTrainingSession);
    }

    @Test
    void findById_shouldReturnMappedTrainingSession() {
        long persistedId = 53L;
        TrainingSessionJpaEntity persistedTrainingSessionEntity = Instancio.create(TrainingSessionJpaEntity.class);
        persistedTrainingSessionEntity.setId(persistedId);
        TrainingSession expectedTrainingSession = Instancio.create(TrainingSession.class);
        expectedTrainingSession.setId(persistedId);

        when(trainingSessionJpaRepository.findById(persistedId)).thenReturn(Optional.of(persistedTrainingSessionEntity));
        when(trainingSessionPersistenceMapper.entityToDomain(persistedTrainingSessionEntity))
            .thenReturn(expectedTrainingSession);

        Optional<TrainingSession> trainingSessionFound = trainingSessionJpaAdapter.findById(persistedId);

        verify(trainingSessionJpaRepository).findById(persistedId);
        verify(trainingSessionPersistenceMapper).entityToDomain(persistedTrainingSessionEntity);
        assertThat(trainingSessionFound).contains(expectedTrainingSession);
        assertThat(trainingSessionFound.get().getId()).isEqualTo(persistedId); // id is excluded from TrainingSession.isEqualTo()
    }

    @Test
    void findById_shouldBeEmptyIfNotFound() {
        Optional<TrainingSession> trainingSessionFound = trainingSessionJpaAdapter.findById(13L);

        assertThat(trainingSessionFound).isEmpty();
    }

    @Test
    void findByAthleteIdAndPeriod_shouldMapAll() {
        long athleteId = 4L;
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 31);
        int size = 3;
        List<TrainingSessionJpaEntity> entities = Instancio.ofList(TrainingSessionJpaEntity.class).size(size).create();
        when(trainingSessionJpaRepository.findAllByAthleteIdAndDateBetween(athleteId, from, to)).thenReturn(entities);
        when(trainingSessionPersistenceMapper.entityToDomain(any(TrainingSessionJpaEntity.class)))
            .thenReturn(Instancio.create(TrainingSession.class));

        List<TrainingSession> result = trainingSessionJpaAdapter.findByAthleteIdAndPeriod(athleteId, from, to);

        verify(trainingSessionJpaRepository).findAllByAthleteIdAndDateBetween(athleteId, from, to);
        verify(trainingSessionPersistenceMapper, times(size)).entityToDomain(any(TrainingSessionJpaEntity.class));
        assertThat(result).hasSize(size);
    }

    @Test
    void findByAthleteIdAndPeriod_shouldReturnEmpty_whenNone() {
        long athleteId = 4L;
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 31);
        when(trainingSessionJpaRepository.findAllByAthleteIdAndDateBetween(athleteId, from, to)).thenReturn(List.of());

        List<TrainingSession> result = trainingSessionJpaAdapter.findByAthleteIdAndPeriod(athleteId, from, to);

        verify(trainingSessionJpaRepository).findAllByAthleteIdAndDateBetween(athleteId, from, to);
        verify(trainingSessionPersistenceMapper, never()).entityToDomain(any(TrainingSessionJpaEntity.class));
        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_shouldDeleteIfExists() {
        long trainingSessionId = 2L;

        trainingSessionJpaAdapter.deleteById(trainingSessionId);

        verify(trainingSessionJpaRepository).deleteById(trainingSessionId);
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        long trainingSessionId = 3L;
        when(trainingSessionJpaRepository.existsById(trainingSessionId)).thenReturn(true);

        assertThat(trainingSessionJpaAdapter.existsById(trainingSessionId)).isTrue();

        verify(trainingSessionJpaRepository).existsById(trainingSessionId);
    }

    @Test
    void existsById_shouldReturnFalse_whenDoesntExist() {
        long trainingSessionId = 4L;
        when(trainingSessionJpaRepository.existsById(trainingSessionId)).thenReturn(false);

        assertThat(trainingSessionJpaAdapter.existsById(trainingSessionId)).isFalse();

        verify(trainingSessionJpaRepository).existsById(trainingSessionId);
    }

}
