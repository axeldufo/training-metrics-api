package com.axel.trainingmetricsapi.identity.application;

import com.axel.trainingmetricsapi.identity.domain.CoachRepository;
import com.axel.trainingmetricsapi.identity.domain.exception.CoachNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCoachUseCaseImplTest {

    @Mock
    private CoachRepository coachRepository;

    @InjectMocks
    private UpdateCoachUseCaseImpl useCase;

    @Test
    void execute_shouldUpdateCoach_whenCoachExists() {
        long coachId = 4L;
        String newName = "New Name";
        when(coachRepository.existsById(coachId)).thenReturn(true);

        useCase.execute(coachId, newName);

        verify(coachRepository).existsById(coachId);
        verify(coachRepository).updateName(coachId, newName);
    }

    @Test
    void execute_shouldThrowException_whenCoachNotFound() {
        long coachId = 4L;
        String newName = "New Name";
        when(coachRepository.existsById(coachId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(coachId, newName))
            .isInstanceOf(CoachNotFoundException.class);

        verify(coachRepository).existsById(coachId);
        verify(coachRepository, never()).updateName(coachId, newName);
    }
}
