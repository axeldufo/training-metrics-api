package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.TrainingSession;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

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
    void findAllByAthleteId_shouldMapAllTrainingSessions() {
        int sizePersisted = 8;
        long athleteId = 4L;
        List<TrainingSessionJpaEntity> persistedTrainingSessions =
            Instancio.ofList(TrainingSessionJpaEntity.class).size(sizePersisted).create();
        when(trainingSessionJpaRepository.findAllByAthleteId(athleteId)).thenReturn(persistedTrainingSessions);
        when(trainingSessionPersistenceMapper.entityToDomain(any(TrainingSessionJpaEntity.class)))
            .thenReturn(Instancio.create(TrainingSession.class));

        List<TrainingSession> trainingSessionsFound = trainingSessionJpaAdapter.findAllByAthleteId(athleteId);

        verify(trainingSessionJpaRepository).findAllByAthleteId(athleteId);
        verify(trainingSessionPersistenceMapper, times(sizePersisted)).entityToDomain(any(TrainingSessionJpaEntity.class));
        assertThat(trainingSessionsFound).hasSize(sizePersisted);
    }

    @Test
    void findAllByAthleteId_shouldReturnEmptyList_ifNoTrainingSessions() {
        long athleteId = 4L;
        List<TrainingSessionJpaEntity> persistedTrainingSessions = List.of();
        when(trainingSessionJpaRepository.findAllByAthleteId(athleteId)).thenReturn(persistedTrainingSessions);

        List<TrainingSession> trainingSessionsFound = trainingSessionJpaAdapter.findAllByAthleteId(athleteId);

        verify(trainingSessionJpaRepository).findAllByAthleteId(athleteId);
        verify(trainingSessionPersistenceMapper, never()).entityToDomain(any(TrainingSessionJpaEntity.class));
        assertThat(trainingSessionsFound).isEmpty();
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
