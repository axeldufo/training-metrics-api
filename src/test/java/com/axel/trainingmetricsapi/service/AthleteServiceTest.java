package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    void save_shouldSaveInRepositoryAndReturnPersistedAthlete() {
        Athlete athlete = Instancio.create(Athlete.class);
        Athlete persistedAthlete = Instancio.create(Athlete.class);
        when(athleteRepository.save(athlete)).thenReturn(persistedAthlete);

        Athlete returnedAthlete = athleteService.save(athlete);

        verify(athleteRepository).save(athlete);
        assertThat(returnedAthlete).isEqualTo(persistedAthlete);
        assertThat(returnedAthlete.getId()).isEqualTo(persistedAthlete.getId()); // id excluded from Athlete.isEqualTo()
    }
}
