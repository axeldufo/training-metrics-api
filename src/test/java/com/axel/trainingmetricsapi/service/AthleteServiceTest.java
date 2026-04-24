package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.repository.AthleteRepository;
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
}
