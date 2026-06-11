package com.axel.trainingmetricsapi.application.athlete;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
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
class DeleteAthleteUseCaseImplTest {

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private DeleteAthleteUseCaseImpl useCase;

    @Test
    void execute_shouldDeleteAthlete_whenExists() {
        long athleteId = 4L;
        long coachId = 2L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), coachId)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));

        useCase.execute(athleteId, coachId);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository).deleteById(athleteId);
    }

    @Test
    void execute_shouldThrowException_whenAthleteDoesntExist() {
        long athleteId = 4L;
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(athleteId, 2L))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository, never()).deleteById(athleteId);
    }

    @Test
    void execute_shouldThrowException_whenAthleteDoesntBelongToRequestingCoach() {
        long athleteId = 4L;
        long coachId = 2L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 8L)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(athleteId, coachId))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository, never()).deleteById(athleteId);
    }
}
