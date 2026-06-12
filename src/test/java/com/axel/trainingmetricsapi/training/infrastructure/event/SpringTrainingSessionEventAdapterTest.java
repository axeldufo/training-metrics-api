package com.axel.trainingmetricsapi.training.infrastructure.event;

import com.axel.trainingmetricsapi.training.application.port.in.TrainingSessionChangedUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringTrainingSessionEventAdapterTest {

    @Mock
    private TrainingSessionChangedUseCase trainingSessionChangedUseCase;

    @InjectMocks
    private SpringTrainingSessionEventAdapter adapter;

    private static final long ATHLETE_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2025, Month.MAY, 21);

    @Test
    void sessionCreated_shouldDelegateToUseCase() {
        adapter.sessionCreated(ATHLETE_ID, DATE);
        verify(trainingSessionChangedUseCase).execute(ATHLETE_ID, DATE);
    }

    @Test
    void sessionUpdated_shouldDelegateToUseCase() {
        adapter.sessionUpdated(ATHLETE_ID, DATE);
        verify(trainingSessionChangedUseCase).execute(ATHLETE_ID, DATE);
    }

    @Test
    void sessionDeleted_shouldDelegateToUseCase() {
        adapter.sessionDeleted(ATHLETE_ID, DATE);
        verify(trainingSessionChangedUseCase).execute(ATHLETE_ID, DATE);
    }
}
