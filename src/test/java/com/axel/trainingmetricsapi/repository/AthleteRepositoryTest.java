package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.mapper.AthleteMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AthleteRepositoryTest {

    @Mock
    private AthleteJpaRepository athleteJpaRepository;

    @Mock
    private AthleteMapper athleteMapper;

    @InjectMocks
    private AthleteJpaAdapter athleteJpaAdapter;

    @Test
    void save_shouldMapAndSaveAthlete() {
        Athlete athlete = Instancio.create(Athlete.class);
        AthleteJpaEntity athleteEntity = Instancio.create(AthleteJpaEntity.class);
        Long persistedId = 42L;
        AthleteJpaEntity savedAthleteEntity = Instancio.create(AthleteJpaEntity.class);
        savedAthleteEntity.setId(persistedId);
        Athlete expectedAthlete = Instancio.create(Athlete.class);
        expectedAthlete.setId(persistedId);

        when(athleteMapper.domainToEntity(athlete)).thenReturn(athleteEntity);
        when(athleteJpaRepository.save(athleteEntity)).thenReturn(savedAthleteEntity);
        when(athleteMapper.entityToDomain(savedAthleteEntity)).thenReturn(expectedAthlete);

        Athlete savedAthlete = athleteJpaAdapter.save(athlete);

        verify(athleteMapper).domainToEntity(athlete);
        verify(athleteJpaRepository).save(athleteEntity);
        assertThat(savedAthlete.getId()).isEqualTo(persistedId);
    }

    @Test
    void findById_shouldReturnMappedAthlete() {
        Long persistedId = 42L;
        AthleteJpaEntity persistedAthleteEntity = Instancio.create(AthleteJpaEntity.class);
        persistedAthleteEntity.setId(persistedId);
        Athlete expectedAthlete = Instancio.create(Athlete.class);
        expectedAthlete.setId(persistedId);

        when(athleteJpaRepository.findById(persistedId)).thenReturn(Optional.of(persistedAthleteEntity));
        when(athleteMapper.entityToDomain(persistedAthleteEntity)).thenReturn(expectedAthlete);

        Optional<Athlete> athleteFound = athleteJpaAdapter.findById(persistedId);

        verify(athleteMapper).entityToDomain(persistedAthleteEntity);
        assertThat(athleteFound).isPresent();
        assertThat(athleteFound.get().getId()).isEqualTo(persistedId);
    }

    @Test
    void findById_shouldBeEmptyIfNotFound() {
        Optional<Athlete> athleteFound = athleteJpaAdapter.findById(18L);

        assertThat(athleteFound).isEmpty();
    }

    @Test
    void findAll_shouldMapAllAthletes() {
        int sizePersisted = 5;
        List<AthleteJpaEntity> persistedAthletes =
            Instancio.ofList(AthleteJpaEntity.class).size(sizePersisted).create();
        when(athleteJpaRepository.findAll()).thenReturn(persistedAthletes);
        when(athleteMapper.entityToDomain(any(AthleteJpaEntity.class))).thenReturn(Instancio.create(Athlete.class));

        List<Athlete> athletesFound = athleteJpaAdapter.findAll();

        verify(athleteMapper, times(sizePersisted)).entityToDomain(any(AthleteJpaEntity.class));
        assertThat(athletesFound).hasSize(sizePersisted);
    }

    @Test
    void deleteById_shouldDeleteIfExists() {
        long athleteId = 4L;

        athleteJpaAdapter.deleteById(athleteId);

        verify(athleteJpaRepository).deleteById(athleteId);
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        long athleteId = 4L;
        when(athleteJpaRepository.existsById(athleteId)).thenReturn(true);

        assertThat(athleteJpaAdapter.existsById(athleteId)).isTrue();

        verify(athleteJpaRepository).existsById(athleteId);
    }

    @Test
    void existsById_shouldReturnFalse_whenDoesntExist() {
        long athleteId = 4L;
        when(athleteJpaRepository.existsById(athleteId)).thenReturn(false);

        assertThat(athleteJpaAdapter.existsById(athleteId)).isFalse();

        verify(athleteJpaRepository).existsById(athleteId);
    }
}
