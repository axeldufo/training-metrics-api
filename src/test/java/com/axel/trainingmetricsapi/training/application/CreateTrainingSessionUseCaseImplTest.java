package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.training.application.port.out.TrainingSessionEventPort;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
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
class CreateTrainingSessionUseCaseImplTest {

    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private TrainingSessionEventPort trainingSessionEventPort;

    @InjectMocks
    private CreateTrainingSessionUseCaseImpl useCase;

    @Test
    void execute_shouldReturnPersistedSession_whenAthleteExists() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        TrainingSession session = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), athlete.getId())
            .create();
        when(athleteRepository.findById(session.getAthleteId())).thenReturn(Optional.of(athlete));
        TrainingSession persisted = Instancio.create(TrainingSession.class);
        when(trainingSessionRepository.save(session)).thenReturn(persisted);

        TrainingSession result = useCase.execute(session, COACH_ID);

        verify(athleteRepository).findById(session.getAthleteId());
        verify(trainingSessionRepository).save(session);
        verify(trainingSessionEventPort).sessionCreated(persisted.getAthleteId(), persisted.getDate());
        assertThat(result).isEqualTo(persisted);
    }

    @Test
    void execute_shouldThrowException_whenAthleteNotFound() {
        TrainingSession session = Instancio.create(TrainingSession.class);
        when(athleteRepository.findById(session.getAthleteId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(session, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(trainingSessionRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowException_whenAthleteDoesntBelongToCoach() {
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
}
