package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.mapper.AthleteMapper;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AthleteRepositoryTest {

    @Mock
    private AthleteJpaRepository athleteJpaRepository;

    @Mock
    private AthleteMapper athleteMapper;

    @InjectMocks
    private AthleteRepositoryImpl athleteRepositoryImpl;

    @Test
    void save_shouldMapAndSaveAthlete() {
        Athlete athlete = Instancio.create(Athlete.class);
        AthleteJpaEntity athleteEntity = Instancio.create(AthleteJpaEntity.class);
        Long persistedId = 42L;
        AthleteJpaEntity savedAthleteEntity = Instancio.create(AthleteJpaEntity.class);
        savedAthleteEntity.setId(persistedId);
        Athlete expectedAthlete = Instancio.create(Athlete.class);
        expectedAthlete.setId(persistedId);

        when(athleteMapper.toEntity(athlete)).thenReturn(athleteEntity);
        when(athleteJpaRepository.save(athleteEntity)).thenReturn(savedAthleteEntity);
        when(athleteMapper.toDomain(savedAthleteEntity)).thenReturn(expectedAthlete);

        Athlete savedAthlete = athleteRepositoryImpl.save(athlete);

        verify(athleteMapper).toEntity(athlete);
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
        when(athleteMapper.toDomain(persistedAthleteEntity)).thenReturn(expectedAthlete);

        Optional<Athlete> athleteFound = athleteRepositoryImpl.findById(persistedId);

        verify(athleteMapper).toDomain(persistedAthleteEntity);
        assertThat(athleteFound).isPresent();
        assertThat(athleteFound.get().getId()).isEqualTo(persistedId);
    }

    @Test
    void findById_shouldBeEmptyIfNotFound() {
        Optional<Athlete> athleteFound = athleteRepositoryImpl.findById(18L);

        assertThat(athleteFound).isEmpty();
    }

    @Test
    void findAll_shouldMapAllAthletes() {
        int sizePersisted = 5;
        List<AthleteJpaEntity> persistedAthletes =
            Instancio.ofList(AthleteJpaEntity.class).size(sizePersisted).create();
        when(athleteJpaRepository.findAll()).thenReturn(persistedAthletes);
        when(athleteMapper.toDomain(any(AthleteJpaEntity.class))).thenReturn(Instancio.create(Athlete.class));

        List<Athlete> athletesFound = athleteRepositoryImpl.findAll();

        verify(athleteMapper, times(sizePersisted)).toDomain(any(AthleteJpaEntity.class));
        assertThat(athletesFound).hasSize(sizePersisted);
    }

    @Test
    void deleteById_shouldDeleteIfExists() {
        Long existingId = 4L;
        when(athleteJpaRepository.existsById(existingId)).thenReturn(true);

        athleteRepositoryImpl.deleteById(existingId);

        verify(athleteJpaRepository).deleteById(existingId);
    }

    @Test
    void deleteById_shouldThrowExceptionIfDoesntExist() {
        assertThatThrownBy(() -> athleteRepositoryImpl.deleteById(4L)).isInstanceOf(AthleteNotFoundException.class);

        verify(athleteJpaRepository, never()).deleteById(any(Long.class));
    }

}
