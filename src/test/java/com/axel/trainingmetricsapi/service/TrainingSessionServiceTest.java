package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.TrainingSessionNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionServiceTest {

    private static final LocalDate FROM = LocalDate.of(2024, 1, 1);
    private static final LocalDate TO = LocalDate.of(2024, 1, 31);

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
    void findByAthleteIdAndPeriod_shouldReturnList_whenAthleteExists() {
        long athleteId = 4L;
        int count = 3;
        List<TrainingSession> expected = Instancio.ofList(TrainingSession.class).size(count).create();
        when(athleteRepository.existsById(athleteId)).thenReturn(true);
        when(trainingSessionRepository.findByAthleteIdAndPeriod(athleteId, FROM, TO)).thenReturn(expected);

        List<TrainingSession> result = trainingSessionService.findByAthleteIdAndPeriod(athleteId, FROM, TO);

        verify(athleteRepository).existsById(athleteId);
        verify(trainingSessionRepository).findByAthleteIdAndPeriod(athleteId, FROM, TO);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findByAthleteIdAndPeriod_shouldThrowException_whenAthleteNotFound() {
        long athleteId = 4L;
        when(athleteRepository.existsById(athleteId)).thenReturn(false);

        assertThatThrownBy(() -> trainingSessionService.findByAthleteIdAndPeriod(athleteId, FROM, TO))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).existsById(athleteId);
        verify(trainingSessionRepository, never()).findByAthleteIdAndPeriod(athleteId, FROM, TO);
    }

    @Test
    void findById_shouldReturnTrainingSession_whenTrainingSessionIsFound() {
        long sessionId = 4L;
        long athleteId = 2L;
        TrainingSession trainingSessionToFind = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), athleteId)
            .create();
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.of(trainingSessionToFind));

        TrainingSession returnedTrainingSession = trainingSessionService.findById(sessionId, athleteId);

        verify(trainingSessionRepository).findById(sessionId);
        assertThat(returnedTrainingSession).isEqualTo(trainingSessionToFind);
        assertThat(returnedTrainingSession.getId()).isEqualTo(trainingSessionToFind.getId()); // id is excluded from TrainingSession.isEqualTo()
    }

    @Test
    void findById_shouldThrowException_whenTrainingSessionNotFound() {
        long sessionId = 4L;
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingSessionService.findById(sessionId, 2L))
            .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(trainingSessionRepository).findById(sessionId);
    }

    @Test
    void findById_shouldThrowException_whenTrainingSessionDoesntBelongToAthlete() {
        long sessionId = 4L;
        long athleteId = 2L;
        TrainingSession trainingSessionToFind = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), 8L)
            .create();
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.of(trainingSessionToFind));

        assertThatThrownBy(() -> trainingSessionService.findById(sessionId, athleteId))
            .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(trainingSessionRepository).findById(sessionId);
    }

    @Test
    void update_shouldReturnPersistedTrainingSession_whenTrainingSessionIsUpdated() {
        long requestAthleteId = 4L;
        TrainingSession trainingSession = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), requestAthleteId)
            .create();
        long trainingSessionId = trainingSession.getId();
        TrainingSession persistedTrainingSession = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), requestAthleteId)
            .create();
        when(trainingSessionRepository.findById(trainingSessionId)).thenReturn(Optional.of(persistedTrainingSession));
        when(trainingSessionRepository.save(trainingSession)).thenReturn(persistedTrainingSession);

        TrainingSession returnedTrainingSession = trainingSessionService.update(trainingSession);

        verify(trainingSessionRepository).findById(trainingSessionId);
        verify(trainingSessionRepository).save(trainingSession);
        assertThat(returnedTrainingSession).isEqualTo(persistedTrainingSession);
        assertThat(returnedTrainingSession.getId()).isEqualTo(persistedTrainingSession.getId()); // id is excluded from TrainingSession.isEqualTo()
    }

    @Test
    void update_shouldThrowException_whenTrainingSessionNotFound() {
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);
        long trainingSessionId = trainingSession.getId();
        when(trainingSessionRepository.findById(trainingSessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingSessionService.update(trainingSession))
            .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(trainingSessionRepository).findById(trainingSessionId);
        verify(trainingSessionRepository, never()).save(trainingSession);
    }

    @Test
    void update_shouldThrowException_whenTrainingSessionDoesntBelongToAthlete() {
        long requestAthleteId = 4L;
        TrainingSession trainingSession = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), requestAthleteId)
            .create();  // session to update contains requestAthleteId
        long trainingSessionId = trainingSession.getId();
        long ownerAthleteId = 8L;
        TrainingSession persistedTrainingSession = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), ownerAthleteId)
            .create(); // session persisted contains ownerAthleteId
        when(trainingSessionRepository.findById(trainingSessionId)).thenReturn(Optional.of(persistedTrainingSession));

        assertThatThrownBy(() -> trainingSessionService.update(trainingSession))
            .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(trainingSessionRepository).findById(trainingSessionId);
        verify(trainingSessionRepository, never()).save(trainingSession);
    }

    @Test
    void deleteById_shouldDeleteTrainingSession_whenExists() {
        long sessionId = 8L;
        long athleteId = 2L;
        TrainingSession trainingSessionToFind = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), athleteId)
            .create();
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.of(trainingSessionToFind));

        trainingSessionService.deleteById(sessionId, athleteId);

        verify(trainingSessionRepository).findById(sessionId);
        verify(trainingSessionRepository).deleteById(sessionId);
    }

    @Test
    void deleteById_shouldThrowException_whenTrainingSessionDoesntExist() {
        long sessionId = 8L;
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingSessionService.deleteById(sessionId, 2L))
            .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(trainingSessionRepository).findById(sessionId);
        verify(trainingSessionRepository, never()).deleteById(sessionId);
    }

    @Test
    void deleteById_shouldThrowException_whenTrainingSessionDoesntBelongToAthlete() {
        long sessionId = 8L;
        long athleteId = 2L;
        TrainingSession trainingSessionToFind = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), 4L)
            .create();
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.of(trainingSessionToFind));

        assertThatThrownBy(() -> trainingSessionService.deleteById(sessionId, athleteId))
            .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(trainingSessionRepository).findById(sessionId);
        verify(trainingSessionRepository, never()).deleteById(sessionId);
    }

}
