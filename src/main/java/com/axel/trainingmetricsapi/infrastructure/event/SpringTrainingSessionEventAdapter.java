package com.axel.trainingmetricsapi.infrastructure.event;

import com.axel.trainingmetricsapi.application.port.in.TrainingSessionChangedUseCase;
import com.axel.trainingmetricsapi.application.port.out.TrainingSessionEventPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SpringTrainingSessionEventAdapter implements TrainingSessionEventPort {

    private final TrainingSessionChangedUseCase trainingSessionChangedUseCase;

    public SpringTrainingSessionEventAdapter(TrainingSessionChangedUseCase trainingSessionChangedUseCase) {
        this.trainingSessionChangedUseCase = trainingSessionChangedUseCase;
    }

    @Override
    public void sessionCreated(long athleteId, LocalDate date) {
        trainingSessionChangedUseCase.execute(athleteId, date);
    }

    @Override
    public void sessionUpdated(long athleteId, LocalDate date) {
        trainingSessionChangedUseCase.execute(athleteId, date);
    }

    @Override
    public void sessionDeleted(long athleteId, LocalDate date) {
        trainingSessionChangedUseCase.execute(athleteId, date);
    }

}
