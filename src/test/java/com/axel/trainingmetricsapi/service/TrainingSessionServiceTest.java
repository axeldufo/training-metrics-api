package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionServiceTest {

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private TrainingSessionServiceImpl trainingSessionService;

    @Test
    void save_shouldReturnPersistedTrainingSession_whenTrainingSessionIsSaved() {
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        long athleteId = trainingSession.getAthleteId();
        when(athleteRepository.existsById(athleteId)).thenReturn(true);
        TrainingSession persistedTrainingSession = Instancio.create(TrainingSession.class);
        when(trainingSessionRepository.save(trainingSession)).thenReturn(persistedTrainingSession);

        TrainingSession returnedTrainingSession = trainingSessionService.save(trainingSession);

        verify(athleteRepository).existsById(athleteId);
        verify(trainingSessionRepository).save(trainingSession);
        assertThat(returnedTrainingSession).isEqualTo(persistedTrainingSession);
        assertThat(returnedTrainingSession.getId()).isEqualTo(persistedTrainingSession.getId()); // id is excluded from TrainingSession.isEqualTo()
    }

    @Test
    void save_shouldThrowException_whenAthleteNotFound() {
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        long athleteId = trainingSession.getAthleteId();
        when(athleteRepository.existsById(athleteId)).thenReturn(false);

        assertThatThrownBy(() -> trainingSessionService.save(trainingSession))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).existsById(athleteId);
        verify(trainingSessionRepository, never()).save(trainingSession);
    }

    @Test
    void findAllByAthleteId_shouldReturnAllAthleteSessions_whenRepositoryReturnsThem() {
        long athleteId = 4L;
        when(athleteRepository.existsById(athleteId)).thenReturn(true);
        List<TrainingSession> persistedTrainingSessions = Instancio.ofList(TrainingSession.class).size(3).create();
        when(trainingSessionRepository.findAllByAthleteId(athleteId)).thenReturn(persistedTrainingSessions);

        List<TrainingSession> returnedTrainingSessions = trainingSessionService.findAllByAthleteId(athleteId);

        verify(trainingSessionRepository).findAllByAthleteId(athleteId);
        assertThat(returnedTrainingSessions).isEqualTo(persistedTrainingSessions);
    }

    @Test
    void findAllByAthleteId_shouldThrowException_whenAthleteNotFound() {
        long athleteId = 4L;
        when(athleteRepository.existsById(athleteId)).thenReturn(false);

        assertThatThrownBy(() -> trainingSessionService.findAllByAthleteId(athleteId))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).existsById(athleteId);
        verify(trainingSessionRepository, never()).findAllByAthleteId(athleteId);
    }

}
