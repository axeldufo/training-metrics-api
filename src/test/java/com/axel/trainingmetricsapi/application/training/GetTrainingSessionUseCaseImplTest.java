package com.axel.trainingmetricsapi.application.training;

import com.axel.trainingmetricsapi.domain.Athlete;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTrainingSessionUseCaseImplTest {

    private static final long ATHLETE_ID = 4L;
    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private GetTrainingSessionUseCaseImpl useCase;

    @Test
    void execute_shouldReturnSession_whenBothFound() {
        long sessionId = 8L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        TrainingSession session = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), ATHLETE_ID)
            .create();
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        TrainingSession result = useCase.execute(sessionId, ATHLETE_ID, COACH_ID);

        verify(athleteRepository).findById(ATHLETE_ID);
        verify(trainingSessionRepository).findById(sessionId);
        assertThat(result).isEqualTo(session);
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(8L, ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(8L, ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);
    }

    @Test
    void execute_shouldThrow_whenSessionNotFound() {
        long sessionId = 8L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(sessionId, ATHLETE_ID, COACH_ID))
            .isInstanceOf(TrainingSessionNotFoundException.class);
    }

    @Test
    void execute_shouldThrow_whenSessionDoesntBelongToAthlete() {
        long sessionId = 8L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        TrainingSession session = Instancio.of(TrainingSession.class)
            .set(field(TrainingSession::getAthleteId), 99L)
            .create();
        when(trainingSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> useCase.execute(sessionId, ATHLETE_ID, COACH_ID))
            .isInstanceOf(TrainingSessionNotFoundException.class);
    }
}
