package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Coach;
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

@ExtendWith(MockitoExtension.class)
class CoachRepositoryTest {

    @Mock
    private CoachJpaRepository coachJpaRepository;

    @Mock
    private CoachPersistenceMapper coachPersistenceMapper;

    @InjectMocks
    private CoachJpaAdapter coachJpaAdapter;

    @Test
    void save_shouldMapAndSaveCoach() {
        Coach coach = Instancio.create(Coach.class);
        CoachJpaEntity coachEntity = Instancio.create(CoachJpaEntity.class);
        Long persistedId = 42L;
        CoachJpaEntity savedCoachEntity = Instancio.create(CoachJpaEntity.class);
        savedCoachEntity.setId(persistedId);
        Coach expectedCoach = Instancio.create(Coach.class);
        expectedCoach.setId(persistedId);

        when(coachPersistenceMapper.domainToEntity(coach)).thenReturn(coachEntity);
        when(coachJpaRepository.save(coachEntity)).thenReturn(savedCoachEntity);
        when(coachPersistenceMapper.entityToDomain(savedCoachEntity)).thenReturn(expectedCoach);

        Coach savedCoach = coachJpaAdapter.save(coach);

        verify(coachPersistenceMapper).domainToEntity(coach);
        verify(coachJpaRepository).save(coachEntity);
        verify(coachPersistenceMapper).entityToDomain(savedCoachEntity);
        assertThat(savedCoach.getId()).isEqualTo(persistedId); // id is excluded from Coach.isEqualTo()
        assertThat(savedCoach).isEqualTo(expectedCoach);
    }

    @Test
    void findById_shouldReturnMappedCoach() {
        Long persistedId = 42L;
        CoachJpaEntity persistedCoachEntity = Instancio.create(CoachJpaEntity.class);
        persistedCoachEntity.setId(persistedId);
        Coach expectedCoach = Instancio.create(Coach.class);
        expectedCoach.setId(persistedId);

        when(coachJpaRepository.findById(persistedId)).thenReturn(Optional.of(persistedCoachEntity));
        when(coachPersistenceMapper.entityToDomain(persistedCoachEntity)).thenReturn(expectedCoach);

        Optional<Coach> coachFound = coachJpaAdapter.findById(persistedId);

        verify(coachJpaRepository).findById(persistedId);
        verify(coachPersistenceMapper).entityToDomain(persistedCoachEntity);
        assertThat(coachFound).contains(expectedCoach);
        assertThat(coachFound.get().getId()).isEqualTo(persistedId);  // id is excluded from Coach.isEqualTo()
    }

    @Test
    void findById_shouldBeEmptyIfNotFound() {
        Optional<Coach> coachFound = coachJpaAdapter.findById(18L);

        assertThat(coachFound).isEmpty();
    }

    @Test
    void findAll_shouldMapAllCoaches() {
        int sizePersisted = 5;
        List<CoachJpaEntity> persistedCoaches =
            Instancio.ofList(CoachJpaEntity.class).size(sizePersisted).create();
        when(coachJpaRepository.findAll()).thenReturn(persistedCoaches);
        when(coachPersistenceMapper.entityToDomain(any(CoachJpaEntity.class))).thenReturn(Instancio.create(Coach.class));

        List<Coach> coachesFound = coachJpaAdapter.findAll();

        verify(coachJpaRepository).findAll();
        verify(coachPersistenceMapper, times(sizePersisted)).entityToDomain(any(CoachJpaEntity.class));
        assertThat(coachesFound).hasSize(sizePersisted);
    }

    @Test
    void deleteById_shouldDeleteIfExists() {
        long coachId = 4L;

        coachJpaAdapter.deleteById(coachId);

        verify(coachJpaRepository).deleteById(coachId);
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        long coachId = 4L;
        when(coachJpaRepository.existsById(coachId)).thenReturn(true);

        assertThat(coachJpaAdapter.existsById(coachId)).isTrue();

        verify(coachJpaRepository).existsById(coachId);
    }

    @Test
    void existsById_shouldReturnFalse_whenDoesntExist() {
        long coachId = 4L;
        when(coachJpaRepository.existsById(coachId)).thenReturn(false);

        assertThat(coachJpaAdapter.existsById(coachId)).isFalse();

        verify(coachJpaRepository).existsById(coachId);
    }
}
