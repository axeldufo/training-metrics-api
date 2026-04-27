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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
