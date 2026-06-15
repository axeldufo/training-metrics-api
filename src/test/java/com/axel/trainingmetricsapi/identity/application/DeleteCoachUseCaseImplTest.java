package com.axel.trainingmetricsapi.identity.application;

import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.identity.domain.CoachRepository;
import com.axel.trainingmetricsapi.identity.domain.exception.CoachHasAthletesException;
import com.axel.trainingmetricsapi.identity.domain.exception.CoachNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCoachUseCaseImplTest {

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private DeleteCoachUseCaseImpl useCase;

    @Test
    void execute_shouldDeleteCoach_whenExists() {
        long coachId = 4L;
        when(coachRepository.existsById(coachId)).thenReturn(true);
        when(athleteRepository.existsByCoachId(coachId)).thenReturn(false);

        useCase.execute(coachId);

        verify(coachRepository).existsById(coachId);
        verify(athleteRepository).existsByCoachId(coachId);
        verify(coachRepository).deleteById(coachId);
    }

    @Test
    void execute_shouldThrowCoachNotFoundException_whenCoachDoesntExist() {
        long coachId = 4L;
        when(coachRepository.existsById(coachId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(coachId))
            .isInstanceOf(CoachNotFoundException.class);

        verify(coachRepository).existsById(coachId);
        verify(coachRepository, never()).deleteById(coachId);
    }

    @Test
    void execute_shouldThrowCoachHasAthletesException_whenAthletesReferenceCoach() {
        long coachId = 4L;
        when(coachRepository.existsById(coachId)).thenReturn(true);
        when(athleteRepository.existsByCoachId(coachId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(coachId))
            .isInstanceOf(CoachHasAthletesException.class);

        verify(athleteRepository).existsByCoachId(coachId);
        verify(coachRepository, never()).deleteById(coachId);
    }
}
