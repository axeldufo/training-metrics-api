package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.domain.CoachRepository;
import com.axel.trainingmetricsapi.domain.exception.CoachNotFoundException;
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
class CoachServiceTest {

    @Mock
    private CoachRepository coachRepository;

    @InjectMocks
    private CoachServiceImpl coachService;

    @Test
    void findAll_shouldReturnAllCoaches_whenRepositoryReturnsThem() {
        List<Coach> persistedCoaches = Instancio.ofList(Coach.class).size(3).create();
        when(coachRepository.findAll()).thenReturn(persistedCoaches);

        List<Coach> returnedCoaches = coachService.findAll();

        verify(coachRepository).findAll();
        assertThat(returnedCoaches).isEqualTo(persistedCoaches);
    }

    @Test
    void save_shouldReturnPersistedCoach_whenCoachIsSaved() {
        Coach coach = Instancio.create(Coach.class);
        Coach persistedCoach = Instancio.create(Coach.class);
        when(coachRepository.save(coach)).thenReturn(persistedCoach);

        Coach returnedCoach = coachService.save(coach);

        verify(coachRepository).save(coach);
        assertThat(returnedCoach).isEqualTo(persistedCoach);
        assertThat(returnedCoach.getId()).isEqualTo(persistedCoach.getId()); // id is excluded from Coach.isEqualTo()
    }

    @Test
    void findById_shouldReturnCoach_whenCoachIsFound() {
        long id = 4L;
        Coach coachToFind = Instancio.create(Coach.class);
        when(coachRepository.findById(id)).thenReturn(Optional.of(coachToFind));

        Coach returnedCoach = coachService.findById(id);

        verify(coachRepository).findById(id);
        assertThat(returnedCoach).isEqualTo(coachToFind);
        assertThat(returnedCoach.getId()).isEqualTo(coachToFind.getId()); // id is excluded from Coach.isEqualTo()
    }

    @Test
    void findById_shouldThrowException_whenCoachNotFound() {
        long id = 4L;
        when(coachRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coachService.findById(id))
            .isInstanceOf(CoachNotFoundException.class);

        verify(coachRepository).findById(id);
    }

    @Test
    void update_shouldReturnPersistedCoach_whenCoachIsUpdated() {
        Coach coach = Instancio.create(Coach.class);
        Coach persistedCoach = Instancio.create(Coach.class);
        Long coachId = coach.getId();
        when(coachRepository.existsById(coachId)).thenReturn(true);
        when(coachRepository.save(coach)).thenReturn(persistedCoach);

        Coach returnedCoach = coachService.update(coach);

        verify(coachRepository).existsById(coachId);
        verify(coachRepository).save(coach);
        assertThat(returnedCoach).isEqualTo(persistedCoach);
        assertThat(returnedCoach.getId()).isEqualTo(persistedCoach.getId()); // id is excluded from Coach.isEqualTo()
    }

    @Test
    void update_shouldThrowException_whenCoachNotFound() {
        Coach coach = Instancio.create(Coach.class);
        Long coachId = coach.getId();
        when(coachRepository.existsById(coachId)).thenReturn(false);

        assertThatThrownBy(() -> coachService.update(coach))
            .isInstanceOf(CoachNotFoundException.class);

        verify(coachRepository).existsById(coachId);
        verify(coachRepository, never()).save(coach);
    }

    @Test
    void deleteById_shouldDeleteCoach_whenExists() {
        long coachId = 4L;
        when(coachRepository.existsById(coachId)).thenReturn(true);

        coachService.deleteById(coachId);

        verify(coachRepository).existsById(coachId);
        verify(coachRepository).deleteById(coachId);
    }

    @Test
    void deleteById_shouldThrowException_whenCoachDoesntExist() {
        long coachId = 4L;
        when(coachRepository.existsById(coachId)).thenReturn(false);

        assertThatThrownBy(() -> coachService.deleteById(coachId))
            .isInstanceOf(CoachNotFoundException.class);

        verify(coachRepository).existsById(coachId);
        verify(coachRepository, never()).deleteById(coachId);
    }

}
