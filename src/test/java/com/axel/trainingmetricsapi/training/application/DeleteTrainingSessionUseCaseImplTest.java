package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.training.application.port.out.TrainingSessionEventPort;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.training.domain.exception.TrainingSessionNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteTrainingSessionUseCaseImplTest {

    private static final long ATHLETE_ID = 4L;
    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private TrainingSessionEventPort trainingSessionEventPort;

    @InjectMocks
    private DeleteTrainingSessionUseCaseImpl useCase;

    @Test
    void execute_shouldDeleteSession_whenValid() {
        long sessionId = 8L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        TrainingSession existing = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), ATHLETE_ID)
            .create();
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.of(existing));

        useCase.execute(sessionId, ATHLETE_ID, COACH_ID);

        verify(trainingSessionRepository).findById(sessionId);
        verify(trainingSessionRepository).deleteById(sessionId);
        verify(trainingSessionEventPort).sessionDeleted(ATHLETE_ID, existing.getDate());
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(8L, ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(trainingSessionRepository, never()).deleteById(anyLong());
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(8L, ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(trainingSessionRepository, never()).deleteById(anyLong());
    }

    @Test
    void execute_shouldThrow_whenSessionDoesntExist() {
        long sessionId = 8L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(sessionId, ATHLETE_ID, COACH_ID))
            .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(trainingSessionRepository, never()).deleteById(sessionId);
    }

    @Test
    void execute_shouldThrow_whenSessionDoesntBelongToAthlete() {
        long sessionId = 8L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        TrainingSession existing = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), 99L)
            .create();
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(sessionId, ATHLETE_ID, COACH_ID))
            .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(trainingSessionRepository, never()).deleteById(sessionId);
    }
}
