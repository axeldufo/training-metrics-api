package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.exception.AthleteNotFoundException;
import org.instancio.Instancio;
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
class AthleteServiceTest {

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private AthleteServiceImpl athleteService;

    @Test
    void findAll_shouldReturnAllAthletes_whenRepositoryReturnsThem() {
        List<Athlete> persistedAthletes = Instancio.ofList(Athlete.class).size(3).create();
        when(athleteRepository.findAll()).thenReturn(persistedAthletes);

        List<Athlete> returnedAthletes = athleteService.findAll();

        verify(athleteRepository).findAll();
        assertThat(returnedAthletes).isEqualTo(persistedAthletes);
    }

    @Test
    void findAll_shouldReturnAnEmptyList_whenRepositoryReturnsAnEmptyList() {
        when(athleteRepository.findAll()).thenReturn(List.of());

        List<Athlete> returnedAthletes = athleteService.findAll();

        verify(athleteRepository).findAll();
        assertThat(returnedAthletes).isEmpty();
    }

    @Test
    void save_shouldReturnPersistedAthlete_whenAthleteIsSaved() {
        Athlete athlete = Instancio.create(Athlete.class);
        Athlete persistedAthlete = Instancio.create(Athlete.class);
        when(athleteRepository.save(athlete)).thenReturn(persistedAthlete);

        Athlete returnedAthlete = athleteService.save(athlete);

        verify(athleteRepository).save(athlete);
        assertThat(returnedAthlete).isEqualTo(persistedAthlete);
        assertThat(returnedAthlete.getId()).isEqualTo(persistedAthlete.getId()); // id is excluded from Athlete.isEqualTo()
    }

    @Test
    void findById_shouldReturnAthlete_whenAthleteIsFound() {
        long id = 4L;
        Athlete athleteToFind = Instancio.create(Athlete.class);
        when(athleteRepository.findById(id)).thenReturn(Optional.of(athleteToFind));

        Athlete returnedAthlete = athleteService.findById(id);

        verify(athleteRepository).findById(id);
        assertThat(returnedAthlete).isEqualTo(athleteToFind);
        assertThat(returnedAthlete.getId()).isEqualTo(athleteToFind.getId()); // id is excluded from Athlete.isEqualTo()
    }

    @Test
    void findById_shouldThrowException_whenAthleteNotFound() {
        long id = 4L;
        when(athleteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> athleteService.findById(id))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(id);
    }

    @Test
    void update_shouldReturnPersistedAthlete_whenAthleteIsUpdated() {
        Athlete athlete = Instancio.create(Athlete.class);
        Athlete persistedAthlete = Instancio.create(Athlete.class);
        Long athleteId = athlete.getId();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        when(athleteRepository.save(athlete)).thenReturn(persistedAthlete);

        Athlete returnedAthlete = athleteService.update(athlete);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository).save(athlete);
        assertThat(returnedAthlete).isEqualTo(persistedAthlete);
        assertThat(returnedAthlete.getId()).isEqualTo(persistedAthlete.getId()); // id is excluded from Athlete.isEqualTo()
    }

    @Test
    void update_shouldThrowException_whenAthleteNotFound() {
        Athlete athlete = Instancio.create(Athlete.class);
        Long athleteId = athlete.getId();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> athleteService.update(athlete))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository, never()).save(athlete);
    }

    @Test
    void deleteById_shouldDeleteAthlete_whenExists() {
        long athleteId = 4L;
        when(athleteRepository.existsById(athleteId)).thenReturn(true);

        athleteService.deleteById(athleteId);

        verify(athleteRepository).existsById(athleteId);
        verify(athleteRepository).deleteById(athleteId);
    }

    @Test
    void deleteById_shouldThrowException_whenAthleteDoesntExist() {
        long athleteId = 4L;
        when(athleteRepository.existsById(athleteId)).thenReturn(false);

        assertThatThrownBy(() -> athleteService.deleteById(athleteId))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).existsById(athleteId);
        verify(athleteRepository, never()).deleteById(athleteId);
    }
}
