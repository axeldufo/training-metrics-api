package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.PageResult;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
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
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AthleteServiceTest {

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private AthleteServiceImpl athleteService;

    @Test
    void findAllByCoachId_shouldReturnAllAthletes_whenRepositoryReturnsThem() {
        long coachId = 4L;
        int pageNumber = 0;
        int pageSize = 20;
        int nbPersistedAthletes = 3;
        List<Athlete> persistedAthletes = Instancio.ofList(Athlete.class).size(nbPersistedAthletes).create();
        when(athleteRepository.findAllByCoachId(coachId, pageNumber, pageSize)).thenReturn(
            new PageResult<>(persistedAthletes, nbPersistedAthletes, pageNumber, pageSize));

        PageResult<Athlete> returnedAthletes = athleteService.findAllByCoachId(coachId, pageNumber, pageSize);

        verify(athleteRepository).findAllByCoachId(coachId, pageNumber, pageSize);
        assertThat(returnedAthletes.content()).isEqualTo(persistedAthletes);
        assertThat(returnedAthletes.totalElements()).isEqualTo(nbPersistedAthletes);
        assertThat(returnedAthletes.pageNumber()).isEqualTo(pageNumber);
        assertThat(returnedAthletes.pageSize()).isEqualTo(pageSize);
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
        long athleteId = 4L;
        long requestingCoachId = 2L;
        Athlete athleteToFind = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), requestingCoachId)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athleteToFind));

        Athlete returnedAthlete = athleteService.findById(athleteId, requestingCoachId);

        verify(athleteRepository).findById(athleteId);
        assertThat(returnedAthlete).isEqualTo(athleteToFind);
        assertThat(returnedAthlete.getId()).isEqualTo(athleteToFind.getId()); // id is excluded from Athlete.isEqualTo()
    }

    @Test
    void findById_shouldThrowException_whenAthleteNotFound() {
        long athleteId = 4L;
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> athleteService.findById(athleteId, 2L))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
    }

    @Test
    void findById_shouldThrowException_whenAthleteDoesntBelongToRequestingCoach() {
        long athleteId = 4L;
        long requestingCoachId = 2L;
        Athlete athleteToFind = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 8L)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athleteToFind));

        assertThatThrownBy(() -> athleteService.findById(athleteId, requestingCoachId))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
    }

    @Test
    void update_shouldReturnPersistedAthlete_whenAthleteIsUpdated() {
        long requestingCoachId = 2L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), requestingCoachId)
            .create();
        Long athleteId = athlete.getId();
        Athlete persistedAthlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), requestingCoachId)
            .create();

        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(persistedAthlete));
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
    void update_shouldThrowException_whenAthleteDoesntBelongToRequestingCoach() {
        long requestingCoachId = 2L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), requestingCoachId)
            .create(); // athlete to update contains requestingCoachId
        Long athleteId = athlete.getId();
        long ownerCoachId = 8L;
        Athlete persistedAthlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), ownerCoachId)
            .create(); // athlete persisted contains ownerCoachId
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(persistedAthlete));

        assertThatThrownBy(() -> athleteService.update(athlete))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository, never()).save(athlete);
    }

    @Test
    void deleteById_shouldDeleteAthlete_whenExists() {
        long athleteId = 4L;
        long requestingCoachId = 2L;
        Athlete persistedAthlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), requestingCoachId)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(persistedAthlete));

        athleteService.deleteById(athleteId, requestingCoachId);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository).deleteById(athleteId);
    }

    @Test
    void deleteById_shouldThrowException_whenAthleteDoesntExist() {
        long athleteId = 4L;
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> athleteService.deleteById(athleteId, 2L))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository, never()).deleteById(athleteId);
    }

    @Test
    void deleteById_shouldThrowException_whenAthleteDoesntBelongToRequestingCoach() {
        long athleteId = 4L;
        long requestingCoachId = 2L;
        Athlete athleteToFind = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 8L)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athleteToFind));

        assertThatThrownBy(() -> athleteService.deleteById(athleteId, requestingCoachId))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository, never()).deleteById(athleteId);
    }
}
