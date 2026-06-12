package com.axel.trainingmetricsapi.identity.application;

import com.axel.trainingmetricsapi.identity.domain.Coach;
import com.axel.trainingmetricsapi.identity.domain.CoachRepository;
import com.axel.trainingmetricsapi.identity.domain.exception.CoachNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCoachUseCaseImplTest {

    @Mock
    private CoachRepository coachRepository;

    @InjectMocks
    private GetCoachUseCaseImpl useCase;

    @Test
    void execute_shouldReturnCoach_whenCoachIsFound() {
        long coachId = 4L;
        Coach coach = Instancio.create(Coach.class);
        when(coachRepository.findById(coachId)).thenReturn(Optional.of(coach));

        Coach result = useCase.execute(coachId);

        verify(coachRepository).findById(coachId);
        assertThat(result).isEqualTo(coach);
        assertThat(result.getId()).isEqualTo(coach.getId());
    }

    @Test
    void execute_shouldThrowException_whenCoachNotFound() {
        long coachId = 4L;
        when(coachRepository.findById(coachId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(coachId))
            .isInstanceOf(CoachNotFoundException.class);

        verify(coachRepository).findById(coachId);
    }
}
