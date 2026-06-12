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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTrainingSessionUseCaseImplTest {

    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private TrainingSessionEventPort trainingSessionEventPort;

    @InjectMocks
    private UpdateTrainingSessionUseCaseImpl useCase;

    @Test
    void execute_shouldReturnUpdatedSession_whenValid() {
        long athleteId = 4L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        TrainingSession session = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), athleteId)
            .create();
        TrainingSession existing = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), athleteId)
            .create();
        when(trainingSessionRepository.findById(session.getId())).thenReturn(Optional.of(existing));
        TrainingSession updated = Instancio.create(TrainingSession.class);
        when(trainingSessionRepository.save(session)).thenReturn(updated);

        TrainingSession result = useCase.execute(session, COACH_ID);

        verify(trainingSessionRepository).findById(session.getId());
        verify(trainingSessionRepository).save(session);
        verify(trainingSessionEventPort).sessionUpdated(updated.getAthleteId(), updated.getDate());
        assertThat(result).isEqualTo(updated);
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        TrainingSession session = Instancio.create(TrainingSession.class);
        when(athleteRepository.findById(session.getAthleteId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(session, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(trainingSessionRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        TrainingSession session = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), athlete.getId())
            .create();
        when(athleteRepository.findById(session.getAthleteId())).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(session, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(trainingSessionRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrow_whenSessionNotFound() {
        long athleteId = 4L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        TrainingSession session = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), athleteId)
            .create();
        when(trainingSessionRepository.findById(session.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(session, COACH_ID))
            .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(trainingSessionRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrow_whenSessionDoesntBelongToAthlete() {
        long athleteId = 4L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        TrainingSession session = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), athleteId)
            .create();
        TrainingSession existing = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), 99L)
            .create();
        when(trainingSessionRepository.findById(session.getId())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(session, COACH_ID))
            .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(trainingSessionRepository, never()).save(any());
    }
}
